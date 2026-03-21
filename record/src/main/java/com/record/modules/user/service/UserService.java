package com.record.modules.user.service;

import com.record.modules.user.model.dto.UserProfileUpdateRequest;
import com.record.modules.user.model.entity.User;
import com.record.modules.user.model.vo.UserProfileVO;

public interface UserService {
    User getOrCreateByOpenid(String openid);
    UserProfileVO getCurrentProfile(Long userId);
    UserProfileVO updateProfile(Long userId, UserProfileUpdateRequest request);
}

