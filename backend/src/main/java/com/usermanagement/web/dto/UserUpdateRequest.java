package com.usermanagement.web.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户请求 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    /**
     * 名
     */
    @Size(max = 100, message = "名长度不能超过 100 个字符")
    private String firstName;

    /**
     * 姓
     */
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
