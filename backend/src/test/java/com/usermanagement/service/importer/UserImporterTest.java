package com.usermanagement.service.importer;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserImporter 单元测试
 */
class UserImporterTest {

    private UserImporter userImporter;

    @BeforeEach
    void setUp() {
        userImporter = new UserImporter();
    }

    @Nested
    @DisplayName("Excel 导入测试")
    class ExcelImportTests {

        @Test
        @DisplayName("应该从 Excel 文件读取用户数据")
        void shouldReadUserDataFromExcel() throws IOException {
            // Given
            byte[] excelData = createTestExcelFile();

            // When
            List<Map<String, String>> users = userImporter.readExcel(excelData);

            // Then
            assertThat(users).hasSize(2);
            assertThat(users.get(0).get("email")).isEqualTo("user1@example.com");
            assertThat(users.get(0).get("firstName")).isEqualTo("John");
            assertThat(users.get(0).get("lastName")).isEqualTo("Doe");
            assertThat(users.get(0).get("password")).isEqualTo("Password123!");
        }

        @Test
        @DisplayName("应该处理空的 Excel 文件")
        void shouldHandleEmptyExcelFile() throws IOException {
            // Given
            byte[] excelData = createEmptyExcelFile();

            // When
            List<Map<String, String>> users = userImporter.readExcel(excelData);

            // Then
            assertThat(users).isEmpty();
        }

        @Test
        @DisplayName("Excel 只有标题行时返回空列表")
        void shouldReturnEmptyListWhenOnlyHeaderRow() throws IOException {
            // Given
            byte[] excelData = createExcelWithHeaderOnly();

            // When
            List<Map<String, String>> users = userImporter.readExcel(excelData);

            // Then
            assertThat(users).isEmpty();
        }

        @Test
        @DisplayName("应该忽略空行")
        void shouldIgnoreEmptyRows() throws IOException {
            // Given
            byte[] excelData = createExcelWithEmptyRows();

            // When
            List<Map<String, String>> users = userImporter.readExcel(excelData);

            // Then
            assertThat(users).hasSize(1);
        }

        @Test
        @DisplayName("无效 Excel 格式时抛出异常")
        void shouldThrowExceptionForInvalidExcelFormat() {
            // Given
            byte[] invalidData = "not an excel file".getBytes(StandardCharsets.UTF_8);

            // When & Then
            assertThatThrownBy(() -> userImporter.readExcel(invalidData))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("读取 Excel 文件失败");
        }

        @Test
        @DisplayName("应该读取所有必填字段")
        void shouldReadAllRequiredFields() throws IOException {
            // Given
            byte[] excelData = createTestExcelFile();

            // When
            List<Map<String, String>> users = userImporter.readExcel(excelData);

            // Then
            Map<String, String> user = users.get(0);
            assertThat(user).containsKeys("email", "firstName", "lastName", "password");
            assertThat(user.get("phone")).isEqualTo("13800138000");
            assertThat(user.get("departmentId")).isEqualTo("dept-001");
        }
    }

    @Nested
    @DisplayName("CSV 导入测试")
    class CsvImportTests {

        @Test
        @DisplayName("应该从 CSV 文件读取用户数据")
        void shouldReadUserDataFromCsv() {
            // Given
            String csvContent = "email,firstName,lastName,password,phone,departmentId\n" +
                    "user1@example.com,John,Doe,Password123!,13800138000,dept-001\n" +
                    "user2@example.com,Jane,Smith,Password456!,13900139000,dept-002";

            // When
            List<Map<String, String>> users = userImporter.readCsv(csvContent.getBytes(StandardCharsets.UTF_8));

            // Then
            assertThat(users).hasSize(2);
            assertThat(users.get(0).get("email")).isEqualTo("user1@example.com");
            assertThat(users.get(0).get("firstName")).isEqualTo("John");
            assertThat(users.get(0).get("lastName")).isEqualTo("Doe");
        }

        @Test
        @DisplayName("应该处理空的 CSV 文件")
        void shouldHandleEmptyCsvFile() {
            // Given
            String csvContent = "";

            // When
            List<Map<String, String>> users = userImporter.readCsv(csvContent.getBytes(StandardCharsets.UTF_8));

            // Then
            assertThat(users).isEmpty();
        }

