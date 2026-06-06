package com.record.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.LoginType;
import com.record.common.exception.BusinessException;
import com.record.common.exception.ErrorCode;
import com.record.integration.wechat.WechatAuthClient;
import com.record.integration.wechat.WechatCode2SessionResponse;
import com.record.modules.user.mapper.UserIdentityMapper;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.entity.User;
import com.record.modules.user.model.entity.UserIdentity;
import com.record.modules.user.model.vo.UserIdentityVO;
import com.record.modules.user.service.UserIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户第三方账号绑定关系管理实现。
 */
@Service
public class UserIdentityServiceImpl implements UserIdentityService {

    private final UserIdentityMapper userIdentityMapper;
    private final UserMapper userMapper;
    private final WechatAuthClient wechatAuthClient;

    public UserIdentityServiceImpl(UserIdentityMapper userIdentityMapper,
                                   UserMapper userMapper,
                                   WechatAuthClient wechatAuthClient) {
        this.userIdentityMapper = userIdentityMapper;
        this.userMapper = userMapper;
        this.wechatAuthClient = wechatAuthClient;
    }

    @Override
    public List<UserIdentityVO> listByUserId(Long userId) {
        List<UserIdentity> identities = userIdentityMapper.selectList(new LambdaQueryWrapper<UserIdentity>()
                .eq(UserIdentity::getUserId, userId)
                .orderByAsc(UserIdentity::getBoundAt));
        return identities.stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public void unbind(Long userId, LoginType provider) {
        long total = userIdentityMapper.selectCount(new LambdaQueryWrapper<UserIdentity>()
                .eq(UserIdentity::getUserId, userId));
        if (total <= 1) {
            throw new BusinessException(ErrorCode.USER_ERROR, "至少需要保留一种登录方式，无法解绑");
        }

        UserIdentity target = userIdentityMapper.selectOne(new LambdaQueryWrapper<UserIdentity>()
                .eq(UserIdentity::getUserId, userId)
                .eq(UserIdentity::getProvider, provider));
        if (target == null) {
            throw new BusinessException(ErrorCode.USER_ERROR, "未绑定该平台，无需解绑");
        }
        userIdentityMapper.deleteById(target.getId());

        // 微信场景：解绑同时清掉 sys_user.openid，否则后续公众号推送仍指向已解绑的 openid
        if (provider == LoginType.WECHAT) {
            User user = userMapper.selectById(userId);
            if (user != null && user.getOpenid() != null) {
                user.setOpenid(null);
                userMapper.updateById(user);
            }
        }
    }

    @Override
    @Transactional
    public void bindWechatByCode(Long userId, String code) {
        WechatCode2SessionResponse response = wechatAuthClient.code2Session(code);
        String openid = response.getOpenid();

        // 校验 1：该微信号是否已被绑定
        UserIdentity duplicated = userIdentityMapper.selectOne(new LambdaQueryWrapper<UserIdentity>()
                .eq(UserIdentity::getProvider, LoginType.WECHAT)
                .eq(UserIdentity::getProviderUserId, openid));
        if (duplicated != null) {
            if (duplicated.getUserId().equals(userId)) {
                return;
            }
            throw new BusinessException(ErrorCode.USER_ERROR, "该微信号已被其他用户绑定");
        }

        // 校验 2：当前用户是否已绑微信
        UserIdentity existing = userIdentityMapper.selectOne(new LambdaQueryWrapper<UserIdentity>()
                .eq(UserIdentity::getUserId, userId)
                .eq(UserIdentity::getProvider, LoginType.WECHAT));
        if (existing != null) {
            throw new BusinessException(ErrorCode.USER_ERROR, "当前账号已绑定微信，请先解绑");
        }

        UserIdentity identity = new UserIdentity();
        identity.setUserId(userId);
        identity.setProvider(LoginType.WECHAT);
        identity.setProviderUserId(openid);
        identity.setBoundAt(LocalDateTime.now());
        userIdentityMapper.insert(identity);

        // 同步把 openid 写到 sys_user.openid 以维持公众号/订阅消息推送链路
        User user = userMapper.selectById(userId);
        if (user != null && (user.getOpenid() == null || user.getOpenid().isBlank())) {
            user.setOpenid(openid);
            userMapper.updateById(user);
        }
    }

    private UserIdentityVO toVO(UserIdentity identity) {
        return UserIdentityVO.builder()
                .id(identity.getId())
                .provider(identity.getProvider())
                .providerUserId(identity.getProviderUserId())
                .nickname(identity.getNickname())
                .avatarUrl(identity.getAvatarUrl())
                .boundAt(identity.getBoundAt())
                .build();
    }
}
