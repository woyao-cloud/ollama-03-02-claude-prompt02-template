package com.usermanagement.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 配置列表分页响应 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigListResponse {

    /**
     * 配置列表
     */
    private List<SystemConfigDTO> content;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 总元素数
     */
    private long totalElements;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 是否为第一页
     */
    private boolean first;

    /**
     * 是否为最后一页
     */
    private boolean last;
}
