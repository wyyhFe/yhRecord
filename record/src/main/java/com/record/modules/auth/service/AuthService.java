package com.record.modules.auth.service;

import com.record.modules.auth.vo.AuthTokenVO;

public interface AuthService {
    AuthTokenVO wxLogin(String code);
    AuthTokenVO refreshToken(String refreshToken);
    void logout(Long userId);
}
