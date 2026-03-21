package com.record.modules.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.exception.LedgerException;
import com.record.modules.ledger.mapper.LedgerBookMapper;
import com.record.modules.ledger.mapper.LedgerEntryMapper;
import com.record.modules.ledger.mapper.LedgerEntryTagRelMapper;
import com.record.modules.ledger.model.dto.CreateBookRequest;
import com.record.modules.ledger.model.dto.CreateLedgerEntryRequest;
import com.record.modules.ledger.model.dto.UpdateLedgerEntryRequest;
import com.record.modules.ledger.model.entity.LedgerBook;
import com.record.modules.ledger.model.entity.LedgerEntry;
import com.record.modules.ledger.model.entity.LedgerEntryTagRel;
import com.record.modules.ledger.model.vo.LedgerBookVO;
import com.record.modules.ledger.model.vo.LedgerEntryVO;
import com.record.modules.ledger.model.vo.YearStatisticsVO;
import com.record.modules.ledger.service.LedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 记账服务实现。
 */
@Service
public class LedgerServiceImpl implements LedgerService {

    private final LedgerBookMapper ledgerBookMapper;
    private final LedgerEntryMapper ledgerEntryMapper;
    private final LedgerEntryTagRelMapper ledgerEntryTagRelMapper;

    public LedgerServiceImpl(LedgerBookMapper ledgerBookMapper,
                             LedgerEntryMapper ledgerEntryMapper,
                             LedgerEntryTagRelMapper ledgerEntryTagRelMapper) {
        this.ledgerBookMapper = ledgerBookMapper;
        this.ledgerEntryMapper = ledgerEntryMapper;
        this.ledgerEntryTagRelMapper = ledgerEntryTagRelMapper;
    }

    @Override
    public LedgerBookVO createBook(Long userId, CreateBookRequest request) {
        LedgerBook book = new LedgerBook();
        book.setUserId(userId);
        book.setName(request.getName());
        book.setDescription(request.getDescription());
        ledgerBookMapper.insert(book);
        return LedgerBookVO.builder().id(book.getId()).name(book.getName()).description(book.getDescription()).build();
    }

    @Override
    public List<LedgerBookVO> listBooks(Long userId) {
        return ledgerBookMapper.selectList(new LambdaQueryWrapper<LedgerBook>().eq(LedgerBook::getUserId, userId))
                .stream()
                .map(item -> LedgerBookVO.builder().id(item.getId()).name(item.getName()).description(item.getDescription()).build())
                .toList();
    }

    @Override
    @Transactional
    public LedgerEntryVO createEntry(Long userId, CreateLedgerEntryRequest request) {
        validateAmount(request.getAmount());
        LedgerEntry entry = new LedgerEntry();
        fillEntry(entry, userId, request);
        ledgerEntryMapper.insert(entry);
        replaceTags(entry.getId(), request.getTagIds());
        return toVO(entry);
    }

    @Override
    @Transactional
    public LedgerEntryVO updateEntry(Long userId, Long entryId, UpdateLedgerEntryRequest request) {
        validateAmount(request.getAmount());
        LedgerEntry entry = requireOwnedEntry(userId, entryId);
        fillEntry(entry, userId, request);
        ledgerEntryMapper.updateById(entry);
        replaceTags(entryId, request.getTagIds());
        return toVO(entry);
    }

    @Override
    @Transactional
    public void deleteEntry(Long userId, Long entryId) {
        requireOwnedEntry(userId, entryId);
        ledgerEntryTagRelMapper.delete(new LambdaQueryWrapper<LedgerEntryTagRel>().eq(LedgerEntryTagRel::getEntryId, entryId));
        ledgerEntryMapper.deleteById(entryId);
    }

