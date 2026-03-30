package com.usermanagement.repository;

import com.usermanagement.domain.Role;
import com.usermanagement.domain.RoleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
