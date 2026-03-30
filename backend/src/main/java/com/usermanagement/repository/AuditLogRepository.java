package com.usermanagement.repository;

import com.usermanagement.domain.AuditLog;
import com.usermanagement.domain.AuditOperationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 审计日志 Repository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * 按用户 ID 分页查询审计日志
     */
    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);

    /**
     * 按资源类型和资源 ID 查询审计日志
     */
    Page<AuditLog> findByResourceTypeAndResourceId(String resourceType, UUID resourceId, Pageable pageable);

    /**
     * 按操作类型分页查询审计日志
     */
    Page<AuditLog> findByOperationType(AuditOperationType operationType, Pageable pageable);

    /**
     * 按用户 ID 和操作类型分页查询审计日志
     */
    Page<AuditLog> findByUserIdAndOperationType(UUID userId, AuditOperationType operationType, Pageable pageable);

    /**
     * 按时间范围查询审计日志
     */
    Page<AuditLog> findByCreatedAtBetween(Instant startTime, Instant endTime, Pageable pageable);

    /**
     * 按用户 ID 和时间范围查询审计日志
     */
    Page<AuditLog> findByUserIdAndCreatedAtBetween(UUID userId, Instant startTime, Instant endTime, Pageable pageable);

    /**
     * 按资源类型和时间范围查询审计日志
     */
    Page<AuditLog> findByResourceTypeAndCreatedAtBetween(String resourceType, Instant startTime, Instant endTime, Pageable pageable);

    /**
     * 按操作结果查询审计日志
     */
    Page<AuditLog> findByOperationResult(String operationResult, Pageable pageable);

    /**
     * 按用户邮箱分页查询审计日志
     */
    Page<AuditLog> findByUserEmail(String userEmail, Pageable pageable);

    /**
     * 高级查询 - 支持多条件筛选
     */
    @Query("SELECT al FROM AuditLog al " +
           "WHERE (:userId IS NULL OR al.userId = :userId) " +
           "AND (:userEmail IS NULL OR al.userEmail = :userEmail) " +
           "AND (:resourceType IS NULL OR al.resourceType = :resourceType) " +
           "AND (:operationType IS NULL OR al.operationType = :operationType) " +
           "AND (:startTime IS NULL OR al.createdAt >= :startTime) " +
           "AND (:endTime IS NULL OR al.createdAt <= :endTime) " +
           "AND (:operationResult IS NULL OR al.operationResult = :operationResult)")
    Page<AuditLog> findByFilters(
        @Param("userId") UUID userId,
        @Param("userEmail") String userEmail,
        @Param("resourceType") String resourceType,
        @Param("operationType") AuditOperationType operationType,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime,
        @Param("operationResult") String operationResult,
        Pageable pageable
    );

    /**
     * 查询用户的最新登录记录
     */
    Optional<AuditLog> findFirstByUserIdAndOperationTypeOrderByCreatedAtDesc(UUID userId, AuditOperationType operationType);

    /**
     * 按资源 ID 查询所有审计日志
     */
    List<AuditLog> findByResourceId(UUID resourceId);
}
