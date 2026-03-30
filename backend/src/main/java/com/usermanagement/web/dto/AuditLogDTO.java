package com.usermanagement.web.dto;

import com.usermanagement.domain.AuditOperationType;
import lombok.*;

import java.time.Instant;
import java.util.Map;

/**
 * 审计日志响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {

    /**
     * 审计日志 ID
     */
    private String id;

    /**
     * 操作用户 ID
     */
    private String userId;

    /**
     * 操作用户邮箱
     */
    private String userEmail;

    /**
     * 操作类型
     */
    private AuditOperationType operationType;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源 ID
     */
    private String resourceId;

    /**
     * 操作描述
     */
    private String operationDescription;

    /**
     * 操作前的数据
     */
    private Map<String, Object> oldValue;

    /**
     * 操作后的数据
     */
    private Map<String, Object> newValue;

    /**
     * 客户端 IP
     */
    private String clientIp;

    /**
     * 客户端 User-Agent
     */
    private String userAgent;

    /**
     * 操作结果
     */
    private String operationResult;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private Instant createdAt;
}
