package com.usermanagement.web.mapper;

import com.usermanagement.domain.User;
import com.usermanagement.web.dto.UserCreateRequest;
import com.usermanagement.web.dto.UserDTO;
import com.usermanagement.web.dto.UserUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 用户对象映射接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * 将 User 实体映射为 UserDTO
     *
     * @param user 用户实体
     * @return 用户 DTO
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserDTO toDto(User user);

    /**
     * 将 UserCreateRequest 映射为 User 实体
     *
     * @param request 创建请求
     * @return 用户实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toEntity(UserCreateRequest request);

    /**
     * 使用 UserUpdateRequest 更新 User 实体
     * 只更新非空字段
     *
     * @param request 更新请求
     * @param user    目标用户实体
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);
}
