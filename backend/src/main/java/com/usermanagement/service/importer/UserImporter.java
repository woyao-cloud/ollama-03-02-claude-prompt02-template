package com.usermanagement.service.importer;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180ParserBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 用户数据导入器 - 支持 Excel 和 CSV 格式
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class UserImporter {

    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "email", "firstName", "lastName", "password"
    );

    /**
     * 从 Excel 文件读取用户数据
     *
     * @param data Excel 文件字节数组
     * @return 用户数据列表
     */
    public List<Map<String, String>> readExcel(byte[] data) {
        List<Map<String, String>> users = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(data))) {
            Sheet sheet = workbook.getSheetAt(0);

            // 获取标题行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return users;
            }

            Map<String, Integer> headerMap = getHeaderMap(headerRow);

            // 读取数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                Map<String, String> userData = readRow(row, headerMap);
                if (!userData.isEmpty()) {
                    users.add(userData);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("读取 Excel 文件失败", e);
        }

        return users;
    }

    /**
     * 从 CSV 文件读取用户数据
     *
     * @param data CSV 文件字节数组
     * @return 用户数据列表
     */
    public List<Map<String, String>> readCsv(byte[] data) {
        List<Map<String, String>> users = new ArrayList<>();

        String csvContent = new String(data, StandardCharsets.UTF_8);
        try (CSVReader reader = new CSVReaderBuilder(new StringReader(csvContent))
                .withCSVParser(new RFC4180ParserBuilder().build())
                .build()) {

            String[] headers = reader.readNext();
            if (headers == null) {
                return users;
            }

            Map<String, Integer> headerMap = getHeaderMap(headers);

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (isEmptyLine(line)) {
                    continue;
                }

                Map<String, String> userData = readLine(line, headerMap);
                if (!userData.isEmpty()) {
                    users.add(userData);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("读取 CSV 文件失败", e);
        }

        return users;
    }

    /**
     * 判断是否为 Excel 格式
     *
     * @param data 文件字节数组
     * @return 是否为 Excel 格式
     */
    public boolean isExcelFormat(byte[] data) {
        if (data == null || data.length < 4) {
            return false;
        }
        // XSSF (.xlsx) 文件的 magic number: 50 4B 03 04 (PK)
        return (data[0] == 0x50 && data[1] == 0x4B && data[2] == 0x03 && data[3] == 0x04);
    }

    /**
     * 判断是否为 CSV 格式
     *
     * @param data 文件字节数组
     * @return 是否为 CSV 格式
     */
    public boolean isCsvFormat(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        String content = new String(data, StandardCharsets.UTF_8);
        // CSV 通常包含逗号且第一行是标题
        return content.contains(",") && !isExcelFormat(data);
    }

    /**
     * 从 Excel Row 读取数据
     */
    private Map<String, String> readRow(Row row, Map<String, Integer> headerMap) {
        Map<String, String> userData = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            String header = entry.getKey();
            int cellIndex = entry.getValue();

            Cell cell = row.getCell(cellIndex);
            String value = getCellValue(cell);
            userData.put(header, value != null ? value.trim() : "");
        }

        return userData;
    }

    /**
     * 从 CSV 行读取数据
     */
    private Map<String, String> readLine(String[] line, Map<String, Integer> headerMap) {
        Map<String, String> userData = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            String header = entry.getKey();
            int index = entry.getValue();

            String value = (index < line.length) ? line[index] : "";
            userData.put(header, value != null ? value.trim() : "");
        }

        return userData;
    }

    /**
     * 获取 Excel 标题映射
     */
    private Map<String, Integer> getHeaderMap(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();

        for (Cell cell : headerRow) {
            String headerName = getCellValue(cell);
            if (headerName != null && !headerName.isBlank()) {
                headerMap.put(headerName.trim().toLowerCase(), cell.getColumnIndex());
            }
        }

        return headerMap;
    }

    /**
     * 获取 CSV 标题映射
     */
    private Map<String, Integer> getHeaderMap(String[] headers) {
        Map<String, Integer> headerMap = new HashMap<>();

        for (int i = 0; i < headers.length; i++) {
            if (headers[i] != null && !headers[i].isBlank()) {
                headerMap.put(headers[i].trim().toLowerCase(), i);
            }
        }

        return headerMap;
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * 判断是否为空行
     */
    private boolean isEmptyRow(Row row) {
        for (Cell cell : row) {
            if (getCellValue(cell) != null && !getCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否为空行 (CSV)
     */
    private boolean isEmptyLine(String[] line) {
        if (line == null || line.length == 0) {
            return true;
        }
        for (String value : line) {
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
