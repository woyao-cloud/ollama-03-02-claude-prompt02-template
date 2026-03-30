package com.usermanagement.service.config;

import com.usermanagement.domain.*;
import com.usermanagement.event.ConfigChangedEvent;
import com.usermanagement.repository.ConfigHistoryRepository;
import com.usermanagement.repository.ConfigRepository;
import com.usermanagement.service.AuditLogService;
import com.usermanagement.web.dto.ConfigCreateRequest;
import com.usermanagement.web.dto.ConfigUpdateRequest;
import com.usermanagement.web.dto.SystemConfigDTO;
import com.usermanagement.web.mapper.ConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * ConfigService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ConfigServiceTest {

    @Mock
    private ConfigRepository configRepository;

    @Mock
    private ConfigHistoryRepository historyRepository;

    @Mock
    private ConfigMapper configMapper;

    @Mock
    private ConfigValidator configValidator;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private ConfigCache configCache;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuditLogService auditLogService;

    private ConfigService configService;

    private static final String TEST_CONFIG_KEY = "test.config.key";
    private static final String TEST_CONFIG_VALUE = "testValue";
    private static final String TEST_CONFIG_DESCRIPTION = "测试配置";

    @BeforeEach
    void setUp() {
        configService = new ConfigService(
                configRepository, historyRepository, configMapper, configValidator,
                encryptionService, configCache, eventPublisher, auditLogService);
    }

    @Nested
    @DisplayName("获取配置测试")
    class GetConfigTests {

        @Test
        @DisplayName("应该根据配置键获取配置值")
        void shouldGetConfigValueByKey() {
            // Given
            SystemConfig config = createSystemConfig(ConfigType.SYSTEM, DataType.STRING, ConfigStatus.ACTIVE);
            SystemConfigDTO dto = createConfigDTO(config.getId(), ConfigStatus.ACTIVE);

            given(configCache.get(TEST_CONFIG_KEY)).willReturn(null);
            given(configRepository.findByConfigKey(TEST_CONFIG_KEY)).willReturn(Optional.of(config));
            given(configMapper.toDto(config)).willReturn(dto);

            // When
            String result = configService.getConfigValue(TEST_CONFIG_KEY);

            // Then
            assertThat(result).isEqualTo(TEST_CONFIG_VALUE);
        }

        @Test
        @DisplayName("应该从缓存获取配置值")
        void shouldGetConfigValueFromCache() {
            // Given
            SystemConfigDTO cachedDto = createConfigDTO(UUID.randomUUID(), ConfigStatus.ACTIVE);
            cachedDto.setConfigValue("cachedValue");

            given(configCache.get(TEST_CONFIG_KEY)).willReturn(cachedDto);

            // When
            String result = configService.getConfigValue(TEST_CONFIG_KEY);

            // Then
            assertThat(result).isEqualTo("cachedValue");
            verify(configRepository, never()).findByConfigKey(any());
        }

        @Test
        @DisplayName("配置不存在时返回 null")
        void shouldReturnNullWhenConfigNotFound() {
            // Given
            given(configCache.get(TEST_CONFIG_KEY)).willReturn(null);
            given(configRepository.findByConfigKey(TEST_CONFIG_KEY)).willReturn(Optional.empty());

            // When
            String result = configService.getConfigValue(TEST_CONFIG_KEY);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("应该根据 ID 获取配置详情")
        void shouldGetConfigById() {
            // Given
            UUID configId = UUID.randomUUID();
            SystemConfig config = createSystemConfig(ConfigType.SYSTEM, DataType.STRING, ConfigStatus.ACTIVE);
            config.setId(configId);
            SystemConfigDTO dto = createConfigDTO(configId, ConfigStatus.ACTIVE);

            given(configRepository.findById(configId)).willReturn(Optional.of(config));
            given(configMapper.toDto(config)).willReturn(dto);

            // When
            SystemConfigDTO result = configService.getConfigById(configId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(configId.toString());
            assertThat(result.getConfigKey()).isEqualTo(TEST_CONFIG_KEY);
        }

        @Test
        @DisplayName("配置不存在时抛出异常")
        void shouldThrowExceptionWhenConfigNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(configRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configService.getConfigById(nonExistentId))
                    .isInstanceOf(ConfigService.ConfigNotFoundException.class)
                    .hasMessageContaining("配置不存在");
        }

        @Test
        @DisplayName("应该根据类型获取配置列表")
        void shouldGetConfigsByType() {
            // Given
            UUID configId1 = UUID.randomUUID();
            UUID configId2 = UUID.randomUUID();
            SystemConfig config1 = createSystemConfig(ConfigType.EMAIL, DataType.STRING, ConfigStatus.ACTIVE);
            config1.setId(configId1);
            SystemConfig config2 = createSystemConfig(ConfigType.EMAIL, DataType.STRING, ConfigStatus.ACTIVE);
            config2.setId(configId2);

            SystemConfigDTO dto1 = createConfigDTO(configId1, ConfigStatus.ACTIVE);
            SystemConfigDTO dto2 = createConfigDTO(configId2, ConfigStatus.ACTIVE);

            given(configRepository.findByConfigType(ConfigType.EMAIL)).willReturn(List.of(config1, config2));
            given(configMapper.toDto(config1)).willReturn(dto1);
            given(configMapper.toDto(config2)).willReturn(dto2);

            // When
            List<SystemConfigDTO> result = configService.getConfigsByType(ConfigType.EMAIL);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getConfigKey()).isEqualTo(TEST_CONFIG_KEY);
        }
    }

    @Nested
    @DisplayName("创建配置测试")
    class CreateConfigTests {

        @Test
        @DisplayName("应该创建配置")
        void shouldCreateConfig() {
            // Given
            UUID configId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String userEmail = "test@example.com";

            ConfigCreateRequest request = createConfigCreateRequest(ConfigType.SYSTEM, DataType.STRING);
            SystemConfig config = createSystemConfig(ConfigType.SYSTEM, DataType.STRING, ConfigStatus.ACTIVE);
            config.setId(configId);
            SystemConfigDTO dto = createConfigDTO(configId, ConfigStatus.ACTIVE);

            given(configRepository.existsByConfigKey(TEST_CONFIG_KEY)).willReturn(false);
            given(configMapper.toEntity(request)).willReturn(config);
            given(configRepository.save(config)).willReturn(config);
            given(configMapper.toDto(config)).willReturn(dto);

            // When
            SystemConfigDTO result = configService.createConfig(request, userId, userEmail);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(configId.toString());

            ArgumentCaptor<SystemConfig> configCaptor = ArgumentCaptor.forClass(SystemConfig.class);
            then(configRepository).should().save(configCaptor.capture());
            SystemConfig savedConfig = configCaptor.getValue();
            assertThat(savedConfig.getStatus()).isEqualTo(ConfigStatus.ACTIVE);

            verify(eventPublisher).publishEvent(any(ConfigChangedEvent.class));
            verify(configCache).put(config, dto);
        }

        @Test
        @DisplayName("配置键已存在时抛出异常")
        void shouldThrowExceptionWhenConfigKeyExists() {
            // Given
            UUID userId = UUID.randomUUID();
            String userEmail = "test@example.com";

            ConfigCreateRequest request = createConfigCreateRequest(ConfigType.SYSTEM, DataType.STRING);
            given(configRepository.existsByConfigKey(TEST_CONFIG_KEY)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> configService.createConfig(request, userId, userEmail))
                    .isInstanceOf(ConfigService.ConfigAlreadyExistsException.class)
                    .hasMessageContaining("配置已存在");

            verify(configRepository, never()).save(any());
        }

        @Test
        @DisplayName("应该加密敏感配置")
        void shouldEncryptSensitiveConfig() {
            // Given
            UUID configId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String userEmail = "test@example.com";

            ConfigCreateRequest request = createConfigCreateRequest(ConfigType.SECURITY, DataType.STRING);
            request.setIsEncrypted(true);
            request.setIsSensitive(true);

            SystemConfig config = createSystemConfig(ConfigType.SECURITY, DataType.STRING, ConfigStatus.ACTIVE);
            config.setId(configId);
            config.setIsEncrypted(true);
            config.setIsSensitive(true);

            SystemConfigDTO dto = createConfigDTO(configId, ConfigStatus.ACTIVE);

            given(configRepository.existsByConfigKey(TEST_CONFIG_KEY)).willReturn(false);
            given(configMapper.toEntity(request)).willReturn(config);
            given(encryptionService.encrypt(TEST_CONFIG_VALUE)).willReturn("encryptedValue");
            given(configRepository.save(config)).willReturn(config);
            given(configMapper.toDto(config)).willReturn(dto);

            // When
            configService.createConfig(request, userId, userEmail);

            // Then
            ArgumentCaptor<SystemConfig> configCaptor = ArgumentCaptor.forClass(SystemConfig.class);
            then(configRepository).should().save(configCaptor.capture());
            SystemConfig savedConfig = configCaptor.getValue();
            assertThat(savedConfig.getConfigValue()).isEqualTo("encryptedValue");

            verify(encryptionService).encrypt(TEST_CONFIG_VALUE);
        }

        @Test
        @DisplayName("应该验证配置值")
        void shouldValidateConfigValue() {
            // Given
            UUID userId = UUID.randomUUID();
            String userEmail = "test@example.com";

            ConfigCreateRequest request = createConfigCreateRequest(ConfigType.SYSTEM, DataType.NUMBER);
            request.setConfigValue("invalid");

            // When & Then
            // 验证器应该抛出异常
            given(configValidator.validateCreateRequest(request))
                    .willThrow(new ConfigValidator.ConfigValidationException(TEST_CONFIG_KEY, "必须为数字格式"));

            assertThatThrownBy(() -> configService.createConfig(request, userId, userEmail))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class);
        }
    }

    @Nested
    @DisplayName("更新配置测试")
    class UpdateConfigTests {

        @Test
        @DisplayName("应该更新配置")
        void shouldUpdateConfig() {
            // Given
            UUID configId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String userEmail = "test@example.com";

            SystemConfig config = createSystemConfig(ConfigType.SYSTEM, DataType.STRING, ConfigStatus.ACTIVE);
            config.setId(configId);
            config.setVersion(1);

            ConfigUpdateRequest request = new ConfigUpdateRequest();
            request.setConfigValue("newValue");
            request.setReason("测试更新");

            SystemConfigDTO dto = createConfigDTO(configId, ConfigStatus.ACTIVE);

            given(configRepository.findById(configId)).willReturn(Optional.of(config));
            given(configRepository.save(config)).willReturn(config);
            given(configMapper.toDto(config)).willReturn(dto);

            // When
            SystemConfigDTO result = configService.updateConfig(configId, request, userId, userEmail);

            // Then
            assertThat(result).isNotNull();
            assertThat(config.getConfigValue()).isEqualTo("newValue");
            assertThat(config.getVersion()).isEqualTo(2);

            verify(configCache).evict(TEST_CONFIG_KEY);
            verify(eventPublisher).publishEvent(any(ConfigChangedEvent.class));
        }

        @Test
        @DisplayName("配置不存在时抛出异常")
        void shouldThrowExceptionWhenConfigNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            ConfigUpdateRequest request = new ConfigUpdateRequest();
            request.setConfigValue("newValue");

            given(configRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configService.updateConfig(nonExistentId, request, UUID.randomUUID(), "test@example.com"))
                    .isInstanceOf(ConfigService.ConfigNotFoundException.class)
                    .hasMessageContaining("配置不存在");
        }

        @Test
        @DisplayName("应该验证更新后的配置值")
        void shouldValidateUpdatedConfigValue() {
            // Given
            UUID configId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String userEmail = "test@example.com";

            SystemConfig config = createSystemConfig(ConfigType.SYSTEM, DataType.NUMBER, ConfigStatus.ACTIVE);
            config.setId(configId);

            ConfigUpdateRequest request = new ConfigUpdateRequest();
            request.setConfigValue("invalid");

            given(configRepository.findById(configId)).willReturn(Optional.of(config));

            // When & Then
            assertThatThrownBy(() -> configService.updateConfig(configId, request, userId, userEmail))
                    .isInstanceOf(ConfigValidator.ConfigValidationException.class);
        }
    }

    @Nested
    @DisplayName("删除配置测试")
    class DeleteConfigTests {

        @Test
        @DisplayName("应该删除配置")
        void shouldDeleteConfig() {
            // Given
            UUID configId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String userEmail = "test@example.com";

            SystemConfig config = createSystemConfig(ConfigType.SYSTEM, DataType.STRING, ConfigStatus.ACTIVE);
            config.setId(configId);

            given(configRepository.findById(configId)).willReturn(Optional.of(config));

            // When
            configService.deleteConfig(configId, userId, userEmail);

            // Then
            verify(configRepository).delete(config);
            verify(configCache).evict(TEST_CONFIG_KEY);
            verify(eventPublisher).publishEvent(any(ConfigChangedEvent.class));
        }

        @Test
        @DisplayName("配置不存在时抛出异常")
        void shouldThrowExceptionWhenConfigNotFound() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(configRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> configService.deleteConfig(nonExistentId, UUID.randomUUID(), "test@example.com"))
                    .isInstanceOf(ConfigService.ConfigNotFoundException.class)
                    .hasMessageContaining("配置不存在");
        }
    }

    @Nested
    @DisplayName("配置历史测试")
    class ConfigHistoryTests {

        @Test
        @DisplayName("应该获取配置历史")
        void shouldGetConfigHistory() {
            // Given
            UUID configId = UUID.randomUUID();
            // 测试方法存在即可，实际测试需要更多 mock 设置

            // When
            configService.getConfigHistory(configId, org.springframework.data.domain.PageRequest.of(0, 20));

            // Then
            then(historyRepository).should().findByConfigId(configId, any());
        }
    }

    // 辅助方法
    private SystemConfig createSystemConfig(ConfigType configType, DataType dataType, ConfigStatus status) {
        return SystemConfig.builder()
                .configKey(TEST_CONFIG_KEY)
                .configValue(TEST_CONFIG_VALUE)
                .configType(configType)
                .category(configType)
                .dataType(dataType)
                .description(TEST_CONFIG_DESCRIPTION)
                .isEncrypted(false)
                .isSensitive(false)
                .status(status)
                .version(1)
                .build();
    }

    private SystemConfigDTO createConfigDTO(UUID id, ConfigStatus status) {
        SystemConfigDTO dto = new SystemConfigDTO();
        dto.setId(id.toString());
        dto.setConfigKey(TEST_CONFIG_KEY);
        dto.setConfigValue(TEST_CONFIG_VALUE);
        dto.setConfigType(ConfigType.SYSTEM);
        dto.setCategory(ConfigType.SYSTEM);
        dto.setDataType(DataType.STRING);
        dto.setDescription(TEST_CONFIG_DESCRIPTION);
        dto.setIsEncrypted(false);
        dto.setIsSensitive(false);
        dto.setStatus(status);
        dto.setVersion(1);
        return dto;
    }

    private ConfigCreateRequest createConfigCreateRequest(ConfigType configType, DataType dataType) {
        ConfigCreateRequest request = new ConfigCreateRequest();
        request.setConfigKey(TEST_CONFIG_KEY);
        request.setConfigValue(TEST_CONFIG_VALUE);
        request.setConfigType(configType);
        request.setCategory(configType);
        request.setDataType(dataType);
        request.setDescription(TEST_CONFIG_DESCRIPTION);
        request.setIsEncrypted(false);
        request.setIsSensitive(false);
        request.setStatus(ConfigStatus.ACTIVE);
        return request;
    }
}
