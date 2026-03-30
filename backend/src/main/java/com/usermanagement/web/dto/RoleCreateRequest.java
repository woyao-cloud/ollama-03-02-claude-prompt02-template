package com.usermanagement.web.dto;

import com.usermanagement.domain.DataScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建角色请求 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateRequest {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过 50 个字符")
    private String name;

    /**
     * 角色代码
     */
    @NotBlank(message = "角色代码不能为空")
    @Size(max = 50, message = "角色代码长度不能超过 50 个字符")
    private String code;

    /**
     * 描述
     */
    @Size(max = 500, message = "描述长度不能超过 500 个字符")
    private String description;

    /**
     * 数据权限范围
     */
    private DataScope dataScope;
}
