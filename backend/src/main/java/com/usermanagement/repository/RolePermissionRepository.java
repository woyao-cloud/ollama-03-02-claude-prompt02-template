package com.usermanagement.repository;

import com.usermanagement.domain.RolePermission;
import com.usermanagement.domain.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 角色权限关联数据访问接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    /**
     * 根据角色 ID 查找权限关联
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.id.roleId = :roleId")
    List<RolePermission> findByRoleId(@Param("roleId") UUID roleId);

    /**
     * 根据多个角色 ID 查找权限关联
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.id.roleId IN :roleIds")
    List<RolePermission> findAllByRoleIdIn(@Param("roleIds") List<UUID> roleIds);

    /**
     * 根据权限 ID 查找角色关联
     */
    @Query("SELECT rp FROM RolePermission rp WHERE rp.id.permissionId = :permissionId")
    List<RolePermission> findByPermissionId(@Param("permissionId") UUID permissionId);

    /**
     * 删除角色的所有权限关联
     */
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.id.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") UUID roleId);

    /**
     * 删除权限的所有角色关联
     */
    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.id.permissionId = :permissionId")
    void deleteByPermissionId(@Param("permissionId") UUID permissionId);

    /**
     * 检查角色是否有指定权限
     */
    boolean existsByIdRoleIdAndIdPermissionId(UUID roleId, UUID permissionId);
}
