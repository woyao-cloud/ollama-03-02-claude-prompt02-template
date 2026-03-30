package com.usermanagement.event;

import com.usermanagement.domain.ChangeType;
import com.usermanagement.domain.ConfigType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * 配置变更事件 - 用于通知配置已变更
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Getter
public class ConfigChangedEvent extends ApplicationEvent {

    /**
     * 配置 ID
     */
    private final UUID configId;

    /**
     * 配置键
     */
    private final String configKey;

    /**
     * 配置类型
     */
    private final ConfigType configType;

    /**
     * 变更类型
     */
    private final ChangeType changeType;

    /**
     * 旧值
     */
    private final String oldValue;

    /**
     * 新值
     */
    private final String newValue;

    /**
     * 变更人 ID
     */
    private final UUID changedBy;

    /**
     * 变更人邮箱
     */
    private final String changedByEmail;

    public ConfigChangedEvent(Object source, UUID configId, String configKey, ConfigType configType,
                              ChangeType changeType, String oldValue, String newValue,
                              UUID changedBy, String changedByEmail) {
        super(source);
        this.configId = configId;
        this.configKey = configKey;
        this.configType = configType;
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changedByEmail = changedByEmail;
    }
}
