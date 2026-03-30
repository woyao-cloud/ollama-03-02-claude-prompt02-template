package com.usermanagement.web.controller;

import com.usermanagement.domain.ConfigStatus;
import com.usermanagement.domain.ConfigType;
import com.usermanagement.service.config.ConfigService;
import com.usermanagement.web.dto.ConfigCreateRequest;
import com.usermanagement.web.dto.ConfigHistoryDTO;
import com.usermanagement.web.dto.ConfigListResponse;
import com.usermanagement.web.dto.ConfigUpdateRequest;
import com.usermanagement.web.dto.SystemConfigDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 系统配置管理 REST API 控制器
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/configs")
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * 获取配置列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 配置列表
     */
    @GetMapping
    public ResponseEntity<ConfigListResponse> getConfigs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SystemConfigDTO> configs = configService.getConfigs(pageable);

        ConfigListResponse response = ConfigListResponse.builder()
                .content(configs.getContent())
                .page(configs.getNumber())
                .size(configs.getSize())
                .totalElements(configs.getTotalElements())
                .totalPages(configs.getTotalPages())
                .first(configs.isFirst())
                .last(configs.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 高级查询配置
     */
    @GetMapping("/search")
    public ResponseEntity<ConfigListResponse> searchConfigs(
            @RequestParam(required = false) String configKey,
            @RequestParam(required = false) ConfigType configType,
            @RequestParam(required = false) ConfigType category,
            @RequestParam(required = false) ConfigStatus status,
            @RequestParam(required = false) Boolean isSensitive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SystemConfigDTO> configs = configService.getConfigsByFilters(
                configKey, configType, category, status, isSensitive, pageable);

        ConfigListResponse response = ConfigListResponse.builder()
                .content(configs.getContent())
                .page(configs.getNumber())
                .size(configs.getSize())
                .totalElements(configs.getTotalElements())
                .totalPages(configs.getTotalPages())
                .first(configs.isFirst())
                .last(configs.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 根据类型获取配置
     *
     * @param type 配置类型
     * @return 配置列表
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<SystemConfigDTO>> getConfigsByType(@PathVariable ConfigType type) {
        List<SystemConfigDTO> configs = configService.getConfigsByType(type);
        return ResponseEntity.ok(configs);
    }

    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    @GetMapping("/key/{configKey}")
    public ResponseEntity<SystemConfigDTO> getConfigByKey(@PathVariable String configKey) {
        SystemConfigDTO config = configService.getConfigByKey(configKey);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    /**
     * 根据 ID 获取配置
     *
     * @param id 配置 ID
     * @return 配置详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<SystemConfigDTO> getConfigById(@PathVariable UUID id) {
        SystemConfigDTO config = configService.getConfigById(id);
        return ResponseEntity.ok(config);
    }

    /**
     * 创建配置
     *
     * @param request 创建配置请求
     * @return 配置 DTO
     */
    @PostMapping
    public ResponseEntity<SystemConfigDTO> createConfig(
            @Valid @RequestBody ConfigCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        SystemConfigDTO config = configService.createConfig(request, userId, userDetails.getUsername());
        logger.info("配置创建成功：{}", config.getConfigKey());
        return ResponseEntity.status(HttpStatus.CREATED).body(config);
    }

    /**
     * 更新配置
     *
     * @param id      配置 ID
     * @param request 更新配置请求
     * @return 配置 DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<SystemConfigDTO> updateConfig(
            @PathVariable UUID id,
            @Valid @RequestBody ConfigUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        SystemConfigDTO config = configService.updateConfig(id, request, userId, userDetails.getUsername());
        logger.info("配置更新成功：{}", config.getConfigKey());
        return ResponseEntity.ok(config);
    }

    /**
     * 删除配置
     *
     * @param id 配置 ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        configService.deleteConfig(id, userId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取配置历史
     *
     * @param id   配置 ID
     * @param page 页码
     * @param size 每页大小
     * @return 配置历史列表
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<Map<String, Object>> getConfigHistory(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConfigHistoryDTO> history = configService.getConfigHistory(id, pageable);

        Map<String, Object> response = Map.of(
                "content", history.getContent(),
                "page", history.getNumber(),
                "size", history.getSize(),
                "totalElements", history.getTotalElements(),
                "totalPages", history.getTotalPages(),
                "first", history.isFirst(),
                "last", history.isLast()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 回滚配置到历史版本
     *
     * @param configId  配置 ID
     * @param historyId 历史记录 ID
     * @return 配置 DTO
     */
    @PostMapping("/{configId}/rollback/{historyId}")
    public ResponseEntity<SystemConfigDTO> rollbackConfig(
            @PathVariable UUID configId,
            @PathVariable UUID historyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        SystemConfigDTO config = configService.rollbackConfig(configId, historyId, userId, userDetails.getUsername());
        logger.info("配置回滚成功：{} -> 版本 {}", configId, historyId);
        return ResponseEntity.ok(config);
    }

    /**
     * 刷新所有配置缓存
     *
     * @return 成功响应
     */
    @PostMapping("/refresh-cache")
    public ResponseEntity<Map<String, String>> refreshCache() {
        configService.refreshAllCache();
        return ResponseEntity.ok(Map.of("message", "配置缓存已刷新"));
    }

    /**
     * 批量获取配置值
     *
     * @param keys 配置键列表（逗号分隔）
     * @return 配置值 Map
     */
    @GetMapping("/batch")
    public ResponseEntity<Map<String, String>> getBatchConfigs(
            @RequestParam String keys) {
        List<String> keyList = List.of(keys.split(","));
        Map<String, String> configs = configService.getConfigs(keyList);
        return ResponseEntity.ok(configs);
    }
}
