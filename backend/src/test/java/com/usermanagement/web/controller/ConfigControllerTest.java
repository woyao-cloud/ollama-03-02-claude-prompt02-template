package com.usermanagement.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.ConfigStatus;
import com.usermanagement.domain.ConfigType;
import com.usermanagement.domain.DataType;
import com.usermanagement.service.config.ConfigService;
import com.usermanagement.web.dto.ConfigCreateRequest;
import com.usermanagement.web.dto.ConfigListResponse;
import com.usermanagement.web.dto.ConfigUpdateRequest;
import com.usermanagement.web.dto.SystemConfigDTO;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ConfigController 单元测试
 */
@WebMvcTest(ConfigController.class)
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConfigService configService;

    private static final String TEST_CONFIG_KEY = "test.config.key";
    private static final String TEST_CONFIG_VALUE = "testValue";

    @Nested
    @DisplayName("获取配置列表测试")
    class GetConfigListTests {

        @Test
        @DisplayName("应该返回配置列表")
        void shouldReturnConfigList() throws Exception {
            // Given
            UUID configId = UUID.randomUUID();
            SystemConfigDTO dto = createConfigDTO(configId, ConfigType.SYSTEM, ConfigStatus.ACTIVE);

            ConfigListResponse response = ConfigListResponse.builder()
                    .content(List.of(dto))
                    .page(0)
                    .size(20)
                    .totalElements(1)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            given(configService.getConfigs(PageRequest.of(0, 20)))
                    .willReturn(new PageImpl<>(List.of(dto)));

            // When & Then
            mockMvc.perform(get("/api/configs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(configId.toString()))
                    .andExpect(jsonPath("$.content[0].configKey").value(TEST_CONFIG_KEY));
        }

        @Test
        @DisplayName("应该返回空列表")
        void shouldReturnEmptyList() throws Exception {
            // Given
            ConfigListResponse response = ConfigListResponse.builder()
                    .content(List.of())
                    .page(0)
                    .size(20)
                    .totalElements(0)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            given(configService.getConfigs(PageRequest.of(0, 20)))
                    .willReturn(new PageImpl<>(List.of()));

            // When & Then
            mockMvc.perform(get("/api/configs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("根据类型获取配置测试")
    class GetConfigsByTypeTests {

        @Test
        @DisplayName("应该根据类型返回配置列表")
        void shouldReturnConfigsByType() throws Exception {
            // Given
            UUID configId = UUID.randomUUID();
            SystemConfigDTO dto = createConfigDTO(configId, ConfigType.EMAIL, ConfigStatus.ACTIVE);

            given(configService.getConfigsByType(ConfigType.EMAIL)).willReturn(List.of(dto));

            // When & Then
            mockMvc.perform(get("/api/configs/type/{type}", ConfigType.EMAIL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(configId.toString()))
                    .andExpect(jsonPath("$[0].configType").value("EMAIL"));
        }
    }

    @Nested
    @DisplayName("根据配置键获取配置测试")
    class GetConfigByKeyTests {

        @Test
        @DisplayName("应该根据配置键获取配置")
        void shouldGetConfigByKey() throws Exception {
            // Given
            UUID configId = UUID.randomUUID();
            SystemConfigDTO dto = createConfigDTO(configId, ConfigType.SYSTEM, ConfigStatus.ACTIVE);

            given(configService.getConfigByKey(TEST_CONFIG_KEY)).willReturn(dto);

            // When & Then
            mockMvc.perform(get("/api/configs/key/{key}", TEST_CONFIG_KEY))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(configId.toString()))
                    .andExpect(jsonPath("$.configKey").value(TEST_CONFIG_KEY));
        }

        @Test
        @DisplayName("配置不存在时返回 404")
        void shouldReturn404WhenConfigNotFound() throws Exception {
            // Given
            given(configService.getConfigByKey(TEST_CONFIG_KEY)).willReturn(null);

            // When & Then
            mockMvc.perform(get("/api/configs/key/{key}", TEST_CONFIG_KEY))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("根据 ID 获取配置测试")
    class GetConfigByIdTests {

        @Test
        @DisplayName("应该根据 ID 获取配置")
        void shouldGetConfigById() throws Exception {
            // Given
            UUID configId = UUID.randomUUID();
            SystemConfigDTO dto = createConfigDTO(configId, ConfigType.SYSTEM, ConfigStatus.ACTIVE);

            given(configService.getConfigById(configId)).willReturn(dto);

            // When & Then
            mockMvc.perform(get("/api/configs/{id}", configId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(configId.toString()))
                    .andExpect(jsonPath("$.configKey").value(TEST_CONFIG_KEY));
        }
    }

    @Nested
    @DisplayName("创建配置测试")
    class CreateConfigTests {

        @Test
        @DisplayName("应该创建配置")
        void shouldCreateConfig() throws Exception {
            // Given
            UUID configId = UUID.randomUUID();
            ConfigCreateRequest request = new ConfigCreateRequest();
            request.setConfigKey(TEST_CONFIG_KEY);
            request.setConfigValue(TEST_CONFIG_VALUE);
            request.setConfigType(ConfigType.SYSTEM);
            request.setCategory(ConfigType.SYSTEM);
            request.setDataType(DataType.STRING);
            request.setDescription("测试配置");

            SystemConfigDTO response = createConfigDTO(configId, ConfigType.SYSTEM, ConfigStatus.ACTIVE);

            given(configService.createConfig(any(ConfigCreateRequest.class), any(UUID.class), any(String.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/configs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(configId.toString()))
                    .andExpect(jsonPath("$.configKey").value(TEST_CONFIG_KEY));
        }

        @Test
        @DisplayName("配置键已存在时返回 400")
        void shouldReturn400WhenConfigKeyExists() throws Exception {
            // Given
            ConfigCreateRequest request = new ConfigCreateRequest();
            request.setConfigKey(TEST_CONFIG_KEY);
            request.setConfigValue(TEST_CONFIG_VALUE);
            request.setConfigType(ConfigType.SYSTEM);
            request.setCategory(ConfigType.SYSTEM);
            request.setDataType(DataType.STRING);

            given(configService.createConfig(any(ConfigCreateRequest.class), any(UUID.class), any(String.class)))
                    .willThrow(new ConfigService.ConfigAlreadyExistsException(TEST_CONFIG_KEY));

            // When & Then
            mockMvc.perform(post("/api/configs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("请求参数无效时返回 400")
        void shouldReturn400WhenInvalidRequest() throws Exception {
            // Given
            ConfigCreateRequest request = new ConfigCreateRequest();
            // configKey 和 configValue 为空

            // When & Then
            mockMvc.perform(post("/api/configs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("更新配置测试")
    class UpdateConfigTests {

        @Test
        @DisplayName("应该更新配置")
        void shouldUpdateConfig() throws Exception {
            // Given
            UUID configId = UUID.randomUUID();
            ConfigUpdateRequest request = new ConfigUpdateRequest();
            request.setConfigValue("newValue");
            request.setReason("测试更新");

            SystemConfigDTO response = createConfigDTO(configId, ConfigType.SYSTEM, ConfigStatus.ACTIVE);
            response.setConfigValue("newValue");

            given(configService.updateConfig(any(UUID.class), any(ConfigUpdateRequest.class), any(UUID.class), any(String.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/configs/{id}", configId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(configId.toString()))
                    .andExpect(jsonPath("$.configValue").value("newValue"));
        }

        @Test
        @DisplayName("配置不存在时返回 404")
        void shouldReturn404WhenConfigNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            ConfigUpdateRequest request = new ConfigUpdateRequest();
            request.setConfigValue("newValue");

            given(configService.updateConfig(any(UUID.class), any(ConfigUpdateRequest.class), any(UUID.class), any(String.class)))
                    .willThrow(new ConfigService.ConfigNotFoundException("配置不存在"));

            // When & Then
            mockMvc.perform(put("/api/configs/{id}", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("删除配置测试")
    class DeleteConfigTests {

        @Test
        @DisplayName("应该删除配置")
        void shouldDeleteConfig() throws Exception {
            // Given
            UUID configId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(delete("/api/configs/{id}", configId))
                    .andExpect(status().isNoContent());

            then(configService).should().deleteConfig(configId, any(UUID.class), any(String.class));
        }
    }

    @Nested
    @DisplayName("获取配置历史测试")
    class GetConfigHistoryTests {

        @Test
        @DisplayName("应该获取配置历史")
        void shouldGetConfigHistory() throws Exception {
            // Given
            UUID configId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(get("/api/configs/{id}/history", configId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("刷新缓存测试")
    class RefreshCacheTests {

        @Test
        @DisplayName("应该刷新所有配置缓存")
        void shouldRefreshCache() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/configs/refresh-cache"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("配置缓存已刷新"));

            then(configService).should().refreshAllCache();
        }
    }

    @Nested
    @DisplayName("批量获取配置测试")
    class GetBatchConfigsTests {

        @Test
        @DisplayName("应该批量获取配置值")
        void shouldGetBatchConfigs() throws Exception {
            // Given
            String keys = "key1,key2,key3";
            java.util.Map<String, String> configs = java.util.Map.of(
                    "key1", "value1",
                    "key2", "value2",
                    "key3", "value3"
            );

            given(configService.getConfigs(List.of("key1", "key2", "key3"))).willReturn(configs);

            // When & Then
            mockMvc.perform(get("/api/configs/batch")
                    .param("keys", keys))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.key1").value("value1"))
                    .andExpect(jsonPath("$.key2").value("value2"))
                    .andExpect(jsonPath("$.key3").value("value3"));
        }
    }

    // 辅助方法
    private SystemConfigDTO createConfigDTO(UUID id, ConfigType configType, ConfigStatus status) {
        SystemConfigDTO dto = new SystemConfigDTO();
        dto.setId(id.toString());
        dto.setConfigKey(TEST_CONFIG_KEY);
        dto.setConfigValue(TEST_CONFIG_VALUE);
        dto.setConfigType(configType);
        dto.setCategory(configType);
        dto.setDataType(DataType.STRING);
        dto.setDescription("测试配置");
        dto.setIsEncrypted(false);
        dto.setIsSensitive(false);
        dto.setStatus(status);
        dto.setVersion(1);
        return dto;
    }
}