    @Override
    public List<LedgerEntryVO> monthEntries(Long userId, Integer year, Integer month, Long bookId) {
        return ledgerEntryMapper.selectList(new LambdaQueryWrapper<LedgerEntry>()
                        .eq(LedgerEntry::getUserId, userId)
                        .eq(bookId != null, LedgerEntry::getBookId, bookId)
                        .apply("YEAR(entry_date) = {0}", year)
                        .apply("MONTH(entry_date) = {0}", month)
                        .orderByDesc(LedgerEntry::getEntryDate))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public YearStatisticsVO yearStatistics(Long userId, Integer year) {
        List<LedgerEntry> entries = ledgerEntryMapper.selectList(new LambdaQueryWrapper<LedgerEntry>()
                .eq(LedgerEntry::getUserId, userId)
                .apply("YEAR(entry_date) = {0}", year));
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (LedgerEntry entry : entries) {
            List<Long> tagIds = ledgerEntryTagRelMapper.selectList(new LambdaQueryWrapper<LedgerEntryTagRel>()
                            .eq(LedgerEntryTagRel::getEntryId, entry.getId()))
                    .stream()
                    .map(LedgerEntryTagRel::getTagId)
                    .toList();
            for (Long tagId : tagIds) {
                amountMap.merge(tagId, entry.getAmount(), BigDecimal::add);
                total = total.add(entry.getAmount());
            }
        }
        BigDecimal safeTotal = total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : total;
        return YearStatisticsVO.builder()
                .year(year)
                .items(amountMap.entrySet().stream()
                        .map(item -> YearStatisticsVO.TagAmountVO.builder()
                                .tagId(item.getKey())
                                .amount(item.getValue())
                                .ratio(item.getValue().divide(safeTotal, 4, RoundingMode.HALF_UP))
                                .build())
                        .toList())
                .build();
    }

    /**
     * 金额必须为正数，且最多保留两位小数。
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new LedgerException("金额不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new LedgerException("金额必须大于 0");
        }
        if (amount.scale() > 2) {
            throw new LedgerException("金额最多只能保留两位小数");
        }
    }

    private void fillEntry(LedgerEntry entry, Long userId, CreateLedgerEntryRequest request) {
        entry.setUserId(userId);
        entry.setBookId(request.getBookId());
        entry.setType(request.getType());
        entry.setAmount(request.getAmount());
        entry.setEntryDate(request.getEntryDate());
        entry.setRemark(request.getRemark());
        entry.setImagePath(request.getImagePath());
    }

    private void replaceTags(Long entryId, List<Long> tagIds) {
        ledgerEntryTagRelMapper.delete(new LambdaQueryWrapper<LedgerEntryTagRel>().eq(LedgerEntryTagRel::getEntryId, entryId));
        if (tagIds == null) {
            return;
        }
        for (Long tagId : tagIds) {
            LedgerEntryTagRel rel = new LedgerEntryTagRel();
            rel.setEntryId(entryId);
            rel.setTagId(tagId);
            ledgerEntryTagRelMapper.insert(rel);
        }
    }

    private LedgerEntry requireOwnedEntry(Long userId, Long entryId) {
        LedgerEntry entry = ledgerEntryMapper.selectById(entryId);
        if (entry == null || !entry.getUserId().equals(userId)) {
            throw new LedgerException("账单不存在或无权限访问");
        }
        return entry;
    }

    private LedgerEntryVO toVO(LedgerEntry entry) {
        List<Long> tagIds = ledgerEntryTagRelMapper.selectList(new LambdaQueryWrapper<LedgerEntryTagRel>()
                        .eq(LedgerEntryTagRel::getEntryId, entry.getId()))
                .stream()
                .map(LedgerEntryTagRel::getTagId)
                .toList();
        return LedgerEntryVO.builder()
                .id(entry.getId())
                .bookId(entry.getBookId())
                .type(entry.getType())
                .amount(entry.getAmount())
                .entryDate(entry.getEntryDate())
                .remark(entry.getRemark())
                .imagePath(entry.getImagePath())
                .tagIds(tagIds)
                .build();
    }
}
