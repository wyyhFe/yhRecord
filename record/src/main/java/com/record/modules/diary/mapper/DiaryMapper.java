package com.record.modules.diary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.record.modules.diary.model.entity.Diary;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DiaryMapper extends BaseMapper<Diary> {

    /**
     * 按日期范围统计每天的日记数量。
     * 一次 SQL 查询，GROUP BY record_date，供日历摘要使用。
     */
    @Select("""
        SELECT record_date AS date, COUNT(*) AS cnt
        FROM biz_diary
        WHERE user_id = #{userId} AND deleted_at IS NULL
        AND record_date BETWEEN #{start} AND #{end}
        GROUP BY record_date
        """)
    List<Map<String, Object>> countByDateRange(@Param("userId") Long userId,
                                               @Param("start") LocalDate start,
                                               @Param("end") LocalDate end);
}

