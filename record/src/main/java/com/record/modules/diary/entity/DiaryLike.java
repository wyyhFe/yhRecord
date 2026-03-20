package com.record.modules.diary.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 日记点赞实体。
 * 通过 diaryId + userId 唯一约束避免重复点赞。
 */
@Data
@TableName("diary_like")
public class DiaryLike {
    @TableId
    private Long id;
    /** 所属日记 ID。 */
    private Long diaryId;
    /** 点赞用户 ID。 */
    private Long userId;
}
