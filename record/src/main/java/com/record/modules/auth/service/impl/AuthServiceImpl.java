package com.record.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.config.AppProperties;
import com.record.common.constant.RedisKeyConstants;
import com.record.common.exception.AuthException;
import com.record.integration.oauth.OAuthProvider;
import com.record.integration.oauth.OAuthProviderRegistry;
import com.record.integration.oauth.OAuthUserInfo;
import com.record.integration.wechat.WechatAuthClient;
import com.record.integration.wechat.WechatCode2SessionResponse;
import com.record.modules.auth.model.vo.AuthTokenVO;
import com.record.modules.auth.service.AuthService;
import com.record.common.exception.BusinessException;
import com.record.common.exception.ErrorCode;
import com.record.modules.user.mapper.UserIdentityMapper;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.entity.User;
import com.record.modules.user.model.entity.UserIdentity;
import com.record.common.enums.LoginType;
import com.record.modules.user.service.UserService;
import com.record.modules.system.service.RoleService;
import com.record.modules.system.model.entity.Role;
import com.record.modules.system.mapper.RoleMapper;
import com.record.modules.system.mapper.UserRoleMapper;
import com.record.modules.system.model.entity.UserRole;
import com.record.security.service.JwtTokenProvider;
import com.record.common.enums.CommonStatus;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    /** Redis Hash 字段：当前活跃 sessionId。 */
    private static final String FIELD_SID = "sid";
    /** Redis Hash 字段：当前活跃 refreshToken。 */
    private static final String FIELD_REFRESH_TOKEN = "token";
    /** Redis Hash 字段：会话过期时间戳（毫秒）。 */
    private static final String FIELD_EXPIRE_AT = "expire_at";

    private final WechatAuthClient wechatAuthClient;
    private final OAuthProviderRegistry oAuthProviderRegistry;
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserIdentityMapper userIdentityMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate stringRedisTemplate;
    private final RoleService roleService;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final AppProperties appProperties;

    public AuthServiceImpl(WechatAuthClient wechatAuthClient,
                           OAuthProviderRegistry oAuthProviderRegistry,
                           UserService userService,
                           UserMapper userMapper,
                           UserIdentityMapper userIdentityMapper,
                           JwtTokenProvider jwtTokenProvider,
                           StringRedisTemplate stringRedisTemplate,
                           RoleService roleService,
                           RoleMapper roleMapper,
                           UserRoleMapper userRoleMapper,
                           AppProperties appProperties) {
        this.wechatAuthClient = wechatAuthClient;
        this.oAuthProviderRegistry = oAuthProviderRegistry;
        this.userService = userService;
        this.userMapper = userMapper;
        this.userIdentityMapper = userIdentityMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.stringRedisTemplate = stringRedisTemplate;
        this.roleService = roleService;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.appProperties = appProperties;
    }

    @Override
    public AuthTokenVO wxLogin(String code) {
        WechatCode2SessionResponse response = wechatAuthClient.code2Session(code);
        User user = userService.getOrCreateByIdentity(LoginType.WECHAT, response.getOpenid(), null, null);
        return issueToken(user);
    }

    @Override
    public AuthTokenVO refreshToken(String refreshToken) {
        // 每一步都打日志，定位 401 时直接看 IDE 控制台知道挂在哪
        Claims claims;
        try {
            claims = jwtTokenProvider.parseToken(refreshToken);
        } catch (Exception ex) {
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

        // 会话快照都存 Redis Hash，key 自带 TTL（refreshTokenExpireDays），过期即 entries 为空
        String key = RedisKeyConstants.USER_SESSION + userId;
        Object cachedSid = stringRedisTemplate.opsForHash().get(key, FIELD_SID);
        Object cachedToken = stringRedisTemplate.opsForHash().get(key, FIELD_REFRESH_TOKEN);
        if (cachedSid == null) {
            log.warn("[refresh] Redis 中没有该用户会话（key={}），可能 TTL 已过", key);
            throw new AuthException("登录会话已失效，请重新登录");
        }
        if (!sessionId.equals(cachedSid.toString())) {
            // 最常见原因：在别处又登过一次，sid 被覆盖
            log.warn("[refresh] sessionId 不匹配 token={} redis={}（多设备登录可能顶替了）", sessionId, cachedSid);
            throw new AuthException("登录会话已失效，请重新登录");
        }
        if (cachedToken == null || !refreshToken.equals(cachedToken.toString())) {
            log.warn("[refresh] refreshToken 与 Redis 中记录的不一致");
            throw new AuthException("refreshToken 已失效");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("[refresh] 用户已不存在 userId={}", userId);
            throw new AuthException("用户不存在");
        }
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

        LoginType loginType = parseProvider(provider);
        User user = userService.getOrCreateByIdentity(
                loginType,
                userInfo.getProviderUserId(),
                userInfo.getNickname(),
                userInfo.getAvatarUrl());

        log.info("[OAuth] {} 登录成功, userId={}, nickname={}", provider, user.getId(), user.getNickname());
        return issueToken(user);
    }

    @Override
    public void bindIdentity(Long userId, String provider, String code) {
        OAuthProvider p = oAuthProviderRegistry.getProvider(provider);
        OAuthUserInfo userInfo = p.handleCallback(code);
        LoginType loginType = parseProvider(provider);

        // 校验 1：该第三方账号是否已被本系统绑定
        UserIdentity duplicated = userIdentityMapper.selectOne(new LambdaQueryWrapper<UserIdentity>()
                .eq(UserIdentity::getProvider, loginType)
                .eq(UserIdentity::getProviderUserId, userInfo.getProviderUserId()));
        if (duplicated != null) {
            if (duplicated.getUserId().equals(userId)) {
                log.info("[OAuth-bind] 重复绑定，幂等忽略 userId={} provider={}", userId, provider);
                return;
            }
            throw new BusinessException(ErrorCode.USER_ERROR, "该 " + provider + " 账号已被其他用户绑定");
        }

        // 校验 2：当前用户是否已绑定同一类型平台
        UserIdentity sameProvider = userIdentityMapper.selectOne(new LambdaQueryWrapper<UserIdentity>()
                .eq(UserIdentity::getUserId, userId)
                .eq(UserIdentity::getProvider, loginType));
        if (sameProvider != null) {
            throw new BusinessException(ErrorCode.USER_ERROR, "当前账号已绑定 " + provider + "，请先解绑");
        }

        UserIdentity identity = new UserIdentity();
        identity.setUserId(userId);
        identity.setProvider(loginType);
        identity.setProviderUserId(userInfo.getProviderUserId());
        identity.setNickname(userInfo.getNickname());
        identity.setAvatarUrl(userInfo.getAvatarUrl());
        identity.setBoundAt(LocalDateTime.now());
        userIdentityMapper.insert(identity);

        log.info("[OAuth-bind] 绑定成功 userId={} provider={} providerUserId={}",
                userId, provider, userInfo.getProviderUserId());
    }

    private LoginType parseProvider(String provider) {
        try {
            return LoginType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AuthException("不支持的登录方式: " + provider);
        }
    }

    @Override
    public AuthTokenVO adminLogin(String username, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new AuthException("用户名或密码错误");
        }
        // 检查是否设置了密码
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new AuthException("该账号未设置密码，请先注册");
        }
        // 验证密码
        if (!passwordEncoder().matches(password, user.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }
        // 检查用户状态
        if (user.getStatus() == CommonStatus.DISABLED) {
            throw new AuthException("账号已被禁用");
        }
        return issueToken(user);
    }

    @Override
    public AuthTokenVO adminRegister(String username, String password) {
        // 查重
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (existing != null) {
            throw new BusinessException(ErrorCode.USER_ERROR, "用户名已被占用");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder().encode(password));
        user.setLoginType(LoginType.ADMIN);
        user.setNickname(username);
        user.setStatus(CommonStatus.ENABLED);
        userMapper.insert(user);

        // 赋予 admin 角色
        Role adminRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getName, "admin")
                .last("LIMIT 1"));
        if (adminRole != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(adminRole.getId());
            userRoleMapper.insert(ur);
        }

        return issueToken(user);
    }

    @Override
    public void logout(Long userId) {
        stringRedisTemplate.delete(RedisKeyConstants.USER_SESSION + userId);
    }

    private AuthTokenVO issueToken(User user) {
        String key = RedisKeyConstants.USER_SESSION + user.getId();

        // 复用未过期的旧会话，避免每次登录都 HSET 写 Redis
        String existingSessionId = getValidSessionId(key);
        if (existingSessionId != null) {
            return buildExistingSessionToken(user, existingSessionId, key);
        }

        String sessionId = UUID.randomUUID().toString();
        String openId = user.getOpenid() != null ? user.getOpenid() : "";
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), openId, sessionId);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), openId, sessionId);

        storeSession(key, sessionId, refreshToken);

        return buildTokenResponse(user, accessToken, refreshToken, sessionId);
    }

    /** 检查 Redis 中是否有未过期的有效会话，有则返回 sessionId。 */
    private String getValidSessionId(String key) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) return null;

        Object expireAt = entries.get(FIELD_EXPIRE_AT);
        if (expireAt == null) return null;

        try {
            if (System.currentTimeMillis() >= Long.parseLong(expireAt.toString())) return null;
        } catch (NumberFormatException e) {
            return null;
        }
        return entries.containsKey(FIELD_SID) ? entries.get(FIELD_SID).toString() : null;
    }

    /** 会话未过期，复用 sid 和 refreshToken，仅刷新 accessToken。 */
    private AuthTokenVO buildExistingSessionToken(User user, String sessionId, String key) {
        String openId = user.getOpenid() != null ? user.getOpenid() : "";
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), openId, sessionId);

        Object cachedToken = stringRedisTemplate.opsForHash().get(key, FIELD_REFRESH_TOKEN);
        String refreshToken = cachedToken != null ? cachedToken.toString()
                : jwtTokenProvider.createRefreshToken(user.getId(), openId, sessionId);

        return buildTokenResponse(user, accessToken, refreshToken, sessionId);
    }

    private AuthTokenVO buildTokenResponse(User user, String accessToken, String refreshToken, String sessionId) {
        List<String> roles = roleService.getRoleNamesByUserId(user.getId());
        long expiresInSeconds = appProperties.getSecurity().getJwt().getAccessTokenExpireMinutes() * 60L;
        return AuthTokenVO.builder()
                .userId(user.getId())
                .openid(user.getOpenid())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .sessionId(sessionId)
                .username(user.getUsername())
                .roles(roles)
                .expiresIn(expiresInSeconds)
                .build();
    }

    /**
     * 会话信息写 Redis Hash + 过期时间戳；key 的 TTL = refreshTokenExpireDays，过期即整体清除。
     * 重新登录时同一 key 直接覆盖，天然实现"单设备登录"语义。
     */
    private void storeSession(String key, String sessionId, String refreshToken) {
        long ttlDays = appProperties.getSecurity().getJwt().getRefreshTokenExpireDays();
        long expireAt = System.currentTimeMillis() + ttlDays * 86400L * 1000L;

        stringRedisTemplate.opsForHash().putAll(key, Map.of(
                FIELD_SID, sessionId,
                FIELD_REFRESH_TOKEN, refreshToken,
                FIELD_EXPIRE_AT, String.valueOf(expireAt)
        ));
        stringRedisTemplate.expire(key, ttlDays, TimeUnit.DAYS);
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
