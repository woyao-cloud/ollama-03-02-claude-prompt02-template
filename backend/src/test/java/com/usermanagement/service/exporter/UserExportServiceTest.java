package com.usermanagement.service.exporter;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * UserExportService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserExportServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new UserExportService(userRepository);
    }

    @Nested
    @DisplayName("Excel 导出测试")
    class ExcelExportTests {

        @Test
        @DisplayName("应该导出用户列表为 Excel")
        void shouldExportUsersToExcel() {
            // Given
            List<User> users = List.of(
                    createUser("user1@example.com", "John", "Doe"),
                    createUser("user2@example.com", "Jane", "Smith")
            );
            Page<User> userPage = new PageImpl<>(users);
            Pageable pageable = PageRequest.of(0, 100);

            given(userRepository.findAll(pageable)).willReturn(userPage);

            // When
            byte[] excelData = exportService.exportUsers(pageable, null, null, null);

            // Then
            assertThat(excelData).isNotNull();
            assertThat(excelData.length).isGreaterThan(0);

            // 验证 Excel 内容
            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
                Sheet sheet = workbook.getSheetAt(0);
                assertThat(sheet.getLastRowNum()).isEqualTo(2); // 标题行 + 2 数据行

                // 验证标题行
                Row headerRow = sheet.getRow(0);
                assertThat(getCellValue(headerRow, 0)).isEqualTo("邮箱");
                assertThat(getCellValue(headerRow, 1)).isEqualTo("名");
                assertThat(getCellValue(headerRow, 2)).isEqualTo("姓");
                assertThat(getCellValue(headerRow, 3)).isEqualTo("手机号");
                assertThat(getCellValue(headerRow, 4)).isEqualTo("状态");

                // 验证数据行
                Row row1 = sheet.getRow(1);
                assertThat(getCellValue(row1, 0)).isEqualTo("user1@example.com");
                assertThat(getCellValue(row1, 1)).isEqualTo("John");
                assertThat(getCellValue(row1, 2)).isEqualTo("Doe");

                Row row2 = sheet.getRow(2);
                assertThat(getCellValue(row2, 0)).isEqualTo("user2@example.com");
            }
        }

        @Test
        @DisplayName("应该导出空列表时创建有效 Excel")
        void shouldExportEmptyList() {
            // Given
            Page<User> emptyPage = new PageImpl<>(List.of());
            Pageable pageable = PageRequest.of(0, 100);

            given(userRepository.findAll(pageable)).willReturn(emptyPage);

            // When
            byte[] excelData = exportService.exportUsers(pageable, null, null, null);

            // Then
            assertThat(excelData).isNotNull();
            assertThat(excelData.length).isGreaterThan(0);

            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
                Sheet sheet = workbook.getSheetAt(0);
                assertThat(sheet.getSheetName()).isEqualTo("Users");
            }
        }

        @Test
        @DisplayName("应该按部门筛选导出")
        void shouldExportFilteredByDepartment() {
            // Given
            UUID departmentId = UUID.randomUUID();
            List<User> users = List.of(
                    createUserWithDepartment("user1@example.com", "John", "Doe", departmentId)
            );
            Page<User> userPage = new PageImpl<>(users);
            Pageable pageable = PageRequest.of(0, 100);

            given(userRepository.findByDepartmentId(departmentId, pageable)).willReturn(userPage);

            // When
            byte[] excelData = exportService.exportUsers(pageable, null, departmentId, null);

            // Then
            assertThat(excelData).isNotNull();

            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
                Sheet sheet = workbook.getSheetAt(0);
                assertThat(sheet.getLastRowNum()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("应该按状态筛选导出")
        void shouldExportFilteredByStatus() {
            // Given
            List<User> activeUsers = List.of(
                    createUserWithStatus("user1@example.com", "John", "Doe", UserStatus.ACTIVE)
            );
            Page<User> userPage = new PageImpl<>(activeUsers);
            Pageable pageable = PageRequest.of(0, 100);

            given(userRepository.findByStatus(UserStatus.ACTIVE, pageable)).willReturn(userPage);

            // When
            byte[] excelData = exportService.exportUsers(pageable, null, null, UserStatus.ACTIVE);

            // Then
            assertThat(excelData).isNotNull();

            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
                Sheet sheet = workbook.getSheetAt(0);
                Row dataRow = sheet.getRow(1);
                assertThat(getCellValue(dataRow, 4)).isEqualTo("ACTIVE");
            }
        }

        @Test
        @DisplayName("应该按部门和状态筛选导出")
        void shouldExportFilteredByDepartmentAndStatus() {
            // Given
            UUID departmentId = UUID.randomUUID();
            List<User> users = List.of(
                    createUserWithDepartmentAndStatus("user1@example.com", "John", "Doe", departmentId, UserStatus.ACTIVE)
            );
            Page<User> userPage = new PageImpl<>(users);
            Pageable pageable = PageRequest.of(0, 100);

            given(userRepository.findByDepartmentIdAndStatus(departmentId, UserStatus.ACTIVE, pageable)).willReturn(userPage);

            // When
            byte[] excelData = exportService.exportUsers(pageable, null, departmentId, UserStatus.ACTIVE);

            // Then
            assertThat(excelData).isNotNull();
        }

        @Test
        @DisplayName("应该包含所有用户字段")
        void shouldExportAllUserFields() {
            // Given
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("test@example.com");
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setPhone("13800138000");
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerified(true);
            user.setDepartmentId(UUID.randomUUID());

            Page<User> userPage = new PageImpl<>(List.of(user));
            Pageable pageable = PageRequest.of(0, 100);

            given(userRepository.findAll(pageable)).willReturn(userPage);

            // When
            byte[] excelData = exportService.exportUsers(pageable, null, null, null);

            // Then
            assertThat(excelData).isNotNull();

            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);

                // 验证所有列
                assertThat(headerRow.getLastCellNum()).isGreaterThanOrEqualTo(5);
            }
        }

        @Test
        @DisplayName("应该正确处理中文姓名")
        void shouldHandleChineseNames() {
            // Given
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("chinese@example.com");
            user.setFirstName("中");
            user.setLastName("文");
            user.setStatus(UserStatus.ACTIVE);

            Page<User> userPage = new PageImpl<>(List.of(user));
            Pageable pageable = PageRequest.of(0, 100);

            given(userRepository.findAll(pageable)).willReturn(userPage);

            // When
            byte[] excelData = exportService.exportUsers(pageable, null, null, null);

            // Then
            assertThat(excelData).isNotNull();

            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
                Sheet sheet = workbook.getSheetAt(0);
                Row dataRow = sheet.getRow(1);
                assertThat(getCellValue(dataRow, 1)).isEqualTo("中");
                assertThat(getCellValue(dataRow, 2)).isEqualTo("文");
            }
        }

        @Test
        @DisplayName("应该正确处理 null 字段")
        void shouldHandleNullFields() {
            // Given
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("nophone@example.com");
            user.setFirstName("No");
            user.setLastName("Phone");
            user.setPhone(null);
            user.setStatus(UserStatus.ACTIVE);

            Page<User> userPage = new PageImpl<>(List.of(user));
            Pageable pageable = PageRequest.of(0, 100);

            given(userRepository.findAll(pageable)).willReturn(userPage);

            // When
            byte[] excelData = exportService.exportUsers(pageable, null, null, null);

            // Then
            assertThat(excelData).isNotNull();

            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
                Sheet sheet = workbook.getSheetAt(0);
                // 不应抛出异常
                assertThat(sheet.getLastRowNum()).isEqualTo(1);
            }
        }
    }

    @Nested
    @DisplayName("导出性能测试")
    class PerformanceTests {

        @Test
        @DisplayName("应该支持大批量导出")
        void shouldSupportLargeBatchExport() {
            // Given
            List<User> largeUserList = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                largeUserList.add(createUser("user" + i + "@example.com", "First" + i, "Last" + i));
            }
            Page<User> userPage = new PageImpl<>(largeUserList);
            Pageable pageable = PageRequest.of(0, 100);

            given(userRepository.findAll(pageable)).willReturn(userPage);

            // When
            byte[] excelData = exportService.exportUsers(pageable, null, null, null);

            // Then
            assertThat(excelData).isNotNull();
            assertThat(excelData.length).isGreaterThan(0);
        }
    }

    // 辅助方法
    private User createUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone("13800138000");
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private User createUserWithDepartment(String email, String firstName, String lastName, UUID departmentId) {
        User user = createUser(email, firstName, lastName);
        user.setDepartmentId(departmentId);
        return user;
    }

    private User createUserWithStatus(String email, String firstName, String lastName, UserStatus status) {
        User user = createUser(email, firstName, lastName);
        user.setStatus(status);
        return user;
    }

    private User createUserWithDepartmentAndStatus(String email, String firstName, String lastName, UUID departmentId, UserStatus status) {
        User user = createUserWithDepartment(email, firstName, lastName, departmentId);
        user.setStatus(status);
        return user;
    }

    private String getCellValue(Row row, int cellIndex) {
        if (row == null) return null;
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return "";
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
            return cell.getStringCellValue();
        }
        return cell.toString();
    }
}
