package com.usermanagement.service;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.web.dto.UserCreateRequest;
import com.usermanagement.web.dto.UserDTO;
import com.usermanagement.web.dto.UserListResponse;
import com.usermanagement.web.dto.UserUpdateRequest;
import com.usermanagement.web.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * 用户服务实现类
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    /**
     * 创建用户
     *
     * @param request 创建用户请求
     * @return 用户 DTO
     */
    @Override
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
        user.setFailedLoginAttempts(0);

        user = userRepository.save(user);
        logger.info("用户创建成功：{}", user.getEmail());

        return userMapper.toDto(user);
    }

    /**
     * 根据 ID 获取用户
     *
     * 性能优化：使用 JOIN FETCH 避免 N+1 查询
     *
     * @param id 用户 ID
     * @return 用户 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        // 使用 JOIN FETCH 一次性加载用户、部门和角色，避免 N+1 查询
        User user = userRepository.findByIdWithDepartmentAndRoles(id)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return userMapper.toDto(user);
    }

    /**
     * 获取用户列表（分页）
     *
     * 性能优化：使用 JOIN FETCH 避免 N+1 查询
     *
     * @param pageable     分页参数
     * @param keyword      关键词（邮箱/姓名）
     * @param departmentId 部门 ID
     * @param status       用户状态
     * @return 分页用户列表
     */
    @Override
    @Transactional(readOnly = true)
    public UserListResponse getUsers(Pageable pageable, String keyword, UUID departmentId, UserStatus status) {
        Page<User> userPage;

        // 根据筛选条件查询 - 优先使用带 JOIN FETCH 的查询避免 N+1
        if (departmentId != null && status != null) {
            userPage = userRepository.findByDepartmentIdAndStatusWithRoles(departmentId, status, pageable);
        } else if (departmentId != null) {
            userPage = userRepository.findByDepartmentId(departmentId, pageable);
        } else if (status != null) {
            userPage = userRepository.findByStatus(status, pageable);
        } else {
            // 使用带 JOIN FETCH 的查询避免 N+1
            userPage = userRepository.findAllWithDepartmentAndRoles(pageable);
        }

        // 映射为 DTO
        Page<UserDTO> dtoPage = userPage.map(userMapper::toDto);

        return new UserListResponse(dtoPage);
    }

    /**
     * 更新用户
     *
     * @param id      用户 ID
     * @param request 更新用户请求
     * @return 用户 DTO
     */
    @Override
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
     * 删除用户（软删除）
     *
     * @param id 用户 ID
     */
    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        user.setDeletedAt(Instant.now());
        userRepository.delete(user);
        logger.info("用户删除成功：{}", user.getEmail());
    }

    /**
     * 更新用户状态
     *
     * @param id     用户 ID
     * @param status 用户状态
     * @return 用户 DTO
     */
    @Override
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
     * 重置用户密码
     *
     * @param id          用户 ID
     * @param newPassword 新密码
     */
    @Override
    @Transactional
    public void resetPassword(UUID id, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);
        logger.info("用户密码重置成功：{}", user.getEmail());
    }
}
