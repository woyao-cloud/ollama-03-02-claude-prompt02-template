package com.usermanagement.service;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 审批服务 - 处理用户注册审批流程
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class ApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalService.class);

    private final UserRepository userRepository;

    public ApprovalService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 审批通过用户
     *
     * @param userId 用户 ID
     */
    @Transactional
    public void approveUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalArgumentException("用户已激活，无需审批");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        userRepository.save(user);

        logger.info("用户审批通过：{}", user.getEmail());
    }

    /**
     * 审批拒绝用户
     *
     * @param userId 用户 ID
     * @param reason 拒绝原因
     */
    @Transactional
    public void rejectUser(UUID userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        logger.info("用户审批拒绝：{}, 原因：{}", user.getEmail(), reason);
    }

    /**
     * 获取待审批用户列表
     *
     * @param pageable 分页参数
     * @return 待审批用户分页列表
     */
    @Transactional(readOnly = true)
    public Page<User> getPendingUsers(Pageable pageable) {
        return userRepository.findByStatus(UserStatus.PENDING, pageable);
    }

    /**
     * 根据 ID 获取待审批用户
     *
     * @param userId 用户 ID
     * @return 用户 Optional
     */
    @Transactional(readOnly = true)
    public Optional<User> getPendingUserById(UUID userId) {
        return userRepository.findById(userId);
    }
}
