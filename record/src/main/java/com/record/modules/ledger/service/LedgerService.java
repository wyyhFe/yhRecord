package com.record.modules.ledger.service;

import com.record.modules.ledger.model.dto.CreateBookRequest;
import com.record.modules.ledger.model.dto.CreateLedgerEntryRequest;
import com.record.modules.ledger.model.dto.UpdateLedgerEntryRequest;
import com.record.modules.ledger.model.vo.LedgerBookVO;
import com.record.modules.ledger.model.vo.LedgerEntryVO;
import com.record.modules.ledger.model.vo.YearStatisticsVO;

import java.time.LocalDate;
import java.util.List;

public interface LedgerService {
    LedgerBookVO createBook(Long userId, CreateBookRequest request);

    List<LedgerBookVO> listBooks(Long userId);

    LedgerEntryVO createEntry(Long userId, CreateLedgerEntryRequest request);

    LedgerEntryVO updateEntry(Long userId, Long entryId, UpdateLedgerEntryRequest request);

    void deleteEntry(Long userId, Long entryId);

    void restoreEntry(Long userId, Long entryId);

    void forceDeleteEntry(Long userId, Long entryId);

    List<LedgerEntryVO> monthEntries(Long userId, Integer year, Integer month, Long bookId);

    YearStatisticsVO yearStatistics(Long userId, Integer year, Long bookId);

    /**
     * 按日期范围拉取账单。
     * 仅给跨模块场景使用（例如 AI 模块做账单分析），需要带标签的 VO。
     * limit 为空或 ≤ 0 时不限制条数；调用方需要负责把 limit 控制在合理范围。
     */
    List<LedgerEntryVO> rangeEntries(Long userId, Long bookId, LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 按账本 ID 查询账本，做用户归属校验。
     * 找不到或不属于当前用户都返回 null，调用方需要自行处理。
     */
    LedgerBookVO findBook(Long userId, Long bookId);
}
