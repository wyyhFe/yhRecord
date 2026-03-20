package com.record.modules.recycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.ResourceType;
import com.record.modules.recycle.entity.RecycleBinRecord;
import com.record.modules.recycle.mapper.RecycleBinRecordMapper;
import com.record.modules.recycle.service.RecycleBinService;
import com.record.modules.recycle.vo.RecycleBinItemVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecycleBinServiceImpl implements RecycleBinService {

    private final RecycleBinRecordMapper recycleBinRecordMapper;

    public RecycleBinServiceImpl(RecycleBinRecordMapper recycleBinRecordMapper) {
        this.recycleBinRecordMapper = recycleBinRecordMapper;
    }

    @Override
    public void add(Long userId, ResourceType resourceType, Long resourceId) {
        RecycleBinRecord record = new RecycleBinRecord();
        record.setUserId(userId);
        record.setResourceType(resourceType);
        record.setResourceId(resourceId);
        record.setDeletedAt(LocalDateTime.now());
        record.setExpireAt(LocalDateTime.now().plusDays(15));
        recycleBinRecordMapper.insert(record);
    }

    @Override
    public List<RecycleBinItemVO> list(Long userId) {
        return recycleBinRecordMapper.selectList(new LambdaQueryWrapper<RecycleBinRecord>()
                        .eq(RecycleBinRecord::getUserId, userId)
                        .orderByDesc(RecycleBinRecord::getDeletedAt))
                .stream().map(item -> RecycleBinItemVO.builder()
                        .id(item.getId())
                        .resourceType(item.getResourceType())
                        .resourceId(item.getResourceId())
                        .deletedAt(item.getDeletedAt())
                        .expireAt(item.getExpireAt())
                        .build()).toList();
    }

    @Override
    public void removeByResource(ResourceType resourceType, Long resourceId) {
        recycleBinRecordMapper.delete(new LambdaQueryWrapper<RecycleBinRecord>()
                .eq(RecycleBinRecord::getResourceType, resourceType)
                .eq(RecycleBinRecord::getResourceId, resourceId));
    }

    @Override
    public void restore(Long userId, Long recycleId) {
        recycleBinRecordMapper.delete(new LambdaQueryWrapper<RecycleBinRecord>()
                .eq(RecycleBinRecord::getId, recycleId)
                .eq(RecycleBinRecord::getUserId, userId));
    }

    @Override
    public void forceDelete(Long userId, Long recycleId) {
        recycleBinRecordMapper.delete(new LambdaQueryWrapper<RecycleBinRecord>()
                .eq(RecycleBinRecord::getId, recycleId)
                .eq(RecycleBinRecord::getUserId, userId));
    }

    @Override
    public void purgeExpired() {
        recycleBinRecordMapper.delete(new LambdaQueryWrapper<RecycleBinRecord>()
                .lt(RecycleBinRecord::getExpireAt, LocalDateTime.now()));
    }
}
