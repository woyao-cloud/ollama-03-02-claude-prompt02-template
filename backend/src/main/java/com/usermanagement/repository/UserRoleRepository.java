package com.usermanagement.repository;

import com.usermanagement.domain.UserRole;
import com.usermanagement.domain.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 用户角色关联数据访问接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    /**
     * 根据用户 ID 查找角色关联
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.id.userId = :userId")
    List<UserRole> findByUserId(@Param("userId") UUID userId);

    /**
     * 根据用户 ID 查找角色关联 (别名方法)
     */
    default List<UserRole> findAllByUserId(UUID userId) {
        return findByUserId(userId);
    }

    /**
     * 根据角色 ID 查找用户关联
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.id.roleId = :roleId")
    List<UserRole> findByRoleId(@Param("roleId") UUID roleId);

    /**
     * 删除用户的所有角色关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.id.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * 删除角色的所有用户关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.id.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") UUID roleId);

    /**
     * 检查用户是否有指定角色
     */
    boolean existsByIdUserIdAndIdRoleId(UUID userId, UUID roleId);
}
