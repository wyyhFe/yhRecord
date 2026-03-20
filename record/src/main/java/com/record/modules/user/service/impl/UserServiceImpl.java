package com.record.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.enums.CommonStatus;
import com.record.modules.diary.service.DiaryService;
import com.record.modules.user.dto.UserProfileUpdateRequest;
import com.record.modules.user.entity.User;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.service.UserService;
import com.record.modules.user.vo.UserProfileVO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final DiaryService diaryService;

    public UserServiceImpl(UserMapper userMapper, @Lazy DiaryService diaryService) {
        this.userMapper = userMapper;
        this.diaryService = diaryService;
    }

    @Override
    public User getOrCreateByOpenid(String openid) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        if (user != null) {
            return user;
        }
        user = new User();
        user.setOpenid(openid);
        user.setNickname("微信用户");
        user.setStatus(CommonStatus.ENABLED);
        userMapper.insert(user);
        return user;
    }

    @Override
    public UserProfileVO getCurrentProfile(Long userId) {
        return toVO(userMapper.selectById(userId));
    }

    @Override
    public UserProfileVO updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        user.setNickname(request.getNickname());
        user.setAvatarPath(request.getAvatarPath());
        user.setGender(request.getGender());
        user.setOfficialAccountOpenid(request.getOfficialAccountOpenid());
        user.setBirthday(request.getBirthday());
        user.setSignature(request.getSignature());
        userMapper.updateById(user);
        return toVO(userMapper.selectById(userId));
    }

    private UserProfileVO toVO(User user) {
        return UserProfileVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .nickname(user.getNickname())
                .avatarPath(user.getAvatarPath())
                .gender(user.getGender())
                .officialAccountOpenid(user.getOfficialAccountOpenid())
                .birthday(user.getBirthday())
                .signature(user.getSignature())
                .diaryCount(diaryService.countByUser(user.getId()))
                .build();
    }
}
