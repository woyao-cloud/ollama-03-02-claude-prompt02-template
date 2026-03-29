package com.usermanagement.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少为 6 位")
    private String password;

    @NotBlank(message = "名不能为空")
    @Size(max = 100, message = "名长度不能超过 100 个字符")
    private String firstName;

    @NotBlank(message = "姓不能为空")
    @Size(max = 100, message = "姓长度不能超过 100 个字符")
    private String lastName;
}
