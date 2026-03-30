package com.usermanagement.repository;

import com.usermanagement.domain.Role;
import com.usermanagement.domain.RoleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 角色数据访问接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * 根据角色代码查找
     */
    Optional<Role> findByCode(String code);

    /**
     * 根据角色名称查找
     */
    Optional<Role> findByName(String name);

    /**
     * 根据状态查找角色
     */
    List<Role> findByStatus(RoleStatus status);

    /**
     * 检查代码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 获取所有角色（带权限，避免 N+1）
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions p WHERE r.deletedAt IS NULL")
    List<Role> findAllWithPermissions();

    /**
     * 根据 ID 获取角色（带权限，避免 N+1）
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions p WHERE r.id = :id AND r.deletedAt IS NULL")
    java.util.Optional<Role> findByIdWithPermissions(@Param("id") UUID id);

    /**
     * 根据状态获取角色（带权限，避免 N+1）
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions p WHERE r.status = :status AND r.deletedAt IS NULL")
    List<Role> findByStatusWithPermissions(@Param("status") RoleStatus status);
}
