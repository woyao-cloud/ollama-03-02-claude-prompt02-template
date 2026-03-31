package com.usermanagement.service.config;

import com.usermanagement.domain.*;
import com.usermanagement.event.ConfigChangedEvent;
import com.usermanagement.repository.ConfigHistoryRepository;
import com.usermanagement.repository.ConfigRepository;
import com.usermanagement.service.AuditLogService;
import com.usermanagement.web.dto.ConfigCreateRequest;
import com.usermanagement.web.dto.ConfigHistoryDTO;
import com.usermanagement.web.dto.ConfigUpdateRequest;
import com.usermanagement.web.dto.SystemConfigDTO;
import com.usermanagement.web.mapper.ConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 系统配置服务 - 配置 CRUD 管理
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class ConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    private final ConfigRepository configRepository;
    private final ConfigHistoryRepository historyRepository;
    private final ConfigMapper configMapper;
    private final ConfigValidator configValidator;
    private final EncryptionService encryptionService;
    private final ConfigCache configCache;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogService auditLogService;

    public ConfigService(
            ConfigRepository configRepository,
            ConfigHistoryRepository historyRepository,
            ConfigMapper configMapper,
            ConfigValidator configValidator,
            EncryptionService encryptionService,
            ConfigCache configCache,
            ApplicationEventPublisher eventPublisher,
            AuditLogService auditLogService) {
        this.configRepository = configRepository;
        this.historyRepository = historyRepository;
        this.configMapper = configMapper;
        this.configValidator = configValidator;
        this.encryptionService = encryptionService;
        this.configCache = configCache;
        this.eventPublisher = eventPublisher;
        this.auditLogService = auditLogService;
    }

    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值，不存在返回 null
     */
    public String getConfigValue(String configKey) {
        SystemConfigDTO dto = configCache.get(configKey);
        if (dto != null) {
            return dto.getConfigValue();
        }

        return configRepository.findByConfigKey(configKey)
                .map(config -> {
                    String value = config.getConfigValue();
                    // 解密加密的配置
                    if (Boolean.TRUE.equals(config.getIsEncrypted()) && value != null) {
                        try {
                            value = encryptionService.decrypt(value);
                        } catch (EncryptionService.EncryptionException e) {
                            log.error("配置解密失败：{}", configKey, e);
                            throw new ConfigDecryptionException(configKey, e);
                        }
                    }
                    return value;
                })
                .orElse(null);
    }

    /**
     * 根据配置键获取配置详情
     *
     * @param configKey 配置键
     * @return 配置 DTO
     */
    public SystemConfigDTO getConfigByKey(String configKey) {
        SystemConfigDTO cached = configCache.get(configKey);
        if (cached != null) {
            return cached;
        }

        return configRepository.findByConfigKey(configKey)
                .map(this::buildConfigDTO)
                .orElse(null);
    }

    /**
     * 根据 ID 获取配置
     *
     * @param id 配置 ID
     * @return 配置 DTO
     */
    public SystemConfigDTO getConfigById(UUID id) {
        return configRepository.findById(id)
                .map(this::buildConfigDTO)
                .orElseThrow(() -> new ConfigNotFoundException("配置不存在：" + id));
    }

    /**
     * 根据类型获取配置列表
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    public List<SystemConfigDTO> getConfigsByType(ConfigType configType) {
        return configRepository.findByConfigType(configType)
                .stream()
                .map(this::buildConfigDTO)
                .toList();
    }

    /**
     * 批量获取配置
     *
     * @param configKeys 配置键列表
     * @return 配置 Map
     */
    public Map<String, String> getConfigs(List<String> configKeys) {
        Map<String, String> result = new HashMap<>();
        for (String key : configKeys) {
            String value = getConfigValue(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 分页查询配置
     */
    public Page<SystemConfigDTO> getConfigs(Pageable pageable) {
        return configRepository.findAll(pageable).map(this::buildConfigDTO);
    }

    /**
     * 高级查询配置
     */
    public Page<SystemConfigDTO> getConfigsByFilters(
            String configKey,
            ConfigType configType,
            ConfigType category,
            ConfigStatus status,
            Boolean isSensitive,
            Pageable pageable) {
        return configRepository.findByFilters(configKey, configType, category, status, isSensitive, pageable)
                .map(this::buildConfigDTO);
    }

    /**
     * 创建配置
     */
    @Transactional
    public SystemConfigDTO createConfig(ConfigCreateRequest request, UUID userId, String userEmail) {
        // 验证请求
        configValidator.validateCreateRequest(request);

        // 检查配置键是否已存在
        if (configRepository.existsByConfigKey(request.getConfigKey())) {
            throw new ConfigAlreadyExistsException(request.getConfigKey());
        }

        // 创建配置
        SystemConfig config = configMapper.toEntity(request);

        // 加密敏感配置
        if (Boolean.TRUE.equals(request.getIsEncrypted()) && request.getConfigValue() != null) {
            config.setConfigValue(encryptionService.encrypt(request.getConfigValue()));
        }

        config = configRepository.save(config);
        log.info("配置创建成功：{}", config.getConfigKey());

        // 保存历史记录
        saveConfigHistory(config, null, request.getConfigValue(), ChangeType.CREATE, userId, userEmail, null);

        // 发布变更事件
        publishConfigChangedEvent(config, ChangeType.CREATE, null, request.getConfigValue(), userId, userEmail);

        // 更新缓存
        SystemConfigDTO dto = configMapper.toDto(config);
        configCache.put(config, dto);

        return dto;
    }

    /**
     * 更新配置
     */
    @Transactional
    public SystemConfigDTO updateConfig(UUID id, ConfigUpdateRequest request, UUID userId, String userEmail) {
        SystemConfig config = configRepository.findById(id)
                .orElseThrow(() -> new ConfigNotFoundException("配置不存在：" + id));

        String oldValue = config.getConfigValue();
        String oldDecryptedValue = Boolean.TRUE.equals(config.getIsEncrypted()) && oldValue != null
                ? encryptionService.decrypt(oldValue)
                : oldValue;

        // 验证配置值
        SystemConfig tempConfig = SystemConfig.builder()
                .configKey(config.getConfigKey())
                .dataType(config.getDataType())
                .minValue(config.getMinValue())
                .maxValue(config.getMaxValue())
                .regexPattern(config.getRegexPattern())
                .options(config.getOptions())
                .build();
        configValidator.validateConfigValue(tempConfig, request.getConfigValue());

        // 加密敏感配置
        String newValue = request.getConfigValue();
        if (Boolean.TRUE.equals(config.getIsEncrypted()) && newValue != null) {
            newValue = encryptionService.encrypt(newValue);
        }

        // 更新配置
        // 注意：version 字段由 JPA @Version 注解自动管理，实现乐观锁
        config.setConfigValue(newValue);
        config.setUpdatedBy(userId);

        // 更新状态（如果提供）
        if (request.getStatus() != null) {
            config.setStatus(request.getStatus());
        }

        config = configRepository.save(config);
        log.info("配置更新成功：{} -> {}", config.getConfigKey(), request.getConfigValue());

        // 保存历史记录
        saveConfigHistory(config, oldDecryptedValue, request.getConfigValue(), ChangeType.UPDATE, userId, userEmail, request.getReason());

        // 发布变更事件
        publishConfigChangedEvent(config, ChangeType.UPDATE, oldDecryptedValue, request.getConfigValue(), userId, userEmail);

        // 更新缓存
        configCache.evict(config.getConfigKey());

        return configMapper.toDto(config);
    }

    /**
     * 删除配置
     */
    @Transactional
    public void deleteConfig(UUID id, UUID userId, String userEmail) {
        SystemConfig config = configRepository.findById(id)
                .orElseThrow(() -> new ConfigNotFoundException("配置不存在：" + id));

        String configKey = config.getConfigKey();
        String oldValue = config.getConfigValue();
        if (Boolean.TRUE.equals(config.getIsEncrypted()) && oldValue != null) {
            oldValue = encryptionService.decrypt(oldValue);
        }

        // 保存历史记录
        saveConfigHistory(config, oldValue, null, ChangeType.DELETE, userId, userEmail, "删除配置");

        // 发布变更事件
        publishConfigChangedEvent(config, ChangeType.DELETE, oldValue, null, userId, userEmail);

        // 删除配置
        configRepository.delete(config);

        // 清除缓存
        configCache.evict(configKey);

        log.info("配置删除成功：{}", configKey);
    }

    /**
     * 获取配置历史
     */
    public Page<ConfigHistoryDTO> getConfigHistory(UUID configId, Pageable pageable) {
        return historyRepository.findByConfigId(configId, pageable)
                .map(configMapper::toHistoryDto);
    }

    /**
     * 获取配置历史（按配置键）
     */
    public Page<ConfigHistoryDTO> getConfigHistoryByKey(String configKey, Pageable pageable) {
        return historyRepository.findByConfigKey(configKey, pageable)
                .map(configMapper::toHistoryDto);
    }

    /**
     * 回滚配置到历史版本
     */
    @Transactional
    public SystemConfigDTO rollbackConfig(UUID configId, UUID historyId, UUID userId, String userEmail) {
        // 获取历史记录
        ConfigHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new ConfigNotFoundException("历史记录不存在：" + historyId));

        // 验证历史记录属于指定配置
        if (!history.getConfigId().equals(configId)) {
            throw new IllegalArgumentException("历史记录不属于指定配置");
        }

        // 获取配置
        SystemConfig config = configRepository.findById(configId)
                .orElseThrow(() -> new ConfigNotFoundException("配置不存在：" + configId));

        // 使用历史旧值作为新值
        ConfigUpdateRequest request = ConfigUpdateRequest.builder()
                .configValue(history.getOldValue())
                .reason("回滚到版本：" + history.getId())
                .build();

        return updateConfig(configId, request, userId, userEmail);
    }

    /**
     * 刷新所有配置缓存
     */
    public void refreshAllCache() {
        configCache.evictAll();
        log.info("所有配置缓存已刷新");
    }

    /**
     * 预加载配置
     */
    @Transactional(readOnly = true)
    public void preloadConfigs() {
        List<SystemConfig> configs = configRepository.findAll();
        configCache.preload(configs, this::buildConfigDTO);
    }

    /**
     * 构建配置 DTO（解密敏感配置）
     */
    private SystemConfigDTO buildConfigDTO(SystemConfig config) {
        SystemConfigDTO dto = configMapper.toDto(config);

        // 解密配置值（仅当需要展示时）
        if (Boolean.TRUE.equals(config.getIsEncrypted()) && config.getConfigValue() != null) {
            try {
                dto.setConfigValue(encryptionService.decrypt(config.getConfigValue()));
            } catch (EncryptionService.EncryptionException e) {
                log.error("配置解密失败：{}", config.getConfigKey(), e);
                dto.setConfigValue("***ENCRYPTED***");
            }
        }

        return dto;
    }

    /**
     * 保存配置历史
     */
    private void saveConfigHistory(SystemConfig config, String oldValue, String newValue,
                                   ChangeType changeType, UUID userId, String userEmail, String reason) {
        ConfigHistory history = ConfigHistory.builder()
                .configId(config.getId())
                .configKey(config.getConfigKey())
                .oldValue(oldValue)
                .newValue(newValue)
                .changeType(changeType)
                .changedBy(userId)
                .changedByEmail(userEmail)
                .reason(reason)
                .build();
        historyRepository.save(history);
    }

    /**
     * 发布配置变更事件
     */
    private void publishConfigChangedEvent(SystemConfig config, ChangeType changeType,
                                           String oldValue, String newValue, UUID userId, String userEmail) {
        ConfigChangedEvent event = new ConfigChangedEvent(
                this,
                config.getId(),
                config.getConfigKey(),
                config.getConfigType(),
                changeType,
                oldValue,
                newValue,
                userId,
                userEmail
        );
        eventPublisher.publishEvent(event);
    }

    /**
     * 配置不存在异常
     */
    public static class ConfigNotFoundException extends RuntimeException {
        public ConfigNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * 配置已存在异常
     */
    public static class ConfigAlreadyExistsException extends RuntimeException {
        public ConfigAlreadyExistsException(String configKey) {
            super("配置已存在：" + configKey);
        }
    }

    /**
     * 配置解密异常
     */
    public static class ConfigDecryptionException extends RuntimeException {
        public ConfigDecryptionException(String configKey, Throwable cause) {
            super("配置解密失败：" + configKey, cause);
        }
    }
}
