package com.usermanagement.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.service.importer.UserImportService;
import com.usermanagement.service.exporter.UserExportService;
import com.usermanagement.web.dto.ImportResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 批量导入导出功能集成测试
 */
@WebMvcTest(UserController.class)
class UserControllerImportExportTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserImportService importService;

    @MockBean
    private UserExportService exportService;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("批量导入 API 测试")
    class ImportApiTests {

        @Test
        @DisplayName("应该支持 Excel 文件导入")
        void shouldImportExcelFile() throws Exception {
            // Given
            byte[] excelData = createTestExcelFile();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "users.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    excelData
            );

            ImportResult result = new ImportResult(10, 10, 0, List.of());
            given(importService.importUsers(excelData)).willReturn(result);

            // When & Then
            mockMvc.perform(multipart("/api/users/import")
                            .file(file)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.total").value(10))
                    .andExpect(jsonPath("$.success").value(10))
                    .andExpect(jsonPath("$.failed").value(0))
                    .andExpect(jsonPath("$.success").value(10));

            verify(importService).importUsers(excelData);
        }

        @Test
        @DisplayName("应该支持 CSV 文件导入")
        void shouldImportCsvFile() throws Exception {
            // Given
            String csvContent = "email,firstName,lastName,password\nuser1@example.com,John,Doe,Password123!";
            byte[] csvData = csvContent.getBytes();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "users.csv",
                    "text/csv",
                    csvData
            );

            ImportResult result = new ImportResult(1, 1, 0, List.of());
            given(importService.importUsers(csvData)).willReturn(result);

            // When & Then
            mockMvc.perform(multipart("/api/users/import")
                            .file(file)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.success").value(1));

            verify(importService).importUsers(csvData);
        }

        @Test
        @DisplayName("导入失败时返回错误信息")
        void shouldReturnErrorsOnImportFailure() throws Exception {
            // Given
            byte[] excelData = createTestExcelFile();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "users.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    excelData
            );

            ImportResult result = new ImportResult(
                    10,
                    5,
                    5,
                    List.of("第 3 行：邮箱格式不正确", "第 7 行：密码强度不足")
            );
            given(importService.importUsers(excelData)).willReturn(result);

            // When & Then
            mockMvc.perform(multipart("/api/users/import")
                            .file(file)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(10))
                    .andExpect(jsonPath("$.success").value(5))
                    .andExpect(jsonPath("$.failed").value(5))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors").value(List.of("第 3 行：邮箱格式不正确", "第 7 行：密码强度不足")));
        }

        @Test
        @DisplayName("空文件时返回错误")
        void shouldReturnErrorForEmptyFile() throws Exception {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "empty.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    new byte[0]
            );

            ImportResult result = new ImportResult(0, 0, 0, List.of("文件中没有数据"));
            given(importService.importUsers(new byte[0])).willReturn(result);

            // When & Then
            mockMvc.perform(multipart("/api/users/import")
                            .file(file)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(0));
        }

        @Test
        @DisplayName("缺少文件时返回 400")
        void shouldReturnBadRequestWhenFileMissing() throws Exception {
            // When & Then
            mockMvc.perform(multipart("/api/users/import")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("批量导出 API 测试")
    class ExportApiTests {

        @Test
        @DisplayName("应该导出用户列表为 Excel")
        void shouldExportUsersToExcel() throws Exception {
            // Given
            byte[] excelData = createTestExcelFile();
            given(exportService.exportUsers(
                    org.springframework.data.domain.PageRequest.of(0, 100),
                    null, null, null
            )).willReturn(excelData);

            // When & Then
            mockMvc.perform(get("/api/users/export")
                            .param("page", "0")
                            .param("size", "100"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .andExpect(header().string("Content-Disposition", "attachment; filename=users_export.xlsx"));

            verify(exportService).exportUsers(
                    org.springframework.data.domain.PageRequest.of(0, 100),
                    null, null, null
            );
        }

        @Test
        @DisplayName("应该支持按部门筛选导出")
        void shouldExportFilteredByDepartment() throws Exception {
            // Given
            String departmentId = "550e8400-e29b-41d4-a716-446655440000";
            byte[] excelData = createTestExcelFile();
            given(exportService.exportUsers(
                    org.springframework.data.domain.PageRequest.of(0, 100),
                    null,
                    java.util.UUID.fromString(departmentId),
                    null
            )).willReturn(excelData);

            // When & Then
            mockMvc.perform(get("/api/users/export")
                            .param("page", "0")
                            .param("size", "100")
                            .param("departmentId", departmentId))
                    .andExpect(status().isOk());

            verify(exportService).exportUsers(
                    org.springframework.data.domain.PageRequest.of(0, 100),
                    null,
                    java.util.UUID.fromString(departmentId),
                    null
            );
        }

        @Test
        @DisplayName("应该支持按状态筛选导出")
        void shouldExportFilteredByStatus() throws Exception {
            // Given
            byte[] excelData = createTestExcelFile();
            given(exportService.exportUsers(
                    org.springframework.data.domain.PageRequest.of(0, 100),
                    null, null, UserStatus.ACTIVE
            )).willReturn(excelData);

            // When & Then
            mockMvc.perform(get("/api/users/export")
                            .param("page", "0")
                            .param("size", "100")
                            .param("status", "ACTIVE"))
                    .andExpect(status().isOk());

            verify(exportService).exportUsers(
                    org.springframework.data.domain.PageRequest.of(0, 100),
                    null, null, UserStatus.ACTIVE
            );
        }

        @Test
        @DisplayName("应该支持分页参数")
        void shouldSupportPagination() throws Exception {
            // Given
            byte[] excelData = createTestExcelFile();
            given(exportService.exportUsers(
                    org.springframework.data.domain.PageRequest.of(1, 50),
                    null, null, null
            )).willReturn(excelData);

            // When & Then
            mockMvc.perform(get("/api/users/export")
                            .param("page", "1")
                            .param("size", "50"))
                    .andExpect(status().isOk());

            verify(exportService).exportUsers(
                    org.springframework.data.domain.PageRequest.of(1, 50),
                    null, null, null
            );
        }

        @Test
        @DisplayName("默认分页参数为 page=0, size=100")
        void shouldUseDefaultPagination() throws Exception {
            // Given
            byte[] excelData = createTestExcelFile();
            given(exportService.exportUsers(
                    org.springframework.data.domain.PageRequest.of(0, 100),
                    null, null, null
            )).willReturn(excelData);

            // When & Then
            mockMvc.perform(get("/api/users/export"))
                    .andExpect(status().isOk());

            verify(exportService).exportUsers(
                    org.springframework.data.domain.PageRequest.of(0, 100),
                    null, null, null
            );
        }
    }

    // 辅助方法：创建测试 Excel 文件
    private byte[] createTestExcelFile() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");

            // 标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"邮箱", "名", "姓", "手机号", "状态"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 数据行
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("user1@example.com");
            dataRow.createCell(1).setCellValue("John");
            dataRow.createCell(2).setCellValue("Doe");

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        }
    }
}
