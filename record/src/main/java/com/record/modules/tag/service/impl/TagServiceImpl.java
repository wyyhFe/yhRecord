package com.record.modules.tag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.CommonStatus;
import com.record.common.enums.LedgerTagType;
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
    public List<TagVO> listTemplates(TagModuleType moduleType, LedgerTagType ledgerType) {
        return tagTemplateMapper.selectList(new LambdaQueryWrapper<TagTemplate>()
                        .eq(moduleType != null, TagTemplate::getModuleType, moduleType)
                        .eq(ledgerType != null, TagTemplate::getLedgerType, ledgerType)
                        .eq(TagTemplate::getStatus, CommonStatus.ENABLED)
                        .orderByAsc(TagTemplate::getSortOrder))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<TagVO> listUserTags(Long userId, TagModuleType moduleType, LedgerTagType ledgerType) {
        return userTagMapper.selectList(new LambdaQueryWrapper<UserTag>()
                        .eq(UserTag::getUserId, userId)
                        .eq(moduleType != null, UserTag::getModuleType, moduleType)
                        .eq(ledgerType != null, UserTag::getLedgerType, ledgerType))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public TagVO create(Long userId, CreateTagRequest request) {
        validateLedgerType(request.getModuleType(), request.getLedgerType());
        ensureUniqueName(userId, request.getModuleType(), request.getLedgerType(), request.getName(), null);

        UserTag tag = new UserTag();
        tag.setUserId(userId);
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tag.setIcon(request.getIcon());
        tag.setModuleType(request.getModuleType());
        tag.setLedgerType(request.getLedgerType());
        userTagMapper.insert(tag);
        return toVO(tag);
    }

    @Override
    public TagVO createFromTemplate(Long userId, CreateFromTemplateRequest request) {
        TagTemplate template = tagTemplateMapper.selectById(request.getTemplateId());
        if (template == null) {
            throw new TagException("标签模板不存在");
        }

        String finalName = request.getName() != null ? request.getName() : template.getName();
        ensureUniqueName(userId, template.getModuleType(), template.getLedgerType(), finalName, null);

        UserTag tag = new UserTag();
        tag.setUserId(userId);
        tag.setTemplateId(template.getId());
        tag.setName(finalName);
        tag.setColor(request.getColor() != null ? request.getColor() : template.getColor());
        tag.setIcon(request.getIcon() != null ? request.getIcon() : template.getIcon());
        tag.setModuleType(template.getModuleType());
        tag.setLedgerType(template.getLedgerType());
        userTagMapper.insert(tag);
        return toVO(tag);
    }

    @Override
    public TagVO update(Long userId, Long id, UpdateTagRequest request) {
        UserTag tag = requireOwnedTag(userId, id);
        LedgerTagType nextLedgerType = request.getLedgerType() != null ? request.getLedgerType() : tag.getLedgerType();
        validateLedgerType(tag.getModuleType(), nextLedgerType);
        ensureUniqueName(userId, tag.getModuleType(), nextLedgerType, request.getName(), id);
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tag.setIcon(request.getIcon());
        tag.setLedgerType(nextLedgerType);
        userTagMapper.updateById(tag);
        return toVO(tag);
    }

    @Override
    public void delete(Long userId, Long id) {
        requireOwnedTag(userId, id);
        userTagMapper.deleteById(id);
    }

    private void validateLedgerType(TagModuleType moduleType, LedgerTagType ledgerType) {
        if (moduleType == TagModuleType.LEDGER && ledgerType == null) {
            throw new TagException("记账标签必须指定收入或支出类型");
        }
        if (moduleType != TagModuleType.LEDGER && ledgerType != null) {
            throw new TagException("只有记账标签才能设置收入或支出类型");
        }
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
     * 同一用户在同一模块和同一记账类型下不允许使用重复标签名。
     */
    private void ensureUniqueName(Long userId,
                                  TagModuleType moduleType,
                                  LedgerTagType ledgerType,
                                  String name,
                                  Long excludeId) {
        if (name == null || name.isBlank()) {
            return;
        }
        Long count = userTagMapper.selectCount(new LambdaQueryWrapper<UserTag>()
                .eq(UserTag::getUserId, userId)
                .eq(UserTag::getModuleType, moduleType)
                .eq(UserTag::getLedgerType, ledgerType)
                .eq(UserTag::getName, name)
                .ne(excludeId != null, UserTag::getId, excludeId));
        if (count != null && count > 0) {
            throw new TagException("同一分类下标签名称不能重复");
        }
    }

    private TagVO toVO(UserTag item) {
        return TagVO.builder()
                .id(item.getId())
                .templateId(item.getTemplateId())
                .name(item.getName())
                .color(item.getColor())
                .icon(item.getIcon())
                .moduleType(item.getModuleType())
                .ledgerType(item.getLedgerType())
                .build();
    }

    private TagVO toVO(TagTemplate item) {
        return TagVO.builder()
                .id(item.getId())
                .name(item.getName())
                .color(item.getColor())
                .icon(item.getIcon())
                .moduleType(item.getModuleType())
                .ledgerType(item.getLedgerType())
                .build();
    }
}
