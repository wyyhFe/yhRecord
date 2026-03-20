package com.record.modules.ledger.service;

import com.record.modules.ledger.dto.CreateBookRequest;
import com.record.modules.ledger.dto.CreateLedgerEntryRequest;
import com.record.modules.ledger.dto.UpdateLedgerEntryRequest;
import com.record.modules.ledger.vo.LedgerBookVO;
import com.record.modules.ledger.vo.LedgerEntryVO;
import com.record.modules.ledger.vo.YearStatisticsVO;

import java.util.List;

public interface LedgerService {
    LedgerBookVO createBook(Long userId, CreateBookRequest request);
    List<LedgerBookVO> listBooks(Long userId);
    LedgerEntryVO createEntry(Long userId, CreateLedgerEntryRequest request);
    LedgerEntryVO updateEntry(Long userId, Long entryId, UpdateLedgerEntryRequest request);
    void deleteEntry(Long userId, Long entryId);
    List<LedgerEntryVO> monthEntries(Long userId, Integer year, Integer month, Long bookId);
    YearStatisticsVO yearStatistics(Long userId, Integer year);
}
