package com.record.modules.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.record.common.constant.RedisKeyConstants;
import com.record.common.exception.AuthException;
import com.record.modules.auth.model.OAuthStateContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * OAuth state 上下文存取。
 * <p>授权跳转前生成随机 state，把上下文（意图 + 当前用户）写进 Redis；
 * 回调时读出并立刻删除，作为防 CSRF 凭证。</p>
 */
@Service
public class OAuthStateService {

    /** state 在 Redis 中的存活时间。10 分钟覆盖正常授权流程时长，过期即认为是过期/重放。 */
    private static final long STATE_TTL_MINUTES = 10;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public OAuthStateService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /** 颁发登录意图的 state。 */
    public String issueLoginState(String provider) {
        return issue(new OAuthStateContext(OAuthStateContext.Intent.LOGIN, null, provider));
    }

    /** 颁发绑定意图的 state，必须传入当前登录用户 ID。 */
    public String issueBindState(String provider, Long userId) {
        return issue(new OAuthStateContext(OAuthStateContext.Intent.BIND, userId, provider));
    }

    /**
     * 消费 state：从 Redis 读取上下文并立刻删除。
     * 找不到（过期或被篡改）抛 AuthException。
     */
    public OAuthStateContext consume(String state) {
        if (state == null || state.isBlank()) {
            throw new AuthException("OAuth state 缺失");
        }
        String key = RedisKeyConstants.OAUTH_STATE + state;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            throw new AuthException("OAuth state 已过期或非法，请重新发起");
        }
        // 一次性消费：读完立即删，防止重放
        stringRedisTemplate.delete(key);
        try {
            return objectMapper.readValue(json, OAuthStateContext.class);
        } catch (JsonProcessingException ex) {
            throw new AuthException("OAuth state 反序列化失败");
        }
    }

    private String issue(OAuthStateContext context) {
        String state = UUID.randomUUID().toString().replace("-", "");
        try {
            String json = objectMapper.writeValueAsString(context);
            stringRedisTemplate.opsForValue().set(
                    RedisKeyConstants.OAUTH_STATE + state,
                    json,
                    STATE_TTL_MINUTES,
                    TimeUnit.MINUTES);
        } catch (JsonProcessingException ex) {
            throw new AuthException("OAuth state 序列化失败");
        }
        return state;
    }
}
