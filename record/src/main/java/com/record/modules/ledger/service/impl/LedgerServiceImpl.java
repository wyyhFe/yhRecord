package com.record.modules.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.enums.LedgerType;
import com.record.common.enums.ResourceType;
import com.record.common.exception.LedgerException;
import com.record.common.util.AuthUtil;
import com.record.common.util.PageQuery;
import com.record.modules.ledger.mapper.LedgerBookMapper;
import com.record.modules.ledger.mapper.LedgerEntryMapper;
import com.record.modules.ledger.mapper.LedgerEntryTagRelMapper;
import com.record.modules.ledger.model.dto.CreateBookRequest;
import com.record.modules.ledger.model.dto.CreateLedgerEntryRequest;
import com.record.modules.ledger.model.dto.UpdateLedgerEntryRequest;
import com.record.modules.ledger.model.entity.LedgerBook;
import com.record.modules.ledger.model.entity.LedgerEntry;
import com.record.modules.ledger.model.entity.LedgerEntryTagRel;
import com.record.modules.ledger.model.vo.CategoryAmountVO;
import com.record.modules.ledger.model.vo.DailyAmountVO;
import com.record.modules.ledger.model.vo.LedgerBookVO;
import com.record.modules.ledger.model.vo.LedgerEntryVO;
import com.record.modules.ledger.model.vo.PeriodStatisticsVO;
import com.record.modules.ledger.model.vo.YearStatisticsVO;
import com.record.modules.ledger.service.LedgerService;
import com.record.modules.recycle.service.RecycleBinService;
import com.record.modules.tag.mapper.UserTagMapper;
import com.record.modules.tag.model.entity.UserTag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LedgerServiceImpl implements LedgerService {

    private final LedgerBookMapper ledgerBookMapper;
    private final LedgerEntryMapper ledgerEntryMapper;
    private final LedgerEntryTagRelMapper ledgerEntryTagRelMapper;
    private final UserTagMapper userTagMapper;
    private final RecycleBinService recycleBinService;

    public LedgerServiceImpl(LedgerBookMapper ledgerBookMapper,
                             LedgerEntryMapper ledgerEntryMapper,
                             LedgerEntryTagRelMapper ledgerEntryTagRelMapper,
                             UserTagMapper userTagMapper,
                             RecycleBinService recycleBinService) {
        this.ledgerBookMapper = ledgerBookMapper;
        this.ledgerEntryMapper = ledgerEntryMapper;
        this.ledgerEntryTagRelMapper = ledgerEntryTagRelMapper;
        this.userTagMapper = userTagMapper;
        this.recycleBinService = recycleBinService;
    }

    @Override
    public LedgerBookVO createBook(Long userId, CreateBookRequest request) {
        LedgerBook book = new LedgerBook();
        book.setUserId(userId);
        book.setName(request.getName());
        book.setDescription(request.getDescription());
        ledgerBookMapper.insert(book);
        return LedgerBookVO.builder()
                .id(book.getId())
                .name(book.getName())
                .description(book.getDescription())
                .build();
    }

    @Override
    public Page<LedgerBookVO> listBooks(Long userId, PageQuery pageQuery) {
        LambdaQueryWrapper<LedgerBook> wrapper = new LambdaQueryWrapper<>();
        if (!AuthUtil.isAdmin()) {
            wrapper.eq(LedgerBook::getUserId, userId);
        }
        wrapper.orderByDesc(LedgerBook::getId);
        Page<LedgerBook> page = ledgerBookMapper.selectPage(pageQuery.toPage(), wrapper);
        List<LedgerBookVO> vos = page.getRecords().stream()
                .map(item -> LedgerBookVO.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .build())
                .toList();
        Page<LedgerBookVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(vos);
        return result;
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
        LedgerEntry entry = requireOwnedEntry(userId, entryId, false);
        fillEntry(entry, userId, request);
        ledgerEntryMapper.updateById(entry);
        replaceTags(entryId, request.getTagIds());
        return toVO(entry);
    }

    @Override
    @Transactional
    public void deleteEntry(Long userId, Long entryId) {
        LedgerEntry entry = requireOwnedEntry(userId, entryId, false);
        entry.setDeletedAt(LocalDateTime.now());
        ledgerEntryMapper.updateById(entry);
        recycleBinService.add(userId, ResourceType.LEDGER_ENTRY, entryId);
    }

    @Override
    @Transactional
    public void restoreEntry(Long userId, Long entryId) {
        LedgerEntry entry = requireOwnedEntry(userId, entryId, true);
        entry.setDeletedAt(null);
        ledgerEntryMapper.updateById(entry);
        recycleBinService.removeByResource(ResourceType.LEDGER_ENTRY, entryId);
    }

    @Override
    @Transactional
    public void forceDeleteEntry(Long userId, Long entryId) {
        requireOwnedEntry(userId, entryId, true);
        ledgerEntryTagRelMapper.delete(new LambdaQueryWrapper<LedgerEntryTagRel>()
                .eq(LedgerEntryTagRel::getEntryId, entryId));
        ledgerEntryMapper.deleteById(entryId);
        recycleBinService.removeByResource(ResourceType.LEDGER_ENTRY, entryId);
    }

    @Override
    public Page<LedgerEntryVO> monthEntries(Long userId, PageQuery pageQuery, Integer year, Integer month, Long bookId) {
        LambdaQueryWrapper<LedgerEntry> wrapper = new LambdaQueryWrapper<LedgerEntry>()
                .eq(bookId != null, LedgerEntry::getBookId, bookId)
                .isNull(LedgerEntry::getDeletedAt)
                .apply("YEAR(entry_date) = {0}", year)
                .apply(month != null, "MONTH(entry_date) = {0}", month)
                .orderByDesc(LedgerEntry::getEntryDate)
                .orderByDesc(LedgerEntry::getId);
        if (!AuthUtil.isAdmin()) {
            wrapper.eq(LedgerEntry::getUserId, userId);
        }
        Page<LedgerEntry> page = ledgerEntryMapper.selectPage(pageQuery.toPage(), wrapper);
        List<LedgerEntryVO> vos = page.getRecords().stream().map(this::toVO).toList();
        Page<LedgerEntryVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(vos);
        return result;
    }

    @Override
    public YearStatisticsVO yearStatistics(Long userId, Integer year, Long bookId) {
        LambdaQueryWrapper<LedgerEntry> wrapper = new LambdaQueryWrapper<LedgerEntry>()
                .eq(bookId != null, LedgerEntry::getBookId, bookId)
                .isNull(LedgerEntry::getDeletedAt)
                .apply("YEAR(entry_date) = {0}", year);
        if (!AuthUtil.isAdmin()) {
            wrapper.eq(LedgerEntry::getUserId, userId);
        }
        List<LedgerEntry> entries = ledgerEntryMapper.selectList(wrapper);

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

        Map<Long, UserTag> tagMap = loadTagMap(amountMap.keySet());
        BigDecimal safeTotal = total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : total;

        return YearStatisticsVO.builder()
                .year(year)
                .items(amountMap.entrySet().stream()
                        .map(item -> {
                            UserTag tag = tagMap.get(item.getKey());
                            return YearStatisticsVO.TagAmountVO.builder()
                                    .tagId(item.getKey())
                                    .tagName(tag != null ? tag.getName() : null)
                                    .amount(item.getValue())
                                    .ratio(item.getValue().divide(safeTotal, 4, RoundingMode.HALF_UP))
                                    .build();
                        })
                        .toList())
                .build();
    }

    @Override
    public List<LedgerEntryVO> rangeEntries(Long userId, Long bookId, LocalDate startDate, LocalDate endDate, Integer limit) {
        // 调用方一般是 AI 模块做账单分析，这里做兜底校验，避免传错日期把数据库扫穿。
        if (startDate == null || endDate == null) {
            throw new LedgerException("分析时间范围不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new LedgerException("开始日期不能晚于结束日期");
        }

        LambdaQueryWrapper<LedgerEntry> wrapper = new LambdaQueryWrapper<LedgerEntry>()
                .eq(bookId != null, LedgerEntry::getBookId, bookId)
                .isNull(LedgerEntry::getDeletedAt)
                .ge(LedgerEntry::getEntryDate, startDate)
                .le(LedgerEntry::getEntryDate, endDate)
                .orderByDesc(LedgerEntry::getEntryDate)
                .orderByDesc(LedgerEntry::getId);
        if (!AuthUtil.isAdmin()) {
            wrapper.eq(LedgerEntry::getUserId, userId);
        }
        if (limit != null && limit > 0) {
            // 直接拼 SQL 的 LIMIT，避免再走 MP 的 Page 包一层
            wrapper.last("LIMIT " + limit);
        }

        // 复用 toVO 一次性把标签信息一起带上，AI 端聚合分类时直接用
        return ledgerEntryMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public PeriodStatisticsVO periodStatistics(Long userId, LocalDate startDate, LocalDate endDate, String type, Long bookId) {
        if (startDate == null || endDate == null) {
            throw new LedgerException("统计时间范围不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new LedgerException("开始日期不能晚于结束日期");
        }

        // 1. 查区间内所有记录
        LambdaQueryWrapper<LedgerEntry> queryWrapper = new LambdaQueryWrapper<LedgerEntry>()
                .eq(bookId != null, LedgerEntry::getBookId, bookId)
                .isNull(LedgerEntry::getDeletedAt)
                .ge(LedgerEntry::getEntryDate, startDate)
                .le(LedgerEntry::getEntryDate, endDate);
        if (!AuthUtil.isAdmin()) {
            queryWrapper.eq(LedgerEntry::getUserId, userId);
        }
        List<LedgerEntry> entries = ledgerEntryMapper.selectList(queryWrapper);

        // 2. 按类型过滤
        LedgerType filterType = "INCOME".equalsIgnoreCase(type) ? LedgerType.INCOME : LedgerType.EXPENSE;
        List<LedgerEntry> filtered = entries.stream()
                .filter(e -> e.getType() == filterType)
                .toList();

        // 3. 总额
        BigDecimal totalAmount = filtered.stream()
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 日均
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal dailyAverage = days > 0
                ? totalAmount.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 5. 结余（收入 - 支出，不受 type 过滤影响）
        BigDecimal incomeTotal = entries.stream()
                .filter(e -> e.getType() == LedgerType.INCOME)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenseTotal = entries.stream()
                .filter(e -> e.getType() == LedgerType.EXPENSE)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = incomeTotal.subtract(expenseTotal);

        // 6. 每日趋势
        Map<LocalDate, BigDecimal> dailyMap = filtered.stream()
                .collect(Collectors.groupingBy(
                        LedgerEntry::getEntryDate,
                        Collectors.reducing(BigDecimal.ZERO, LedgerEntry::getAmount, BigDecimal::add)));
        List<DailyAmountVO> dailyTrend = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dailyTrend.add(DailyAmountVO.builder()
                    .date(d)
                    .amount(dailyMap.getOrDefault(d, BigDecimal.ZERO))
                    .build());
        }

        // 7. 分类构成（按标签聚合）
        Map<Long, BigDecimal> categoryMap = new HashMap<>();
        for (LedgerEntry entry : filtered) {
            List<Long> tagIds = ledgerEntryTagRelMapper.selectList(
                    new LambdaQueryWrapper<LedgerEntryTagRel>().eq(LedgerEntryTagRel::getEntryId, entry.getId()))
                    .stream().map(LedgerEntryTagRel::getTagId).toList();
            if (tagIds.isEmpty()) {
                categoryMap.merge(0L, entry.getAmount(), BigDecimal::add); // 0 = 未分类
            } else {
                for (Long tagId : tagIds) {
                    categoryMap.merge(tagId, entry.getAmount(), BigDecimal::add);
                }
            }
        }
        BigDecimal safeTotal = totalAmount.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : totalAmount;
        Map<Long, UserTag> tagMap = loadTagMap(categoryMap.keySet().stream().filter(id -> id != 0).collect(Collectors.toSet()));
        List<CategoryAmountVO> categories = categoryMap.entrySet().stream()
                .map(e -> {
                    UserTag tag = e.getKey() == 0 ? null : tagMap.get(e.getKey());
                    return CategoryAmountVO.builder()
                            .tagId(e.getKey() == 0 ? null : e.getKey())
                            .tagName(tag != null ? tag.getName() : "未分类")
                            .amount(e.getValue())
                            .ratio(e.getValue().divide(safeTotal, 4, RoundingMode.HALF_UP))
                            .build();
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .toList();

        // 8. 上期总额（环比）
        long periodDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate prevEnd = startDate.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(periodDays - 1);
        List<LedgerEntry> prevEntries = ledgerEntryMapper.selectList(new LambdaQueryWrapper<LedgerEntry>()
                .eq(LedgerEntry::getUserId, userId)
                .eq(bookId != null, LedgerEntry::getBookId, bookId)
                .isNull(LedgerEntry::getDeletedAt)
                .ge(LedgerEntry::getEntryDate, prevStart)
                .le(LedgerEntry::getEntryDate, prevEnd)
                .eq(LedgerEntry::getType, filterType));
        BigDecimal previousTotal = prevEntries.stream()
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PeriodStatisticsVO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .type(type != null ? type.toUpperCase() : "EXPENSE")
                .totalAmount(totalAmount)
                .dailyAverage(dailyAverage)
                .previousTotal(previousTotal)
                .balance(balance)
                .dailyTrend(dailyTrend)
                .categories(categories)
                .build();
    }

    @Override
    public LedgerBookVO findBook(Long userId, Long bookId) {
        if (bookId == null) {
            return null;
        }
        LedgerBook book = ledgerBookMapper.selectById(bookId);
        if (book == null) {
            return null;
        }
        if (!AuthUtil.isAdmin() && !Objects.equals(book.getUserId(), userId)) {
            return null;
        }
        return LedgerBookVO.builder()
                .id(book.getId())
                .name(book.getName())
                .description(book.getDescription())
                .build();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new LedgerException("金额不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new LedgerException("金额必须大于 0");
        }
        if (amount.scale() > 2) {
            throw new LedgerException("金额最多保留两位小数");
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
        entry.setDeletedAt(null);
    }

    private void replaceTags(Long entryId, List<Long> tagIds) {
        ledgerEntryTagRelMapper.delete(new LambdaQueryWrapper<LedgerEntryTagRel>()
                .eq(LedgerEntryTagRel::getEntryId, entryId));
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

    private LedgerEntry requireOwnedEntry(Long userId, Long entryId, boolean includeDeleted) {
        LedgerEntry entry = ledgerEntryMapper.selectById(entryId);
        if (entry == null) {
            throw new LedgerException("账单不存在");
        }
        if (AuthUtil.isAdmin()) {
            return entry;
        }
        if (!entry.getUserId().equals(userId) || (!includeDeleted && entry.getDeletedAt() != null)) {
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

        Map<Long, UserTag> tagMap = loadTagMap(tagIds);
        List<LedgerEntryVO.TagItemVO> tags = tagIds.stream()
                .map(tagMap::get)
                .filter(Objects::nonNull)
//            .filter(tag -> tag != null)
                .map(tag -> LedgerEntryVO.TagItemVO.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .color(tag.getColor())
                        .build())
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
                .tags(tags)
                .build();
    }

    private Map<Long, UserTag> loadTagMap(Iterable<Long> tagIds) {
        List<Long> ids = new java.util.ArrayList<>();
        for (Long tagId : tagIds) {
            if (tagId != null) {
                ids.add(tagId);
            }
        }
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userTagMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(UserTag::getId, Function.identity()));
    }
}
