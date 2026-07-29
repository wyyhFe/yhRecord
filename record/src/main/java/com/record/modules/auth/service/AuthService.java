package com.record.modules.auth.service;

import com.record.modules.auth.model.vo.AuthTokenVO;
import com.record.modules.auth.model.vo.QrTokenVO;

public interface AuthService {
    AuthTokenVO wxLogin(String code);
    AuthTokenVO refreshToken(String refreshToken);
    void logout(Long userId);

    /**
     * 构建指定 OAuth 提供商的授权 URL。
     */
    String buildOAuthAuthorizeUrl(String provider, String state);

    /**
     * 处理 OAuth 回调：用 code 换取用户信息并签发令牌。
     */
    AuthTokenVO oauthLogin(String provider, String code);

    /**
     * 已登录态下，把当前 OAuth 回调来的第三方账号绑定到指定用户。
     * 不签发新 token，只写 sys_user_identity。
     *
     * @param userId   当前登录用户 ID（从 state 里反查而来）
     * @param provider 第三方平台名（github / google 等）
     * @param code     回调带回的授权码
     */
    void bindIdentity(Long userId, String provider, String code);

    /**
     * 管理后台登录（用户名 + 密码）。
     */
    AuthTokenVO adminLogin(String username, String password);

    /**
     * 管理后台注册（创建本地账号，自动赋予 admin 角色）。
     */
    AuthTokenVO adminRegister(String username, String password);

    /**
     * 绑定手机号（微信小程序端，需已登录）。
     * 自动使用手机号后 6 位作为初始管理后台登录密码。
     *
     * @param userId 当前登录用户 ID
     * @param code   微信 phone 授权 code
     */
    void bindPhone(Long userId, String code);

    /**
     * 管理后台手机号 + 密码登录。
     * 返回的 AuthTokenVO.requirePasswordChange 标记是否为初始密码。
     */
    AuthTokenVO phoneLogin(String phone, String password);

    /**
     * 修改密码（需已登录）。
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 生成二维码扫码登录的临时 token（管理后台调用，无登录态）。
     * token 有效期 120 秒，存入 Redis。
     */
    QrTokenVO generateQrToken();

    /**
     * 小程序端扫码确认登录（需已通过微信登录的小程序用户）。
     * 为小程序关联的用户签发一套管理后台用的 JWT token。
     */
    AuthTokenVO confirmQrLogin(Long userId, String qrToken);

    /**
     * 管理后台轮询二维码状态。
     * PENDING → 返回 null；CONFIRMED → 返回 token 并删除 Redis key。
     */
    AuthTokenVO getQrStatus(String qrToken);
}

