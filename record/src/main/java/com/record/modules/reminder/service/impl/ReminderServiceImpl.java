package com.record.modules.reminder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.config.AppProperties;
import com.record.common.config.AppProperties.Reminder.DiaryFieldMapping;
import com.record.common.config.AppProperties.Reminder.LedgerDailyFieldMapping;
import com.record.common.config.AppProperties.Reminder.LedgerMonthlyFieldMapping;
import com.record.common.config.AppProperties.Reminder.MemorialFieldMapping;
import com.record.common.enums.LedgerType;
import com.record.common.enums.ReminderBusinessType;
import com.record.common.enums.ReminderChannel;
import com.record.integration.wechat.MiniProgramMessageClient;
import com.record.integration.wechat.MiniProgramMessageRequest;
import com.record.integration.wechat.OfficialAccountClient;
import com.record.integration.wechat.OfficialMessageRequest;
import com.record.modules.diary.mapper.DiaryMapper;
import com.record.modules.diary.model.entity.Diary;
import com.record.modules.ledger.mapper.LedgerBookMapper;
import com.record.modules.ledger.mapper.LedgerEntryMapper;
import com.record.modules.ledger.model.entity.LedgerBook;
import com.record.modules.ledger.model.entity.LedgerEntry;
import com.record.modules.memorial.mapper.MemorialDayMapper;
import com.record.modules.memorial.model.entity.MemorialDay;
import com.record.modules.reminder.mapper.ReminderLogMapper;
import com.record.modules.reminder.mapper.ReminderSettingMapper;
import com.record.modules.reminder.model.dto.ReminderSettingRequest;
import com.record.modules.reminder.model.entity.ReminderLog;
import com.record.modules.reminder.model.entity.ReminderSetting;
import com.record.modules.reminder.model.vo.ReminderSettingVO;
import com.record.modules.reminder.service.ReminderService;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 提醒服务实现。
 * 当前主通道为小程序订阅消息，公众号模板消息作为扩展通道保留。
 */
