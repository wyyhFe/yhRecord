package com.record.modules.file.controller;

import com.record.common.model.ApiResponse;
import com.record.modules.file.model.dto.UploadPolicyRequest;
import com.record.modules.file.model.vo.UploadPolicyVO;
import com.record.modules.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件上传辅助接口。
 */
@Tag(name = "文件上传")
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "生成 OSS 上传策略")
    @PostMapping("/upload-policy")
    public ApiResponse<UploadPolicyVO> uploadPolicy(@Valid @RequestBody UploadPolicyRequest request) {
        return ApiResponse.success(fileService.generateUploadPolicy(request.getDir(), request.getExpireSeconds()));
    }
}
