package com.record.modules.reminder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.config.AppProperties;
import com.record.common.enums.ReminderBusinessType;
import com.record.common.enums.ReminderChannel;
import com.record.integration.wechat.MiniProgramMessageClient;
import com.record.integration.wechat.MiniProgramMessageRequest;
import com.record.integration.wechat.OfficialAccountClient;
import com.record.integration.wechat.OfficialMessageRequest;
import com.record.modules.diary.entity.Diary;
import com.record.modules.diary.mapper.DiaryMapper;
import com.record.modules.memorial.entity.MemorialDay;
import com.record.modules.memorial.mapper.MemorialDayMapper;
import com.record.modules.reminder.dto.ReminderSettingRequest;
import com.record.modules.reminder.entity.ReminderLog;
import com.record.modules.reminder.entity.ReminderSetting;
import com.record.modules.reminder.mapper.ReminderLogMapper;
import com.record.modules.reminder.mapper.ReminderSettingMapper;
import com.record.modules.reminder.service.ReminderService;
import com.record.modules.reminder.vo.ReminderSettingVO;
import com.record.modules.user.entity.User;
import com.record.modules.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 提醒服务实现。
 * 当前主通道为小程序订阅消息，公众号模板消息作为扩展通道并行支持。
 */
@Service
public class ReminderServiceImpl implements ReminderService {

    private final ReminderSettingMapper reminderSettingMapper;
    private final ReminderLogMapper reminderLogMapper;
    private final UserMapper userMapper;
    private final DiaryMapper diaryMapper;
    private final MemorialDayMapper memorialDayMapper;
    private final MiniProgramMessageClient miniProgramMessageClient;
    private final OfficialAccountClient officialAccountClient;
    private final AppProperties appProperties;

    public ReminderServiceImpl(ReminderSettingMapper reminderSettingMapper,
                               ReminderLogMapper reminderLogMapper,
                               UserMapper userMapper,
                               DiaryMapper diaryMapper,
                               MemorialDayMapper memorialDayMapper,
                               MiniProgramMessageClient miniProgramMessageClient,
                               OfficialAccountClient officialAccountClient,
                               AppProperties appProperties) {
        this.reminderSettingMapper = reminderSettingMapper;
        this.reminderLogMapper = reminderLogMapper;
        this.userMapper = userMapper;
        this.diaryMapper = diaryMapper;
        this.memorialDayMapper = memorialDayMapper;
        this.miniProgramMessageClient = miniProgramMessageClient;
        this.officialAccountClient = officialAccountClient;
        this.appProperties = appProperties;
    }

    @Override
    public ReminderSettingVO getSetting(Long userId) {
        return toVO(getOrCreate(userId));
    }

    @Override
    public ReminderSettingVO saveSetting(Long userId, ReminderSettingRequest request) {
        ReminderSetting setting = getOrCreate(userId);
        setting.setDiaryReminderEnabled(Boolean.TRUE.equals(request.getDiaryReminderEnabled()));
        setting.setDiaryReminderTime(request.getDiaryReminderTime());
        setting.setMiniProgramReminderEnabled(Boolean.TRUE.equals(request.getMiniProgramReminderEnabled()));
        setting.setOfficialAccountReminderEnabled(Boolean.TRUE.equals(request.getOfficialAccountReminderEnabled()));
        reminderSettingMapper.updateById(setting);
        return toVO(setting);
    }

    /**
     * 派发“今天还没写日记”的每日提醒。
     * 同一用户同一天同一通道只会发送一次。
     */
    @Override
    public void dispatchDiaryReminders() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        List<ReminderSetting> settings = reminderSettingMapper.selectList(new LambdaQueryWrapper<ReminderSetting>()
                .eq(ReminderSetting::getDiaryReminderEnabled, true)
                .eq(ReminderSetting::getDiaryReminderTime, now));

