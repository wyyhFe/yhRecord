package com.record.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用业务配置。
 * 统一管理 JWT、微信、地图、文件上传和提醒相关参数。
 */
@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Security security = new Security();
    private Wechat wechat = new Wechat();
    private OAuth oauth = new OAuth();
    private Map map = new Map();
    private File file = new File();
    private Reminder reminder = new Reminder();

    @Data
    public static class Security {
        private Jwt jwt = new Jwt();

        @Data
        public static class Jwt {
            /** JWT 签发者。 */
            private String issuer;

            /** JWT 密钥。 */
            private String secret;

            /** accessToken 有效期，单位分钟。 */
            private long accessTokenExpireMinutes;

            /** refreshToken 有效期，单位天。 */
            private long refreshTokenExpireDays;
        }
    }

    @Data
    public static class Wechat {
        /** 小程序 AppID。 */
        private String appId;

        /** 小程序 AppSecret。 */
        private String appSecret;

        /** 微信 code2Session 接口地址。 */
        private String code2sessionUrl;
    }

    @Data
    public static class Map {
        /** 地图服务提供商。 */
        private String provider;

        /** 腾讯地图 Key。 */
        private String apiKey;

        /** 逆地理编码接口地址。 */
        private String reverseGeocodeUrl;
    }

    @Data
    public static class File {
        /** 文件大小上限，单位 MB。 */
        private long maxSizeMb;

        /** 允许上传的图片后缀。 */
        private List<String> allowedImageSuffixes = new ArrayList<>();

        /** 允许上传的视频后缀。 */
        private List<String> allowedVideoSuffixes = new ArrayList<>();

        /** 允许保存的对象存储路径前缀。 */
        private List<String> allowedPathPrefixes = new ArrayList<>();
    }

    @Data
    public static class OAuth {
        private Github github = new Github();
        private Google google = new Google();

        @Data
        public static class Github {
            private String clientId;
            private String clientSecret;
            private String redirectUri;
        }

        @Data
        public static class Google {
            private String clientId;
            private String clientSecret;
            private String redirectUri;
        }
    }

    @Data
    public static class Reminder {
        private MiniProgram miniProgram = new MiniProgram();
        private OfficialAccount officialAccount = new OfficialAccount();

        @Data
        public static class MiniProgram {
            /** 获取小程序 access_token 的接口地址。 */
            private String tokenUrl;

            /** 发送小程序订阅消息的接口地址。 */
            private String sendUrl;

            /** 日记提醒模板 ID。 */
            private String diaryTemplateId;

            /** 每日记账提醒模板 ID。 */
            private String ledgerTemplateId;

            /** 记账月报模板 ID。 */
            private String ledgerMonthlyTemplateId;

            /** 纪念日提醒模板 ID。 */
            private String memorialTemplateId;

            /** 日记提醒点击后跳转页面。 */
            private String diaryPage;

            /** 每日记账提醒点击后跳转页面。 */
            private String ledgerPage;

            /** 记账月报点击后跳转页面。 */
            private String ledgerMonthlyPage;

            /** 纪念日提醒点击后跳转页面。 */
            private String memorialPage;

            /** 小程序订阅消息环境状态。 */
            private String state;

            /** 小程序订阅消息语言。 */
            private String lang;

            /** 日记模板字段映射。 */
            private DiaryFieldMapping diaryFields = new DiaryFieldMapping();

            /** 每日记账模板字段映射。 */
            private LedgerDailyFieldMapping ledgerFields = new LedgerDailyFieldMapping();

            /** 记账月报模板字段映射。 */
            private LedgerMonthlyFieldMapping ledgerMonthlyFields = new LedgerMonthlyFieldMapping();

            /** 纪念日模板字段映射。 */
            private MemorialFieldMapping memorialFields = new MemorialFieldMapping();
        }

        @Data
        public static class OfficialAccount {
            /** 公众号 AppID。 */
            private String appId;

            /** 公众号 AppSecret。 */
            private String appSecret;

            /** 获取公众号 access_token 的接口地址。 */
            private String tokenUrl;

            /** 发送公众号模板消息的接口地址。 */
            private String sendUrl;

            /** 日记提醒模板 ID。 */
            private String diaryTemplateId;

            /** 每日记账提醒模板 ID。 */
            private String ledgerTemplateId;

            /** 记账月报模板 ID。 */
            private String ledgerMonthlyTemplateId;

            /** 纪念日提醒模板 ID。 */
            private String memorialTemplateId;

            /** 日记模板字段映射。 */
            private DiaryFieldMapping diaryFields = new DiaryFieldMapping();

            /** 每日记账模板字段映射。 */
            private LedgerDailyFieldMapping ledgerFields = new LedgerDailyFieldMapping();

            /** 记账月报模板字段映射。 */
            private LedgerMonthlyFieldMapping ledgerMonthlyFields = new LedgerMonthlyFieldMapping();

            /** 纪念日模板字段映射。 */
            private MemorialFieldMapping memorialFields = new MemorialFieldMapping();
        }

        @Data
        public static class DiaryFieldMapping {
            /** 标题字段，例如 `thing1`。如果 yml 配置了同名字段，会覆盖这里的默认值。 */
            private String titleField = "thing1";

            /** 记录内容字段，例如 `thing2`。 */
            private String contentField = "thing2";

            /** 记录日期字段，例如 `date3`。 */
            private String dateField = "date3";
        }

        @Data
        public static class LedgerDailyFieldMapping {
            /** 时间字段，例如 `time1`。 */
            private String timeField = "time1";

            /** 提醒内容字段，例如 `thing3`。 */
            private String contentField = "thing3";

            /** 今日支出字段，例如 `amount15`。 */
            private String todayExpenseField = "amount15";

            /** 今日收入字段，例如 `amount17`。 */
            private String todayIncomeField = "amount17";

            /** 支出总额字段，例如 `amount5`。 */
            private String totalExpenseField = "amount5";
        }

        @Data
        public static class LedgerMonthlyFieldMapping {
            /** 账本名称字段，例如 `thing5`。 */
            private String bookNameField = "thing5";

            /** 账单月份字段，例如 `thing3`。 */
            private String monthField = "thing3";

            /** 上月支出字段，例如 `amount1`。 */
            private String lastMonthExpenseField = "amount1";

            /** 上月收入字段，例如 `amount2`。 */
            private String lastMonthIncomeField = "amount2";

            /** 备注字段，例如 `thing4`。 */
            private String remarkField = "thing4";
        }

        @Data
        public static class MemorialFieldMapping {
            /** 纪念时间字段，例如 `time2`。 */
            private String timeField = "time2";

            /** 备注字段，例如 `thing4`。 */
            private String remarkField = "thing4";

            /** 纪念日名称字段，例如 `thing5`。 */
            private String nameField = "thing5";
        }
    }
}
