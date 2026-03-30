package com.usermanagement.web.dto;

import com.usermanagement.domain.AuditOperationType;
import lombok.*;

import java.time.Instant;

/**
 * 审计日志筛选条件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogFilter {

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源 ID
     */
    private String resourceId;

    /**
     * 操作类型
     */
    private AuditOperationType operationType;

    /**
     * 开始时间
     */
    private Instant startTime;

    /**
     * 结束时间
     */
    private Instant endTime;

    /**
     * 操作结果
     */
    private String operationResult;

    /**
     * 页码（从 0 开始）
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 20;
}
