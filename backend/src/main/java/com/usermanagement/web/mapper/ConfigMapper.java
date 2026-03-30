package com.usermanagement.web.mapper;

import com.usermanagement.domain.ConfigHistory;
import com.usermanagement.domain.SystemConfig;
import com.usermanagement.web.dto.ConfigCreateRequest;
import com.usermanagement.web.dto.ConfigHistoryDTO;
import com.usermanagement.web.dto.SystemConfigDTO;
import org.mapstruct.*;

/**
 * 系统配置对象映射接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface ConfigMapper {

    /**
     * 将 SystemConfig 实体映射为 SystemConfigDTO
     *
     * @param config 配置实体
     * @return 配置 DTO
     */
    SystemConfigDTO toDto(SystemConfig config);

    /**
     * 将 ConfigHistory 实体映射为 ConfigHistoryDTO
     *
     * @param history 历史记录实体
     * @return 历史记录 DTO
     */
    ConfigHistoryDTO toHistoryDto(ConfigHistory history);

    /**
     * 将 ConfigCreateRequest 映射为 SystemConfig 实体
     *
     * @param request 创建请求
     * @return 配置实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    SystemConfig toEntity(ConfigCreateRequest request);

    /**
     * 使用配置值更新 SystemConfig 实体
     *
     * @param configValue 新配置值
     * @param config      目标配置实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "configKey", ignore = true)
    @Mapping(target = "configType", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "dataType", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "isEncrypted", ignore = true)
    @Mapping(target = "isSensitive", ignore = true)
    @Mapping(target = "defaultValue", ignore = true)
    @Mapping(target = "minValue", ignore = true)
    @Mapping(target = "maxValue", ignore = true)
    @Mapping(target = "regexPattern", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateConfigValue(String configValue, @MappingTarget SystemConfig config);

    /**
     * 更新配置状态
     *
     * @param status 新状态
     * @param config 目标配置实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "configKey", ignore = true)
    @Mapping(target = "configValue", ignore = true)
    @Mapping(target = "configType", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "dataType", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "isEncrypted", ignore = true)
    @Mapping(target = "isSensitive", ignore = true)
    @Mapping(target = "defaultValue", ignore = true)
    @Mapping(target = "minValue", ignore = true)
    @Mapping(target = "maxValue", ignore = true)
    @Mapping(target = "regexPattern", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateConfigStatus(@NonNull String status, @MappingTarget SystemConfig config);
}
