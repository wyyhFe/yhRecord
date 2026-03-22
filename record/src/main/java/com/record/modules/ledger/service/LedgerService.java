package com.record.modules.ledger.service;

import com.record.modules.ledger.model.dto.CreateBookRequest;
import com.record.modules.ledger.model.dto.CreateLedgerEntryRequest;
import com.record.modules.ledger.model.dto.UpdateLedgerEntryRequest;
import com.record.modules.ledger.model.vo.LedgerBookVO;
import com.record.modules.ledger.model.vo.LedgerEntryVO;
import com.record.modules.ledger.model.vo.YearStatisticsVO;

import java.util.List;

public interface LedgerService {
    LedgerBookVO createBook(Long userId, CreateBookRequest request);

    List<LedgerBookVO> listBooks(Long userId);

    LedgerEntryVO createEntry(Long userId, CreateLedgerEntryRequest request);

    LedgerEntryVO updateEntry(Long userId, Long entryId, UpdateLedgerEntryRequest request);

    void deleteEntry(Long userId, Long entryId);

    List<LedgerEntryVO> monthEntries(Long userId, Integer year, Integer month, Long bookId);

    YearStatisticsVO yearStatistics(Long userId, Integer year, Long bookId);
}
