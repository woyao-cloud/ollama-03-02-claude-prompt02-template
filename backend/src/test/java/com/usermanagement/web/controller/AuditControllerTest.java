package com.usermanagement.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.AuditOperationType;
import com.usermanagement.service.AuditLogService;
import com.usermanagement.web.dto.AuditLogDTO;
import com.usermanagement.web.dto.AuditLogFilter;
import com.usermanagement.web.dto.AuditLogListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuditController 单元测试
 */
@WebMvcTest(AuditController.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID TEST_ID = UUID.randomUUID();
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_USER_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("查询审计日志列表 API 测试")
    class GetAuditLogListApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该获取分页审计日志列表")
        void shouldGetPaginatedAuditLogs() throws Exception {
            // Given
            AuditLogDTO dto1 = createAuditLogDTO(AuditOperationType.CREATE);
            AuditLogDTO dto2 = createAuditLogDTO(AuditOperationType.UPDATE);
            List<AuditLogDTO> content = List.of(dto1, dto2);
            AuditLogListResponse response = new AuditLogListResponse(
                content, 2, 1, 0, 20
            );

            given(auditLogService.findByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(content, PageRequest.of(0, 20), 2));

            // When & Then
            mockMvc.perform(get("/api/audit-logs")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0));
        }

        @Test
        @WithMockUser
        @DisplayName("应该支持按用户 ID 筛选")
        void shouldFilterByUserId() throws Exception {
            // Given
            AuditLogListResponse response = new AuditLogListResponse(List.of(), 0, 0, 0, 20);
            given(auditLogService.findByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

            // When & Then
            mockMvc.perform(get("/api/audit-logs")
                    .param("userId", TEST_USER_ID.toString()))
                .andExpect(status().isOk());

            then(auditLogService).should().findByFilters(
                any(UUID.class), any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        @WithMockUser
        @DisplayName("应该支持按用户邮箱筛选")
        void shouldFilterByUserEmail() throws Exception {
            // Given
            AuditLogListResponse response = new AuditLogListResponse(List.of(), 0, 0, 0, 20);
            given(auditLogService.findByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

            // When & Then
            mockMvc.perform(get("/api/audit-logs")
                    .param("userEmail", TEST_USER_EMAIL))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("应该支持按资源类型筛选")
        void shouldFilterByResourceType() throws Exception {
            // Given
            AuditLogListResponse response = new AuditLogListResponse(List.of(), 0, 0, 0, 20);
            given(auditLogService.findByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

            // When & Then
            mockMvc.perform(get("/api/audit-logs")
                    .param("resourceType", "USER"))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("应该支持按操作类型筛选")
        void shouldFilterByOperationType() throws Exception {
            // Given
            AuditLogListResponse response = new AuditLogListResponse(List.of(), 0, 0, 0, 20);
            given(auditLogService.findByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

            // When & Then
            mockMvc.perform(get("/api/audit-logs")
                    .param("operationType", "CREATE"))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("应该支持按时间范围筛选")
        void shouldFilterByTimeRange() throws Exception {
            // Given
            Instant startTime = Instant.now().minusSeconds(3600);
            Instant endTime = Instant.now();
            AuditLogListResponse response = new AuditLogListResponse(List.of(), 0, 0, 0, 20);
            given(auditLogService.findByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

            // When & Then
            mockMvc.perform(get("/api/audit-logs")
                    .param("startTime", startTime.toString())
                    .param("endTime", endTime.toString()))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("应该支持按操作结果筛选")
        void shouldFilterByOperationResult() throws Exception {
            // Given
            AuditLogListResponse response = new AuditLogListResponse(List.of(), 0, 0, 0, 20);
            given(auditLogService.findByFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

            // When & Then
            mockMvc.perform(get("/api/audit-logs")
                    .param("operationResult", "FAILURE"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("查询审计日志详情 API 测试")
    class GetAuditLogApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该获取审计日志详情")
        void shouldGetAuditLogById() throws Exception {
            // Given
            AuditLogDTO dto = createAuditLogDTO(AuditOperationType.UPDATE);
            given(auditLogService.findByResourceId(TEST_ID))
                .willReturn(List.of(dto));

            // When & Then
            mockMvc.perform(get("/api/audit-logs/{id}", TEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_ID.toString()))
                .andExpect(jsonPath("$.operationType").value("UPDATE"))
                .andExpect(jsonPath("$.resourceType").value("USER"));
        }

        @Test
        @WithMockUser
        @DisplayName("审计日志不存在时返回 404")
        void shouldReturn404WhenAuditLogNotFound() throws Exception {
            // Given
            given(auditLogService.findByResourceId(TEST_ID)).willReturn(List.of());

            // When & Then
            mockMvc.perform(get("/api/audit-logs/{id}", TEST_ID))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("查询资源审计历史 API 测试")
    class GetResourceAuditHistoryApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该获取资源的审计历史")
        void shouldGetResourceAuditHistory() throws Exception {
            // Given
            UUID resourceId = UUID.randomUUID();
            AuditLogDTO dto1 = createAuditLogDTO(AuditOperationType.CREATE);
            AuditLogDTO dto2 = createAuditLogDTO(AuditOperationType.UPDATE);
            List<AuditLogDTO> content = List.of(dto1, dto2);
            AuditLogListResponse response = new AuditLogListResponse(
                content, 2, 1, 0, 20
            );

            given(auditLogService.findByResource("USER", resourceId, PageRequest.of(0, 20)))
                .willReturn(new PageImpl<>(content, PageRequest.of(0, 20), 2));

            // When & Then
            mockMvc.perform(get("/api/audit-logs/resources/USER/{resourceId}", resourceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
        }
    }

    @Nested
    @DisplayName("查询用户审计日志 API 测试")
    class GetUserAuditLogsApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该获取用户的审计日志")
        void shouldGetUserAuditLogs() throws Exception {
            // Given
            AuditLogDTO dto1 = createAuditLogDTO(AuditOperationType.LOGIN);
            AuditLogDTO dto2 = createAuditLogDTO(AuditOperationType.LOGOUT);
            List<AuditLogDTO> content = List.of(dto1, dto2);

            given(auditLogService.findByUserId(TEST_USER_ID, PageRequest.of(0, 20)))
                .willReturn(new PageImpl<>(content, PageRequest.of(0, 20), 2));

            // When & Then
            mockMvc.perform(get("/api/audit-logs/users/{userId}", TEST_USER_ID)
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
        }
    }

    @Nested
    @DisplayName("导出审计日志 API 测试")
    class ExportAuditLogsApiTests {

        @Test
        @WithMockUser
        @DisplayName("应该导出用户的审计日志")
        void shouldExportUserAuditLogs() throws Exception {
            // Given
            AuditLogDTO dto1 = createAuditLogDTO(AuditOperationType.CREATE);
            AuditLogDTO dto2 = createAuditLogDTO(AuditOperationType.UPDATE);
            List<AuditLogDTO> content = List.of(dto1, dto2);

            given(auditLogService.exportByUserId(TEST_USER_ID)).willReturn(content);

            // When & Then
            mockMvc.perform(get("/api/audit-logs/export/users/{userId}", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @WithMockUser
        @DisplayName("应该导出时间范围内的审计日志")
        void shouldExportAuditLogsByTimeRange() throws Exception {
            // Given
            Instant startTime = Instant.now().minusSeconds(86400);
            Instant endTime = Instant.now();
            AuditLogDTO dto1 = createAuditLogDTO(AuditOperationType.CREATE);
            AuditLogDTO dto2 = createAuditLogDTO(AuditOperationType.UPDATE);
            List<AuditLogDTO> content = List.of(dto1, dto2);

            given(auditLogService.exportByTimeRange(startTime, endTime)).willReturn(content);

            // When & Then
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("startTime", startTime.toString());
            requestBody.put("endTime", endTime.toString());

            mockMvc.perform(post("/api/audit-logs/export/time-range")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
        }
    }

    // 辅助方法
    private AuditLogDTO createAuditLogDTO(AuditOperationType operationType) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(TEST_ID.toString());
        dto.setUserId(TEST_USER_ID.toString());
        dto.setUserEmail(TEST_USER_EMAIL);
        dto.setOperationType(operationType);
        dto.setResourceType("USER");
        dto.setResourceId(TEST_ID.toString());
        dto.setOperationDescription("测试操作");
        dto.setClientIp("192.168.1.1");
        dto.setUserAgent("Mozilla/5.0");
        dto.setOperationResult("SUCCESS");
        dto.setCreatedAt(Instant.now());
        return dto;
    }
}
