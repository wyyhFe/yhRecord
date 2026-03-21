package com.record.modules.file.service;

import com.record.modules.file.model.vo.UploadPolicyVO;

public interface FileService {
    UploadPolicyVO generateUploadPolicy(String dir, int expireSeconds);
}

