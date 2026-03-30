package com.usermanagement.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 部门响应 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {

    /**
     * 部门 ID
     */
    private String id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门代码
     */
    private String code;

    /**
     * 父部门 ID
     */
    private String parentId;

    /**
     * 部门负责人 ID
     */
    private String managerId;

    /**
     * 部门层级 (1-5)
     */
    private Integer level;

    /**
     * Materialized Path
     */
    private String path;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 描述
     */
    private String description;

    /**
     * 部门状态
     */
    private String status;

    /**
     * 子部门列表 (用于树形结构)
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Builder.Default
    private List<DepartmentDTO> children = null;
}
