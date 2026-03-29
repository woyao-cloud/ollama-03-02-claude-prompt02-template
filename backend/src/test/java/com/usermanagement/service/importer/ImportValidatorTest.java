package com.usermanagement.service.importer;

import com.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * ImportValidator 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ImportValidatorTest {

    @Mock
    private UserRepository userRepository;

    private ImportValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ImportValidator(userRepository);
    }

    @Nested
    @DisplayName("邮箱验证测试")
    class EmailValidationTests {

        @Test
        @DisplayName("应该验证有效邮箱格式")
        void shouldValidateValidEmail() {
            // Given
            String email = "test@example.com";

            // When
            boolean isValid = validator.validateEmail(email);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("应该拒绝无效邮箱格式 - 缺少@")
        void shouldRejectInvalidEmailWithoutAt() {
            // Given
            String email = "testexample.com";

            // When
            boolean isValid = validator.validateEmail(email);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("应该拒绝无效邮箱格式 - 缺少域名")
        void shouldRejectInvalidEmailWithoutDomain() {
            // Given
            String email = "test@";

            // When
            boolean isValid = validator.validateEmail(email);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("应该拒绝空邮箱")
        void shouldRejectEmptyEmail() {
            // Given
            String email = "";

            // When
            boolean isValid = validator.validateEmail(email);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("应该拒绝 null 邮箱")
        void shouldRejectNullEmail() {
            // Given
            String email = null;

            // When
            boolean isValid = validator.validateEmail(email);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("应该拒绝空白邮箱")
        void shouldRejectBlankEmail() {
            // Given
            String email = "   ";

            // When
            boolean isValid = validator.validateEmail(email);

            // Then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("必填字段验证测试")
    class RequiredFieldValidationTests {

        @Test
        @DisplayName("应该验证完整的用户数据")
        void shouldValidateCompleteUserData() {
            // Given
            Map<String, String> userData = Map.of(
                    "email", "test@example.com",
                    "firstName", "John",
                    "lastName", "Doe",
                    "password", "Password123!"
            );

            // When
            Set<String> errors = validator.validateRequiredFields(userData);

            // Then
            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("应该报告缺失的必填字段")
        void shouldReportMissingRequiredFields() {
            // Given
            Map<String, String> userData = Map.of(
                    "email", "test@example.com"
                    // 缺少 firstName, lastName, password
            );

            // When
            Set<String> errors = validator.validateRequiredFields(userData);

            // Then
            assertThat(errors).contains("firstName", "lastName", "password");
        }

        @Test
        @DisplayName("应该报告空白值的必填字段")
        void shouldReportBlankRequiredFields() {
            // Given
            Map<String, String> userData = Map.of(
                    "email", "test@example.com",
                    "firstName", "   ",
                    "lastName", "Doe",
                    "password", "Password123!"
            );

            // When
            Set<String> errors = validator.validateRequiredFields(userData);

            // Then
            assertThat(errors).contains("firstName");
        }
    }

    @Nested
    @DisplayName="重复邮箱检查测试"
    class DuplicateEmailCheckTests {

        @Test
        @DisplayName("应该检测已存在的邮箱")
        void shouldDetectExistingEmail() {
            // Given
            String email = "existing@example.com";
            given(userRepository.existsByEmail(email)).willReturn(true);

            // When
            boolean isDuplicate = validator.isEmailDuplicate(email);

            // Then
            assertThat(isDuplicate).isTrue();
        }

        @Test
        @DisplayName("应该接受不存在的邮箱")
        void shouldAcceptNonExistingEmail() {
            // Given
            String email = "new@example.com";
            given(userRepository.existsByEmail(email)).willReturn(false);

            // When
            boolean isDuplicate = validator.isEmailDuplicate(email);

            // Then
            assertThat(isDuplicate).isFalse();
        }
    }

    @Nested
    @DisplayName("密码验证测试")
    class PasswordValidationTests {

        @Test
        @DisplayName("应该验证有效密码")
        void shouldValidateValidPassword() {
            // Given
            String password = "Password123!";

            // When
            boolean isValid = validator.validatePassword(password);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("应该拒绝太短的密码")
        void shouldRejectTooShortPassword() {
            // Given
            String password = "Short1!";

            // When
            boolean isValid = validator.validatePassword(password);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("应该拒绝没有大写字母的密码")
        void shouldRejectPasswordWithoutUppercase() {
            // Given
            String password = "password123!";

            // When
            boolean isValid = validator.validatePassword(password);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("应该拒绝没有小写字母的密码")
        void shouldRejectPasswordWithoutLowercase() {
            // Given
            String password = "PASSWORD123!";

            // When
            boolean isValid = validator.validatePassword(password);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("应该拒绝没有数字的密码")
        void shouldRejectPasswordWithoutDigit() {
            // Given
            String password = "Password!@#";

            // When
            boolean isValid = validator.validatePassword(password);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("应该拒绝没有特殊字符的密码")
        void shouldRejectPasswordWithoutSpecialChar() {
            // Given
            String password = "Password123";

            // When
            boolean isValid = validator.validatePassword(password);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("应该拒绝空密码")
        void shouldRejectEmptyPassword() {
            // Given
            String password = "";

            // When
            boolean isValid = validator.validatePassword(password);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("应该拒绝 null 密码")
        void shouldRejectNullPassword() {
            // Given
            String password = null;

            // When
            boolean isValid = validator.validatePassword(password);

            // Then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("批量邮箱重复检查测试")
    class BatchDuplicateCheckTests {

        @Test
        @DisplayName("应该检测批量邮箱中的重复")
        void shouldDetectDuplicatesInBatch() {
            // Given
            Set<String> emails = Set.of(
                    "user1@example.com",
                    "user2@example.com",
                    "user1@example.com"  // 重复
            );

            // When
            Set<String> duplicates = validator.findDuplicateEmailsInBatch(emails);

            // Then
            assertThat(duplicates).contains("user1@example.com");
        }

        @Test
        @DisplayName("没有重复时返回空集合")
        void shouldReturnEmptyWhenNoDuplicates() {
            // Given
            Set<String> emails = Set.of(
                    "user1@example.com",
                    "user2@example.com",
                    "user3@example.com"
            );

            // When
            Set<String> duplicates = validator.findDuplicateEmailsInBatch(emails);

            // Then
            assertThat(duplicates).isEmpty();
        }
    }

    @Nested
    @DisplayName("手机号验证测试")
    class PhoneValidationTests {

        @Test
        @DisplayName("应该验证有效手机号")
        void shouldValidateValidPhone() {
            // Given
            String phone = "13800138000";

            // When
            boolean isValid = validator.validatePhone(phone);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("应该接受空手机号（可选字段）")
        void shouldAcceptEmptyPhone() {
            // Given
            String phone = "";

            // When
            boolean isValid = validator.validatePhone(phone);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("应该拒绝格式错误的手机号")
        void shouldRejectInvalidPhone() {
            // Given
            String phone = "invalid-phone";

            // When
            boolean isValid = validator.validatePhone(phone);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("应该拒绝过长的手机号")
        void shouldRejectTooLongPhone() {
            // Given
            String phone = "12345678901234567890123";

            // When
            boolean isValid = validator.validatePhone(phone);

            // Then
            assertThat(isValid).isFalse();
        }
    }
}
