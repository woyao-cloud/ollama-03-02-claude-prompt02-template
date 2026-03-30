package com.usermanagement.web.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门移动请求 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentMoveRequest {

    /**
     * 新父部门 ID (可选，为空表示移动到根节点)
     */
    private String parentId;

    /**
     * 排序号 (可选)
     */
    @Min(value = 0, message = "排序号不能小于 0")
    private Integer sortOrder;
}
