package com.record.modules.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * OSS 上传签名返回对象。
 */
@Data
@Builder
@Schema(description = "OSS 上传签名返回对象")
public class UploadPolicyVO {
    @Schema(description = "OSS AccessKeyId")
    private String accessKeyId;

    @Schema(description = "Base64 编码后的 policy")
    private String policy;

    @Schema(description = "上传签名")
    private String signature;

    @Schema(description = "允许上传的目录前缀", example = "diary/20260321")
    private String dir;

    @Schema(description = "上传域名", example = "https://wyhosskey.oss-cn-hangzhou.aliyuncs.com")
    private String host;

    @Schema(description = "过期时间戳", example = "1711000000")
    private long expire;
}
