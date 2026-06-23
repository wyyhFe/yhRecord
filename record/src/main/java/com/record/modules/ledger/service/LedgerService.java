package com.record.modules.ledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.util.PageQuery;
import com.record.modules.ledger.model.dto.CreateBookRequest;
import com.record.modules.ledger.model.dto.CreateLedgerEntryRequest;
import com.record.modules.ledger.model.dto.UpdateLedgerEntryRequest;
import com.record.modules.ledger.model.vo.LedgerBookVO;
import com.record.modules.ledger.model.vo.LedgerEntryVO;
import com.record.modules.ledger.model.vo.PeriodStatisticsVO;
import com.record.modules.ledger.model.vo.YearStatisticsVO;

import java.time.LocalDate;
import java.util.List;

public interface LedgerService {
    LedgerBookVO createBook(Long userId, CreateBookRequest request);

    /**
     * 分页查询账本列表（管理员可查全量）。
     */
    Page<LedgerBookVO> listBooks(Long userId, PageQuery pageQuery);

    LedgerEntryVO createEntry(Long userId, CreateLedgerEntryRequest request);

    LedgerEntryVO updateEntry(Long userId, Long entryId, UpdateLedgerEntryRequest request);

    void deleteEntry(Long userId, Long entryId);

    void restoreEntry(Long userId, Long entryId);

    void forceDeleteEntry(Long userId, Long entryId);

    /**
     * 分页查询月账单（管理员可查全量）。
     */
    Page<LedgerEntryVO> monthEntries(Long userId, PageQuery pageQuery, Integer year, Integer month, Long bookId);

    YearStatisticsVO yearStatistics(Long userId, Integer year, Long bookId);

    PeriodStatisticsVO periodStatistics(Long userId, LocalDate startDate, LocalDate endDate, String type, Long bookId);

    List<LedgerEntryVO> rangeEntries(Long userId, Long bookId, LocalDate startDate, LocalDate endDate, Integer limit);

    LedgerBookVO findBook(Long userId, Long bookId);
}
