package com.usermanagement.service.cache;

import com.usermanagement.domain.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * CacheEvictionListener 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CacheEvictionListener 测试")
class CacheEvictionListenerTest {

    @Mock
    private DepartmentCache departmentCache;

    private CacheEvictionListener evictionListener;

    @BeforeEach
    void setUp() {
        evictionListener = new CacheEvictionListener(departmentCache);
    }

    @Test
    @DisplayName("应该清除部门创建后的缓存")
    void shouldEvictCacheAfterDepartmentCreated() {
        // Given
        Department department = new Department();
        department.setName("技术部");

        // When
        evictionListener.onDepartmentCreated(department);

        // Then
        then(departmentCache).should().evictCache();
    }

    @Test
    @DisplayName("应该清除部门更新后的缓存")
    void shouldEvictCacheAfterDepartmentUpdated() {
        // Given
        Department department = new Department();
        department.setName("技术部");

        // When
        evictionListener.onDepartmentUpdated(department);

        // Then
        then(departmentCache).should().evictCache();
    }

    @Test
    @DisplayName("应该清除部门删除后的缓存")
    void shouldEvictCacheAfterDepartmentDeleted() {
        // Given
        Department department = new Department();
        department.setName("技术部");

        // When
        evictionListener.onDepartmentDeleted(department);

        // Then
        then(departmentCache).should().evictCache();
    }

    @Test
    @DisplayName("应该清除部门移动后的缓存")
    void shouldEvictCacheAfterDepartmentMoved() {
        // Given
        Department department = new Department();
        department.setName("技术部");

        // When
        evictionListener.onDepartmentMoved(department);

        // Then
        then(departmentCache).should().evictCache();
    }

    @Test
    @DisplayName("部门为 null 时不执行清除操作")
    void shouldNotEvictCacheWhenDepartmentIsNull() {
        // When & Then
        evictionListener.onDepartmentCreated(null);
        evictionListener.onDepartmentUpdated(null);
        evictionListener.onDepartmentDeleted(null);
        evictionListener.onDepartmentMoved(null);

        then(departmentCache).should(never()).evictCache();
    }

    @Test
    @DisplayName("清除缓存失败时不抛出异常")
    void shouldNotThrowExceptionWhenEvictionFails() {
        // Given
        doThrow(new RuntimeException("Cache eviction failed"))
            .when(departmentCache).evictCache();

        Department department = new Department();

        // When & Then - 不应该抛出异常
        assertThat(evictionListener).isNotNull();
        evictionListener.onDepartmentCreated(department);
    }
}
