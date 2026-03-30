package com.usermanagement.web.dto;

import com.usermanagement.domain.AuditOperationType;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * 审计日志分页列表响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogListResponse {

    /**
     * 审计日志列表
     */
    private List<AuditLogDTO> content;

    /**
     * 总元素数
     */
    private long totalElements;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 当前页码
     */
    private int number;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 从 Page 构建响应
     */
    public AuditLogListResponse(List<AuditLogDTO> content, long totalElements, int totalPages, int number, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.number = number;
        this.size = size;
    }
}
