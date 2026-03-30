package com.usermanagement.service;

import com.usermanagement.domain.AuditLog;
import com.usermanagement.domain.AuditOperationType;
import com.usermanagement.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

/**
 * AuditLogService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLogService auditLogService;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final String TEST_CLIENT_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "Mozilla/5.0";

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogService(auditLogRepository);
    }

    @Nested
    @DisplayName("记录审计日志测试")
    class LogAuditTests {

        @Test
        @DisplayName("应该记录成功的创建操作")
        void shouldLogSuccessfulCreateOperation() {
            // Given
            UUID resourceId = UUID.randomUUID();
            Map<String, Object> newValue = Map.of("id", resourceId.toString(), "email", TEST_USER_EMAIL);

            // When
            auditLogService.logAudit(
                TEST_USER_ID,
                TEST_USER_EMAIL,
                AuditOperationType.CREATE,
                "USER",
                resourceId,
                "创建用户",
                null,
                newValue,
                TEST_CLIENT_IP,
                TEST_USER_AGENT,
                "SUCCESS",
                null
            );

            // Then
            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            then(auditLogRepository).should().save(captor.capture());
            AuditLog savedLog = captor.getValue();

            assertThat(savedLog.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(savedLog.getUserEmail()).isEqualTo(TEST_USER_EMAIL);
            assertThat(savedLog.getOperationType()).isEqualTo(AuditOperationType.CREATE);
            assertThat(savedLog.getResourceType()).isEqualTo("USER");
            assertThat(savedLog.getResourceId()).isEqualTo(resourceId);
            assertThat(savedLog.getOperationDescription()).isEqualTo("创建用户");
            assertThat(savedLog.getOldValue()).isNull();
            assertThat(savedLog.getNewValue()).isEqualTo(newValue);
            assertThat(savedLog.getClientIp()).isEqualTo(TEST_CLIENT_IP);
            assertThat(savedLog.getUserAgent()).isEqualTo(TEST_USER_AGENT);
            assertThat(savedLog.getOperationResult()).isEqualTo("SUCCESS");
            assertThat(savedLog.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("应该记录成功的更新操作（包含旧值和新值）")
        void shouldLogSuccessfulUpdateOperation() {
            // Given
            UUID resourceId = UUID.randomUUID();
            Map<String, Object> oldValue = Map.of("email", "old@example.com", "firstName", "Old");
            Map<String, Object> newValue = Map.of("email", "new@example.com", "firstName", "New");

            // When
            auditLogService.logAudit(
                TEST_USER_ID,
                TEST_USER_EMAIL,
                AuditOperationType.UPDATE,
                "USER",
                resourceId,
                "更新用户信息",
                oldValue,
                newValue,
                TEST_CLIENT_IP,
                TEST_USER_AGENT,
                "SUCCESS",
                null
            );

            // Then
            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            then(auditLogRepository).should().save(captor.capture());
            AuditLog savedLog = captor.getValue();

            assertThat(savedLog.getOldValue()).isEqualTo(oldValue);
            assertThat(savedLog.getNewValue()).isEqualTo(newValue);
            assertThat(savedLog.getOperationType()).isEqualTo(AuditOperationType.UPDATE);
        }

        @Test
        @DisplayName("应该记录失败的操作")
        void shouldLogFailedOperation() {
            // Given
            UUID resourceId = UUID.randomUUID();
            String errorMessage = "权限不足";

            // When
            auditLogService.logAudit(
                TEST_USER_ID,
                TEST_USER_EMAIL,
                AuditOperationType.DELETE,
                "USER",
                resourceId,
                "删除用户失败",
                null,
                null,
                TEST_CLIENT_IP,
                TEST_USER_AGENT,
                "FAILURE",
                errorMessage
            );

            // Then
            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            then(auditLogRepository).should().save(captor.capture());
            AuditLog savedLog = captor.getValue();

            assertThat(savedLog.getOperationResult()).isEqualTo("FAILURE");
            assertThat(savedLog.getErrorMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("应该记录登录操作")
        void shouldLogLoginOperation() {
            // Given
            // When
            auditLogService.logLoginAudit(
                TEST_USER_ID,
                TEST_USER_EMAIL,
                TEST_CLIENT_IP,
                TEST_USER_AGENT,
                "SUCCESS",
                null
            );

            // Then
            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            then(auditLogRepository).should().save(captor.capture());
            AuditLog savedLog = captor.getValue();

            assertThat(savedLog.getOperationType()).isEqualTo(AuditOperationType.LOGIN);
            assertThat(savedLog.getResourceType()).isEqualTo("AUTH");
            assertThat(savedLog.getUserId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("应该记录登出操作")
        void shouldLogLogoutOperation() {
            // Given
            // When
            auditLogService.logLogoutAudit(
                TEST_USER_ID,
                TEST_USER_EMAIL,
                TEST_CLIENT_IP,
                TEST_USER_AGENT
            );

            // Then
            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            then(auditLogRepository).should().save(captor.capture());
            AuditLog savedLog = captor.getValue();

            assertThat(savedLog.getOperationType()).isEqualTo(AuditOperationType.LOGOUT);
            assertThat(savedLog.getResourceType()).isEqualTo("AUTH");
        }

        @Test
        @DisplayName("应该记录权限变更操作")
        void shouldLogPermissionChangeOperation() {
            // Given
            UUID roleId = UUID.randomUUID();
            Map<String, Object> oldValue = Map.of("permissions", List.of("READ"));
            Map<String, Object> newValue = Map.of("permissions", List.of("READ", "WRITE"));

            // When
            auditLogService.logPermissionChange(
                TEST_USER_ID,
                TEST_USER_EMAIL,
                roleId,
                "ROLE",
                oldValue,
                newValue,
                TEST_CLIENT_IP,
                TEST_USER_AGENT
            );

            // Then
            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            then(auditLogRepository).should().save(captor.capture());
            AuditLog savedLog = captor.getValue();

            assertThat(savedLog.getOperationType()).isEqualTo(AuditOperationType.PERMISSION_CHANGE);
            assertThat(savedLog.getOldValue()).isEqualTo(oldValue);
            assertThat(savedLog.getNewValue()).isEqualTo(newValue);
        }
    }

    @Nested
    @DisplayName("查询审计日志测试")
    class QueryAuditLogTests {

        @Test
        @DisplayName("应该按用户 ID 分页查询审计日志")
        void shouldFindAuditLogsByUserId() {
            // Given
            UUID userId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            AuditLog log1 = createAuditLog(userId, AuditOperationType.CREATE);
            AuditLog log2 = createAuditLog(userId, AuditOperationType.UPDATE);
            Page<AuditLog> logPage = new PageImpl<>(List.of(log1, log2), pageable, 2);

            given(auditLogRepository.findByUserId(userId, pageable)).willReturn(logPage);

            // When
            Page<AuditLog> result = auditLogService.findByUserId(userId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("应该按资源类型和资源 ID 查询审计日志")
        void shouldFindAuditLogsByResourceTypeAndResourceId() {
            // Given
            UUID resourceId = UUID.randomUUID();
            String resourceType = "USER";
            Pageable pageable = PageRequest.of(0, 10);
            AuditLog log = createAuditLog(TEST_USER_ID, AuditOperationType.UPDATE);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            Page<AuditLog> logPage = new PageImpl<>(List.of(log), pageable, 1);

            given(auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId, pageable))
                .willReturn(logPage);

            // When
            Page<AuditLog> result = auditLogService.findByResource(resourceType, resourceId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getResourceId()).isEqualTo(resourceId);
        }

        @Test
        @DisplayName("应该按操作类型查询审计日志")
        void shouldFindAuditLogsByOperationType() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            AuditLog log = createAuditLog(TEST_USER_ID, AuditOperationType.LOGIN);
            Page<AuditLog> logPage = new PageImpl<>(List.of(log), pageable, 1);

            given(auditLogRepository.findByOperationType(AuditOperationType.LOGIN, pageable)).willReturn(logPage);

            // When
            Page<AuditLog> result = auditLogService.findByOperationType(AuditOperationType.LOGIN, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getOperationType()).isEqualTo(AuditOperationType.LOGIN);
        }

        @Test
        @DisplayName("应该按时间范围查询审计日志")
        void shouldFindAuditLogsByTimeRange() {
            // Given
            Instant startTime = Instant.now().minusSeconds(3600);
            Instant endTime = Instant.now();
            Pageable pageable = PageRequest.of(0, 10);
            AuditLog log = createAuditLog(TEST_USER_ID, AuditOperationType.CREATE);
            Page<AuditLog> logPage = new PageImpl<>(List.of(log), pageable, 1);

            given(auditLogRepository.findByCreatedAtBetween(startTime, endTime, pageable)).willReturn(logPage);

            // When
            Page<AuditLog> result = auditLogService.findByTimeRange(startTime, endTime, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("应该按用户邮箱查询审计日志")
        void shouldFindAuditLogsByUserEmail() {
            // Given
            String userEmail = "test@example.com";
            Pageable pageable = PageRequest.of(0, 10);
            AuditLog log = createAuditLog(TEST_USER_ID, AuditOperationType.CREATE);
            log.setUserEmail(userEmail);
            Page<AuditLog> logPage = new PageImpl<>(List.of(log), pageable, 1);

            given(auditLogRepository.findByUserEmail(userEmail, pageable)).willReturn(logPage);

            // When
            Page<AuditLog> result = auditLogService.findByUserEmail(userEmail, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUserEmail()).isEqualTo(userEmail);
        }

        @Test
        @DisplayName("应该按操作结果查询审计日志")
        void shouldFindAuditLogsByOperationResult() {
            // Given
            String operationResult = "FAILURE";
            Pageable pageable = PageRequest.of(0, 10);
            AuditLog log = createAuditLog(TEST_USER_ID, AuditOperationType.LOGIN);
            log.setOperationResult(operationResult);
            Page<AuditLog> logPage = new PageImpl<>(List.of(log), pageable, 1);

            given(auditLogRepository.findByOperationResult(operationResult, pageable)).willReturn(logPage);

            // When
            Page<AuditLog> result = auditLogService.findByOperationResult(operationResult, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getOperationResult()).isEqualTo(operationResult);
        }

        @Test
        @DisplayName("应该使用高级筛选查询审计日志")
        void shouldFindAuditLogsWithFilters() {
            // Given
            Instant startTime = Instant.now().minusSeconds(3600);
            Instant endTime = Instant.now();
            Pageable pageable = PageRequest.of(0, 10);
            AuditLog log = createAuditLog(TEST_USER_ID, AuditOperationType.UPDATE);
            Page<AuditLog> logPage = new PageImpl<>(List.of(log), pageable, 1);

            given(auditLogRepository.findByFilters(
                TEST_USER_ID,
                null,
                "USER",
                AuditOperationType.UPDATE,
                startTime,
                endTime,
                null,
                pageable
            )).willReturn(logPage);

            // When
            Page<AuditLog> result = auditLogService.findByFilters(
                TEST_USER_ID,
                null,
                "USER",
                AuditOperationType.UPDATE,
                startTime,
                endTime,
                null,
                pageable
            );

            // Then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("应该查询用户的最新登录记录")
        void shouldFindLatestLoginRecord() {
            // Given
            AuditLog log = createAuditLog(TEST_USER_ID, AuditOperationType.LOGIN);
            given(auditLogRepository.findFirstByUserIdAndOperationTypeOrderByCreatedAtDesc(
                TEST_USER_ID, AuditOperationType.LOGIN
            )).willReturn(Optional.of(log));

            // When
            Optional<AuditLog> result = auditLogService.findLatestLoginRecord(TEST_USER_ID);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getOperationType()).isEqualTo(AuditOperationType.LOGIN);
        }

        @Test
        @DisplayName("用户无登录记录时返回空")
        void shouldReturnEmptyWhenNoLoginRecord() {
            // Given
            given(auditLogRepository.findFirstByUserIdAndOperationTypeOrderByCreatedAtDesc(
                TEST_USER_ID, AuditOperationType.LOGIN
            )).willReturn(Optional.empty());

            // When
            Optional<AuditLog> result = auditLogService.findLatestLoginRecord(TEST_USER_ID);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("应该按资源 ID 查询所有审计日志")
        void shouldFindAllAuditLogsByResourceId() {
            // Given
            UUID resourceId = UUID.randomUUID();
            AuditLog log1 = createAuditLog(TEST_USER_ID, AuditOperationType.CREATE);
            AuditLog log2 = createAuditLog(TEST_USER_ID, AuditOperationType.UPDATE);
            log1.setResourceId(resourceId);
            log2.setResourceId(resourceId);

            given(auditLogRepository.findByResourceId(resourceId)).willReturn(List.of(log1, log2));

            // When
            List<AuditLog> result = auditLogService.findByResourceId(resourceId);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("导出审计日志测试")
    class ExportAuditLogTests {

        @Test
        @DisplayName("应该导出用户的所有审计日志")
        void shouldExportAllAuditLogsForUser() {
            // Given
            UUID userId = UUID.randomUUID();
            AuditLog log1 = createAuditLog(userId, AuditOperationType.CREATE);
            AuditLog log2 = createAuditLog(userId, AuditOperationType.UPDATE);

            given(auditLogRepository.findByUserId(userId, PageRequest.of(0, 1000)))
                .willReturn(new PageImpl<>(List.of(log1, log2)));

            // When
            List<AuditLog> result = auditLogService.exportByUserId(userId);

            // Then
            assertThat(result).hasSize(2);
            then(auditLogRepository).should().findByUserId(userId, PageRequest.of(0, 1000));
        }

        @Test
        @DisplayName("应该导出指定时间范围的审计日志")
        void shouldExportAuditLogsForTimeRange() {
            // Given
            Instant startTime = Instant.now().minusSeconds(86400);
            Instant endTime = Instant.now();
            AuditLog log1 = createAuditLog(TEST_USER_ID, AuditOperationType.CREATE);
            AuditLog log2 = createAuditLog(TEST_USER_ID, AuditOperationType.UPDATE);

            given(auditLogRepository.findByCreatedAtBetween(startTime, endTime, PageRequest.of(0, 1000)))
                .willReturn(new PageImpl<>(List.of(log1, log2)));

            // When
            List<AuditLog> result = auditLogService.exportByTimeRange(startTime, endTime);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    // 辅助方法
    private AuditLog createAuditLog(UUID userId, AuditOperationType operationType) {
        return AuditLog.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .userEmail(TEST_USER_EMAIL)
            .operationType(operationType)
            .resourceType("USER")
            .operationDescription("Test operation")
            .clientIp(TEST_CLIENT_IP)
            .userAgent(TEST_USER_AGENT)
            .operationResult("SUCCESS")
            .createdAt(Instant.now())
            .build();
    }
}
