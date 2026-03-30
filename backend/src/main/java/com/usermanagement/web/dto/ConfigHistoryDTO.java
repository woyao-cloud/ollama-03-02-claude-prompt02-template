package com.usermanagement.web.dto;

import com.usermanagement.domain.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 配置变更历史响应 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigHistoryDTO {

    /**
     * 历史记录 ID
     */
    private String id;

    /**
     * 配置 ID
     */
    private String configId;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 旧值
     */
    private String oldValue;

    /**
     * 新值
     */
    private String newValue;

    /**
     * 变更类型
     */
    private ChangeType changeType;

    /**
     * 变更人 ID
     */
    private String changedBy;

    /**
     * 变更人邮箱
     */
    private String changedByEmail;

    /**
     * 变更原因
     */
    private String reason;

    /**
     * 变更时间
     */
    private Instant changedAt;
}
