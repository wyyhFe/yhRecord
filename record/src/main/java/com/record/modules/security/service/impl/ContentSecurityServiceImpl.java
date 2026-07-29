package com.record.modules.security.service.impl;

import com.record.integration.wechat.WechatSecCheckClient;
import com.record.modules.security.model.ContentCheckResult;
import com.record.modules.security.service.ContentSecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContentSecurityServiceImpl implements ContentSecurityService {

    private static final Logger log = LoggerFactory.getLogger(ContentSecurityServiceImpl.class);

    private final WechatSecCheckClient wechatSecCheckClient;

    public ContentSecurityServiceImpl(WechatSecCheckClient wechatSecCheckClient) {
        this.wechatSecCheckClient = wechatSecCheckClient;
    }

    @Override
    public ContentCheckResult checkText(String content, int scene) {
        if (content == null || content.isBlank()) {
            return ContentCheckResult.safe();
        }

        try {
            boolean isSafe = wechatSecCheckClient.checkTextSecurity(content, scene, null);
            if (isSafe) {
                return ContentCheckResult.safe();
            } else {
                log.warn("内容安全检测未通过: content.length={}", content.length());
                return ContentCheckResult.unsafe("内容包含违规信息");
            }
        } catch (Exception e) {
            log.error("内容安全检测异常: {}", e.getMessage());
            return ContentCheckResult.safe();
        }
    }

    @Override
    public ContentCheckResult checkImage(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            return ContentCheckResult.safe();
        }

        try {
            boolean isSafe = wechatSecCheckClient.checkImageSecurity(mediaUrl, null);
            if (isSafe) {
                return ContentCheckResult.safe();
            } else {
                log.warn("图片安全检测未通过: mediaUrl={}", mediaUrl);
                return ContentCheckResult.unsafe("图片包含违规内容");
            }
        } catch (Exception e) {
            log.error("图片安全检测异常: {}", e.getMessage());
            return ContentCheckResult.safe();
        }
    }
}