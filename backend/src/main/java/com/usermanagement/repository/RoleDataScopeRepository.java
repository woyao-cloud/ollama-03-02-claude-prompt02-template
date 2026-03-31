package com.usermanagement.repository;

import com.usermanagement.domain.RoleDataScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 角色自定义数据范围数据访问接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Repository
public interface RoleDataScopeRepository extends JpaRepository<RoleDataScope, UUID> {

    /**
     * 根据角色 ID 查找所有自定义数据范围
     */
    List<RoleDataScope> findByRoleId(UUID roleId);

    /**
     * 根据角色 ID 和范围类型查找
     */
    List<RoleDataScope> findByRoleIdAndScopeType(UUID roleId, RoleDataScope.ScopeType scopeType);

    /**
     * 删除角色的所有自定义数据范围
     */
    void deleteByRoleId(UUID roleId);

    /**
     * 检查角色是否有自定义数据范围
     */
    boolean existsByRoleId(UUID roleId);

    /**
     * 获取角色的部门范围值
     */
    @Query("SELECT r.scopeValue FROM RoleDataScope r WHERE r.roleId = :roleId AND r.scopeType = 'DEPT'")
    List<String> findDeptScopeValuesByRoleId(@Param("roleId") UUID roleId);
}