@Service
public class ReminderServiceImpl implements ReminderService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ReminderSettingMapper reminderSettingMapper;
    private final ReminderLogMapper reminderLogMapper;
    private final UserMapper userMapper;
    private final DiaryMapper diaryMapper;
    private final LedgerEntryMapper ledgerEntryMapper;
    private final LedgerBookMapper ledgerBookMapper;
    private final MemorialDayMapper memorialDayMapper;
    private final MiniProgramMessageClient miniProgramMessageClient;
    private final OfficialAccountClient officialAccountClient;
    private final AppProperties appProperties;

    public ReminderServiceImpl(ReminderSettingMapper reminderSettingMapper,
                               ReminderLogMapper reminderLogMapper,
                               UserMapper userMapper,
                               DiaryMapper diaryMapper,
                               LedgerEntryMapper ledgerEntryMapper,
                               LedgerBookMapper ledgerBookMapper,
                               MemorialDayMapper memorialDayMapper,
                               MiniProgramMessageClient miniProgramMessageClient,
                               OfficialAccountClient officialAccountClient,
                               AppProperties appProperties) {
        this.reminderSettingMapper = reminderSettingMapper;
        this.reminderLogMapper = reminderLogMapper;
        this.userMapper = userMapper;
        this.diaryMapper = diaryMapper;
        this.ledgerEntryMapper = ledgerEntryMapper;
        this.ledgerBookMapper = ledgerBookMapper;
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
        setting.setMiniProgramReminderEnabled(Boolean.TRUE.equals(request.getMiniProgramReminderEnabled()));
        setting.setOfficialAccountReminderEnabled(Boolean.TRUE.equals(request.getOfficialAccountReminderEnabled()));
        reminderSettingMapper.updateById(setting);
        return toVO(setting);
    }

    @Override
    public void dispatchDiaryReminders() {
        LocalDate today = LocalDate.now();
        List<ReminderSetting> settings = reminderSettingMapper.selectList(new LambdaQueryWrapper<ReminderSetting>()
                .eq(ReminderSetting::getDiaryReminderEnabled, true));

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

    @Override
    public void dispatchDailyLedgerReminders() {
        LocalDate today = LocalDate.now();
        List<ReminderSetting> settings = reminderSettingMapper.selectList(new LambdaQueryWrapper<>());

        for (ReminderSetting setting : settings) {
            if (!hasAnyChannelEnabled(setting)) {
                continue;
            }

            Long ledgerCount = ledgerEntryMapper.selectCount(new LambdaQueryWrapper<LedgerEntry>()
                    .eq(LedgerEntry::getUserId, setting.getUserId())
                    .eq(LedgerEntry::getEntryDate, today));
            if (ledgerCount > 0) {
                continue;
            }

            User user = userMapper.selectById(setting.getUserId());
            if (user == null) {
                continue;
            }

            sendDailyLedgerReminder(setting, user, today);
        }
    }

    @Override
    public void dispatchMonthlyLedgerReminders() {
        LocalDate today = LocalDate.now();
        YearMonth reportMonth = YearMonth.from(today).minusMonths(1);
        List<ReminderSetting> settings = reminderSettingMapper.selectList(new LambdaQueryWrapper<>());

        for (ReminderSetting setting : settings) {
            if (!hasAnyChannelEnabled(setting)) {
                continue;
            }

            User user = userMapper.selectById(setting.getUserId());
            if (user == null) {
                continue;
            }

            List<LedgerBook> books = ledgerBookMapper.selectList(new LambdaQueryWrapper<LedgerBook>()
                    .eq(LedgerBook::getUserId, user.getId()));
            for (LedgerBook book : books) {
                sendMonthlyLedgerReminder(setting, user, book, reportMonth, today);
            }
        }
    }

    @Override
    public void dispatchMemorialReminders() {
        LocalDate today = LocalDate.now();
        List<MemorialDay> items = memorialDayMapper.selectList(new LambdaQueryWrapper<>());

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

    private ReminderSetting getOrCreate(Long userId) {
        ReminderSetting setting = reminderSettingMapper.selectOne(new LambdaQueryWrapper<ReminderSetting>()
                .eq(ReminderSetting::getUserId, userId));
        if (setting != null) {
            return setting;
        }

        setting = new ReminderSetting();
        setting.setUserId(userId);
        setting.setDiaryReminderEnabled(Boolean.FALSE);
        setting.setMiniProgramReminderEnabled(Boolean.FALSE);
        setting.setOfficialAccountReminderEnabled(Boolean.FALSE);
        reminderSettingMapper.insert(setting);
        return setting;
    }

    private ReminderSettingVO toVO(ReminderSetting setting) {
        return ReminderSettingVO.builder()
                .id(setting.getId())
                .diaryReminderEnabled(setting.getDiaryReminderEnabled())
                .miniProgramReminderEnabled(setting.getMiniProgramReminderEnabled())
                .officialAccountReminderEnabled(setting.getOfficialAccountReminderEnabled())
                .build();
    }

    /**
     * 发送日记提醒。
     */
    private void sendDiaryReminder(ReminderSetting setting, User user, LocalDate today) {
        if (Boolean.TRUE.equals(setting.getMiniProgramReminderEnabled())
                && hasTemplate(appProperties.getReminder().getMiniProgram().getDiaryTemplateId())
                && !hasSent(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.MINI_PROGRAM, null, today)) {
            try {
                miniProgramMessageClient.sendSubscribeMessage(MiniProgramMessageRequest.builder()
                        .toUser(user.getOpenid())
                        .templateId(appProperties.getReminder().getMiniProgram().getDiaryTemplateId())
                        .page(appProperties.getReminder().getMiniProgram().getDiaryPage())
                        .miniProgramState(appProperties.getReminder().getMiniProgram().getState())
                        .lang(appProperties.getReminder().getMiniProgram().getLang())
                        .data(buildMiniProgramDiaryData(today))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.MINI_PROGRAM, null, today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.MINI_PROGRAM, null, today, "FAILED", ex.getMessage());
            }
        }

        if (Boolean.TRUE.equals(setting.getOfficialAccountReminderEnabled())
                && hasTemplate(appProperties.getReminder().getOfficialAccount().getDiaryTemplateId())
                && user.getOfficialAccountOpenid() != null
                && !user.getOfficialAccountOpenid().isBlank()
                && !hasSent(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.OFFICIAL_ACCOUNT, null, today)) {
            try {
                officialAccountClient.sendTemplateMessage(OfficialMessageRequest.builder()
                        .toUser(user.getOfficialAccountOpenid())
                        .templateId(appProperties.getReminder().getOfficialAccount().getDiaryTemplateId())
                        .data(buildOfficialDiaryData(today))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.OFFICIAL_ACCOUNT, null, today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.DIARY_DAILY, ReminderChannel.OFFICIAL_ACCOUNT, null, today, "FAILED", ex.getMessage());
            }
        }
    }

    /**
     * 发送每日记账提醒。
     * 只有当天还没有任何流水时才发送。
     */
    private void sendDailyLedgerReminder(ReminderSetting setting, User user, LocalDate today) {
        LedgerDailySummary summary = calculateDailyLedgerSummary(user.getId(), today);

        if (Boolean.TRUE.equals(setting.getMiniProgramReminderEnabled())
                && hasTemplate(appProperties.getReminder().getMiniProgram().getLedgerTemplateId())
                && !hasSent(user.getId(), ReminderBusinessType.LEDGER_DAILY, ReminderChannel.MINI_PROGRAM, null, today)) {
            try {
                miniProgramMessageClient.sendSubscribeMessage(MiniProgramMessageRequest.builder()
                        .toUser(user.getOpenid())
                        .templateId(appProperties.getReminder().getMiniProgram().getLedgerTemplateId())
                        .page(appProperties.getReminder().getMiniProgram().getLedgerPage())
                        .miniProgramState(appProperties.getReminder().getMiniProgram().getState())
                        .lang(appProperties.getReminder().getMiniProgram().getLang())
                        .data(buildMiniProgramLedgerDailyData(summary, today))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.LEDGER_DAILY, ReminderChannel.MINI_PROGRAM, null, today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.LEDGER_DAILY, ReminderChannel.MINI_PROGRAM, null, today, "FAILED", ex.getMessage());
            }
        }

        if (Boolean.TRUE.equals(setting.getOfficialAccountReminderEnabled())
                && hasTemplate(appProperties.getReminder().getOfficialAccount().getLedgerTemplateId())
                && user.getOfficialAccountOpenid() != null
                && !user.getOfficialAccountOpenid().isBlank()
                && !hasSent(user.getId(), ReminderBusinessType.LEDGER_DAILY, ReminderChannel.OFFICIAL_ACCOUNT, null, today)) {
            try {
                officialAccountClient.sendTemplateMessage(OfficialMessageRequest.builder()
                        .toUser(user.getOfficialAccountOpenid())
                        .templateId(appProperties.getReminder().getOfficialAccount().getLedgerTemplateId())
                        .data(buildOfficialLedgerDailyData(summary, today))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.LEDGER_DAILY, ReminderChannel.OFFICIAL_ACCOUNT, null, today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.LEDGER_DAILY, ReminderChannel.OFFICIAL_ACCOUNT, null, today, "FAILED", ex.getMessage());
            }
        }
    }

    /**
     * 发送记账月报提醒。
     */
    private void sendMonthlyLedgerReminder(ReminderSetting setting, User user, LedgerBook book, YearMonth reportMonth, LocalDate today) {
        LedgerMonthlySummary summary = calculateMonthlyLedgerSummary(user.getId(), book.getId(), reportMonth);

        if (Boolean.TRUE.equals(setting.getMiniProgramReminderEnabled())
                && hasTemplate(appProperties.getReminder().getMiniProgram().getLedgerMonthlyTemplateId())
                && !hasSent(user.getId(), ReminderBusinessType.LEDGER_MONTHLY, ReminderChannel.MINI_PROGRAM, book.getId(), today)) {
            try {
                miniProgramMessageClient.sendSubscribeMessage(MiniProgramMessageRequest.builder()
                        .toUser(user.getOpenid())
                        .templateId(appProperties.getReminder().getMiniProgram().getLedgerMonthlyTemplateId())
                        .page(appProperties.getReminder().getMiniProgram().getLedgerMonthlyPage())
                        .miniProgramState(appProperties.getReminder().getMiniProgram().getState())
                        .lang(appProperties.getReminder().getMiniProgram().getLang())
                        .data(buildMiniProgramLedgerMonthlyData(book, summary))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.LEDGER_MONTHLY, ReminderChannel.MINI_PROGRAM, book.getId(), today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.LEDGER_MONTHLY, ReminderChannel.MINI_PROGRAM, book.getId(), today, "FAILED", ex.getMessage());
            }
        }

        if (Boolean.TRUE.equals(setting.getOfficialAccountReminderEnabled())
                && hasTemplate(appProperties.getReminder().getOfficialAccount().getLedgerMonthlyTemplateId())
                && user.getOfficialAccountOpenid() != null
                && !user.getOfficialAccountOpenid().isBlank()
                && !hasSent(user.getId(), ReminderBusinessType.LEDGER_MONTHLY, ReminderChannel.OFFICIAL_ACCOUNT, book.getId(), today)) {
            try {
                officialAccountClient.sendTemplateMessage(OfficialMessageRequest.builder()
                        .toUser(user.getOfficialAccountOpenid())
                        .templateId(appProperties.getReminder().getOfficialAccount().getLedgerMonthlyTemplateId())
                        .data(buildOfficialLedgerMonthlyData(book, summary))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.LEDGER_MONTHLY, ReminderChannel.OFFICIAL_ACCOUNT, book.getId(), today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.LEDGER_MONTHLY, ReminderChannel.OFFICIAL_ACCOUNT, book.getId(), today, "FAILED", ex.getMessage());
            }
        }
    }

    /**
     * 发送纪念日提醒。
     */
    private void sendMemorialReminder(ReminderSetting setting, User user, MemorialDay item, LocalDate today) {
        if (Boolean.TRUE.equals(setting.getMiniProgramReminderEnabled())
                && hasTemplate(appProperties.getReminder().getMiniProgram().getMemorialTemplateId())
                && !hasSent(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.MINI_PROGRAM, item.getId(), today)) {
            try {
                miniProgramMessageClient.sendSubscribeMessage(MiniProgramMessageRequest.builder()
                        .toUser(user.getOpenid())
                        .templateId(appProperties.getReminder().getMiniProgram().getMemorialTemplateId())
                        .page(appProperties.getReminder().getMiniProgram().getMemorialPage())
                        .miniProgramState(appProperties.getReminder().getMiniProgram().getState())
                        .lang(appProperties.getReminder().getMiniProgram().getLang())
                        .data(buildMiniProgramMemorialData(item))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.MINI_PROGRAM, item.getId(), today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.MINI_PROGRAM, item.getId(), today, "FAILED", ex.getMessage());
            }
        }

        if (Boolean.TRUE.equals(setting.getOfficialAccountReminderEnabled())
                && hasTemplate(appProperties.getReminder().getOfficialAccount().getMemorialTemplateId())
                && user.getOfficialAccountOpenid() != null
                && !user.getOfficialAccountOpenid().isBlank()
                && !hasSent(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.OFFICIAL_ACCOUNT, item.getId(), today)) {
            try {
                officialAccountClient.sendTemplateMessage(OfficialMessageRequest.builder()
                        .toUser(user.getOfficialAccountOpenid())
                        .templateId(appProperties.getReminder().getOfficialAccount().getMemorialTemplateId())
                        .data(buildOfficialMemorialData(item))
                        .build());
                saveLog(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.OFFICIAL_ACCOUNT, item.getId(), today, "SUCCESS", "sent");
            } catch (Exception ex) {
                saveLog(user.getId(), ReminderBusinessType.MEMORIAL_DAY, ReminderChannel.OFFICIAL_ACCOUNT, item.getId(), today, "FAILED", ex.getMessage());
            }
        }
    }

    private LedgerDailySummary calculateDailyLedgerSummary(Long userId, LocalDate date) {
        List<LedgerEntry> entries = ledgerEntryMapper.selectList(new LambdaQueryWrapper<LedgerEntry>()
                .eq(LedgerEntry::getUserId, userId)
                .eq(LedgerEntry::getEntryDate, date));

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (LedgerEntry entry : entries) {
            if (entry.getType() == LedgerType.INCOME) {
                income = income.add(safeAmount(entry.getAmount()));
            } else if (entry.getType() == LedgerType.EXPENSE) {
                expense = expense.add(safeAmount(entry.getAmount()));
            }
        }
        return new LedgerDailySummary(income, expense);
    }

    private LedgerMonthlySummary calculateMonthlyLedgerSummary(Long userId, Long bookId, YearMonth month) {
        List<LedgerEntry> entries = ledgerEntryMapper.selectList(new LambdaQueryWrapper<LedgerEntry>()
                .eq(LedgerEntry::getUserId, userId)
                .eq(LedgerEntry::getBookId, bookId)
                .ge(LedgerEntry::getEntryDate, month.atDay(1))
                .le(LedgerEntry::getEntryDate, month.atEndOfMonth()));

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (LedgerEntry entry : entries) {
            if (entry.getType() == LedgerType.INCOME) {
                income = income.add(safeAmount(entry.getAmount()));
            } else if (entry.getType() == LedgerType.EXPENSE) {
                expense = expense.add(safeAmount(entry.getAmount()));
            }
        }
        return new LedgerMonthlySummary(month, income, expense);
    }

    private Map<String, MiniProgramMessageRequest.TemplateData> buildMiniProgramDiaryData(LocalDate today) {
        DiaryFieldMapping fields = appProperties.getReminder().getMiniProgram().getDiaryFields();
        Map<String, MiniProgramMessageRequest.TemplateData> data = new LinkedHashMap<>();
        data.put(fields.getDateField(), miniValue(today.format(DATE_FORMATTER)));
        data.put(fields.getContentField(), miniValue("今天还没有记录日记"));
        data.put(fields.getTitleField(), miniValue("每日记录提醒"));
        return data;
    }

    private Map<String, OfficialMessageRequest.TemplateData> buildOfficialDiaryData(LocalDate today) {
        DiaryFieldMapping fields = appProperties.getReminder().getOfficialAccount().getDiaryFields();
        Map<String, OfficialMessageRequest.TemplateData> data = new LinkedHashMap<>();
        data.put(fields.getDateField(), officialValue(today.format(DATE_FORMATTER)));
        data.put(fields.getContentField(), officialValue("今天还没有记录日记"));
        data.put(fields.getTitleField(), officialValue("每日记录提醒"));
        return data;
    }

    private Map<String, MiniProgramMessageRequest.TemplateData> buildMiniProgramLedgerDailyData(LedgerDailySummary summary, LocalDate today) {
        LedgerDailyFieldMapping fields = appProperties.getReminder().getMiniProgram().getLedgerFields();
        Map<String, MiniProgramMessageRequest.TemplateData> data = new LinkedHashMap<>();
        data.put(fields.getTimeField(), miniValue(today.format(DATE_FORMATTER) + " 20:30"));
        data.put(fields.getContentField(), miniValue("今天还没有记账，记得补齐当天流水"));
        data.put(fields.getTodayExpenseField(), miniValue(formatAmount(summary.expense())));
        data.put(fields.getTodayIncomeField(), miniValue(formatAmount(summary.income())));
        data.put(fields.getTotalExpenseField(), miniValue(formatAmount(summary.expense())));
        return data;
    }

    private Map<String, OfficialMessageRequest.TemplateData> buildOfficialLedgerDailyData(LedgerDailySummary summary, LocalDate today) {
        LedgerDailyFieldMapping fields = appProperties.getReminder().getOfficialAccount().getLedgerFields();
        Map<String, OfficialMessageRequest.TemplateData> data = new LinkedHashMap<>();
        data.put(fields.getTimeField(), officialValue(today.format(DATE_FORMATTER) + " 20:30"));
        data.put(fields.getContentField(), officialValue("今天还没有记账，记得补齐当天流水"));
        data.put(fields.getTodayExpenseField(), officialValue(formatAmount(summary.expense())));
        data.put(fields.getTodayIncomeField(), officialValue(formatAmount(summary.income())));
        data.put(fields.getTotalExpenseField(), officialValue(formatAmount(summary.expense())));
        return data;
    }

    private Map<String, MiniProgramMessageRequest.TemplateData> buildMiniProgramLedgerMonthlyData(LedgerBook book, LedgerMonthlySummary summary) {
        LedgerMonthlyFieldMapping fields = appProperties.getReminder().getMiniProgram().getLedgerMonthlyFields();
        Map<String, MiniProgramMessageRequest.TemplateData> data = new LinkedHashMap<>();
        data.put(fields.getBookNameField(), miniValue(book.getName()));
        data.put(fields.getMonthField(), miniValue(summary.month().format(MONTH_FORMATTER)));
        data.put(fields.getLastMonthExpenseField(), miniValue(formatAmount(summary.expense())));
        data.put(fields.getLastMonthIncomeField(), miniValue(formatAmount(summary.income())));
        data.put(fields.getRemarkField(), miniValue("上月账单月报已生成，可进入账本查看详情"));
        return data;
    }

    private Map<String, OfficialMessageRequest.TemplateData> buildOfficialLedgerMonthlyData(LedgerBook book, LedgerMonthlySummary summary) {
        LedgerMonthlyFieldMapping fields = appProperties.getReminder().getOfficialAccount().getLedgerMonthlyFields();
        Map<String, OfficialMessageRequest.TemplateData> data = new LinkedHashMap<>();
        data.put(fields.getBookNameField(), officialValue(book.getName()));
        data.put(fields.getMonthField(), officialValue(summary.month().format(MONTH_FORMATTER)));
        data.put(fields.getLastMonthExpenseField(), officialValue(formatAmount(summary.expense())));
        data.put(fields.getLastMonthIncomeField(), officialValue(formatAmount(summary.income())));
        data.put(fields.getRemarkField(), officialValue("上月账单月报已生成，可进入账本查看详情"));
        return data;
    }

    private Map<String, MiniProgramMessageRequest.TemplateData> buildMiniProgramMemorialData(MemorialDay item) {
        MemorialFieldMapping fields = appProperties.getReminder().getMiniProgram().getMemorialFields();
        Map<String, MiniProgramMessageRequest.TemplateData> data = new LinkedHashMap<>();
        data.put(fields.getTimeField(), miniValue(formatMemorialTime(item)));
        data.put(fields.getRemarkField(), miniValue(item.getRemark() == null || item.getRemark().isBlank() ? "纪念日到了，记得查看详情" : item.getRemark()));
        data.put(fields.getNameField(), miniValue(item.getTitle()));
        return data;
    }

    private Map<String, OfficialMessageRequest.TemplateData> buildOfficialMemorialData(MemorialDay item) {
        MemorialFieldMapping fields = appProperties.getReminder().getOfficialAccount().getMemorialFields();
        Map<String, OfficialMessageRequest.TemplateData> data = new LinkedHashMap<>();
        data.put(fields.getTimeField(), officialValue(formatMemorialTime(item)));
        data.put(fields.getRemarkField(), officialValue(item.getRemark() == null || item.getRemark().isBlank() ? "纪念日到了，记得查看详情" : item.getRemark()));
        data.put(fields.getNameField(), officialValue(item.getTitle()));
        return data;
    }

    private String formatMemorialTime(MemorialDay item) {
        if (item.getRemindAt() != null) {
            return item.getRemindAt().format(DATE_TIME_FORMATTER);
        }
        return item.getMemorialDate().format(DATE_FORMATTER) + " " + LocalTime.of(9, 0).format(TIME_FORMATTER);
    }

    private boolean matches(MemorialDay item, LocalDate today) {
        if (Boolean.TRUE.equals(item.getAnnualRepeat())) {
            return item.getMemorialDate().getMonthValue() == today.getMonthValue()
                    && item.getMemorialDate().getDayOfMonth() == today.getDayOfMonth();
        }
        return item.getMemorialDate().equals(today);
    }

    private boolean hasAnyChannelEnabled(ReminderSetting setting) {
        return Boolean.TRUE.equals(setting.getMiniProgramReminderEnabled())
                || Boolean.TRUE.equals(setting.getOfficialAccountReminderEnabled());
    }

    private boolean hasTemplate(String templateId) {
        return templateId != null && !templateId.isBlank();
    }

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

    private BigDecimal safeAmount(BigDecimal amount) {
        return Objects.requireNonNullElse(amount, BigDecimal.ZERO);
    }

    private String formatAmount(BigDecimal amount) {
        return safeAmount(amount).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private MiniProgramMessageRequest.TemplateData miniValue(String value) {
        return MiniProgramMessageRequest.TemplateData.builder().value(value).build();
    }

    private OfficialMessageRequest.TemplateData officialValue(String value) {
        return OfficialMessageRequest.TemplateData.builder().value(value).build();
    }

    private record LedgerDailySummary(BigDecimal income, BigDecimal expense) {
    }

    private record LedgerMonthlySummary(YearMonth month, BigDecimal income, BigDecimal expense) {
    }
}
