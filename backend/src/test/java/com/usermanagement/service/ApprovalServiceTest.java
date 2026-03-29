package com.usermanagement.service;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * ApprovalService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private UserRepository userRepository;

    private ApprovalService approvalService;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_EMAIL = "pending@example.com";

    @BeforeEach
    void setUp() {
        approvalService = new ApprovalService(userRepository);
    }

    @Test
    @DisplayName("审批通过 - 用户状态变为 ACTIVE")
    void shouldApproveUserSuccessfully() {
        // Given
        User pendingUser = createUser(TEST_USER_ID, UserStatus.PENDING, false);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(pendingUser));

        // When
        approvalService.approveUser(TEST_USER_ID);

        // Then
        assertThat(pendingUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(pendingUser.getEmailVerified()).isTrue();
        verify(userRepository).save(pendingUser);
    }

    @Test
    @DisplayName("审批通过失败 - 用户不存在")
    void shouldThrowException_whenUserNotFoundForApproval() {
        // Given
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> approvalService.approveUser(TEST_USER_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户不存在");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("审批通过失败 - 用户已激活")
    void shouldThrowException_whenUserAlreadyActive() {
        // Given
        User activeUser = createUser(TEST_USER_ID, UserStatus.ACTIVE, true);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(activeUser));

        // When & Then
        assertThatThrownBy(() -> approvalService.approveUser(TEST_USER_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户已激活，无需审批");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("审批拒绝 - 用户状态变为 INACTIVE")
    void shouldRejectUserSuccessfully() {
        // Given
        User pendingUser = createUser(TEST_USER_ID, UserStatus.PENDING, false);
        String rejectionReason = "资料不完整";
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(pendingUser));

        // When
        approvalService.rejectUser(TEST_USER_ID, rejectionReason);

        // Then
        assertThat(pendingUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).save(pendingUser);
    }

    @Test
    @DisplayName("审批拒绝失败 - 用户不存在")
    void shouldThrowException_whenUserNotFoundForRejection() {
        // Given
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());
        String rejectionReason = "资料不完整";

        // When & Then
        assertThatThrownBy(() -> approvalService.rejectUser(TEST_USER_ID, rejectionReason))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("用户不存在");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("获取待审批用户列表")
    void shouldGetPendingUsersSuccessfully() {
        // Given
        User user1 = createUser(TEST_USER_ID, UserStatus.PENDING, false);
        User user2 = createUser(UUID.randomUUID(), UserStatus.PENDING, false);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> pendingPage = new PageImpl<>(Arrays.asList(user1, user2), pageable, 2);

        given(userRepository.findByStatus(UserStatus.PENDING, pageable)).willReturn(pendingPage);

        // When
        Page<User> result = approvalService.getPendingUsers(pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        then(userRepository).should().findByStatus(UserStatus.PENDING, pageable);
    }

    @Test
    @DisplayName("获取待审批用户详情")
    void shouldGetPendingUserDetailsSuccessfully() {
        // Given
        User pendingUser = createUser(TEST_USER_ID, UserStatus.PENDING, false);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(pendingUser));

        // When
        Optional<User> result = approvalService.getPendingUserById(TEST_USER_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(UserStatus.PENDING);
    }

    @Test
    @DisplayName("获取待审批用户详情 - 用户不存在")
    void shouldReturnEmpty_whenUserNotFound() {
        // Given
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        // When
        Optional<User> result = approvalService.getPendingUserById(TEST_USER_ID);

        // Then
        assertThat(result).isEmpty();
    }

    private User createUser(UUID id, UserStatus status, Boolean emailVerified) {
        User user = new User();
        user.setId(id);
        user.setEmail(TEST_EMAIL);
        user.setPasswordHash("hashedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setStatus(status);
        user.setEmailVerified(emailVerified);
        user.setCreatedAt(Instant.now());
        return user;
    }
}
