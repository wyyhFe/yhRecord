package com.record.modules.checkin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.CommonStatus;
import com.record.common.exception.CheckinException;
import com.record.modules.checkin.mapper.CheckinMediaMapper;
import com.record.modules.checkin.mapper.CheckinRecordMapper;
import com.record.modules.checkin.mapper.CheckinRecordTagMapper;
import com.record.modules.checkin.mapper.CheckinTagMapper;
import com.record.modules.checkin.mapper.CheckinTaskMapper;
import com.record.modules.checkin.model.dto.CheckinMediaDTO;
import com.record.modules.checkin.model.dto.CheckinRequest;
import com.record.modules.checkin.model.dto.CreateCheckinTagRequest;
import com.record.modules.checkin.model.dto.CreateCheckinTaskRequest;
import com.record.modules.checkin.model.dto.MendCheckinRequest;
import com.record.modules.checkin.model.entity.CheckinMedia;
import com.record.modules.checkin.model.entity.CheckinRecord;
import com.record.modules.checkin.model.entity.CheckinRecordTag;
import com.record.modules.checkin.model.entity.CheckinTag;
import com.record.modules.checkin.model.entity.CheckinTask;
import com.record.modules.checkin.model.vo.CheckinTaskVO;
import com.record.modules.checkin.model.vo.HeatmapDayVO;
import com.record.modules.checkin.model.vo.HeatmapVO;
import com.record.modules.checkin.service.CheckinService;
import com.record.modules.checkin.service.MedalService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CheckinServiceImpl implements CheckinService {

    private final CheckinTaskMapper checkinTaskMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final CheckinMediaMapper checkinMediaMapper;
    private final CheckinTagMapper checkinTagMapper;
    private final CheckinRecordTagMapper checkinRecordTagMapper;
    private final MedalService medalService;

    public CheckinServiceImpl(CheckinTaskMapper checkinTaskMapper,
                              CheckinRecordMapper checkinRecordMapper,
                              CheckinMediaMapper checkinMediaMapper,
                              CheckinTagMapper checkinTagMapper,
                              CheckinRecordTagMapper checkinRecordTagMapper,
                              @Lazy MedalService medalService) {
        this.checkinTaskMapper = checkinTaskMapper;
        this.checkinRecordMapper = checkinRecordMapper;
        this.checkinMediaMapper = checkinMediaMapper;
        this.checkinTagMapper = checkinTagMapper;
        this.checkinRecordTagMapper = checkinRecordTagMapper;
        this.medalService = medalService;
    }

    @Override
    public CheckinTaskVO createTask(Long userId, CreateCheckinTaskRequest request) {
        CheckinTask task = new CheckinTask();
        task.setUserId(userId);
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setStartDate(request.getStartDate());
        task.setStatus(CommonStatus.ENABLED);
        checkinTaskMapper.insert(task);
        return toVO(task);
    }

    @Override
    public List<CheckinTaskVO> listTasks(Long userId) {
        return checkinTaskMapper.selectList(new LambdaQueryWrapper<CheckinTask>()
                        .eq(CheckinTask::getUserId, userId)
                        .eq(CheckinTask::getStatus, CommonStatus.ENABLED))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        CheckinTask task = requireOwnedTask(userId, taskId);
        checkinRecordMapper.delete(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getTaskId, taskId)
                .eq(CheckinRecord::getUserId, userId));
        checkinTaskMapper.deleteById(task.getId());
    }

    @Override
    public void checkin(Long userId, Long taskId, CheckinRequest request) {
        CheckinTask task = requireOwnedTask(userId, taskId);

        LocalDate checkinDate = request.getCheckinDate() == null ? LocalDate.now() : request.getCheckinDate();
        Long count = checkinRecordMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getTaskId, taskId)
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinDate, checkinDate));
        if (count > 0) {
            throw new CheckinException("同一个任务当天不能重复打卡");
        }

        CheckinRecord record = new CheckinRecord();
        record.setTaskId(taskId);
        record.setUserId(userId);
        record.setCheckinDate(checkinDate);
        record.setCheckedAt(LocalDateTime.now());
        record.setRemark(request.getRemark());
        record.setMood(request.getMood());
        checkinRecordMapper.insert(record);

        // 保存标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                CheckinRecordTag recordTag = new CheckinRecordTag();
                recordTag.setRecordId(record.getId());
                recordTag.setTagId(tagId);
                checkinRecordTagMapper.insert(recordTag);
            }
        }

        if (request.getMediaList() != null) {
            for (int i = 0; i < request.getMediaList().size(); i++) {
                CheckinMediaDTO dto = request.getMediaList().get(i);
                CheckinMedia media = new CheckinMedia();
                media.setRecordId(record.getId());
                media.setMediaType(dto.getMediaType());
                media.setFilePath(dto.getFilePath());
                media.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : i + 1);
                checkinMediaMapper.insert(media);
            }
        }

        // 打卡成功后检查勋章
        medalService.checkAndUnlock(userId);
    }

    @Override
    public List<CheckinTaskVO> listByDate(Long userId, LocalDate date) {
        List<CheckinRecord> records = checkinRecordMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinDate, date));
        if (records.isEmpty()) {
            return List.of();
        }

        List<Long> taskIds = records.stream()
                .map(CheckinRecord::getTaskId).distinct().toList();
        Map<Long, CheckinRecord> recordMap = records.stream()
                .collect(Collectors.toMap(CheckinRecord::getTaskId, r -> r, (a, b) -> a));

        // 批量加载附件，避免 N+1
        List<Long> recordIds = records.stream().map(CheckinRecord::getId).toList();
        Map<Long, List<String>> mediaMap = checkinMediaMapper.selectList(
                new LambdaQueryWrapper<CheckinMedia>()
                        .in(CheckinMedia::getRecordId, recordIds)
                        .orderByAsc(CheckinMedia::getSortOrder))
                .stream()
                .collect(Collectors.groupingBy(
                        CheckinMedia::getRecordId,
                        Collectors.mapping(CheckinMedia::getFilePath, Collectors.toList())));

        // 批量加载标签，避免 N+1
        List<CheckinRecordTag> recordTags = checkinRecordTagMapper.selectList(
                new LambdaQueryWrapper<CheckinRecordTag>()
                        .in(CheckinRecordTag::getRecordId, recordIds));
        Set<Long> allTagIds = recordTags.stream()
                .map(CheckinRecordTag::getTagId).collect(Collectors.toSet());
        Map<Long, String> tagNameMap = allTagIds.isEmpty() ? Map.of() :
                checkinTagMapper.selectBatchIds(allTagIds).stream()
                        .collect(Collectors.toMap(CheckinTag::getId, CheckinTag::getName, (a, b) -> a));
        Map<Long, List<String>> tagMap = recordTags.stream()
                .collect(Collectors.groupingBy(
                        CheckinRecordTag::getRecordId,
                        Collectors.mapping(rt -> tagNameMap.getOrDefault(rt.getTagId(), ""), Collectors.toList())));

        return checkinTaskMapper.selectBatchIds(taskIds).stream()
                .map(task -> {
                    CheckinRecord record = recordMap.get(task.getId());
                    return CheckinTaskVO.builder()
                            .id(task.getId())
                            .name(task.getName())
                            .description(task.getDescription())
                            .startDate(task.getStartDate())
                            .totalCount(0) // day-detail 场景不关心累计次数
                            .latestCheckedAt(record != null ? record.getCheckedAt() : null)
                            .remark(record != null ? record.getRemark() : null)
                            .mediaPaths(record != null ? mediaMap.getOrDefault(record.getId(), List.of()) : List.of())
                            .mood(record != null ? record.getMood() : null)
                            .tagNames(record != null ? tagMap.getOrDefault(record.getId(), List.of()) : List.of())
                            .build();
                })
                .toList();
    }

    @Override
    public Map<LocalDate, Long> countByDateRange(Long userId, LocalDate start, LocalDate end) {
        List<Map<String, Object>> rows = checkinRecordMapper.countByDateRange(userId, start, end);
        Map<LocalDate, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            LocalDate date = toLocalDate(row.get("date"));
            Long cnt = ((Number) row.get("cnt")).longValue();
            result.put(date, cnt);
        }
        return result;
    }

    /**
     * 安全转换 SQL 日期为 LocalDate（兼容 java.sql.Date 和 java.time.LocalDate）。
     */
    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof java.sql.Date sd) return sd.toLocalDate();
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        throw new IllegalArgumentException("无法转换日期类型: " + value.getClass());
    }

    private CheckinTask requireOwnedTask(Long userId, Long taskId) {
        CheckinTask task = checkinTaskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new CheckinException("打卡任务不存在");
        }
        return task;
    }

    private CheckinTaskVO toVO(CheckinTask task) {
        List<CheckinRecord> records = checkinRecordMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getTaskId, task.getId())
                .orderByDesc(CheckinRecord::getCheckedAt));
        return CheckinTaskVO.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .startDate(task.getStartDate())
                .totalCount(records.size())
                .latestCheckedAt(records.stream().map(CheckinRecord::getCheckedAt).max(Comparator.naturalOrder()).orElse(null))
                .build();
    }

    @Override
    public HeatmapVO getHeatmap(Long userId, int year, int month) {
        // 1. 当月每天的完成数
        List<Map<String, Object>> rows = checkinRecordMapper.countByMonth(userId, year, month);
        Map<LocalDate, Long> dayCountMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            LocalDate date = toLocalDate(row.get("date"));
            Long cnt = ((Number) row.get("cnt")).longValue();
            dayCountMap.put(date, cnt);
        }

        // 2. 当月启用的任务数
        long totalTasks = checkinTaskMapper.selectCount(new LambdaQueryWrapper<CheckinTask>()
                .eq(CheckinTask::getUserId, userId)
                .eq(CheckinTask::getStatus, CommonStatus.ENABLED));

        // 3. 构建每天的详情
        YearMonth ym = YearMonth.of(year, month);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();
        List<HeatmapDayVO> days = new java.util.ArrayList<>();
        int monthCheckinDays = 0;
        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
            long completed = dayCountMap.getOrDefault(date, 0L);
            days.add(HeatmapDayVO.builder()
                    .date(date)
                    .totalTasks((int) totalTasks)
                    .completedTasks((int) completed)
                    .build());
            if (completed > 0) {
                monthCheckinDays++;
            }
        }

        // 4. 计算连续天数
        int[] streaks = calculateStreaks(userId);

        return HeatmapVO.builder()
                .year(year)
                .month(month)
                .currentStreak(streaks[0])
                .bestStreak(streaks[1])
                .monthCheckinDays(monthCheckinDays)
                .monthTotalDays(ym.lengthOfMonth())
                .days(days)
                .build();
    }

    /**
     * 计算连续天数。返回 [当前连续, 历史最佳]。
     */
    private int[] calculateStreaks(Long userId) {
        List<LocalDate> dates = checkinRecordMapper.selectDistinctDates(userId);
        if (dates.isEmpty()) {
            return new int[]{0, 0};
        }

        Set<LocalDate> dateSet = new HashSet<>(dates);
        LocalDate today = LocalDate.now();

        // 当前连续天数：从今天（或昨天）开始往前数
        int currentStreak = 0;
        LocalDate cursor = dateSet.contains(today) ? today : today.minusDays(1);
        while (dateSet.contains(cursor)) {
            currentStreak++;
            cursor = cursor.minusDays(1);
        }

        // 历史最佳连续天数
        int bestStreak = 0;
        int streak = 0;
        LocalDate prev = null;
        for (LocalDate date : dates) { // dates 已降序
            if (prev == null || prev.minusDays(1).equals(date)) {
                streak++;
            } else {
                bestStreak = Math.max(bestStreak, streak);
                streak = 1;
            }
            prev = date;
        }
        bestStreak = Math.max(bestStreak, streak);

        return new int[]{currentStreak, bestStreak};
    }

    @Override
    public List<CheckinTag> listTags(Long userId) {
        return checkinTagMapper.selectList(new LambdaQueryWrapper<CheckinTag>()
                .and(w -> w.isNull(CheckinTag::getUserId).or().eq(CheckinTag::getUserId, userId))
                .orderByAsc(CheckinTag::getIsSystem).orderByAsc(CheckinTag::getId));
    }

    @Override
    public CheckinTag createTag(Long userId, CreateCheckinTagRequest request) {
        CheckinTag tag = new CheckinTag();
        tag.setUserId(userId);
        tag.setName(request.getName());
        tag.setIcon(request.getIcon());
        tag.setIsSystem(false);
        checkinTagMapper.insert(tag);
        return tag;
    }

    @Override
    @Transactional
    public void mendCheckin(Long userId, MendCheckinRequest request) {
        Long taskId = request.getTaskId();
        LocalDate mendDate = request.getMendDate();

        // 1. 校验任务归属
        requireOwnedTask(userId, taskId);

        // 2. 只能补最近 7 天
        if (mendDate.isBefore(LocalDate.now().minusDays(7)) || mendDate.isAfter(LocalDate.now())) {
            throw new CheckinException("只能补最近 7 天的打卡");
        }

        // 3. 校验当月补卡次数不超过 2 次
        long used = checkinRecordMapper.countMonthlyMend(userId, mendDate.getYear(), mendDate.getMonthValue());
        if (used >= 2) {
            throw new CheckinException("本月补卡次数已用完");
        }

        // 4. 该日该任务不能已有记录
        Long existCount = checkinRecordMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getTaskId, taskId)
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinDate, mendDate));
        if (existCount > 0) {
            throw new CheckinException("该日已有打卡记录");
        }

        // 5. 创建补卡记录
        CheckinRecord record = new CheckinRecord();
        record.setTaskId(taskId);
        record.setUserId(userId);
        record.setCheckinDate(mendDate);
        record.setCheckedAt(LocalDateTime.now());
        record.setIsMend(true);
        checkinRecordMapper.insert(record);

        // 6. 检查勋章
        medalService.checkAndUnlock(userId);
    }

    @Override
    public long getMonthlyMendRemaining(Long userId) {
        LocalDate today = LocalDate.now();
        long used = checkinRecordMapper.countMonthlyMend(userId, today.getYear(), today.getMonthValue());
        return Math.max(0, 2 - used);
    }
}
