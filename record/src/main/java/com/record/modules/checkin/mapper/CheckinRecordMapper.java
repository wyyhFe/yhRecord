package com.record.modules.checkin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.record.modules.checkin.model.entity.CheckinRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {

    /**
     * 按日期范围统计每天的打卡次数。
     * 一次 SQL 查询，GROUP BY checkin_date。
     */
    @Select("""
        SELECT checkin_date AS date, COUNT(*) AS cnt
        FROM checkin_record
        WHERE user_id = #{userId}
        AND checkin_date BETWEEN #{start} AND #{end}
        GROUP BY checkin_date
        """)
    List<Map<String, Object>> countByDateRange(@Param("userId") Long userId,
                                                 @Param("start") LocalDate start,
                                                 @Param("end") LocalDate end);
}

