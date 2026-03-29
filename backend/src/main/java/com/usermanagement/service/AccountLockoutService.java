package com.usermanagement.service;

import com.usermanagement.config.AppProperties;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * 账户锁定服务
 * <p>
 * 处理登录失败次数记录和账户锁定逻辑
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class AccountLockoutService {

    private final UserRepository userRepository;
    private final AppProperties appProperties;

    public AccountLockoutService(UserRepository userRepository, AppProperties appProperties) {
        this.userRepository = userRepository;
        this.appProperties = appProperties;
    }

    /**
     * 记录登录失败
     * <p>
     * 增加失败次数，达到阈值后锁定账户
     *
     * @param email 用户邮箱
     * @throws LockedException 当账户被锁定时抛出
     */
    @Transactional
    public void recordLoginFailure(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();
        int failedAttempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
        failedAttempts++;
        user.setFailedLoginAttempts(failedAttempts);

        int threshold = appProperties.getAccount().getLockout().getThreshold();
        if (failedAttempts >= threshold) {
            lockUser(user);
            throw new LockedException("账户已被锁定，请稍后再试");
        }

        userRepository.save(user);
    }

    /**
     * 检查账户是否被锁定
     * <p>
     * 如果锁定已过期，自动解锁并重置失败次数
     *
     * @param email 用户邮箱
     * @return true 如果账户被锁定
     */
    @Transactional
    public boolean isAccountLocked(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        // 检查锁定是否过期
        if (user.getStatus() == UserStatus.LOCKED && user.getLockedUntil() != null) {
            if (Instant.now().isAfter(user.getLockedUntil())) {
                // 锁定已过期，自动解锁
                user.setStatus(UserStatus.ACTIVE);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * 重置失败次数
     * <p>
     * 登录成功后调用，清除失败记录并解锁账户
     *
     * @param email 用户邮箱
     */
    @Transactional
    public void resetFailedAttempts(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    /**
     * 锁定用户账户
     */
    private void lockUser(User user) {
        user.setStatus(UserStatus.LOCKED);
        int durationSeconds = appProperties.getAccount().getLockout().getDuration();
        user.setLockedUntil(Instant.now().plusSeconds(durationSeconds));
        userRepository.save(user);
    }
}
