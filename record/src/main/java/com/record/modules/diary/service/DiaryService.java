package com.record.modules.diary.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.enums.VisibilityType;
import com.record.common.util.PageQuery;
import com.record.modules.diary.model.dto.CreateDiaryRequest;
import com.record.modules.diary.model.dto.DiaryCommentRequest;
import com.record.modules.diary.model.dto.UpdateDiaryRequest;
import com.record.modules.diary.model.vo.DiaryCommentVO;
import com.record.modules.diary.model.vo.DiaryVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DiaryService {

    DiaryVO create(Long userId, CreateDiaryRequest request);

    DiaryVO update(Long userId, Long diaryId, UpdateDiaryRequest request);

    Page<DiaryVO> list(Long userId, PageQuery pageQuery, VisibilityType visibility, Long tagId, String keyword);

    DiaryVO detail(Long userId, Long diaryId);

    void delete(Long userId, Long diaryId);

    void restore(Long userId, Long diaryId);

    void forceDelete(Long userId, Long diaryId);

    List<DiaryVO> listByDate(Long userId, LocalDate date);

    long countByUser(Long userId);

    void toggleLike(Long userId, Long diaryId);

    void comment(Long userId, Long diaryId, DiaryCommentRequest request);

    List<DiaryCommentVO> comments(Long userId, Long diaryId);

    /**
     * 按日期范围批量统计每天的日记数量。
     * 一次查询完成，比逐天查询高效 N 倍。
     *
     * @return Map<日期, 数量>
     */
    Map<LocalDate, Long> countByDateRange(Long userId, LocalDate start, LocalDate end);

    /**
     * 日记大厅：分页查询所有用户的公开日记。
     */
    Page<DiaryVO> hall(PageQuery pageQuery);

    /**
     * 查询指定用户的公开日记（供他人主页展示）。
     */
    Page<DiaryVO> publicListByUser(Long userId, PageQuery pageQuery);

    /**
     * 查看公开日记详情（不需要是日记所有者，但日记必须是 PUBLIC）。
     */
    DiaryVO publicDetail(Long diaryId);

    /**
     * 统计指定用户的公开日记数量。
     */
    long countPublicByUser(Long userId);
}
