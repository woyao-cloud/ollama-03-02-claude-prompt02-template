package com.usermanagement.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CacheProperties 单元测试
 */
@SpringBootTest(classes = CacheProperties.class)
@EnableConfigurationProperties(CacheProperties.class)
@TestPropertySource(properties = {
    "cache.department.tree.expiration=1800",
    "cache.department.tree.null-expiration=300"
})
@DisplayName("CacheProperties 测试")
class CachePropertiesTest {

    @Autowired
    private CacheProperties cacheProperties;

    @Test
    @DisplayName("应该加载部门树缓存过期时间")
    void shouldLoadDepartmentTreeExpiration() {
        // Then
        assertThat(cacheProperties.getDepartment().getTree().getExpiration())
            .isEqualTo(1800);
    }

    @Test
    @DisplayName("应该加载空值缓存过期时间")
    void shouldLoadNullValueExpiration() {
        // Then
        assertThat(cacheProperties.getDepartment().getTree().getNullExpiration())
            .isEqualTo(300);
    }

    @Test
    @DisplayName("应该使用默认值当配置未指定时")
    void shouldUseDefaultValueWhenNotConfigured() {
        // Given - 使用默认配置
        CacheProperties properties = new CacheProperties();
        CacheProperties.Department department = new CacheProperties.Department();
        CacheProperties.DepartmentTree tree = new CacheProperties.DepartmentTree();

        // When - 未设置自定义值
        department.setTree(tree);
        properties.setDepartment(department);

        // Then - 使用默认值
        assertThat(properties.getDepartment().getTree().getExpiration())
            .isEqualTo(1800); // 默认 30 分钟
    }
}
