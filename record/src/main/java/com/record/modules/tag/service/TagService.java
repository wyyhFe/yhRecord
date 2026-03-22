package com.record.modules.tag.service;

import com.record.common.enums.LedgerTagType;
import com.record.common.enums.TagModuleType;
import com.record.modules.tag.model.dto.CreateFromTemplateRequest;
import com.record.modules.tag.model.dto.CreateTagRequest;
import com.record.modules.tag.model.dto.UpdateTagRequest;
import com.record.modules.tag.model.vo.TagVO;

import java.util.List;

public interface TagService {
    List<TagVO> listTemplates(TagModuleType moduleType, LedgerTagType ledgerType);

    List<TagVO> listUserTags(Long userId, TagModuleType moduleType, LedgerTagType ledgerType);

    TagVO create(Long userId, CreateTagRequest request);

    TagVO createFromTemplate(Long userId, CreateFromTemplateRequest request);

    TagVO update(Long userId, Long id, UpdateTagRequest request);

    void delete(Long userId, Long id);
}
