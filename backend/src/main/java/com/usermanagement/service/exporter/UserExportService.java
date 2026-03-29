package com.usermanagement.service.exporter;

import com.usermanagement.domain.User;
import com.usermanagement.domain.UserStatus;
import com.usermanagement.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 用户批量导出服务
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class UserExportService {

    private static final String[] HEADERS = {"邮箱", "名", "姓", "手机号", "状态", "部门 ID", "邮箱已验证"};

    private final UserRepository userRepository;

    public UserExportService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 导出用户列表为 Excel
     *
     * @param pageable     分页参数
     * @param keyword      关键词（预留）
     * @param departmentId 部门 ID
     * @param status       用户状态
     * @return Excel 文件字节数组
     */
    @Transactional(readOnly = true)
    public byte[] exportUsers(Pageable pageable, String keyword, UUID departmentId, UserStatus status) {
        Page<User> userPage = findUsers(pageable, departmentId, status);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");

            // 创建标题行
            createHeaderRow(sheet);

            // 填充数据
            fillDataRows(sheet, userPage.getContent());

            // 自动调整列宽
            autoSizeColumns(sheet);

            // 写入输出流
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }

    /**
     * 查询用户
     */
    private Page<User> findUsers(Pageable pageable, UUID departmentId, UserStatus status) {
        if (departmentId != null && status != null) {
            return userRepository.findByDepartmentIdAndStatus(departmentId, status, pageable);
        } else if (departmentId != null) {
            return userRepository.findByDepartmentId(departmentId, pageable);
        } else if (status != null) {
            return userRepository.findByStatus(status, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    /**
     * 创建标题行
     */
    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(20);

        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 创建标题样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 填充数据行
     */
    private void fillDataRows(Sheet sheet, List<User> users) {
        CellStyle dataStyle = createDataStyle(sheet.getWorkbook());

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Row row = sheet.createRow(i + 1);
            row.setHeightInPoints(18);

            createCell(row, 0, user.getEmail(), dataStyle);
            createCell(row, 1, user.getFirstName(), dataStyle);
            createCell(row, 2, user.getLastName(), dataStyle);
            createCell(row, 3, user.getPhone() != null ? user.getPhone() : "", dataStyle);
            createCell(row, 4, user.getStatus() != null ? user.getStatus().name() : "", dataStyle);
            createCell(row, 5, user.getDepartmentId() != null ? user.getDepartmentId().toString() : "", dataStyle);
            createCell(row, 6, user.getEmailVerified() != null && user.getEmailVerified() ? "是" : "否", dataStyle);
        }
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    /**
     * 创建单元格
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * 自动调整列宽
     */
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            // 确保最小列宽
            int columnWidth = sheet.getColumnWidth(i);
            if (columnWidth < 2560) {
                sheet.setColumnWidth(i, 2560); // 至少 20 个字符宽度
            }
            // 最大列宽限制
            if (columnWidth > 10240) {
                sheet.setColumnWidth(i, 10240); // 最多 80 个字符宽度
            }
        }
    }
}
