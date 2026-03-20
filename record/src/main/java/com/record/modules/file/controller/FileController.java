package com.record.modules.file.controller;

import com.record.common.model.ApiResponse;
import com.record.modules.file.dto.UploadPolicyRequest;
import com.record.modules.file.service.FileService;
import com.record.modules.file.vo.UploadPolicyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件相关接口。
 */
@Tag(name = "文件")
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 获取 OSS 直传签名。
     */
    @Operation(summary = "获取 OSS 上传签名")
    @PostMapping("/upload-policy")
    public ApiResponse<UploadPolicyVO> uploadPolicy(@Valid @RequestBody UploadPolicyRequest request) {
        return ApiResponse.success(fileService.generateUploadPolicy(request.getDir(), request.getExpireSeconds()));
    }
}
