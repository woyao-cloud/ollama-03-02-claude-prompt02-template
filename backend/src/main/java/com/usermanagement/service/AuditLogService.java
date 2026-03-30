package com.usermanagement.service;

import com.usermanagement.domain.AuditLog;
import com.usermanagement.domain.AuditOperationType;
import com.usermanagement.infrastructure.audit.AsyncAuditLogWriter;
import com.usermanagement.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 审计日志服务
 *
 * 性能优化:
 * - 支持同步和异步两种写入模式
 * - 异步模式使用队列 + 批量写入
 * - 关键操作 (如登录) 使用同步确保可靠性
 */
@Service
@Transactional(readOnly = true)
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;
    private final AsyncAuditLogWriter asyncAuditLogWriter;

    /**
     * 是否启用异步写入
     */
    @Value("${audit.async.enabled:true}")
    private boolean asyncEnabled;

    public AuditLogService(
        AuditLogRepository auditLogRepository,
        AsyncAuditLogWriter asyncAuditLogWriter
    ) {
        this.auditLogRepository = auditLogRepository;
        this.asyncAuditLogWriter = asyncAuditLogWriter;
    }

    /**
     * 记录审计日志
     *
     * 性能优化：支持异步写入模式
     */
    public void logAudit(
        UUID userId,
        String userEmail,
        AuditOperationType operationType,
        String resourceType,
        UUID resourceId,
        String operationDescription,
        Map<String, Object> oldValue,
        Map<String, Object> newValue,
        String clientIp,
        String userAgent,
        String operationResult,
        String errorMessage
    ) {
        AuditLog auditLog = AuditLog.builder()
            .userId(userId)
            .userEmail(userEmail)
            .operationType(operationType)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .operationDescription(operationDescription)
            .oldValue(oldValue)
            .newValue(newValue)
            .clientIp(clientIp)
            .userAgent(userAgent)
            .operationResult(operationResult)
            .errorMessage(errorMessage)
            .build();

        if (asyncEnabled) {
            // 异步写入模式
            boolean submitted = asyncAuditLogWriter.submit(auditLog);
            if (submitted) {
                logger.debug("审计日志已异步提交：{} - {} - {}", userEmail, operationType, resourceType);
            } else {
                // 异步提交失败，降级为同步写入
                logger.warn("异步提交审计日志失败，降级为同步写入");
                auditLogRepository.save(auditLog);
            }
        } else {
            // 同步写入模式
            auditLogRepository.save(auditLog);
            logger.info("审计日志记录：{} - {} - {} - {}", userEmail, operationType, resourceType, operationResult);
        }
    }

    /**
     * 记录登录审计日志
     */
    @Transactional
    public void logLoginAudit(
        UUID userId,
        String userEmail,
        String clientIp,
        String userAgent,
        String operationResult,
        String errorMessage
    ) {
        logAudit(
            userId,
            userEmail,
            AuditOperationType.LOGIN,
            "AUTH",
            userId,
            "用户登录",
            null,
            null,
            clientIp,
            userAgent,
            operationResult,
            errorMessage
        );
    }

    /**
     * 记录登出审计日志
     */
    @Transactional
    public void logLogoutAudit(
        UUID userId,
        String userEmail,
        String clientIp,
        String userAgent
    ) {
        logAudit(
            userId,
            userEmail,
            AuditOperationType.LOGOUT,
            "AUTH",
            userId,
            "用户登出",
            null,
            null,
            clientIp,
            userAgent,
            "SUCCESS",
            null
        );
    }

    /**
     * 记录权限变更审计日志
     */
    @Transactional
    public void logPermissionChange(
        UUID userId,
        String userEmail,
        UUID resourceId,
        String resourceType,
        Map<String, Object> oldValue,
        Map<String, Object> newValue,
        String clientIp,
        String userAgent
    ) {
        logAudit(
            userId,
            userEmail,
            AuditOperationType.PERMISSION_CHANGE,
            resourceType,
            resourceId,
            "权限变更",
            oldValue,
            newValue,
            clientIp,
            userAgent,
            "SUCCESS",
            null
        );
    }

    /**
     * 按用户 ID 分页查询审计日志
     */
    public Page<AuditLog> findByUserId(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    /**
     * 按资源查询审计日志
     */
    public Page<AuditLog> findByResource(String resourceType, UUID resourceId, Pageable pageable) {
        return auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId, pageable);
    }

    /**
     * 按操作类型查询审计日志
     */
    public Page<AuditLog> findByOperationType(AuditOperationType operationType, Pageable pageable) {
        return auditLogRepository.findByOperationType(operationType, pageable);
    }

    /**
     * 按时间范围查询审计日志
     */
    public Page<AuditLog> findByTimeRange(Instant startTime, Instant endTime, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetween(startTime, endTime, pageable);
    }

    /**
     * 按用户邮箱查询审计日志
     */
    public Page<AuditLog> findByUserEmail(String userEmail, Pageable pageable) {
        return auditLogRepository.findByUserEmail(userEmail, pageable);
    }

    /**
     * 按操作结果查询审计日志
     */
    public Page<AuditLog> findByOperationResult(String operationResult, Pageable pageable) {
        return auditLogRepository.findByOperationResult(operationResult, pageable);
    }

    /**
     * 高级筛选查询审计日志
     */
    public Page<AuditLog> findByFilters(
        UUID userId,
        String userEmail,
        String resourceType,
        AuditOperationType operationType,
        Instant startTime,
        Instant endTime,
        String operationResult,
        Pageable pageable
    ) {
        return auditLogRepository.findByFilters(
            userId, userEmail, resourceType, operationType, startTime, endTime, operationResult, pageable
        );
    }

    /**
     * 查询用户的最新登录记录
     */
    public Optional<AuditLog> findLatestLoginRecord(UUID userId) {
        return auditLogRepository.findFirstByUserIdAndOperationTypeOrderByCreatedAtDesc(userId, AuditOperationType.LOGIN);
    }

    /**
     * 按资源 ID 查询所有审计日志
     */
    public List<AuditLog> findByResourceId(UUID resourceId) {
        return auditLogRepository.findByResourceId(resourceId);
    }

    /**
     * 导出用户的所有审计日志（用于导出功能）
     */
    public List<AuditLog> exportByUserId(UUID userId) {
        return auditLogRepository.findByUserId(userId, PageRequest.of(0, 1000)).getContent();
    }

    /**
     * 导出指定时间范围的审计日志
     */
    public List<AuditLog> exportByTimeRange(Instant startTime, Instant endTime) {
        return auditLogRepository.findByCreatedAtBetween(startTime, endTime, PageRequest.of(0, 1000)).getContent();
    }
}
