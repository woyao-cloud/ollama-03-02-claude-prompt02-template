package com.usermanagement.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.usermanagement.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 用户响应 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /**
     * 用户 ID
     */
    private String id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 名
     */
    private String firstName;

    /**
     * 姓
     */
    private String lastName;

    /**
     * 全名（计算字段）
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String fullName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像 URL
     */
    private String avatarUrl;

    /**
     * 所属部门 ID
     */
    private String departmentId;

    /**
     * 用户状态
     */
    private UserStatus status;

    /**
     * 邮箱是否已验证
     */
    private Boolean emailVerified;

    /**
     * 最后登录时间
     */
    private Instant lastLoginAt;

    /**
     * 最后登录 IP
     */
    private String lastLoginIp;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 更新时间
     */
    private Instant updatedAt;
}
