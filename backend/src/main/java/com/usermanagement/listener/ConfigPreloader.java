package com.usermanagement.listener;

import com.usermanagement.service.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 配置预加载器 - 应用启动时预加载关键配置
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class ConfigPreloader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigPreloader.class);

    private final ConfigService configService;

    public ConfigPreloader(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * 应用启动完成后预加载配置
     */
    @EventListener(ApplicationReadyEvent.class)
    public void preloadConfigs() {
        log.info("开始预加载系统配置...");
        try {
            configService.preloadConfigs();
            log.info("系统配置预加载完成");
        } catch (Exception e) {
            log.error("系统配置预加载失败", e);
        }
    }
}
