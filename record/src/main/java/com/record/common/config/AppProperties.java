package com.record.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目业务配置。
 * 统一承接 JWT、微信、地图、文件、提醒等可变参数。
 */
@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Security security = new Security();
    private Wechat wechat = new Wechat();
    private Map map = new Map();
    private File file = new File();
    private Reminder reminder = new Reminder();

    @Data
    public static class Security {
        private Jwt jwt = new Jwt();

        @Data
        public static class Jwt {
            private String issuer;
            private String secret;
            private long accessTokenExpireMinutes;
            private long refreshTokenExpireDays;
        }
    }

    @Data
    public static class Wechat {
        private String appId;
        private String appSecret;
        private String code2sessionUrl;
    }

    @Data
    public static class Map {
        private String provider;
        private String apiKey;
        private String reverseGeocodeUrl;
    }

    @Data
    public static class File {
        private long maxSizeMb;
        private List<String> allowedImageSuffixes = new ArrayList<>();
        private List<String> allowedVideoSuffixes = new ArrayList<>();
        private List<String> allowedPathPrefixes = new ArrayList<>();
    }

    @Data
    public static class Reminder {
        /**
         * 主提醒方案：小程序订阅消息。
         */
        private MiniProgram miniProgram = new MiniProgram();

        /**
         * 扩展提醒方案：公众号模板消息。
         */
        private OfficialAccount officialAccount = new OfficialAccount();

        @Data
        public static class MiniProgram {
            private String tokenUrl;
            private String sendUrl;
            private String diaryTemplateId;
            private String memorialTemplateId;
            private String diaryPage;
            private String memorialPage;
            private String state;
            private String lang;
        }

        @Data
        public static class OfficialAccount {
            private String appId;
            private String appSecret;
            private String tokenUrl;
            private String sendUrl;
            private String diaryTemplateId;
            private String memorialTemplateId;
        }
    }
}
