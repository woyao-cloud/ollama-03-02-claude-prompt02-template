package com.usermanagement.web.dto;

import com.usermanagement.domain.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置值响应 DTO（用于获取单个配置值）
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigValueResponse {

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 数据类型
     */
    private DataType dataType;

    /**
     * 配置描述
     */
    private String description;
}
