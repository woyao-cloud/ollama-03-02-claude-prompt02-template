package com.usermanagement.repository;

import com.usermanagement.domain.Permission;
import com.usermanagement.domain.PermissionStatus;
import com.usermanagement.domain.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 权限数据访问接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * 根据权限代码查找
     */
    Optional<Permission> findByCode(String code);

    /**
     * 根据状态查找权限
     */
    List<Permission> findByStatus(PermissionStatus status);

    /**
     * 根据类型查找权限
     */
    List<Permission> findByType(PermissionType type);

    /**
     * 根据资源查找权限
     */
    List<Permission> findByResource(String resource);

    /**
     * 根据父权限 ID 查找子权限
     */
    List<Permission> findByParentId(UUID parentId);

    /**
     * 检查代码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 根据资源查找激活的权限
     */
    List<Permission> findByResourceAndStatus(String resource, PermissionStatus status);
}
