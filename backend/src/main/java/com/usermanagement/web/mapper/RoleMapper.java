package com.usermanagement.web.mapper;

import com.usermanagement.domain.Role;
import com.usermanagement.web.dto.RoleCreateRequest;
import com.usermanagement.web.dto.RoleDTO;
import com.usermanagement.web.dto.RoleUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 角色对象映射接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    /**
     * 将 Role 实体映射为 RoleDTO
     *
     * @param role 角色实体
     * @return 角色 DTO
     */
    RoleDTO toDto(Role role);

    /**
     * 将 RoleCreateRequest 映射为 Role 实体
     *
     * @param request 创建请求
     * @return 角色实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Role toEntity(RoleCreateRequest request);

    /**
     * 使用 RoleUpdateRequest 更新 Role 实体
     * 只更新非空字段
     *
     * @param request 更新请求
     * @param role    目标角色实体
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(RoleUpdateRequest request, @MappingTarget Role role);
}
