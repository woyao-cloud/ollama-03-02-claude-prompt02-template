package com.usermanagement.repository;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户数据访问接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户 Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据状态查找用户
     *
     * @param status 状态
     * @return 用户列表
     */
    List<User> findByStatus(UserStatus status);

    /**
     * 根据部门查找用户
     *
     * @param departmentId 部门 ID
     * @return 用户列表
     */
    List<User> findByDepartmentId(UUID departmentId);

    /**
     * 查找激活用户
     *
     * @return 激活用户列表
     */
    List<User> findByStatusAndEmailVerifiedTrue(UserStatus status);

    /**
     * 根据邮箱模糊查询
     *
     * @param emailPattern 邮箱模式
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:emailPattern% AND u.deletedAt IS NULL")
    List<User> findByEmailContaining(@Param("emailPattern") String emailPattern);
}
