package com.record.modules.diary.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.enums.VisibilityType;
import com.record.modules.diary.dto.CreateDiaryRequest;
import com.record.modules.diary.dto.DiaryCommentRequest;
import com.record.modules.diary.dto.UpdateDiaryRequest;
import com.record.modules.diary.vo.DiaryVO;

import java.time.LocalDate;
import java.util.List;

public interface DiaryService {
    DiaryVO create(Long userId, CreateDiaryRequest request);
    DiaryVO update(Long userId, Long diaryId, UpdateDiaryRequest request);
    Page<DiaryVO> list(Long userId, long current, long size, VisibilityType visibility, Long tagId, String keyword);
    DiaryVO detail(Long userId, Long diaryId);
    void delete(Long userId, Long diaryId);
    void restore(Long userId, Long diaryId);
    void forceDelete(Long userId, Long diaryId);
    List<DiaryVO> listByDate(Long userId, LocalDate date);
    long countByUser(Long userId);
    void toggleLike(Long userId, Long diaryId);
    void comment(Long userId, Long diaryId, DiaryCommentRequest request);
    List<String> comments(Long userId, Long diaryId);
}
