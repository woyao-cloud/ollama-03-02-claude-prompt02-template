package com.usermanagement.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 测试配置类
 */
@TestConfiguration
@EnableJpaAuditing
public class TestConfig {

    @Bean
    public TestConfig testConfig() {
        return new TestConfig();
    }
}
