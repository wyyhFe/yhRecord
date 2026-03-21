package com.record.modules.file.service.impl;

import com.aliyun.oss.common.auth.ServiceSignature;
import com.record.common.config.AliOssProperties;
import com.record.common.exception.FileException;
import com.record.modules.file.model.vo.UploadPolicyVO;
import com.record.modules.file.service.FileService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * 文件服务实现。
 * 当前只负责生成前端直传对象存储所需的上传策略。
 */
@Service
public class FileServiceImpl implements FileService {

    private final AliOssProperties aliOssProperties;

    public FileServiceImpl(AliOssProperties aliOssProperties) {
        this.aliOssProperties = aliOssProperties;
    }

    @Override
    public UploadPolicyVO generateUploadPolicy(String dir, int expireSeconds) {
        if (dir == null || dir.isBlank()) {
            throw new FileException("上传目录不能为空");
        }

        long expireEndTime = Instant.now().getEpochSecond() + expireSeconds;
        String policy = """
                {"expiration":"%s","conditions":[["starts-with","$key","%s"]]}
                """.formatted(Instant.ofEpochSecond(expireEndTime), dir);
        String encodedPolicy = Base64.getEncoder().encodeToString(policy.getBytes(StandardCharsets.UTF_8));
        String signature = ServiceSignature.create().computeSignature(aliOssProperties.getAccessKeySecret(), encodedPolicy);

        return UploadPolicyVO.builder()
                .accessKeyId(aliOssProperties.getAccessKeyId())
                .policy(encodedPolicy)
                .signature(signature)
                .dir(dir)
                .host("https://" + aliOssProperties.getBucketName() + "." + aliOssProperties.getEndpoint())
                .expire(expireEndTime)
                .build();
    }
}
