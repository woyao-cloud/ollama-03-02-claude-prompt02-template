package com.usermanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置属性
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    private Department department = new Department();

    @Data
    public static class Department {
        private DepartmentTree tree = new DepartmentTree();
    }

    @Data
    public static class DepartmentTree {
        /**
         * 部门树缓存过期时间（秒），默认 30 分钟
         */
        private Long expiration = 1800L;

        /**
         * 空值缓存过期时间（秒），默认 5 分钟（缓存穿透保护）
         */
        private Long nullExpiration = 300L;
    }
}
