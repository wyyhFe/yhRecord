package com.record.modules.security.controller;

import com.record.common.model.ApiResponse;
import com.record.modules.security.model.ContentCheckRequest;
import com.record.modules.security.model.ContentCheckResult;
import com.record.modules.security.model.ImageCheckRequest;
import com.record.modules.security.service.ContentSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "内容安全")
@RestController
@RequestMapping("/security")
public class ContentSecurityController {

    private final ContentSecurityService contentSecurityService;

    public ContentSecurityController(ContentSecurityService contentSecurityService) {
        this.contentSecurityService = contentSecurityService;
    }

    @Operation(summary = "检测文本内容是否安全")
    @PostMapping("/check")
    public ApiResponse<ContentCheckResult> checkContent(@Valid @RequestBody ContentCheckRequest request) {
        int scene = request.getScene() != null ? request.getScene() : 4;
        ContentCheckResult result = contentSecurityService.checkText(request.getContent(), scene);
        return ApiResponse.success(result);
    }

    @Operation(summary = "检测图片内容是否安全")
    @PostMapping("/check-image")
    public ApiResponse<ContentCheckResult> checkImage(@Valid @RequestBody ImageCheckRequest request) {
        ContentCheckResult result = contentSecurityService.checkImage(request.getMediaUrl());
        return ApiResponse.success(result);
    }
}