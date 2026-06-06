package com.record.modules.user.service;

import com.record.common.enums.LoginType;
import com.record.modules.user.model.vo.UserIdentityVO;

import java.util.List;

/**
 * 用户第三方账号绑定关系管理。
 */
public interface UserIdentityService {

    /** 列出指定用户已绑定的全部第三方账号。 */
    List<UserIdentityVO> listByUserId(Long userId);

    /**
     * 解绑指定平台。
     * 强制要求用户保留至少一条绑定关系，避免账号无登录入口而成为孤儿。
     */
    void unbind(Long userId, LoginType provider);

    /**
     * 微信小程序专用绑定：在已登录态下，把当前小程序登录 code 对应的 openid 绑到当前用户。
     * 微信没有浏览器跳转 OAuth 流程，所以走单独的接口。
     */
    void bindWechatByCode(Long userId, String code);
}
