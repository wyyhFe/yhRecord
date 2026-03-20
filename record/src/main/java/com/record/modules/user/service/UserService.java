package com.record.modules.user.service;

import com.record.modules.user.dto.UserProfileUpdateRequest;
import com.record.modules.user.entity.User;
import com.record.modules.user.vo.UserProfileVO;

public interface UserService {
    User getOrCreateByOpenid(String openid);
    UserProfileVO getCurrentProfile(Long userId);
    UserProfileVO updateProfile(Long userId, UserProfileUpdateRequest request);
}
