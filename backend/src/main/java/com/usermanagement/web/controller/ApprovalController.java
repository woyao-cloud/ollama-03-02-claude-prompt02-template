package com.usermanagement.web.controller;

import com.usermanagement.domain.User;
import com.usermanagement.service.ApprovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * 审批控制器 - 处理用户注册审批流程 API
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/approvals")
@Secured("ROLE_ADMIN")
public class ApprovalController {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalController.class);

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    /**
     * 获取待审批用户列表
     *
     * GET /api/approvals/pending
     *
     * @param pageable 分页参数
     * @return 待审批用户分页列表
     */
    @GetMapping("/pending")
    public ResponseEntity<Page<User>> getPendingUsers(Pageable pageable) {
        logger.info("获取待审批用户列表");
        Page<User> pendingUsers = approvalService.getPendingUsers(pageable);
        return ResponseEntity.ok(pendingUsers);
    }

    /**
     * 获取待审批用户详情
     *
     * GET /api/approvals/pending/{userId}
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    @GetMapping("/pending/{userId}")
    public ResponseEntity<User> getPendingUser(@PathVariable UUID userId) {
        logger.info("获取待审批用户详情：{}", userId);
        User user = approvalService.getPendingUserById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return ResponseEntity.ok(user);
    }

    /**
     * 审批通过用户
     *
     * POST /api/approvals/{userId}/approve
     *
     * @param userId 用户 ID
     * @return 成功响应
     */
    @PostMapping("/{userId}/approve")
    public ResponseEntity<Map<String, String>> approveUser(@PathVariable UUID userId) {
        logger.info("审批通过用户：{}", userId);
        approvalService.approveUser(userId);
        return ResponseEntity.ok(Map.of("message", "用户审批通过"));
    }

    /**
     * 审批拒绝用户
     *
     * POST /api/approvals/{userId}/reject
     *
     * @param userId  用户 ID
     * @param request 拒绝原因
     * @return 成功响应
     */
    @PostMapping("/{userId}/reject")
    public ResponseEntity<Map<String, String>> rejectUser(
            @PathVariable UUID userId,
            @RequestBody(required = false) Map<String, String> request) {
        String reason = request != null ? request.getOrDefault("reason", "未提供原因") : "未提供原因";
        logger.info("审批拒绝用户：{}, 原因：{}", userId, reason);
        approvalService.rejectUser(userId, reason);
        return ResponseEntity.ok(Map.of("message", "用户审批拒绝"));
    }
}
