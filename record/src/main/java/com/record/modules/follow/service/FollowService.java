package com.record.modules.follow.service;

import com.record.modules.follow.model.vo.FollowUserVO;

import java.util.List;

public interface FollowService {

    /**
     * 关注用户。
     */
    void follow(Long userId, Long targetUserId);

    /**
     * 取消关注。
     */
    void unfollow(Long userId, Long targetUserId);

    /**
     * 是否已关注。
     */
    boolean isFollowing(Long userId, Long targetUserId);

    /**
     * 获取关注列表（我关注的人）。
     */
    List<FollowUserVO> getFollowing(Long userId);

    /**
     * 获取粉丝列表（关注我的人）。
     */
    List<FollowUserVO> getFollowers(Long userId);

    /**
     * 获取关注数。
     */
    long countFollowing(Long userId);

    /**
     * 获取粉丝数。
     */
    long countFollowers(Long userId);
}
