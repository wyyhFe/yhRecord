package com.record.modules.recycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.ResourceType;
import com.record.modules.diary.mapper.DiaryMapper;
import com.record.modules.diary.model.entity.Diary;
import com.record.modules.ledger.mapper.LedgerBookMapper;
import com.record.modules.ledger.mapper.LedgerEntryMapper;
import com.record.modules.ledger.model.entity.LedgerBook;
import com.record.modules.ledger.model.entity.LedgerEntry;
import com.record.modules.recycle.mapper.RecycleBinRecordMapper;
import com.record.modules.recycle.model.entity.RecycleBinRecord;
import com.record.modules.recycle.model.vo.RecycleBinItemVO;
import com.record.modules.recycle.service.RecycleBinService;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecycleBinServiceImpl implements RecycleBinService {

    private final RecycleBinRecordMapper recycleBinRecordMapper;
    private final DiaryMapper diaryMapper;
    private final LedgerEntryMapper ledgerEntryMapper;
    private final LedgerBookMapper ledgerBookMapper;

    public RecycleBinServiceImpl(RecycleBinRecordMapper recycleBinRecordMapper,
                                 DiaryMapper diaryMapper,
                                 LedgerEntryMapper ledgerEntryMapper,
                                 LedgerBookMapper ledgerBookMapper) {
        this.recycleBinRecordMapper = recycleBinRecordMapper;
        this.diaryMapper = diaryMapper;
        this.ledgerEntryMapper = ledgerEntryMapper;
        this.ledgerBookMapper = ledgerBookMapper;
    }

    @Override
    public void add(Long userId, ResourceType resourceType, Long resourceId) {
        RecycleBinRecord record = new RecycleBinRecord();
        record.setUserId(userId);
        record.setResourceType(resourceType);
        record.setResourceId(resourceId);
        record.setDeletedAt(LocalDateTime.now());
        record.setExpireAt(LocalDateTime.now().plusDays(15));
        recycleBinRecordMapper.insert(record);
    }

    @Override
    public List<RecycleBinItemVO> list(Long userId) {
        List<RecycleBinRecord> records = recycleBinRecordMapper.selectList(new LambdaQueryWrapper<RecycleBinRecord>()
                .eq(RecycleBinRecord::getUserId, userId)
                .orderByDesc(RecycleBinRecord::getDeletedAt));

        List<Long> diaryIds = records.stream()
                .filter(item -> item.getResourceType() == ResourceType.DIARY)
                .map(RecycleBinRecord::getResourceId)
                .toList();
        List<Long> ledgerEntryIds = records.stream()
                .filter(item -> item.getResourceType() == ResourceType.LEDGER_ENTRY)
                .map(RecycleBinRecord::getResourceId)
                .toList();

        Map<Long, Diary> diaryMap = diaryIds.isEmpty()
                ? Map.of()
                : diaryMapper.selectBatchIds(diaryIds).stream()
                .collect(Collectors.toMap(Diary::getId, Function.identity()));
        Map<Long, LedgerEntry> ledgerEntryMap = ledgerEntryIds.isEmpty()
                ? Map.of()
                : ledgerEntryMapper.selectBatchIds(ledgerEntryIds).stream()
                .collect(Collectors.toMap(LedgerEntry::getId, Function.identity()));

        List<Long> bookIds = ledgerEntryMap.values().stream()
                .map(LedgerEntry::getBookId)
                .distinct()
                .toList();
        Map<Long, LedgerBook> bookMap = bookIds.isEmpty()
                ? Map.of()
                : ledgerBookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(LedgerBook::getId, Function.identity()));

        return records.stream()
                .map(item -> buildItem(item, diaryMap, ledgerEntryMap, bookMap))
                .toList();
    }

    @Override
    public RecycleBinRecord getOwnedRecord(Long userId, Long recycleId) {
        return recycleBinRecordMapper.selectOne(new LambdaQueryWrapper<RecycleBinRecord>()
                .eq(RecycleBinRecord::getId, recycleId)
                .eq(RecycleBinRecord::getUserId, userId));
    }

    @Override
    public void removeByResource(ResourceType resourceType, Long resourceId) {
        recycleBinRecordMapper.delete(new LambdaQueryWrapper<RecycleBinRecord>()
                .eq(RecycleBinRecord::getResourceType, resourceType)
                .eq(RecycleBinRecord::getResourceId, resourceId));
    }

    @Override
    public void restore(Long userId, Long recycleId) {
        recycleBinRecordMapper.delete(new LambdaQueryWrapper<RecycleBinRecord>()
                .eq(RecycleBinRecord::getId, recycleId)
                .eq(RecycleBinRecord::getUserId, userId));
    }

    @Override
    public void forceDelete(Long userId, Long recycleId) {
        recycleBinRecordMapper.delete(new LambdaQueryWrapper<RecycleBinRecord>()
                .eq(RecycleBinRecord::getId, recycleId)
                .eq(RecycleBinRecord::getUserId, userId));
    }

    @Override
    public void purgeExpired() {
        recycleBinRecordMapper.delete(new LambdaQueryWrapper<RecycleBinRecord>()
                .lt(RecycleBinRecord::getExpireAt, LocalDateTime.now()));
    }

    private RecycleBinItemVO buildItem(RecycleBinRecord item,
                                       Map<Long, Diary> diaryMap,
                                       Map<Long, LedgerEntry> ledgerEntryMap,
                                       Map<Long, LedgerBook> bookMap) {
        String title = null;
        String subtitle = null;

        if (item.getResourceType() == ResourceType.DIARY) {
            Diary diary = diaryMap.get(item.getResourceId());
            if (diary != null) {
                title = diary.getTitle();
                subtitle = diary.getRecordDate() != null ? diary.getRecordDate().toString() : null;
            }
        } else if (item.getResourceType() == ResourceType.LEDGER_ENTRY) {
            LedgerEntry entry = ledgerEntryMap.get(item.getResourceId());
            if (entry != null) {
                LedgerBook book = bookMap.get(entry.getBookId());
                String amount = entry.getAmount() == null
                        ? "0.00"
                        : entry.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
                title = (entry.getType() == null || "EXPENSE".equals(entry.getType().name()) ? "Expense" : "Income")
                        + " | " + amount;

                StringBuilder builder = new StringBuilder();
                if (entry.getEntryDate() != null) {
                    builder.append(entry.getEntryDate());
                }
                if (book != null && book.getName() != null && !book.getName().isBlank()) {
                    if (builder.length() > 0) {
                        builder.append(" | ");
                    }
                    builder.append(book.getName());
                }
                if (entry.getRemark() != null && !entry.getRemark().isBlank()) {
                    if (builder.length() > 0) {
                        builder.append(" | ");
                    }
                    builder.append(entry.getRemark());
                }
                subtitle = builder.length() == 0 ? null : builder.toString();
            }
        }

        return RecycleBinItemVO.builder()
                .id(item.getId())
                .resourceType(item.getResourceType())
                .resourceId(item.getResourceId())
                .deletedAt(item.getDeletedAt())
                .expireAt(item.getExpireAt())
                .title(title)
                .subtitle(subtitle)
                .build();
    }
}
