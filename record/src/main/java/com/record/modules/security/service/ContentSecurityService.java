package com.record.modules.security.service;

import com.record.modules.security.model.ContentCheckResult;

public interface ContentSecurityService {

    ContentCheckResult checkText(String content, int scene);

    ContentCheckResult checkImage(String mediaUrl);
}