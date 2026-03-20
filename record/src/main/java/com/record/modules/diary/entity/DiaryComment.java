package com.record.modules.diary.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日记评论实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diary_comment")
public class DiaryComment extends BaseEntity {
    @TableId
    private Long id;
    /** 所属日记 ID。 */
    private Long diaryId;
    /** 评论用户 ID。 */
    private Long userId;
    /** 评论内容。 */
    private String content;
}
