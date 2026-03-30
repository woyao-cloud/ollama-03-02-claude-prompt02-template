package com.usermanagement.service.importer;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.web.dto.ImportResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

/**
 * UserImportService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserImportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImportValidator validator;

    @Mock
    private UserImporter importer;

    private UserImportService importService;

    @BeforeEach
    void setUp() {
        importService = new UserImportService(userRepository, passwordEncoder, validator, importer);
    }

    @Nested
    @DisplayName("Excel 导入测试")
    class ExcelImportTests {

        @Test
        @DisplayName("应该从 Excel 文件导入用户")
        void shouldImportUsersFromExcel() {
            // Given
            byte[] excelData = createTestExcelData();
            List<Map<String, String>> excelUsers = List.of(
                    Map.of("email", "user1@example.com", "firstName", "John",
                            "lastName", "Doe", "password", "Password123!")
            );

            given(importer.isExcelFormat(excelData)).willReturn(true);
            given(importer.readExcel(excelData)).willReturn(excelUsers);
            given(validator.validateEmail("user1@example.com")).willReturn(true);
            given(validator.validatePassword("Password123!")).willReturn(true);
            given(validator.isEmailDuplicate("user1@example.com")).willReturn(false);
            given(passwordEncoder.encode("Password123!")).willReturn("hashedPassword");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(java.util.UUID.randomUUID());
                return user;
            });

            // When
            ImportResult result = importService.importUsers(excelData);

            // Then
            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getSuccess()).isEqualTo(1);
            assertThat(result.getFailed()).isEqualTo(0);
            assertThat(result.isSuccess()).isTrue();

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("应该从 CSV 文件导入用户")
        void shouldImportUsersFromCsv() {
            // Given
            byte[] csvData = "email,firstName,lastName,password\nuser1@example.com,John,Doe,Password123!".getBytes();
            List<Map<String, String>> csvUsers = List.of(
                    Map.of("email", "user1@example.com", "firstName", "John",
                            "lastName", "Doe", "password", "Password123!")
            );

            given(importer.isExcelFormat(csvData)).willReturn(false);
            given(importer.isCsvFormat(csvData)).willReturn(true);
            given(importer.readCsv(csvData)).willReturn(csvUsers);
            given(validator.validateEmail("user1@example.com")).willReturn(true);
            given(validator.validatePassword("Password123!")).willReturn(true);
            given(validator.isEmailDuplicate("user1@example.com")).willReturn(false);
            given(passwordEncoder.encode("Password123!")).willReturn("hashedPassword");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(java.util.UUID.randomUUID());
                return user;
            });

            // When
            ImportResult result = importService.importUsers(csvData);

            // Then
            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getSuccess()).isEqualTo(1);
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("应该处理部分失败的导入")
        void shouldHandlePartialFailure() {
            // Given
            byte[] excelData = createTestExcelData();
            List<Map<String, String>> excelUsers = List.of(
                    Map.of("email", "user1@example.com", "firstName", "John",
                            "lastName", "Doe", "password", "Password123!"),
                    Map.of("email", "invalid-email", "firstName", "Jane",
                            "lastName", "Doe", "password", "Password456!")
            );

            given(importer.isExcelFormat(excelData)).willReturn(true);
            given(importer.readExcel(excelData)).willReturn(excelUsers);

            // 第一个用户有效
            given(validator.validateEmail("user1@example.com")).willReturn(true);
            given(validator.validatePassword("Password123!")).willReturn(true);
            given(validator.isEmailDuplicate("user1@example.com")).willReturn(false);
            given(passwordEncoder.encode("Password123!")).willReturn("hashedPassword");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(java.util.UUID.randomUUID());
                return user;
            });

            // 第二个用户邮箱无效
            given(validator.validateEmail("invalid-email")).willReturn(false);

            // When
            ImportResult result = importService.importUsers(excelData);

            // Then
            assertThat(result.getTotal()).isEqualTo(2);
            assertThat(result.getSuccess()).isEqualTo(1);
            assertThat(result.getFailed()).isEqualTo(1);
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
        }

        @Test
        @DisplayName("应该处理批量重复邮箱")
        void shouldHandleBatchDuplicateEmails() {
            // Given
            byte[] excelData = createTestExcelData();
            List<Map<String, String>> excelUsers = List.of(
                    Map.of("email", "user1@example.com", "firstName", "John",
                            "lastName", "Doe", "password", "Password123!"),
                    Map.of("email", "user1@example.com", "firstName", "Jane",
                            "lastName", "Smith", "password", "Password456!")
            );

            given(importer.isExcelFormat(excelData)).willReturn(true);
            given(importer.readExcel(excelData)).willReturn(excelUsers);
            given(validator.findDuplicateEmailsInBatch(Set.of("user1@example.com", "user1@example.com")))
                    .willReturn(Set.of("user1@example.com"));

            // When
            ImportResult result = importService.importUsers(excelData);

            // Then
            assertThat(result.getFailed()).isEqualTo(2);
            assertThat(result.getErrors()).anyMatch(e -> e.contains("重复"));
        }

        @Test
        @DisplayName("空文件时返回空结果")
        void shouldReturnEmptyResultForEmptyFile() {
            // Given
            byte[] emptyData = new byte[0];
            given(importer.isExcelFormat(emptyData)).willReturn(false);
            given(importer.isCsvFormat(emptyData)).willReturn(false);

            // When
            ImportResult result = importService.importUsers(emptyData);

            // Then
            assertThat(result.getTotal()).isEqualTo(0);
            assertThat(result.getSuccess()).isEqualTo(0);
            assertThat(result.getFailed()).isEqualTo(0);
        }

        @Test
        @DisplayName("不支持的文件格式抛出异常")
        void shouldThrowExceptionForUnsupportedFormat() {
            // Given
            byte[] unsupportedData = new byte[]{0x00, 0x01, 0x02};
            given(importer.isExcelFormat(unsupportedData)).willReturn(false);
            given(importer.isCsvFormat(unsupportedData)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> importService.importUsers(unsupportedData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("不支持的文件格式");
        }
    }

    @Nested
    @DisplayName("数据验证测试")
    class ValidationTests {

        @Test
        @DisplayName("应该验证缺失的必填字段")
        void shouldValidateMissingRequiredFields() {
            // Given
            byte[] excelData = createTestExcelData();
            List<Map<String, String>> excelUsers = List.of(
                    Map.of("email", "user1@example.com") // 缺少 firstName, lastName, password
            );

            given(importer.isExcelFormat(excelData)).willReturn(true);
            given(importer.readExcel(excelData)).willReturn(excelUsers);
            given(validator.validateRequiredFields(Map.of("email", "user1@example.com")))
                    .willReturn(Set.of("firstName", "lastName", "password"));

            // When
            ImportResult result = importService.importUsers(excelData);

            // Then
            assertThat(result.getFailed()).isEqualTo(1);
            assertThat(result.getErrors()).anyMatch(e -> e.contains("必填字段"));
        }

        @Test
        @DisplayName("应该验证邮箱格式")
        void shouldValidateEmailFormat() {
            // Given
            byte[] excelData = createTestExcelData();
            List<Map<String, String>> excelUsers = List.of(
                    Map.of("email", "invalid-email", "firstName", "John",
                            "lastName", "Doe", "password", "Password123!")
            );

            given(importer.isExcelFormat(excelData)).willReturn(true);
            given(importer.readExcel(excelData)).willReturn(excelUsers);
            given(validator.validateEmail("invalid-email")).willReturn(false);

            // When
            ImportResult result = importService.importUsers(excelData);

            // Then
            assertThat(result.getFailed()).isEqualTo(1);
            assertThat(result.getErrors()).anyMatch(e -> e.contains("邮箱格式"));
        }

        @Test
        @DisplayName("应该验证密码强度")
        void shouldValidatePasswordStrength() {
            // Given
            byte[] excelData = createTestExcelData();
            List<Map<String, String>> excelUsers = List.of(
                    Map.of("email", "user1@example.com", "firstName", "John",
                            "lastName", "Doe", "password", "weak")
            );

            given(importer.isExcelFormat(excelData)).willReturn(true);
            given(importer.readExcel(excelData)).willReturn(excelUsers);
            given(validator.validateEmail("user1@example.com")).willReturn(true);
            given(validator.validatePassword("weak")).willReturn(false);

            // When
            ImportResult result = importService.importUsers(excelData);

            // Then
            assertThat(result.getFailed()).isEqualTo(1);
            assertThat(result.getErrors()).anyMatch(e -> e.contains("密码强度"));
        }

        @Test
        @DisplayName("应该验证邮箱唯一性")
        void shouldValidateEmailUniqueness() {
            // Given
            byte[] excelData = createTestExcelData();
            List<Map<String, String>> excelUsers = List.of(
                    Map.of("email", "existing@example.com", "firstName", "John",
                            "lastName", "Doe", "password", "Password123!")
            );

            given(importer.isExcelFormat(excelData)).willReturn(true);
            given(importer.readExcel(excelData)).willReturn(excelUsers);
            given(validator.validateEmail("existing@example.com")).willReturn(true);
            given(validator.validatePassword("Password123!")).willReturn(true);
            given(validator.isEmailDuplicate("existing@example.com")).willReturn(true);

            // When
            ImportResult result = importService.importUsers(excelData);

            // Then
            assertThat(result.getFailed()).isEqualTo(1);
            assertThat(result.getErrors()).anyMatch(e -> e.contains("已被注册"));
        }
    }

    @Nested
    @DisplayName="用户创建测试"
    class UserCreationTests {

        @Test
        @DisplayName("应该创建用户并加密密码")
        void shouldCreateUserWithHashedPassword() {
            // Given
            byte[] excelData = createTestExcelData();
            List<Map<String, String>> excelUsers = List.of(
                    Map.of("email", "user1@example.com", "firstName", "John",
                            "lastName", "Doe", "password", "Password123!")
            );

            given(importer.isExcelFormat(excelData)).willReturn(true);
            given(importer.readExcel(excelData)).willReturn(excelUsers);
            given(validator.validateEmail("user1@example.com")).willReturn(true);
            given(validator.validatePassword("Password123!")).willReturn(true);
            given(validator.isEmailDuplicate("user1@example.com")).willReturn(false);
            given(passwordEncoder.encode("Password123!")).willReturn("hashedPassword");

            User savedUser = new User();
            savedUser.setId(java.util.UUID.randomUUID());
            savedUser.setEmail("user1@example.com");
            savedUser.setFirstName("John");
            savedUser.setLastName("Doe");
            savedUser.setPasswordHash("hashedPassword");
            savedUser.setStatus(UserStatus.PENDING);

            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // When
            ImportResult result = importService.importUsers(excelData);

            // Then
            assertThat(result.getSuccess()).isEqualTo(1);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getPasswordHash()).isEqualTo("hashedPassword");
            assertThat(capturedUser.getStatus()).isEqualTo(UserStatus.PENDING);
        }

        @Test
        @DisplayName("应该设置可选字段")
        void shouldSetOptionalFields() {
            // Given
            byte[] excelData = createTestExcelData();
            List<Map<String, String>> excelUsers = List.of(
                    Map.of("email", "user1@example.com", "firstName", "John",
                            "lastName", "Doe", "password", "Password123!",
                            "phone", "13800138000", "departmentId", "dept-001")
            );

            given(importer.isExcelFormat(excelData)).willReturn(true);
            given(importer.readExcel(excelData)).willReturn(excelUsers);
            given(validator.validateEmail("user1@example.com")).willReturn(true);
            given(validator.validatePassword("Password123!")).willReturn(true);
            given(validator.validatePhone("13800138000")).willReturn(true);
            given(validator.isEmailDuplicate("user1@example.com")).willReturn(false);
            given(passwordEncoder.encode("Password123!")).willReturn("hashedPassword");

            User savedUser = new User();
            savedUser.setId(java.util.UUID.randomUUID());
            savedUser.setEmail("user1@example.com");
            savedUser.setFirstName("John");
            savedUser.setLastName("Doe");
            savedUser.setPasswordHash("hashedPassword");
            savedUser.setPhone("13800138000");

            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // When
            importService.importUsers(excelData);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getPhone()).isEqualTo("13800138000");
        }

        @Test
        @DisplayName("应该跳过无效手机号")
        void shouldSkipInvalidPhone() {
            // Given
            byte[] excelData = createTestExcelData();
            List<Map<String, String>> excelUsers = List.of(
                    Map.of("email", "user1@example.com", "firstName", "John",
                            "lastName", "Doe", "password", "Password123!",
                            "phone", "invalid-phone")
            );

            given(importer.isExcelFormat(excelData)).willReturn(true);
            given(importer.readExcel(excelData)).willReturn(excelUsers);
            given(validator.validateEmail("user1@example.com")).willReturn(true);
            given(validator.validatePassword("Password123!")).willReturn(true);
            given(validator.validatePhone("invalid-phone")).willReturn(false);
            given(validator.isEmailDuplicate("user1@example.com")).willReturn(false);
            given(passwordEncoder.encode("Password123!")).willReturn("hashedPassword");

            User savedUser = new User();
            savedUser.setId(java.util.UUID.randomUUID());
            savedUser.setEmail("user1@example.com");

            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // When
            ImportResult result = importService.importUsers(excelData);

            // Then
            assertThat(result.getFailed()).isEqualTo(1);
            assertThat(result.getErrors()).anyMatch(e -> e.contains("手机号格式"));
        }
    }

    // 辅助方法
    private byte[] createTestExcelData() {
        // 模拟 Excel 文件头 (PK)
        return new byte[]{0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00};
    }
}