        @Test
        @DisplayName("CSV 只有标题行时返回空列表")
        void shouldReturnEmptyListWhenOnlyCsvHeader() {
            // Given
            String csvContent = "email,firstName,lastName,password,phone,departmentId";

            // When
            List<Map<String, String>> users = userImporter.readCsv(csvContent.getBytes(StandardCharsets.UTF_8));

            // Then
            assertThat(users).isEmpty();
        }

        @Test
        @DisplayName("应该处理 CSV 中的空行")
        void shouldHandleEmptyLinesInCsv() {
            // Given
            String csvContent = "email,firstName,lastName,password\n" +
                    "user1@example.com,John,Doe,Password123!\n" +
                    "\n" +
                    "user2@example.com,Jane,Smith,Password456!";

            // When
            List<Map<String, String>> users = userImporter.readCsv(csvContent.getBytes(StandardCharsets.UTF_8));

            // Then
            assertThat(users).hasSize(2);
        }

        @Test
        @DisplayName("应该处理带引号的 CSV 字段")
        void shouldHandleQuotedCsvFields() {
            // Given
            String csvContent = "email,firstName,lastName,password\n" +
                    "\"user1@example.com\",\"John, Jr.\",Doe,Password123!";

            // When
            List<Map<String, String>> users = userImporter.readCsv(csvContent.getBytes(StandardCharsets.UTF_8));

            // Then
            assertThat(users).hasSize(1);
            assertThat(users.get(0).get("firstName")).isEqualTo("John, Jr.");
        }

        @Test
        @DisplayName("应该自动检测 Excel 文件格式")
        void shouldDetectExcelFormat() throws IOException {
            // Given
            byte[] excelData = createTestExcelFile();

            // When
            boolean isExcel = userImporter.isExcelFormat(excelData);

            // Then
            assertThat(isExcel).isTrue();
        }

        @Test
        @DisplayName("应该自动检测 CSV 文件格式")
        void shouldDetectCsvFormat() {
            // Given
            String csvContent = "email,firstName,lastName\nuser1@example.com,John,Doe";
            byte[] csvData = csvContent.getBytes(StandardCharsets.UTF_8);

            // When
            boolean isCsv = userImporter.isCsvFormat(csvData);

            // Then
            assertThat(isCsv).isTrue();
        }

        @Test
        @DisplayName("非 CSV 格式返回 false")
        void shouldReturnFalseForNonCsvFormat() {
            // Given
            byte[] binaryData = new byte[]{0x50, 0x4B, 0x03, 0x04}; // ZIP/Excel magic number

            // When
            boolean isCsv = userImporter.isCsvFormat(binaryData);

            // Then
            assertThat(isCsv).isFalse();
        }
    }

    // 辅助方法：创建测试 Excel 文件
    private byte[] createTestExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"email", "firstName", "lastName", "password", "phone", "departmentId"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 创建数据行 1
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("user1@example.com");
            row1.createCell(1).setCellValue("John");
            row1.createCell(2).setCellValue("Doe");
            row1.createCell(3).setCellValue("Password123!");
            row1.createCell(4).setCellValue("13800138000");
            row1.createCell(5).setCellValue("dept-001");

            // 创建数据行 2
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("user2@example.com");
            row2.createCell(1).setCellValue("Jane");
            row2.createCell(2).setCellValue("Smith");
            row2.createCell(3).setCellValue("Password456!");
            row2.createCell(4).setCellValue("13900139000");
            row2.createCell(5).setCellValue("dept-002");

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        }
    }

    // 辅助方法：创建空 Excel 文件
    private byte[] createEmptyExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Users");
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        }
    }

    // 辅助方法：创建只有标题行的 Excel 文件
    private byte[] createExcelWithHeaderOnly() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"email", "firstName", "lastName", "password"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        }
    }

    // 辅助方法：创建包含空行的 Excel 文件
    private byte[] createExcelWithEmptyRows() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");

            // 标题行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("email");
            headerRow.createCell(1).setCellValue("firstName");

            // 空行
            sheet.createRow(1);

            // 数据行
            Row dataRow = sheet.createRow(2);
            dataRow.createCell(0).setCellValue("user1@example.com");
            dataRow.createCell(1).setCellValue("John");

            // 空行
            sheet.createRow(3);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        }
    }
}
