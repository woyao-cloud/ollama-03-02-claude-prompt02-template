package com.usermanagement.web.mapper;

import com.usermanagement.domain.Permission;
import com.usermanagement.web.dto.PermissionCreateRequest;
import com.usermanagement.web.dto.PermissionDTO;
import com.usermanagement.web.dto.PermissionUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.UUID;

/**
 * 权限对象映射接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface PermissionMapper {

    /**
     * 将 Permission 实体映射为 PermissionDTO
     *
     * @param permission 权限实体
     * @return 权限 DTO
     */
    PermissionDTO toDto(Permission permission);

    /**
     * 将 PermissionCreateRequest 映射为 Permission 实体
     *
     * @param request 创建请求
     * @return 权限实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parentId", expression = "java(toUUID(request.getParentId()))")
    Permission toEntity(PermissionCreateRequest request);

    /**
     * 使用 PermissionUpdateRequest 更新 Permission 实体
     * 只更新非空字段
     *
     * @param request    更新请求
     * @param permission 目标权限实体
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parentId", expression = "java(toUUID(request.getParentId()))")
    void updateEntity(PermissionUpdateRequest request, @MappingTarget Permission permission);

    /**
     * 将字符串转换为 UUID
     *
     * @param value 字符串值
     * @return UUID
     */
    default UUID toUUID(String value) {
        return value != null ? UUID.fromString(value) : null;
    }
}
