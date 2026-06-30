package com.record.modules.follow.controller;

import com.record.common.context.UserContext;
import com.record.common.model.ApiResponse;
import com.record.modules.follow.model.vo.FollowUserVO;
import com.record.modules.follow.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "关注")
@RestController
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @Operation(summary = "关注用户")
    @PostMapping("/{targetUserId}")
    public ApiResponse<Void> follow(@PathVariable Long targetUserId) {
        followService.follow(UserContext.getUserId(), targetUserId);
        return ApiResponse.success();
    }

    @Operation(summary = "取消关注")
    @DeleteMapping("/{targetUserId}")
    public ApiResponse<Void> unfollow(@PathVariable Long targetUserId) {
        followService.unfollow(UserContext.getUserId(), targetUserId);
        return ApiResponse.success();
    }

    @Operation(summary = "查询是否已关注")
    @GetMapping("/status/{targetUserId}")
    public ApiResponse<Map<String, Boolean>> status(@PathVariable Long targetUserId) {
        boolean following = followService.isFollowing(UserContext.getUserId(), targetUserId);
        return ApiResponse.success(Map.of("following", following));
    }

    @Operation(summary = "获取关注列表（我关注的人）")
    @GetMapping("/following")
    public ApiResponse<List<FollowUserVO>> following() {
        return ApiResponse.success(followService.getFollowing(UserContext.getUserId()));
    }

    @Operation(summary = "获取粉丝列表（关注我的人）")
    @GetMapping("/followers")
    public ApiResponse<List<FollowUserVO>> followers() {
        return ApiResponse.success(followService.getFollowers(UserContext.getUserId()));
    }

    @Operation(summary = "获取用户的关注/粉丝数")
    @GetMapping("/counts/{userId}")
    public ApiResponse<Map<String, Long>> counts(@PathVariable Long userId) {
        long following = followService.countFollowing(userId);
        long followers = followService.countFollowers(userId);
        return ApiResponse.success(Map.of("following", following, "followers", followers));
    }
}
