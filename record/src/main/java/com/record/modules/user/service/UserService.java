package com.record.modules.user.service;

import com.record.common.enums.LoginType;
import com.record.modules.user.model.dto.UserProfileUpdateRequest;
import com.record.modules.user.model.entity.User;
import com.record.modules.user.model.vo.PublicUserVO;
import com.record.modules.user.model.vo.UserProfileVO;

public interface UserService {

    /**
     * 按第三方账号查找用户；找不到则新建用户 + 写一条绑定关系。
     * 微信场景额外把 openid 同步到 sys_user.openid，保留公众号/订阅消息推送链路。
     *
     * @param provider         第三方平台
     * @param providerUserId   第三方平台用户唯一 ID
     * @param nickname         第三方平台昵称（可空，按平台兜底）
     * @param avatarUrl        第三方平台头像（可空）
     * @return 关联到的本系统用户
     */
    User getOrCreateByIdentity(LoginType provider, String providerUserId, String nickname, String avatarUrl);

    UserProfileVO getCurrentProfile(Long userId);

    UserProfileVO updateProfile(Long userId, UserProfileUpdateRequest request);

    /**
     * 获取指定用户的公开资料（供他人主页展示，不含敏感信息）。
     */
    PublicUserVO getPublicProfile(Long userId);
}
