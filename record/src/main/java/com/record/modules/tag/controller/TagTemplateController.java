package com.record.modules.tag.controller;

import com.record.common.enums.TagModuleType;
import com.record.common.model.ApiResponse;
import com.record.modules.tag.model.vo.TagVO;
import com.record.modules.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签模板接口。
 */
@Tag(name = "标签模板")
@RestController
@RequestMapping("/tag-templates")
public class TagTemplateController {

    private final TagService tagService;

    public TagTemplateController(TagService tagService) {
        this.tagService = tagService;
    }

    @Operation(summary = "查询标签模板")
    @GetMapping("/list")
    public ApiResponse<List<TagVO>> list(@RequestParam(required = false) TagModuleType moduleType) {
        return ApiResponse.success(tagService.listTemplates(moduleType));
    }
}
