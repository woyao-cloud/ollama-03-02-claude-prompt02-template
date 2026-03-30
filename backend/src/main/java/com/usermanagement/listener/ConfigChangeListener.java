package com.usermanagement.listener;

import com.usermanagement.domain.ChangeType;
import com.usermanagement.event.ConfigChangedEvent;
import com.usermanagement.service.config.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 配置变更监听器 - 处理配置变更事件
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class ConfigChangeListener {

    private static final Logger log = LoggerFactory.getLogger(ConfigChangeListener.class);

    private final ConfigCache configCache;

    public ConfigChangeListener(ConfigCache configCache) {
        this.configCache = configCache;
    }

    /**
     * 处理配置变更事件
     */
    @EventListener
    @Async
    public void handleConfigChanged(ConfigChangedEvent event) {
        log.info("配置变更事件：{} = {} (类型：{})",
                event.getConfigKey(),
                event.getNewValue(),
                event.getChangeType());

        // 清除缓存（会在 ConfigService 中重新缓存）
        if (event.getChangeType() != ChangeType.CREATE) {
            configCache.evict(event.getConfigKey());
        }

        // 根据配置类型执行特定操作
        switch (event.getConfigType()) {
            case SECURITY:
                handleSecurityConfigChange(event);
                break;
            case EMAIL:
                handleEmailConfigChange(event);
                break;
            case PERFORMANCE:
                handlePerformanceConfigChange(event);
                break;
            default:
                log.debug("通用配置变更：{}", event.getConfigKey());
        }
    }

    /**
     * 处理安全配置变更
     */
    private void handleSecurityConfigChange(ConfigChangedEvent event) {
        log.info("安全配置变更，可能需要刷新安全上下文：{}", event.getConfigKey());
        // 可以在这里添加刷新安全上下文的逻辑
    }

    /**
     * 处理邮件配置变更
     */
    private void handleEmailConfigChange(ConfigChangedEvent event) {
        log.info("邮件配置变更，可能需要刷新邮件发送器：{}", event.getConfigKey());
        // 可以在这里添加刷新邮件配置的逻辑
    }

    /**
     * 处理性能配置变更
     */
    private void handlePerformanceConfigChange(ConfigChangedEvent event) {
        log.info("性能配置变更，可能需要刷新缓存配置：{}", event.getConfigKey());
        // 可以在这里添加刷新缓存配置的逻辑
    }
}
