package com.record.modules.ledger.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.ledger.model.dto.CreateLedgerEntryRequest;
import com.record.modules.ledger.model.dto.UpdateLedgerEntryRequest;
import com.record.modules.ledger.model.vo.LedgerEntryVO;
import com.record.modules.ledger.model.vo.YearStatisticsVO;
import com.record.modules.ledger.service.LedgerService;
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
@Tag(name = "记账流水")
@RestController
@RequestMapping("/ledger")
public class LedgerEntryController {

    private final LedgerService ledgerService;

    public LedgerEntryController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @Operation(summary = "创建流水")
    @PostMapping("/entries/create")
    public ApiResponse<LedgerEntryVO> create(@Valid @RequestBody CreateLedgerEntryRequest request) {
        return ApiResponse.success(ledgerService.createEntry(UserContext.getUserId(), request));
    }

    @Operation(summary = "更新流水")
    @PutMapping("/entries/update/{id}")
    public ApiResponse<LedgerEntryVO> update(@PathVariable Long id, @Valid @RequestBody UpdateLedgerEntryRequest request) {
        return ApiResponse.success(ledgerService.updateEntry(UserContext.getUserId(), id, request));
    }

    @Operation(summary = "删除流水")
    @DeleteMapping("/entries/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ledgerService.deleteEntry(UserContext.getUserId(), id);
        return ApiResponse.success();
    }

    @Operation(summary = "查询月账单")
    @GetMapping("/entries/month")
    public ApiResponse<List<LedgerEntryVO>> month(@RequestParam Integer year,
                                                  // month 可以不传：
                                                  // 传 month 时查某年某月；不传时查某年全年明细，给前端年视图一次性使用。
                                                  @RequestParam(required = false) Integer month,
                                                  @RequestParam(required = false) Long bookId) {
        return ApiResponse.success(ledgerService.monthEntries(UserContext.getUserId(), year, month, bookId));
    }

    @Operation(summary = "查询年度统计")
    @GetMapping("/statistics/year")
    public ApiResponse<YearStatisticsVO> year(@RequestParam Integer year,
                                              @RequestParam(required = false) Long bookId) {
        return ApiResponse.success(ledgerService.yearStatistics(UserContext.getUserId(), year, bookId));
    }
}
