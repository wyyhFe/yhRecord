package com.record.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.constant.RedisKeyConstants;
import com.record.common.exception.AuthException;
import com.record.integration.wechat.WechatAuthClient;
import com.record.integration.wechat.WechatCode2SessionResponse;
import com.record.modules.auth.service.AuthService;
import com.record.modules.auth.vo.AuthTokenVO;
import com.record.modules.user.entity.User;
import com.record.modules.user.entity.UserSession;
import com.record.modules.user.mapper.UserSessionMapper;
import com.record.modules.user.service.UserService;
import com.record.security.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现。
 * 核心职责是把微信 openid、JWT 双 token 和单设备会话串成一个完整登录链路。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final WechatAuthClient wechatAuthClient;
    private final UserService userService;
    private final UserSessionMapper userSessionMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate stringRedisTemplate;

    public AuthServiceImpl(WechatAuthClient wechatAuthClient,
                           UserService userService,
                           UserSessionMapper userSessionMapper,
                           JwtTokenProvider jwtTokenProvider,
                           StringRedisTemplate stringRedisTemplate) {
        this.wechatAuthClient = wechatAuthClient;
        this.userService = userService;
        this.userSessionMapper = userSessionMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 根据微信 code 换取 openid，并签发业务 token。
     */
    @Override
    public AuthTokenVO wxLogin(String code) {
        WechatCode2SessionResponse response = wechatAuthClient.code2Session(code);
        User user = userService.getOrCreateByOpenid(response.getOpenid());
        return issueToken(user);
    }

    /**
     * 校验 refresh token，并为当前用户重新签发一套 token。
     */
    @Override
    public AuthTokenVO refreshToken(String refreshToken) {
        Claims claims = jwtTokenProvider.parseToken(refreshToken);
        if (!"refresh".equals(claims.get("typ", String.class))) {
            throw new AuthException("refreshToken 无效");
        }
        Long userId = ((Number) claims.get("uid")).longValue();
        String sessionId = claims.get("sid", String.class);

        // Redis 里只保留当前在线会话，用于拦截被顶掉的旧设备。
        String cachedSessionId = stringRedisTemplate.opsForValue().get(RedisKeyConstants.USER_SESSION + userId);
        if (cachedSessionId == null || !cachedSessionId.equals(sessionId)) {
            throw new AuthException("登录状态已失效");
        }

        UserSession session = userSessionMapper.selectOne(new LambdaQueryWrapper<UserSession>()
                .eq(UserSession::getUserId, userId)
                .eq(UserSession::getSessionId, sessionId));
        if (session == null || !refreshToken.equals(session.getRefreshToken()) || session.getRefreshExpireAt().isBefore(LocalDateTime.now())) {
            throw new AuthException("refreshToken 已失效");
        }

        User user = userService.getOrCreateByOpenid(claims.get("openid", String.class));
        return issueToken(user);
    }

    /**
     * 主动退出时删除 Redis 会话和数据库会话记录。
     */
    @Override
    public void logout(Long userId) {
        stringRedisTemplate.delete(RedisKeyConstants.USER_SESSION + userId);
        userSessionMapper.delete(new LambdaQueryWrapper<UserSession>().eq(UserSession::getUserId, userId));
    }

    /**
     * 生成新的 accessToken、refreshToken 和 sessionId。
     * 新登录会先清掉旧会话，保证同一用户只有一个在线设备。
     */
    private AuthTokenVO issueToken(User user) {
        String sessionId = UUID.randomUUID().toString();
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getOpenid(), sessionId);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getOpenid(), sessionId);

        // 新登录直接顶掉旧登录。
        userSessionMapper.delete(new LambdaQueryWrapper<UserSession>().eq(UserSession::getUserId, user.getId()));

        UserSession userSession = new UserSession();
        userSession.setUserId(user.getId());
        userSession.setSessionId(sessionId);
        userSession.setRefreshToken(refreshToken);
        userSession.setRefreshExpireAt(LocalDateTime.now().plusDays(30));
        userSessionMapper.insert(userSession);

        stringRedisTemplate.opsForValue().set(RedisKeyConstants.USER_SESSION + user.getId(), sessionId, 30, TimeUnit.DAYS);
        return AuthTokenVO.builder()
                .userId(user.getId())
                .openid(user.getOpenid())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .sessionId(sessionId)
                .build();
    }
}
