package com.record.modules.tag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.CommonStatus;
import com.record.common.enums.TagModuleType;
import com.record.common.exception.TagException;
import com.record.modules.tag.mapper.TagTemplateMapper;
import com.record.modules.tag.mapper.UserTagMapper;
import com.record.modules.tag.model.dto.CreateFromTemplateRequest;
import com.record.modules.tag.model.dto.CreateTagRequest;
import com.record.modules.tag.model.dto.UpdateTagRequest;
import com.record.modules.tag.model.entity.TagTemplate;
import com.record.modules.tag.model.entity.UserTag;
import com.record.modules.tag.model.vo.TagVO;
import com.record.modules.tag.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签服务实现。
 */
@Service
public class TagServiceImpl implements TagService {

    private final TagTemplateMapper tagTemplateMapper;
    private final UserTagMapper userTagMapper;

    public TagServiceImpl(TagTemplateMapper tagTemplateMapper, UserTagMapper userTagMapper) {
        this.tagTemplateMapper = tagTemplateMapper;
        this.userTagMapper = userTagMapper;
    }

    @Override
    public List<TagVO> listTemplates(TagModuleType moduleType) {
        return tagTemplateMapper.selectList(new LambdaQueryWrapper<TagTemplate>()
                        .eq(moduleType != null, TagTemplate::getModuleType, moduleType)
                        .eq(TagTemplate::getStatus, CommonStatus.ENABLED)
                        .orderByAsc(TagTemplate::getSortOrder))
                .stream()
                .map(item -> TagVO.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .color(item.getColor())
                        .icon(item.getIcon())
                        .moduleType(item.getModuleType())
                        .build())
                .toList();
    }

    @Override
    public List<TagVO> listUserTags(Long userId, TagModuleType moduleType) {
        return userTagMapper.selectList(new LambdaQueryWrapper<UserTag>()
                        .eq(UserTag::getUserId, userId)
                        .eq(moduleType != null, UserTag::getModuleType, moduleType))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public TagVO create(Long userId, CreateTagRequest request) {
        UserTag tag = new UserTag();
        tag.setUserId(userId);
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tag.setIcon(request.getIcon());
        tag.setModuleType(request.getModuleType());
        userTagMapper.insert(tag);
        return toVO(tag);
    }

    @Override
    public TagVO createFromTemplate(Long userId, CreateFromTemplateRequest request) {
        TagTemplate template = tagTemplateMapper.selectById(request.getTemplateId());
        if (template == null) {
            throw new TagException("标签模板不存在");
        }

        UserTag tag = new UserTag();
        tag.setUserId(userId);
        tag.setTemplateId(template.getId());
        tag.setName(request.getName() != null ? request.getName() : template.getName());
        tag.setColor(request.getColor() != null ? request.getColor() : template.getColor());
        tag.setIcon(request.getIcon() != null ? request.getIcon() : template.getIcon());
        tag.setModuleType(template.getModuleType());
        userTagMapper.insert(tag);
        return toVO(tag);
    }

    @Override
    public TagVO update(Long userId, Long id, UpdateTagRequest request) {
        UserTag tag = requireOwnedTag(userId, id);
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tag.setIcon(request.getIcon());
        userTagMapper.updateById(tag);
        return toVO(tag);
    }

    @Override
    public void delete(Long userId, Long id) {
        requireOwnedTag(userId, id);
        userTagMapper.deleteById(id);
    }

    /**
     * 校验标签是否存在且属于当前用户。
     */
    private UserTag requireOwnedTag(Long userId, Long id) {
        UserTag tag = userTagMapper.selectById(id);
        if (tag == null || !tag.getUserId().equals(userId)) {
            throw new TagException("标签不存在或无权限操作");
        }
        return tag;
    }

    /**
     * 将实体转换为接口返回对象。
     */
    private TagVO toVO(UserTag item) {
        return TagVO.builder()
                .id(item.getId())
                .templateId(item.getTemplateId())
                .name(item.getName())
                .color(item.getColor())
                .icon(item.getIcon())
                .moduleType(item.getModuleType())
                .build();
    }
}
