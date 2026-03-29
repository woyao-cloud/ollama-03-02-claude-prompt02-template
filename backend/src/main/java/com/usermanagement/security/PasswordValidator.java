package com.usermanagement.security;

import com.usermanagement.config.AppProperties;
import org.springframework.stereotype.Component;

/**
 * 密码策略验证器
 * <p>
 * 根据配置验证密码复杂度要求
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class PasswordValidator {

    private final AppProperties appProperties;

    public PasswordValidator(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * 验证密码是否符合策略要求
     *
     * @param password 待验证的密码
     * @return true 如果密码符合所有策略要求
     * @throws IllegalArgumentException 如果密码不符合策略要求
     */
    public boolean validate(String password) {
        AppProperties.Password passwordPolicy = appProperties.getPassword();

        // 验证长度
        int minLength = passwordPolicy.getMinLength();
        if (password == null || password.length() < minLength) {
            throw new IllegalArgumentException("密码长度不能少于 " + minLength + " 个字符");
        }

        // 验证大写字母
        if (passwordPolicy.getRequireUppercase() && !containsUppercase(password)) {
            throw new IllegalArgumentException("密码必须包含至少一个大写字母");
        }

        // 验证小写字母
        if (passwordPolicy.getRequireLowercase() && !containsLowercase(password)) {
            throw new IllegalArgumentException("密码必须包含至少一个小写字母");
        }

        // 验证数字
        if (passwordPolicy.getRequireDigit() && !containsDigit(password)) {
            throw new IllegalArgumentException("密码必须包含至少一个数字");
        }

        // 验证特殊字符
        if (passwordPolicy.getRequireSpecial() && !containsSpecialChar(password)) {
            throw new IllegalArgumentException("密码必须包含至少一个特殊字符");
        }

        return true;
    }

    /**
     * 检查是否包含大写字母
     */
    private boolean containsUppercase(String password) {
        return password.chars().anyMatch(Character::isUpperCase);
    }

    /**
     * 检查是否包含小写字母
     */
    private boolean containsLowercase(String password) {
        return password.chars().anyMatch(Character::isLowerCase);
    }

    /**
     * 检查是否包含数字
     */
    private boolean containsDigit(String password) {
        return password.chars().anyMatch(Character::isDigit);
    }

    /**
     * 检查是否包含特殊字符
     */
    private boolean containsSpecialChar(String password) {
        return password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
    }
}
