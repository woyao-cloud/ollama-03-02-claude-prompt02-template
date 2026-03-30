package com.usermanagement.web.dto;

import com.usermanagement.domain.RoleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新角色状态请求 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleStatusUpdateRequest {

    /**
     * 角色状态
     */
    @NotNull(message = "角色状态不能为空")
    private RoleStatus status;
}
