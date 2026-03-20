package com.record.modules.ledger.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.ledger.dto.CreateLedgerEntryRequest;
import com.record.modules.ledger.dto.UpdateLedgerEntryRequest;
import com.record.modules.ledger.service.LedgerService;
import com.record.modules.ledger.vo.LedgerEntryVO;
import com.record.modules.ledger.vo.YearStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 记账流水接口。
 */
@Tag(name = "记账")
@RestController
@RequestMapping("/ledger")
public class LedgerEntryController {

    private final LedgerService ledgerService;

    public LedgerEntryController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * 创建账单流水。
     */
    @Operation(summary = "创建账单")
    @PostMapping("/entries/create")
    public ApiResponse<LedgerEntryVO> create(@Valid @RequestBody CreateLedgerEntryRequest request) {
        return ApiResponse.success(ledgerService.createEntry(UserContext.getUserId(), request));
    }

    /**
     * 更新账单流水。
     */
    @Operation(summary = "更新账单")
    @PutMapping("/entries/update/{id}")
    public ApiResponse<LedgerEntryVO> update(@PathVariable Long id, @Valid @RequestBody UpdateLedgerEntryRequest request) {
        return ApiResponse.success(ledgerService.updateEntry(UserContext.getUserId(), id, request));
    }

    /**
     * 删除账单流水。
     */
    @Operation(summary = "删除账单")
    @DeleteMapping("/entries/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ledgerService.deleteEntry(UserContext.getUserId(), id);
        return ApiResponse.success();
    }

    /**
     * 查询指定月份的账单流水。
     */
    @Operation(summary = "月账单列表")
    @GetMapping("/entries/month")
    public ApiResponse<List<LedgerEntryVO>> month(@RequestParam Integer year,
                                                  @RequestParam Integer month,
                                                  @RequestParam(required = false) Long bookId) {
        return ApiResponse.success(ledgerService.monthEntries(UserContext.getUserId(), year, month, bookId));
    }

    /**
     * 查询年度统计。
     */
    @Operation(summary = "年度统计")
    @GetMapping("/statistics/year")
    public ApiResponse<YearStatisticsVO> year(@RequestParam Integer year) {
        return ApiResponse.success(ledgerService.yearStatistics(UserContext.getUserId(), year));
    }
}
