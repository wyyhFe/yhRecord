package com.record.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.record.common.enums.CommonStatus;
import com.record.common.enums.GenderType;
import com.record.common.enums.LoginType;
import com.record.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用户实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "用户实体")
public class User extends BaseEntity {

    @TableId
    @Schema(description = "用户 ID", example = "10001")
    private Long id;

    @Schema(description = "小程序 openid（用于公众号/订阅消息推送，不再作为登录入口）")
    private String openid;

    @Schema(description = "首次注册来源", example = "WECHAT")
    private LoginType loginType;

    @Schema(description = "昵称", example = "wyh")
    private String nickname;

    @Schema(description = "头像路径", example = "avatar/20260321/demo.jpg")
    private String avatarPath;

    @Schema(description = "性别", example = "FEMALE")
    private GenderType gender;

    @Schema(description = "公众号 openid", example = "oa-openid-demo")
    private String officialAccountOpenid;

    @Schema(description = "生日", example = "2004-02-11")
    private LocalDate birthday;

    @Schema(description = "个性签名", example = "把生活认真记录下来")
    private String signature;

    @Schema(description = "状态", example = "ENABLED")
    private CommonStatus status;

    @Schema(description = "若已被合并，指向目标用户 ID；否则为 null")
    private Long mergedIntoUserId;

    @Schema(description = "管理后台登录用户名，仅管理员使用", example = "admin")
    private String username;

    @Schema(description = "密码（BCrypt 哈希），仅管理员使用")
    private String password;
}
