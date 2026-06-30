package com.record.modules.follow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.exception.FollowException;
import com.record.modules.follow.mapper.UserFollowMapper;
import com.record.modules.follow.model.entity.UserFollow;
import com.record.modules.follow.model.vo.FollowUserVO;
import com.record.modules.follow.service.FollowService;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;

    public FollowServiceImpl(UserFollowMapper userFollowMapper, UserMapper userMapper) {
        this.userFollowMapper = userFollowMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public void follow(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new FollowException("不能关注自己");
        }
        User target = userMapper.selectById(targetUserId);
        if (target == null) {
            throw new FollowException("用户不存在");
        }
        Long count = userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFollowingId, targetUserId));
        if (count > 0) {
            throw new FollowException("已关注该用户");
        }
        UserFollow follow = new UserFollow();
        follow.setFollowerId(userId);
        follow.setFollowingId(targetUserId);
        follow.setCreatedAt(LocalDateTime.now());
        userFollowMapper.insert(follow);
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long targetUserId) {
        userFollowMapper.delete(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFollowingId, targetUserId));
    }

    @Override
    public boolean isFollowing(Long userId, Long targetUserId) {
        if (userId == null) return false;
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFollowingId, targetUserId)) > 0;
    }

    @Override
    public List<FollowUserVO> getFollowing(Long userId) {
        List<UserFollow> follows = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .orderByDesc(UserFollow::getCreatedAt));
        return toVO(follows, UserFollow::getFollowingId);
    }

    @Override
    public List<FollowUserVO> getFollowers(Long userId) {
        List<UserFollow> follows = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowingId, userId)
                .orderByDesc(UserFollow::getCreatedAt));
        return toVO(follows, UserFollow::getFollowerId);
    }

    @Override
    public long countFollowing(Long userId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId));
    }

    @Override
    public long countFollowers(Long userId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowingId, userId));
    }

    private List<FollowUserVO> toVO(List<UserFollow> follows, java.util.function.Function<UserFollow, Long> idExtractor) {
        List<Long> userIds = follows.stream().map(idExtractor).toList();
        if (userIds.isEmpty()) return List.of();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        return follows.stream().map(f -> {
            Long uid = idExtractor.apply(f);
            User u = userMap.get(uid);
            return FollowUserVO.builder()
                    .userId(uid)
                    .nickname(u != null ? u.getNickname() : "")
                    .avatarPath(u != null ? u.getAvatarPath() : null)
                    .followedAt(f.getCreatedAt())
                    .build();
        }).toList();
    }
}
