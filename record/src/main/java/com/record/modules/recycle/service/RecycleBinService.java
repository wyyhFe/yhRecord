package com.record.modules.recycle.service;

import com.record.common.enums.ResourceType;
import com.record.modules.recycle.model.vo.RecycleBinItemVO;

import java.util.List;

public interface RecycleBinService {
    void add(Long userId, ResourceType resourceType, Long resourceId);
    List<RecycleBinItemVO> list(Long userId);
    void removeByResource(ResourceType resourceType, Long resourceId);
    void restore(Long userId, Long recycleId);
    void forceDelete(Long userId, Long recycleId);
    void purgeExpired();
}

