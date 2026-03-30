package com.usermanagement.service.importer;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.web.dto.ImportResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 用户批量导入服务
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class UserImportService {

    private static final Logger logger = LoggerFactory.getLogger(UserImportService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImportValidator validator;
    private final UserImporter importer;

    public UserImportService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ImportValidator validator,
            UserImporter importer
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.importer = importer;
    }

    /**
     * 批量导入用户
     *
     * @param data 文件数据（Excel 或 CSV）
     * @return 导入结果
     */
    @Transactional
    public ImportResult importUsers(byte[] data) {
        List<Map<String, String>> users;

        // 检测文件格式并读取
        if (importer.isExcelFormat(data)) {
            users = importer.readExcel(data);
        } else if (importer.isCsvFormat(data)) {
            users = importer.readCsv(data);
        } else {
            throw new IllegalArgumentException("不支持的文件格式，请上传 Excel (.xlsx) 或 CSV 文件");
        }

        if (users.isEmpty()) {
            return new ImportResult(0, 0, 0, List.of("文件中没有数据"));
        }

        int total = users.size();
        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        // 检查批量重复
        Set<String> allEmails = new HashSet<>();
        for (Map<String, String> userData : users) {
            String email = userData.get("email");
            if (email != null && !email.isBlank()) {
                allEmails.add(email.trim().toLowerCase());
            }
        }
        Set<String> duplicateEmails = validator.findDuplicateEmailsInBatch(allEmails);

        // 逐个处理用户
        for (int i = 0; i < users.size(); i++) {
            Map<String, String> userData = users.get(i);
            int rowNum = i + 2; // Excel 行号（从 1 开始，第 0 行是标题）

            try {
                ImportValidationResult validationResult = validateUserData(userData, duplicateEmails, rowNum);

                if (!validationResult.isValid()) {
                    failed++;
                    errors.addAll(validationResult.getErrors());
                    continue;
                }

                // 创建用户
                User user = createUserFromData(userData);
                user.setPasswordHash(passwordEncoder.encode(userData.get("password")));
                user.setStatus(UserStatus.PENDING);
                user.setEmailVerified(false);

                userRepository.save(user);
                success++;
                logger.info("用户导入成功：{}", user.getEmail());

            } catch (Exception e) {
                failed++;
                errors.add("第 " + rowNum + " 行：导入失败 - " + e.getMessage());
                logger.error("用户导入失败，行号：{}", rowNum, e);
            }
        }

        ImportResult result = new ImportResult(total, success, failed, errors);
        logger.info("批量导入完成：总数={}, 成功={}, 失败={}", total, success, failed);

        return result;
    }

    /**
     * 验证用户数据
     */
    private ImportValidationResult validateUserData(
            Map<String, String> userData,
            Set<String> duplicateEmails,
            int rowNum
    ) {
        ImportValidationResult result = new ImportValidationResult();

        String email = userData.get("email");
        if (email == null || email.isBlank()) {
            result.addError("第 " + rowNum + " 行：邮箱不能为空");
            return result;
        }

        email = email.trim();

        // 检查必填字段
        Set<String> missingFields = validator.validateRequiredFields(userData);
        if (!missingFields.isEmpty()) {
            result.addError("第 " + rowNum + " 行：缺失必填字段：" + String.join(", ", missingFields));
        }

        // 验证邮箱格式
        if (!validator.validateEmail(email)) {
            result.addError("第 " + rowNum + " 行：邮箱格式不正确：" + email);
        }

        // 检查批量重复
        if (duplicateEmails.contains(email.toLowerCase())) {
            result.addError("第 " + rowNum + " 行：邮箱重复：" + email);
        } else if (validator.isEmailDuplicate(email)) {
            // 检查数据库中是否已存在
            result.addError("第 " + rowNum + " 行：邮箱已被注册：" + email);
        }

        // 验证密码强度
        String password = userData.get("password");
        if (password != null && !password.isBlank() && !validator.validatePassword(password)) {
            result.addError("第 " + rowNum + " 行：密码强度不足（需要 8 位以上，包含大小写字母、数字和特殊字符）");
        }

        // 验证手机号（可选）
        String phone = userData.get("phone");
        if (phone != null && !phone.isBlank() && !validator.validatePhone(phone)) {
            result.addError("第 " + rowNum + " 行：手机号格式不正确：" + phone);
        }

        return result;
    }

    /**
     * 从数据创建用户实体
     */
    private User createUserFromData(Map<String, String> userData) {
        User user = new User();
        user.setEmail(userData.get("email").trim());
        user.setFirstName(userData.get("firstName").trim());
        user.setLastName(userData.get("lastName").trim());

        String phone = userData.get("phone");
        if (phone != null && !phone.isBlank()) {
            user.setPhone(phone.trim());
        }

        String departmentId = userData.get("departmentId");
        if (departmentId != null && !departmentId.isBlank()) {
            try {
                user.setDepartmentId(UUID.fromString(departmentId.trim()));
            } catch (IllegalArgumentException e) {
                // 如果不是 UUID 格式，保持原样或记录日志
                logger.warn("部门 ID 格式不正确：{}", departmentId);
            }
        }

        return user;
    }

    /**
     * 内部类：导入验证结果
     */
    private static class ImportValidationResult {
        private final List<String> errors = new ArrayList<>();

        public boolean isValid() {
            return errors.isEmpty();
        }

        public void addError(String error) {
            errors.add(error);
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
