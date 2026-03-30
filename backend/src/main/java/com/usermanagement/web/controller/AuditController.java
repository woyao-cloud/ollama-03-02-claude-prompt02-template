package com.usermanagement.web.controller;

import com.usermanagement.domain.AuditLog;
import com.usermanagement.domain.AuditOperationType;
import com.usermanagement.service.AuditLogService;
import com.usermanagement.web.dto.AuditLogDTO;
import com.usermanagement.web.dto.AuditLogFilter;
import com.usermanagement.web.dto.AuditLogListResponse;
import com.usermanagement.web.mapper.AuditLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 审计日志 Controller
 */
@RestController
@RequestMapping("/api/audit-logs")
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

    private final AuditLogService auditLogService;
    private final AuditLogMapper auditLogMapper;

    public AuditController(AuditLogService auditLogService, AuditLogMapper auditLogMapper) {
        this.auditLogService = auditLogService;
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 获取审计日志列表（支持筛选和分页）
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AuditLogListResponse> getAuditLogs(
        @RequestParam(required = false) String userId,
        @RequestParam(required = false) String userEmail,
        @RequestParam(required = false) String resourceType,
        @RequestParam(required = false) String resourceId,
        @RequestParam(required = false) AuditOperationType operationType,
        @RequestParam(required = false) Instant startTime,
        @RequestParam(required = false) Instant endTime,
        @RequestParam(required = false) String operationResult,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String direction
    ) {
        logger.debug("查询审计日志：userId={}, userEmail={}, resourceType={}, operationType={}",
            userId, userEmail, resourceType, operationType);

        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        UUID parsedUserId = userId != null ? UUID.fromString(userId) : null;
        UUID parsedResourceId = resourceId != null ? UUID.fromString(resourceId) : null;

        Page<AuditLog> auditLogPage = auditLogService.findByFilters(
            parsedUserId,
            userEmail,
            resourceType,
            operationType,
            startTime,
            endTime,
            operationResult,
            pageable
        );

        List<AuditLogDTO> content = auditLogPage.getContent().stream()
            .map(auditLogMapper::toDto)
            .collect(Collectors.toList());

        AuditLogListResponse response = new AuditLogListResponse(
            content,
            auditLogPage.getTotalElements(),
            auditLogPage.getTotalPages(),
            auditLogPage.getNumber(),
            auditLogPage.getSize()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 获取审计日志详情（按资源 ID）
     */
    @GetMapping("/{resourceId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogByResource(@PathVariable UUID resourceId) {
        logger.debug("查询资源审计日志：resourceId={}", resourceId);

        List<AuditLog> auditLogs = auditLogService.findByResourceId(resourceId);

        if (auditLogs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<AuditLogDTO> content = auditLogs.stream()
            .map(auditLogMapper::toDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(content);
    }

    /**
     * 获取资源的审计历史
     */
    @GetMapping("/resources/{resourceType}/{resourceId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AuditLogListResponse> getResourceAuditHistory(
        @PathVariable String resourceType,
        @PathVariable UUID resourceId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String direction
    ) {
        logger.debug("查询资源审计历史：resourceType={}, resourceId={}", resourceType, resourceId);

        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditLog> auditLogPage = auditLogService.findByResource(resourceType, resourceId, pageable);

        List<AuditLogDTO> content = auditLogPage.getContent().stream()
            .map(auditLogMapper::toDto)
            .collect(Collectors.toList());

        AuditLogListResponse response = new AuditLogListResponse(
            content,
            auditLogPage.getTotalElements(),
            auditLogPage.getTotalPages(),
            auditLogPage.getNumber(),
            auditLogPage.getSize()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户的审计日志
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<AuditLogListResponse> getUserAuditLogs(
        @PathVariable UUID userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String direction
    ) {
        logger.debug("查询用户审计日志：userId={}", userId);

        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditLog> auditLogPage = auditLogService.findByUserId(userId, pageable);

        List<AuditLogDTO> content = auditLogPage.getContent().stream()
            .map(auditLogMapper::toDto)
            .collect(Collectors.toList());

        AuditLogListResponse response = new AuditLogListResponse(
            content,
            auditLogPage.getTotalElements(),
            auditLogPage.getTotalPages(),
            auditLogPage.getNumber(),
            auditLogPage.getSize()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 导出用户的审计日志
     */
    @GetMapping("/export/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AuditLogDTO>> exportUserAuditLogs(@PathVariable UUID userId) {
        logger.debug("导出用户审计日志：userId={}", userId);

        List<AuditLog> auditLogs = auditLogService.exportByUserId(userId);

        List<AuditLogDTO> content = auditLogs.stream()
            .map(auditLogMapper::toDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(content);
    }

    /**
     * 导出时间范围内的审计日志
     */
    @PostMapping("/export/time-range")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AuditLogDTO>> exportAuditLogsByTimeRange(
        @RequestBody AuditLogFilter filter
    ) {
        logger.debug("导出时间范围审计日志：startTime={}, endTime={}",
            filter.getStartTime(), filter.getEndTime());

        List<AuditLog> auditLogs = auditLogService.exportByTimeRange(
            filter.getStartTime(),
            filter.getEndTime()
        );

        List<AuditLogDTO> content = auditLogs.stream()
            .map(auditLogMapper::toDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(content);
    }

    /**
     * 获取用户的最新登录记录
     */
    @GetMapping("/users/{userId}/latest-login")
    @PreAuthorize("hasAuthority('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<AuditLogDTO> getLatestLoginRecord(@PathVariable UUID userId) {
        logger.debug("查询用户最新登录记录：userId={}", userId);

        return auditLogService.findLatestLoginRecord(userId)
            .map(auditLogMapper::toDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
