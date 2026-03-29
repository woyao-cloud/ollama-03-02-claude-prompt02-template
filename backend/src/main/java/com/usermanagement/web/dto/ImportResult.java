package com.usermanagement.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量导入结果 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult {

    /**
     * 总记录数
     */
    private Integer total;

    /**
     * 成功导入数
     */
    private Integer success;

    /**
     * 失败数
     */
    private Integer failed;

    /**
     * 错误信息列表
     */
    private List<String> errors;

    /**
     * 判断是否全部成功
     */
    public boolean isSuccess() {
        return this.failed == 0;
    }
}
