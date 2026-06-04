package com.record.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.constant.RedisKeyConstants;
import com.record.common.exception.AuthException;
import com.record.integration.oauth.OAuthProvider;
import com.record.integration.oauth.OAuthProviderRegistry;
import com.record.integration.oauth.OAuthUserInfo;
import com.record.integration.wechat.WechatAuthClient;
import com.record.integration.wechat.WechatCode2SessionResponse;
import com.record.modules.auth.model.vo.AuthTokenVO;
import com.record.modules.auth.service.AuthService;
import com.record.modules.user.mapper.UserSessionMapper;
import com.record.modules.user.model.entity.User;
import com.record.modules.user.model.entity.UserSession;
import com.record.common.enums.LoginType;
import com.record.modules.user.service.UserService;
import com.record.modules.system.service.RoleService;
import com.record.security.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final WechatAuthClient wechatAuthClient;
    private final OAuthProviderRegistry oAuthProviderRegistry;
    private final UserService userService;
    private final UserSessionMapper userSessionMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate stringRedisTemplate;
    private final RoleService roleService;

    public AuthServiceImpl(WechatAuthClient wechatAuthClient,
                           OAuthProviderRegistry oAuthProviderRegistry,
                           UserService userService,
                           UserSessionMapper userSessionMapper,
                           JwtTokenProvider jwtTokenProvider,
                           StringRedisTemplate stringRedisTemplate,
                           RoleService roleService) {
        this.wechatAuthClient = wechatAuthClient;
        this.oAuthProviderRegistry = oAuthProviderRegistry;
        this.userService = userService;
        this.userSessionMapper = userSessionMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.stringRedisTemplate = stringRedisTemplate;
        this.roleService = roleService;
    }

    @Override
    public AuthTokenVO wxLogin(String code) {
        WechatCode2SessionResponse response = wechatAuthClient.code2Session(code);
        User user = userService.getOrCreateByOpenid(response.getOpenid());
        return issueToken(user);
    }

    @Override
    public AuthTokenVO refreshToken(String refreshToken) {
        // 每一步都打日志，定位 401 时直接看 IDE 控制台知道挂在哪
        Claims claims;
        try {
            claims = jwtTokenProvider.parseToken(refreshToken);
        } catch (Exception ex) {
            // JWT 签名错或过期都走这里
            log.warn("[refresh] 解析 refreshToken 失败: {}", ex.getMessage());
            throw ex;
        }

        if (!"refresh".equals(claims.get("typ", String.class))) {
            log.warn("[refresh] 令牌类型错误，typ={}", claims.get("typ"));
            throw new AuthException("refreshToken 类型不正确");
        }

        Long userId = ((Number) claims.get("uid")).longValue();
        String sessionId = claims.get("sid", String.class);
        log.info("[refresh] 开始刷新，userId={} tokenSid={}", userId, sessionId);

        String cachedSessionId = stringRedisTemplate.opsForValue().get(RedisKeyConstants.USER_SESSION + userId);
        log.info("[refresh] Redis 中的 sessionId={}", cachedSessionId);
        if (cachedSessionId == null) {
            log.warn("[refresh] Redis 中没有该用户的 session（key={}）", RedisKeyConstants.USER_SESSION + userId);
            throw new AuthException("登录会话已失效，请重新登录");
        }
        if (!cachedSessionId.equals(sessionId)) {
            // 这是最常见的失败原因：用户在别处又登过一次，sid 被覆盖
            log.warn("[refresh] sessionId 不匹配 token={} redis={}（多设备登录可能顶替了）", sessionId, cachedSessionId);
            throw new AuthException("登录会话已失效，请重新登录");
        }

        UserSession session = userSessionMapper.selectOne(new LambdaQueryWrapper<UserSession>()
                .eq(UserSession::getUserId, userId)
                .eq(UserSession::getSessionId, sessionId));
        if (session == null) {
            log.warn("[refresh] DB 里查不到 user_session 记录 userId={} sessionId={}", userId, sessionId);
            throw new AuthException("refreshToken 已失效");
        }
        if (!refreshToken.equals(session.getRefreshToken())) {
            log.warn("[refresh] DB 中的 refreshToken 与传入的不一致，token 长度={} db 长度={}",
                    refreshToken.length(), session.getRefreshToken() != null ? session.getRefreshToken().length() : 0);
            throw new AuthException("refreshToken 已失效");
        }
        if (session.getRefreshExpireAt().isBefore(LocalDateTime.now())) {
            log.warn("[refresh] refreshToken 已过期 expireAt={}", session.getRefreshExpireAt());
            throw new AuthException("refreshToken 已失效");
        }

        User user = userService.getOrCreateByOpenid(claims.get("openid", String.class));
        log.info("[refresh] 刷新成功，userId={}", userId);
        return issueToken(user);
    }

    @Override
    public String buildOAuthAuthorizeUrl(String provider, String state) {
        OAuthProvider p = oAuthProviderRegistry.getProvider(provider);
        return p.buildAuthorizationUrl(state);
    }

    @Override
    public AuthTokenVO oauthLogin(String provider, String code) {
        OAuthProvider p = oAuthProviderRegistry.getProvider(provider);
        OAuthUserInfo userInfo = p.handleCallback(code);

        User user;
        switch (provider) {
            case "github":
                user = userService.getOrCreateByGithubId(userInfo.getProviderUserId(), userInfo.getNickname(), userInfo.getAvatarUrl());
                break;
            case "google":
                user = userService.getOrCreateByGoogleId(userInfo.getProviderUserId(), userInfo.getNickname(), userInfo.getAvatarUrl());
                break;
            default:
                throw new AuthException("不支持的登录方式: " + provider);
        }

        // 如果是首次通过 OAuth 注册，更新头像
        if (userInfo.getAvatarUrl() != null && (user.getAvatarPath() == null || user.getAvatarPath().isEmpty())) {
            user.setAvatarPath(userInfo.getAvatarUrl());
            user.setNickname(userInfo.getNickname() != null ? userInfo.getNickname() : user.getNickname());
        }

        log.info("[OAuth] {} 登录成功, userId={}, nickname={}", provider, user.getId(), user.getNickname());
        return issueToken(user);
    }

    @Override
    public void logout(Long userId) {
        stringRedisTemplate.delete(RedisKeyConstants.USER_SESSION + userId);
        userSessionMapper.delete(new LambdaQueryWrapper<UserSession>().eq(UserSession::getUserId, userId));
    }

    private AuthTokenVO issueToken(User user) {
        String sessionId = UUID.randomUUID().toString();
        // openid 在 OAuth 登录时可能为 null，使用空字符串兜底
        String openId = user.getOpenid() != null ? user.getOpenid() : "";
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), openId, sessionId);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), openId, sessionId);

        upsertUserSession(user.getId(), sessionId, refreshToken);

        stringRedisTemplate.opsForValue().set(RedisKeyConstants.USER_SESSION + user.getId(), sessionId, 30, TimeUnit.DAYS);

        // 查询用户角色
        List<String> roles = roleService.getRoleNamesByUserId(user.getId());

        return AuthTokenVO.builder()
                .userId(user.getId())
                .openid(user.getOpenid())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .sessionId(sessionId)
                .roles(roles)
                .build();
    }

    /**
     * 单设备登录场景下，每个用户只保留一条会话记录。
     * 如果用户重复点击登录或短时间并发登录，优先更新旧记录，避免唯一索引冲突。
     */
    private void upsertUserSession(Long userId, String sessionId, String refreshToken) {
        LocalDateTime refreshExpireAt = LocalDateTime.now().plusDays(30);
        UserSession existing = userSessionMapper.selectOne(new LambdaQueryWrapper<UserSession>()
                .eq(UserSession::getUserId, userId)
                .last("LIMIT 1"));

        if (existing != null) {
            existing.setSessionId(sessionId);
            existing.setRefreshToken(refreshToken);
            existing.setRefreshExpireAt(refreshExpireAt);
            userSessionMapper.updateById(existing);
            return;
        }

        UserSession userSession = new UserSession();
        userSession.setUserId(userId);
        userSession.setSessionId(sessionId);
        userSession.setRefreshToken(refreshToken);
        userSession.setRefreshExpireAt(refreshExpireAt);

        try {
            userSessionMapper.insert(userSession);
        } catch (DuplicateKeyException exception) {
            UserSession duplicated = userSessionMapper.selectOne(new LambdaQueryWrapper<UserSession>()
                    .eq(UserSession::getUserId, userId)
                    .last("LIMIT 1"));
            if (duplicated == null) {
                throw exception;
            }
            duplicated.setSessionId(sessionId);
            duplicated.setRefreshToken(refreshToken);
            duplicated.setRefreshExpireAt(refreshExpireAt);
            userSessionMapper.updateById(duplicated);
        }
    }
}
