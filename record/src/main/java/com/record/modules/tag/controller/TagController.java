package com.record.modules.tag.controller;

import com.record.common.context.UserContext;
import com.record.common.enums.LedgerTagType;
import com.record.common.enums.TagModuleType;
import com.record.common.model.ApiResponse;
import com.record.modules.tag.model.dto.CreateFromTemplateRequest;
import com.record.modules.tag.model.dto.CreateTagRequest;
import com.record.modules.tag.model.dto.UpdateTagRequest;
import com.record.modules.tag.model.vo.TagVO;
import com.record.modules.tag.service.TagService;
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
 * 用户标签接口。
 */
@Tag(name = "用户标签")
@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Operation(summary = "查询用户标签")
    @GetMapping("/list")
    public ApiResponse<List<TagVO>> list(@RequestParam(required = false) TagModuleType moduleType,
                                         @RequestParam(required = false) LedgerTagType ledgerType) {
        return ApiResponse.success(tagService.listUserTags(UserContext.getUserId(), moduleType, ledgerType));
    }

    @Operation(summary = "创建标签")
    @PostMapping("/create")
    public ApiResponse<TagVO> create(@Valid @RequestBody CreateTagRequest request) {
        return ApiResponse.success(tagService.create(UserContext.getUserId(), request));
    }

    @Operation(summary = "基于模板创建标签")
    @PostMapping("/create-from-template")
    public ApiResponse<TagVO> createFromTemplate(@Valid @RequestBody CreateFromTemplateRequest request) {
        return ApiResponse.success(tagService.createFromTemplate(UserContext.getUserId(), request));
    }

    @Operation(summary = "更新标签")
    @PutMapping("/update/{id}")
    public ApiResponse<TagVO> update(@PathVariable Long id, @RequestBody UpdateTagRequest request) {
        return ApiResponse.success(tagService.update(UserContext.getUserId(), id, request));
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tagService.delete(UserContext.getUserId(), id);
        return ApiResponse.success();
    }
}
