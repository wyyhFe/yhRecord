package com.record.modules.auth.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * OAuth state 上下文，写在 Redis 里，回调时根据 intent 分流。
 */
@Data
public class OAuthStateContext {

    /** 意图：登录 / 绑定。 */
    private Intent intent;

    /** 绑定意图下携带的当前登录用户 ID；登录意图下为 null。 */
    private Long userId;

    /** 第三方平台：github / google，便于回调时校验 path 是否被串改。 */
    private String provider;

    @JsonCreator
    public OAuthStateContext(@JsonProperty("intent") Intent intent,
                             @JsonProperty("userId") Long userId,
                             @JsonProperty("provider") String provider) {
        this.intent = intent;
        this.userId = userId;
        this.provider = provider;
    }

    public OAuthStateContext() {
    }

    public enum Intent {
        LOGIN,
        BIND
    }
}
