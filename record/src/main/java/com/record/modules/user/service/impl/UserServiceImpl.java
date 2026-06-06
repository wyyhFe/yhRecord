package com.record.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.CommonStatus;
import com.record.common.enums.GenderType;
import com.record.common.enums.LoginType;
import com.record.modules.diary.service.DiaryService;
import com.record.modules.user.mapper.UserIdentityMapper;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.dto.UserProfileUpdateRequest;
import com.record.modules.user.model.entity.User;
import com.record.modules.user.model.entity.UserIdentity;
import com.record.modules.user.model.vo.UserProfileVO;
import com.record.modules.user.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户资料服务实现。
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserIdentityMapper userIdentityMapper;
    private final DiaryService diaryService;

    public UserServiceImpl(UserMapper userMapper,
                           UserIdentityMapper userIdentityMapper,
                           @Lazy DiaryService diaryService) {
        this.userMapper = userMapper;
        this.userIdentityMapper = userIdentityMapper;
        this.diaryService = diaryService;
    }

    @Override
    @Transactional
    public User getOrCreateByIdentity(LoginType provider, String providerUserId, String nickname, String avatarUrl) {
        // 1. 已绑定 → 直接拿对应用户
        UserIdentity existing = userIdentityMapper.selectOne(new LambdaQueryWrapper<UserIdentity>()
                .eq(UserIdentity::getProvider, provider)
                .eq(UserIdentity::getProviderUserId, providerUserId));
        if (existing != null) {
            return userMapper.selectById(existing.getUserId());
        }

        // 2. 未绑定 → 新建用户 + 写一条绑定关系
        User user = new User();
        user.setNickname(nickname != null ? nickname : defaultNickname(provider));
        user.setAvatarPath(avatarUrl);
        user.setGender(GenderType.UNKNOWN);
        user.setLoginType(provider);
        user.setStatus(CommonStatus.ENABLED);
        // 微信场景：openid 同步写入 sys_user.openid，保留公众号/订阅消息推送的旧链路
        if (provider == LoginType.WECHAT) {
            user.setOpenid(providerUserId);
        }
        userMapper.insert(user);

        UserIdentity identity = new UserIdentity();
        identity.setUserId(user.getId());
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        identity.setNickname(nickname);
        identity.setAvatarUrl(avatarUrl);
        identity.setBoundAt(LocalDateTime.now());
        userIdentityMapper.insert(identity);

        return user;
    }

    @Override
    public UserProfileVO getCurrentProfile(Long userId) {
        return toVO(userMapper.selectById(userId));
    }

    @Override
    public UserProfileVO updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        user.setNickname(request.getNickname());
        user.setAvatarPath(request.getAvatarPath());
        user.setGender(request.getGender());
        user.setOfficialAccountOpenid(request.getOfficialAccountOpenid());
        user.setBirthday(request.getBirthday());
        user.setSignature(request.getSignature());
        userMapper.updateById(user);
        return toVO(userMapper.selectById(userId));
    }

    private String defaultNickname(LoginType provider) {
        return switch (provider) {
            case WECHAT -> "微信用户";
            case GITHUB -> "GitHub 用户";
            case GOOGLE -> "Google 用户";
        };
    }

    private UserProfileVO toVO(User user) {
        return UserProfileVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .nickname(user.getNickname())
                .avatarPath(user.getAvatarPath())
                .gender(user.getGender())
                .officialAccountOpenid(user.getOfficialAccountOpenid())
                .birthday(user.getBirthday())
                .signature(user.getSignature())
                .diaryCount(diaryService.countByUser(user.getId()))
                .build();
    }
}
