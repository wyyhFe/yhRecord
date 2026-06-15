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
        FROM biz_checkin_record
        WHERE user_id = #{userId}
        AND checkin_date BETWEEN #{start} AND #{end}
        GROUP BY checkin_date
        """)
    List<Map<String, Object>> countByDateRange(@Param("userId") Long userId,
                                                 @Param("start") LocalDate start,
                                                 @Param("end") LocalDate end);

    /**
     * 按月统计每天的打卡次数（热力图专用）。
     */
    @Select("""
        SELECT checkin_date AS date, COUNT(*) AS cnt
        FROM biz_checkin_record
        WHERE user_id = #{userId}
        AND YEAR(checkin_date) = #{year}
        AND MONTH(checkin_date) = #{month}
        GROUP BY checkin_date
        """)
    List<Map<String, Object>> countByMonth(@Param("userId") Long userId,
                                            @Param("year") int year,
                                            @Param("month") int month);

    /**
     * 查询用户所有打卡日期（去重，降序），用于连续天数计算。
     */
    @Select("""
        SELECT DISTINCT checkin_date
        FROM biz_checkin_record
        WHERE user_id = #{userId}
        ORDER BY checkin_date DESC
        """)
    List<LocalDate> selectDistinctDates(@Param("userId") Long userId);

    /**
     * 查询当月已用补卡次数。
     */
    @Select("""
        SELECT COUNT(*) FROM biz_checkin_record
        WHERE user_id = #{userId}
        AND is_mend = 1
        AND YEAR(checkin_date) = #{year}
        AND MONTH(checkin_date) = #{month}
        """)
    long countMonthlyMend(@Param("userId") Long userId,
                          @Param("year") int year,
                          @Param("month") int month);
}

