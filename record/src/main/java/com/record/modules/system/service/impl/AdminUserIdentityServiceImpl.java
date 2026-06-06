package com.record.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.LoginType;
import com.record.common.exception.BusinessException;
import com.record.common.exception.ErrorCode;
import com.record.modules.system.service.AdminUserIdentityService;
import com.record.modules.user.mapper.UserIdentityMapper;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.entity.User;
import com.record.modules.user.model.entity.UserIdentity;
import com.record.modules.user.model.vo.UserIdentityVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 后台第三方账号绑定管理实现。
 */
@Service
public class AdminUserIdentityServiceImpl implements AdminUserIdentityService {

    private final UserIdentityMapper userIdentityMapper;
    private final UserMapper userMapper;

    public AdminUserIdentityServiceImpl(UserIdentityMapper userIdentityMapper, UserMapper userMapper) {
        this.userIdentityMapper = userIdentityMapper;
        this.userMapper = userMapper;
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
    public void forceUnbind(Long userId, Long identityId) {
        UserIdentity identity = userIdentityMapper.selectById(identityId);
        if (identity == null || !identity.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_ERROR, "绑定记录不存在或与用户不匹配");
        }
        userIdentityMapper.deleteById(identityId);

        if (identity.getProvider() == LoginType.WECHAT) {
            User user = userMapper.selectById(userId);
            if (user != null && user.getOpenid() != null) {
                user.setOpenid(null);
                userMapper.updateById(user);
            }
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