        for (ReminderSetting setting : settings) {
            if (!hasAnyChannelEnabled(setting)) {
                continue;
            }

            Long diaryCount = diaryMapper.selectCount(new LambdaQueryWrapper<Diary>()
                    .eq(Diary::getUserId, setting.getUserId())
                    .eq(Diary::getRecordDate, today)
                    .isNull(Diary::getDeletedAt));
            if (diaryCount > 0) {
                continue;
            }

            User user = userMapper.selectById(setting.getUserId());
            if (user == null) {
                continue;
            }

            sendDiaryReminder(setting, user, today);
        }
    }

    /**
     * 派发纪念日提醒。
     * 重复纪念日按月日匹配，普通纪念日按完整日期匹配。
     */
    @Override
    public void dispatchMemorialReminders() {
        LocalDate today = LocalDate.now();
        List<MemorialDay> items = memorialDayMapper.selectList(new LambdaQueryWrapper<MemorialDay>());

        for (MemorialDay item : items) {
            if (!matches(item, today)) {
                continue;
            }

            ReminderSetting setting = getOrCreate(item.getUserId());
            if (!hasAnyChannelEnabled(setting)) {
                continue;
            }

            User user = userMapper.selectById(item.getUserId());
            if (user == null) {
                continue;
            }

            sendMemorialReminder(setting, user, item, today);
        }
    }

    /**
     * 提醒设置不存在时自动创建默认配置。
     */
    private ReminderSetting getOrCreate(Long userId) {
        ReminderSetting setting = reminderSettingMapper.selectOne(new LambdaQueryWrapper<ReminderSetting>()
                .eq(ReminderSetting::getUserId, userId));
        if (setting != null) {
            return setting;
        }

        setting = new ReminderSetting();
        setting.setUserId(userId);
        setting.setDiaryReminderEnabled(Boolean.FALSE);
        setting.setDiaryReminderTime(LocalTime.of(21, 0));
        setting.setMiniProgramReminderEnabled(Boolean.FALSE);
        setting.setOfficialAccountReminderEnabled(Boolean.FALSE);
        reminderSettingMapper.insert(setting);
        return setting;
    }

    /**
     * 将实体转换为前端使用的视图对象。
     */
    private ReminderSettingVO toVO(ReminderSetting setting) {
        return ReminderSettingVO.builder()
                .id(setting.getId())
                .diaryReminderEnabled(setting.getDiaryReminderEnabled())
                .diaryReminderTime(setting.getDiaryReminderTime())
                .miniProgramReminderEnabled(setting.getMiniProgramReminderEnabled())
                .officialAccountReminderEnabled(setting.getOfficialAccountReminderEnabled())
                .build();
    }

    /**
     * 发送日记提醒。
     * 小程序订阅消息为主通道，公众号模板消息可按配置并行发送。
     */
    private void sendDiaryReminder(ReminderSetting setting, User user, LocalDate today) {
        if (Boolean.TRUE.equals(setting.getMiniProgramReminderEnabled())
                && !hasSent(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.MINI_PROGRAM, null, today)) {
            try {
                miniProgramMessageClient.sendSubscribeMessage(MiniProgramMessageRequest.builder()
                        .toUser(user.getOpenid())
                        .templateId(appProperties.getReminder().getMiniProgram().getDiaryTemplateId())
                        .page(appProperties.getReminder().getMiniProgram().getDiaryPage())
                        .miniProgramState(appProperties.getReminder().getMiniProgram().getState())
                        .lang(appProperties.getReminder().getMiniProgram().getLang())
                        .data(Map.of(
                                "thing1", MiniProgramMessageRequest.TemplateData.builder().value("生活记录提醒").build(),
                                "time2", MiniProgramMessageRequest.TemplateData.builder().value(LocalDateTime.now().toString()).build(),
                                "thing3", MiniProgramMessageRequest.TemplateData.builder().value("今天还没有记录日记").build()
                        ))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.MINI_PROGRAM, null, today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.MINI_PROGRAM, null, today, "FAILED", ex.getMessage());
            }
        }

        if (Boolean.TRUE.equals(setting.getOfficialAccountReminderEnabled())
                && user.getOfficialAccountOpenid() != null
                && !user.getOfficialAccountOpenid().isBlank()
                && !hasSent(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.OFFICIAL_ACCOUNT, null, today)) {
            try {
                officialAccountClient.sendTemplateMessage(OfficialMessageRequest.builder()
                        .toUser(user.getOfficialAccountOpenid())
                        .templateId(appProperties.getReminder().getOfficialAccount().getDiaryTemplateId())
                        .data(Map.of(
                                "thing1", OfficialMessageRequest.TemplateData.builder().value("生活记录提醒").build(),
                                "time2", OfficialMessageRequest.TemplateData.builder().value(LocalDateTime.now().toString()).build(),
                                "thing3", OfficialMessageRequest.TemplateData.builder().value("今天还没有记录日记").build()
                        ))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.OFFICIAL_ACCOUNT, null, today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.OFFICIAL_ACCOUNT, null, today, "FAILED", ex.getMessage());
            }
        }
    }

    /**
     * 发送纪念日提醒。
     * 纪念日提醒沿用与日记提醒相同的通道策略。
     */
    private void sendMemorialReminder(ReminderSetting setting, User user, MemorialDay item, LocalDate today) {
        if (Boolean.TRUE.equals(setting.getMiniProgramReminderEnabled())
                && !hasSent(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.MINI_PROGRAM, item.getId(), today)) {
            try {
                miniProgramMessageClient.sendSubscribeMessage(MiniProgramMessageRequest.builder()
                        .toUser(user.getOpenid())
                        .templateId(appProperties.getReminder().getMiniProgram().getMemorialTemplateId())
                        .page(appProperties.getReminder().getMiniProgram().getMemorialPage())
                        .miniProgramState(appProperties.getReminder().getMiniProgram().getState())
                        .lang(appProperties.getReminder().getMiniProgram().getLang())
                        .data(Map.of(
                                "thing1", MiniProgramMessageRequest.TemplateData.builder().value(item.getTitle()).build(),
                                "date2", MiniProgramMessageRequest.TemplateData.builder().value(today.toString()).build(),
                                "thing3", MiniProgramMessageRequest.TemplateData.builder().value("今天有纪念日提醒").build()
                        ))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.MINI_PROGRAM, item.getId(), today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.MINI_PROGRAM, item.getId(), today, "FAILED", ex.getMessage());
            }
        }

        if (Boolean.TRUE.equals(setting.getOfficialAccountReminderEnabled())
                && user.getOfficialAccountOpenid() != null
                && !user.getOfficialAccountOpenid().isBlank()
                && !hasSent(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.OFFICIAL_ACCOUNT, item.getId(), today)) {
            try {
                officialAccountClient.sendTemplateMessage(OfficialMessageRequest.builder()
                        .toUser(user.getOfficialAccountOpenid())
                        .templateId(appProperties.getReminder().getOfficialAccount().getMemorialTemplateId())
                        .data(Map.of(
                                "thing1", OfficialMessageRequest.TemplateData.builder().value(item.getTitle()).build(),
                                "date2", OfficialMessageRequest.TemplateData.builder().value(today.toString()).build(),
                                "thing3", OfficialMessageRequest.TemplateData.builder().value("今天有纪念日提醒").build()
                        ))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.OFFICIAL_ACCOUNT, item.getId(), today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.OFFICIAL_ACCOUNT, item.getId(), today, "FAILED", ex.getMessage());
            }
        }
    }

    /**
     * 判断纪念日是否命中今天。
     */
    private boolean matches(MemorialDay item, LocalDate today) {
        if (Boolean.TRUE.equals(item.getAnnualRepeat())) {
            return item.getMemorialDate().getMonthValue() == today.getMonthValue()
                    && item.getMemorialDate().getDayOfMonth() == today.getDayOfMonth();
        }
        return item.getMemorialDate().equals(today);
    }

    /**
     * 判断是否至少启用了一个提醒通道。
     */
    private boolean hasAnyChannelEnabled(ReminderSetting setting) {
        return Boolean.TRUE.equals(setting.getMiniProgramReminderEnabled())
                || Boolean.TRUE.equals(setting.getOfficialAccountReminderEnabled());
    }

    /**
     * 防止同一业务对象在同一天同一通道被重复提醒。
     */
    private boolean hasSent(Long userId,
                            ReminderBusinessType type,
                            ReminderChannel channel,
                            Long targetId,
                            LocalDate date) {
        return reminderLogMapper.selectCount(new LambdaQueryWrapper<ReminderLog>()
                .eq(ReminderLog::getUserId, userId)
                .eq(ReminderLog::getBusinessType, type)
                .eq(ReminderLog::getChannel, channel)
                .eq(targetId != null, ReminderLog::getTargetId, targetId)
                .eq(ReminderLog::getBusinessDate, date)) > 0;
    }

    /**
     * 记录消息派发日志，便于幂等控制和问题排查。
     */
    private void saveLog(Long userId,
                         ReminderBusinessType type,
                         ReminderChannel channel,
                         Long targetId,
                         LocalDate date,
                         String status,
                         String message) {
        ReminderLog log = new ReminderLog();
        log.setUserId(userId);
        log.setBusinessType(type);
        log.setChannel(channel);
        log.setTargetId(targetId);
        log.setBusinessDate(date);
        log.setSendStatus(status);
        log.setSendMessage(message);
        log.setSentAt(LocalDateTime.now());
        reminderLogMapper.insert(log);
    }
}
