package com.usermanagement.service;

import com.usermanagement.annotation.Audit;
import com.usermanagement.domain.AuditOperationType;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.web.dto.UserCreateRequest;
import com.usermanagement.web.dto.UserDTO;
import com.usermanagement.web.dto.UserUpdateRequest;
import com.usermanagement.web.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 审计日志使用示例服务
 *
 * 演示如何在服务层使用 @Audit 注解自动记录审计日志
 */
@Service
public class AuditLogExampleService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogExampleService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuditLogExampleService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    /**
     * 创建用户 - 自动记录 CREATE 审计日志
     *
     * @Audit 注解会自动记录：
     * - 操作类型：CREATE
     * - 资源类型：USER
     * - 资源 ID：从返回值中提取
     * - 操作描述：创建用户
     * - 新值：创建的用户信息
     */
    @Audit(
        operationType = AuditOperationType.CREATE,
        resourceType = "USER",
        resourceId = "#result.id",
        description = "'创建用户：' + #request.email",
        includeNewValue = true
    )
    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        // 创建用户
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(false);

        user = userRepository.save(user);
        logger.info("用户创建成功：{}", user.getEmail());

        return userMapper.toDto(user);
    }

    /**
     * 更新用户 - 自动记录 UPDATE 审计日志
     *
     * @Audit 注解会自动记录：
     * - 操作类型：UPDATE
     * - 资源类型：USER
     * - 资源 ID：从参数 id 获取
     * - 操作描述：更新用户信息
     * - 旧值：更新前的用户信息（需要手动获取）
     * - 新值：更新后的用户信息
     */
    @Audit(
        operationType = AuditOperationType.UPDATE,
        resourceType = "USER",
        resourceId = "#id",
        description = "'更新用户信息：' + #id",
        includeOldValue = true,
        includeNewValue = true
    )
    @Transactional
    public UserDTO updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        userMapper.updateEntity(request, user);
        user = userRepository.save(user);
        logger.info("用户更新成功：{}", user.getEmail());

        return userMapper.toDto(user);
    }

    /**
     * 删除用户 - 自动记录 DELETE 审计日志
     *
     * @Audit 注解会自动记录：
     * - 操作类型：DELETE
     * - 资源类型：USER
     * - 资源 ID：从参数 id 获取
     * - 操作描述：删除用户
     * - 旧值：删除前的用户信息
     */
    @Audit(
        operationType = AuditOperationType.DELETE,
        resourceType = "USER",
        resourceId = "#id",
        description = "'删除用户：' + #id",
        includeOldValue = true,
        includeNewValue = false
    )
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        logger.info("用户删除成功：{}", user.getEmail());
        userRepository.delete(user);
    }

    /**
     * 更新用户状态 - 自动记录 STATUS_CHANGE 审计日志
     */
    @Audit(
        operationType = AuditOperationType.STATUS_CHANGE,
        resourceType = "USER",
        resourceId = "#id",
        description = "'更新用户状态：' + #status",
        includeOldValue = true,
        includeNewValue = true
    )
    @Transactional
    public UserDTO updateUserStatus(UUID id, UserStatus status) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        user.setStatus(status);
        user = userRepository.save(user);
        logger.info("用户状态更新成功：{} -> {}", user.getEmail(), status);

        return userMapper.toDto(user);
    }

    /**
     * 重置密码 - 自动记录 PASSWORD_RESET 审计日志
     */
    @Audit(
        operationType = AuditOperationType.PASSWORD_RESET,
        resourceType = "USER",
        resourceId = "#id",
        description = "'重置用户密码：' + #id",
        includeOldValue = false,
        includeNewValue = false
    )
    @Transactional
    public void resetPassword(UUID id, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("用户密码重置成功：{}", user.getEmail());
    }
}
