package com.record.modules.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新用户资料请求体。
 */
@Data
@Schema(description = "更新用户资料请求体")
public class UserProfileUpdateRequest {
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
}
