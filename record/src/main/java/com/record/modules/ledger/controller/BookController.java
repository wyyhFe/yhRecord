package com.record.modules.ledger.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.ledger.dto.CreateBookRequest;
import com.record.modules.ledger.service.LedgerService;
import com.record.modules.ledger.vo.LedgerBookVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 账本接口。
 */
@Tag(name = "账本")
@RestController
@RequestMapping("/books")
public class BookController {

    private final LedgerService ledgerService;

    public BookController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * 创建账本。
     */
    @Operation(summary = "创建账本")
    @PostMapping("/create")
    public ApiResponse<LedgerBookVO> create(@Valid @RequestBody CreateBookRequest request) {
        return ApiResponse.success(ledgerService.createBook(UserContext.getUserId(), request));
    }

    /**
     * 查询当前用户的账本列表。
     */
    @Operation(summary = "账本列表")
    @GetMapping("/list")
    public ApiResponse<List<LedgerBookVO>> list() {
        return ApiResponse.success(ledgerService.listBooks(UserContext.getUserId()));
    }
}
