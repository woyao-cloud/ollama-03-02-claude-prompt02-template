package com.usermanagement.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 部门树响应 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentTreeResponse {

    /**
     * 部门树列表
     */
    private List<DepartmentDTO> tree;

    /**
     * 部门总数
     */
    private Long total;
}
