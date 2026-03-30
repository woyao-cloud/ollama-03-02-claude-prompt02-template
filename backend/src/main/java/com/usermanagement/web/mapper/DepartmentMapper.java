package com.usermanagement.web.mapper;

import com.usermanagement.domain.Department;
import com.usermanagement.web.dto.DepartmentCreateRequest;
import com.usermanagement.web.dto.DepartmentDTO;
import com.usermanagement.web.dto.DepartmentUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门对象映射接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    /**
     * 将 Department 实体映射为 DepartmentDTO
     *
     * @param department 部门实体
     * @return 部门 DTO
     */
    @Mapping(target = "id", expression = "java(department.getId().toString())")
    @Mapping(target = "parentId", expression = "java(department.getParentId() != null ? department.getParentId().toString() : null)")
    @Mapping(target = "managerId", expression = "java(department.getManagerId() != null ? department.getManagerId().toString() : null)")
    @Mapping(target = "status", expression = "java(department.getStatus().name())")
    DepartmentDTO toDto(Department department);

    /**
     * 将 Department 实体列表映射为 DepartmentDTO 列表 (用于树形结构)
     *
     * @param departments 部门实体列表
     * @return 部门 DTO 列表
     */
    default List<DepartmentDTO> toTreeDto(List<Department> departments) {
        if (departments == null) {
            return null;
        }
        return departments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 将 DepartmentCreateRequest 映射为 Department 实体
     *
     * @param request 创建请求
     * @return 部门实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentId", expression = "java(request.getParentId() != null ? java.util.UUID.fromString(request.getParentId()) : null)")
    @Mapping(target = "managerId", expression = "java(request.getManagerId() != null ? java.util.UUID.fromString(request.getManagerId()) : null)")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "path", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Department toEntity(DepartmentCreateRequest request);

    /**
     * 使用 DepartmentUpdateRequest 更新 Department 实体
     * 只更新非空字段
     *
     * @param request    更新请求
     * @param department 目标部门实体
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "path", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(DepartmentUpdateRequest request, @MappingTarget Department department);
}
