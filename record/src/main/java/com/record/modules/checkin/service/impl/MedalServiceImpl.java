package com.record.modules.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.CommonStatus;
import com.record.modules.checkin.mapper.AchievementMedalMapper;
import com.record.modules.checkin.mapper.CheckinRecordMapper;
import com.record.modules.checkin.mapper.CheckinTaskMapper;
import com.record.modules.checkin.mapper.UserMedalMapper;
import com.record.modules.checkin.model.entity.AchievementMedal;
import com.record.modules.checkin.model.entity.CheckinRecord;
import com.record.modules.checkin.model.entity.CheckinTask;
import com.record.modules.checkin.model.entity.UserMedal;
import com.record.modules.checkin.model.vo.MedalVO;
import com.record.modules.checkin.service.MedalService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MedalServiceImpl implements MedalService {

    private final AchievementMedalMapper medalMapper;
    private final UserMedalMapper userMedalMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final CheckinTaskMapper checkinTaskMapper;

    public MedalServiceImpl(AchievementMedalMapper medalMapper,
                            UserMedalMapper userMedalMapper,
                            CheckinRecordMapper checkinRecordMapper,
                            CheckinTaskMapper checkinTaskMapper) {
        this.medalMapper = medalMapper;
        this.userMedalMapper = userMedalMapper;
        this.checkinRecordMapper = checkinRecordMapper;
        this.checkinTaskMapper = checkinTaskMapper;
    }

    @Override
    public List<MedalVO> listMedals(Long userId) {
        List<AchievementMedal> allMedals = medalMapper.selectList(
                new LambdaQueryWrapper<AchievementMedal>().orderByAsc(AchievementMedal::getSortOrder));
        Map<Long, UserMedal> unlockedMap = userMedalMapper.selectList(
                new LambdaQueryWrapper<UserMedal>().eq(UserMedal::getUserId, userId))
                .stream()
                .collect(Collectors.toMap(UserMedal::getMedalId, m -> m, (a, b) -> a));

        return allMedals.stream().map(medal -> {
            UserMedal um = unlockedMap.get(medal.getId());
            return MedalVO.builder()
                    .id(medal.getId())
                    .code(medal.getCode())
                    .name(medal.getName())
                    .description(medal.getDescription())
                    .icon(medal.getIcon())
                    .category(medal.getCategory())
                    .difficulty(medal.getDifficulty())
                    .unlocked(um != null)
                    .unlockedAt(um != null ? um.getUnlockedAt() : null)
                    .build();
        }).toList();
    }

    @Override
    public List<MedalVO> checkAndUnlock(Long userId) {
        List<AchievementMedal> allMedals = medalMapper.selectList(
                new LambdaQueryWrapper<AchievementMedal>().orderByAsc(AchievementMedal::getSortOrder));
        Set<Long> unlockedIds = userMedalMapper.selectList(
                new LambdaQueryWrapper<UserMedal>().eq(UserMedal::getUserId, userId))
                .stream()
                .map(UserMedal::getMedalId)
                .collect(Collectors.toSet());

        List<MedalVO> newlyUnlocked = new ArrayList<>();

        for (AchievementMedal medal : allMedals) {
            if (unlockedIds.contains(medal.getId())) continue;

            if (checkMedalCondition(userId, medal)) {
                // 解锁
                UserMedal um = new UserMedal();
                um.setUserId(userId);
                um.setMedalId(medal.getId());
                um.setUnlockedAt(LocalDateTime.now());
                userMedalMapper.insert(um);

                newlyUnlocked.add(MedalVO.builder()
                        .id(medal.getId())
                        .code(medal.getCode())
                        .name(medal.getName())
                        .description(medal.getDescription())
                        .icon(medal.getIcon())
                        .category(medal.getCategory())
                        .difficulty(medal.getDifficulty())
                        .unlocked(true)
                        .unlockedAt(um.getUnlockedAt())
                        .build());
            }
        }

        return newlyUnlocked;
    }

    private boolean checkMedalCondition(Long userId, AchievementMedal medal) {
        return switch (medal.getCategory()) {
            case "STREAK" -> checkStreak(userId, medal.getConditionValue());
            case "TOTAL" -> checkTotal(userId, medal.getConditionValue());
            case "TIME" -> checkTime(userId, medal.getCode(), medal.getConditionValue());
            case "SPECIAL" -> checkSpecial(userId, medal.getCode(), medal.getConditionValue());
            default -> false;
        };
    }

    /** 连续天数检查 */
    private boolean checkStreak(Long userId, int target) {
        List<LocalDate> dates = checkinRecordMapper.selectDistinctDates(userId);
        if (dates.isEmpty()) return false;

        Set<LocalDate> dateSet = new HashSet<>(dates);
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate cursor = dateSet.contains(today) ? today : today.minusDays(1);
        while (dateSet.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak >= target;
    }

    /** 累计次数检查 */
    private boolean checkTotal(Long userId, int target) {
        Long count = checkinRecordMapper.selectCount(
                new LambdaQueryWrapper<CheckinRecord>().eq(CheckinRecord::getUserId, userId));
        return count >= target;
    }

    /** 时间段检查（早起鸟/夜猫子） */
    private boolean checkTime(Long userId, String code, int target) {
        LocalTime boundary = code.startsWith("early") ? LocalTime.of(6, 0) : LocalTime.of(23, 0);
        List<CheckinRecord> records = checkinRecordMapper.selectList(
                new LambdaQueryWrapper<CheckinRecord>().eq(CheckinRecord::getUserId, userId));
        long count;
        if (code.startsWith("early")) {
            count = records.stream().filter(r -> r.getCheckedAt().toLocalTime().isBefore(boundary)).count();
        } else {
            count = records.stream().filter(r -> !r.getCheckedAt().toLocalTime().isBefore(boundary)).count();
        }
        return count >= target;
    }

    /** 特殊成就检查 */
    private boolean checkSpecial(Long userId, String code, int target) {
        return switch (code) {
            case "all_done_day" -> {
                // 检查是否有某天完成了所有启用任务
                long totalTasks = checkinTaskMapper.selectCount(
                        new LambdaQueryWrapper<CheckinTask>()
                                .eq(CheckinTask::getUserId, userId)
                                .eq(CheckinTask::getStatus, CommonStatus.ENABLED));
                if (totalTasks == 0) yield false;
                List<LocalDate> dates = checkinRecordMapper.selectDistinctDates(userId);
                for (LocalDate date : dates) {
                    Long dayCount = checkinRecordMapper.selectCount(
                            new LambdaQueryWrapper<CheckinRecord>()
                                    .eq(CheckinRecord::getUserId, userId)
                                    .eq(CheckinRecord::getCheckinDate, date));
                    if (dayCount >= totalTasks) {
                        yield true;
                    }
                }
                yield false;
            }
            case "multi_task_5" -> {
                long taskCount = checkinTaskMapper.selectCount(
                        new LambdaQueryWrapper<CheckinTask>()
                                .eq(CheckinTask::getUserId, userId)
                                .eq(CheckinTask::getStatus, CommonStatus.ENABLED));
                yield taskCount >= target;
            }
            case "comeback" -> {
                // 断签后重新连续 7 天
                List<LocalDate> dates = checkinRecordMapper.selectDistinctDates(userId);
                if (dates.size() < 8) yield false; // 至少需要 7+1 天数据
                Set<LocalDate> dateSet = new HashSet<>(dates);
                LocalDate today = LocalDate.now();
                int streak = 0;
                LocalDate cursor = dateSet.contains(today) ? today : today.minusDays(1);
                while (dateSet.contains(cursor)) {
                    streak++;
                    cursor = cursor.minusDays(1);
                }
                if (streak < target) yield false;
                // 检查 streak 之前是否有断签（说明确实是"回归"）
                LocalDate beforeStreak = (dateSet.contains(today) ? today : today.minusDays(1)).minusDays(streak);
                yield !dateSet.contains(beforeStreak);
            }
            default -> false;
        };
    }
}
