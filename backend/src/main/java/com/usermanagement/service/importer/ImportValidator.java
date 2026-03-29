package com.usermanagement.service.importer;

import com.usermanagement.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 导入数据验证器
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class ImportValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;

    public ImportValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 验证邮箱格式
     *
     * @param email 邮箱地址
     * @return 是否有效
     */
    public boolean validateEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * 验证密码强度
     *
     * @param password 密码
     * @return 是否有效
     */
    public boolean validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        // 必须包含大写字母
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        // 必须包含小写字母
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        // 必须包含数字
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        // 必须包含特殊字符
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            return false;
        }
        return true;
    }

    /**
     * 验证手机号格式
     *
     * @param phone 手机号
     * @return 是否有效
     */
    public boolean validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return true; // 可选字段
        }
        if (phone.length() > 20) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证必填字段
     *
     * @param userData 用户数据
     * @return 缺失的字段集合
     */
    public Set<String> validateRequiredFields(Map<String, String> userData) {
        Set<String> missingFields = new HashSet<>();

        if (isBlank(userData.get("email"))) {
            missingFields.add("email");
        }
        if (isBlank(userData.get("firstName"))) {
            missingFields.add("firstName");
        }
        if (isBlank(userData.get("lastName"))) {
            missingFields.add("lastName");
        }
        if (isBlank(userData.get("password"))) {
            missingFields.add("password");
        }

        return missingFields;
    }

    /**
     * 检查邮箱是否已存在
     *
     * @param email 邮箱地址
     * @return 是否重复
     */
    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 在批量导入中查找重复邮箱
     *
     * @param emails 邮箱集合
     * @return 重复的邮箱集合
     */
    public Set<String> findDuplicateEmailsInBatch(Set<String> emails) {
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();

        for (String email : emails) {
            if (!seen.add(email)) {
                duplicates.add(email);
            }
        }

        return duplicates;
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}
