package com.record.modules.memorial.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.common.model.PageResult;
import com.record.common.util.PageQuery;
import com.record.modules.memorial.model.dto.CreateMemorialDayRequest;
import com.record.modules.memorial.model.dto.UpdateMemorialDayRequest;
import com.record.modules.memorial.model.vo.MemorialDayVO;
import com.record.modules.memorial.service.MemorialDayService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 纪念日接口。
 */
@Tag(name = "纪念日")
@RestController
@RequestMapping("/memorial-days")
public class MemorialDayController {

    private final MemorialDayService memorialDayService;

    public MemorialDayController(MemorialDayService memorialDayService) {
        this.memorialDayService = memorialDayService;
    }

    @Operation(summary = "创建纪念日")
    @PostMapping("/create")
    public ApiResponse<MemorialDayVO> create(@Valid @RequestBody CreateMemorialDayRequest request) {
        return ApiResponse.success(memorialDayService.create(UserContext.getUserId(), request));
    }

    @Operation(summary = "分页查询纪念日列表（管理员返回全量）")
    @GetMapping("/list")
    public ApiResponse<PageResult<MemorialDayVO>> list(PageQuery pageQuery) {
        return ApiResponse.success(PageResult.from(memorialDayService.list(UserContext.getUserId(), pageQuery)));
    }

    @Operation(summary = "更新纪念日")
    @PutMapping("/update/{id}")
    public ApiResponse<MemorialDayVO> update(@PathVariable Long id, @RequestBody UpdateMemorialDayRequest request) {
        return ApiResponse.success(memorialDayService.update(UserContext.getUserId(), id, request));
    }

    @Operation(summary = "删除纪念日")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        memorialDayService.delete(UserContext.getUserId(), id);
        return ApiResponse.success();
    }
}
