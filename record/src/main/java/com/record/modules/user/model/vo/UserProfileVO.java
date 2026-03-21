package com.record.modules.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户资料返回对象。
 */
@Data
@Builder
@Schema(description = "用户资料返回对象")
public class UserProfileVO {

    @Schema(description = "用户 ID", example = "10001")
    private Long id;

    @Schema(description = "小程序 openid")
    private String openid;

    @Schema(description = "昵称", example = "wyh")
    private String nickname;

    @Schema(description = "头像路径", example = "avatar/20260321/demo.jpg")
    private String avatarPath;

    @Schema(description = "性别", example = "FEMALE")
    private String gender;

    @Schema(description = "公众号 openid", example = "oa-openid-demo")
    private String officialAccountOpenid;

    @Schema(description = "生日", example = "2004-02-11")
    private LocalDate birthday;

    @Schema(description = "个性签名", example = "把生活认真记下来")
    private String signature;

    @Schema(description = "日记数量", example = "23")
    private long diaryCount;
}
