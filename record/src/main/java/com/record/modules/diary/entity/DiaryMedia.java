package com.record.modules.diary.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日记附件实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diary_media")
public class DiaryMedia extends BaseEntity {
    @TableId
    private Long id;
    /** 所属日记 ID。 */
    private Long diaryId;
    /** 附件类型。 */
    private String mediaType;
    /** OSS 路径。 */
    private String filePath;
    /** 排序值。 */
    private Integer sortOrder;
}
