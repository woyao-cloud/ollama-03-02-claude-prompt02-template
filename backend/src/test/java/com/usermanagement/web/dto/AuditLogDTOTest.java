package com.usermanagement.web.dto;

import com.usermanagement.domain.AuditOperationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuditLog DTO 测试
 */
class AuditLogDTOTest {

    private static final UUID TEST_ID = UUID.randomUUID();
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final Instant TEST_CREATED_AT = Instant.now();

    @Nested
    @DisplayName("AuditLogDTO 测试")
    class AuditLogDTOTests {

        @Test
        @DisplayName("应该创建 AuditLogDTO")
        void shouldCreateAuditLogDTO() {
            // When
            AuditLogDTO dto = new AuditLogDTO();
            dto.setId(TEST_ID.toString());
            dto.setUserId(TEST_USER_ID.toString());
            dto.setUserEmail(TEST_USER_EMAIL);
            dto.setOperationType(AuditOperationType.CREATE);
            dto.setResourceType("USER");
            dto.setResourceId(TEST_ID.toString());
            dto.setOperationDescription("创建用户");
            dto.setClientIp("192.168.1.1");
            dto.setUserAgent("Mozilla/5.0");
            dto.setOperationResult("SUCCESS");
            dto.setCreatedAt(TEST_CREATED_AT);

            // Then
            assertThat(dto.getId()).isEqualTo(TEST_ID.toString());
            assertThat(dto.getUserId()).isEqualTo(TEST_USER_ID.toString());
            assertThat(dto.getUserEmail()).isEqualTo(TEST_USER_EMAIL);
            assertThat(dto.getOperationType()).isEqualTo(AuditOperationType.CREATE);
            assertThat(dto.getResourceType()).isEqualTo("USER");
            assertThat(dto.getOperationResult()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("应该包含旧值和新值")
        void shouldIncludeOldAndNewValues() {
            // Given
            AuditLogDTO dto = new AuditLogDTO();
            Map<String, Object> oldValue = Map.of("email", "old@example.com");
            Map<String, Object> newValue = Map.of("email", "new@example.com");

            // When
            dto.setOldValue(oldValue);
            dto.setNewValue(newValue);

            // Then
            assertThat(dto.getOldValue()).isEqualTo(oldValue);
            assertThat(dto.getNewValue()).isEqualTo(newValue);
        }
    }

    @Nested
    @DisplayName("AuditLogFilter 测试")
    class AuditLogFilterTests {

        @Test
        @DisplayName("应该创建筛选条件")
        void shouldCreateAuditLogFilter() {
            // When
            AuditLogFilter filter = new AuditLogFilter();
            filter.setUserId(TEST_USER_ID.toString());
            filter.setUserEmail(TEST_USER_EMAIL);
            filter.setResourceType("USER");
            filter.setOperationType(AuditOperationType.UPDATE);
            filter.setStartTime(Instant.now().minusSeconds(3600));
            filter.setEndTime(Instant.now());
            filter.setOperationResult("SUCCESS");
            filter.setPage(0);
            filter.setSize(20);

            // Then
            assertThat(filter.getUserId()).isEqualTo(TEST_USER_ID.toString());
            assertThat(filter.getUserEmail()).isEqualTo(TEST_USER_EMAIL);
            assertThat(filter.getResourceType()).isEqualTo("USER");
            assertThat(filter.getOperationType()).isEqualTo(AuditOperationType.UPDATE);
            assertThat(filter.getPage()).isEqualTo(0);
            assertThat(filter.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("应该支持部分筛选条件")
        void shouldSupportPartialFilters() {
            // When
            AuditLogFilter filter = new AuditLogFilter();
            filter.setResourceType("ROLE");
            filter.setPage(1);
            filter.setSize(50);

            // Then
            assertThat(filter.getResourceType()).isEqualTo("ROLE");
            assertThat(filter.getUserId()).isNull();
            assertThat(filter.getOperationType()).isNull();
        }
    }

    @Nested
    @DisplayName("AuditLogListResponse 测试")
    class AuditLogListResponseTests {

        @Test
        @DisplayName("应该创建分页响应")
        void shouldCreatePaginatedResponse() {
            // Given
            AuditLogDTO dto1 = createAuditLogDTO();
            AuditLogDTO dto2 = createAuditLogDTO();
            List<AuditLogDTO> content = List.of(dto1, dto2);

            // When
            AuditLogListResponse response = new AuditLogListResponse(content, 2, 1, 0, 20);

            // Then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getNumber()).isEqualTo(0);
            assertThat(response.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("应该支持空列表")
        void shouldSupportEmptyList() {
            // When
            AuditLogListResponse response = new AuditLogListResponse(List.of(), 0, 0, 0, 20);

            // Then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isEqualTo(0);
        }
    }

    private AuditLogDTO createAuditLogDTO() {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(UUID.randomUUID().toString());
        dto.setUserId(TEST_USER_ID.toString());
        dto.setUserEmail(TEST_USER_EMAIL);
        dto.setOperationType(AuditOperationType.CREATE);
        dto.setResourceType("USER");
        dto.setOperationResult("SUCCESS");
        dto.setCreatedAt(TEST_CREATED_AT);
        return dto;
    }
}
