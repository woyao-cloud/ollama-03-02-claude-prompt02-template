package com.usermanagement.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建用户请求 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须在 8-100 个字符之间")
    private String password;

    /**
     * 名
     */
    @NotBlank(message = "名不能为空")
    @Size(max = 100, message = "名长度不能超过 100 个字符")
    private String firstName;

    /**
     * 姓
     */
    @NotBlank(message = "姓不能为空")
    @Size(max = 100, message = "姓长度不能超过 100 个字符")
    private String lastName;

    /**
     * 手机号
     */
    @Size(max = 20, message = "手机号长度不能超过 20 个字符")
    private String phone;

    /**
     * 所属部门 ID
     */
    private String departmentId;
}
