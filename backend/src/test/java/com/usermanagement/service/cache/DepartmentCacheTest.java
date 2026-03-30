package com.usermanagement.service.cache;

import com.usermanagement.config.CacheProperties;
import com.usermanagement.web.dto.DepartmentTreeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * DepartmentCache 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentCache 测试")
class DepartmentCacheTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private CacheProperties cacheProperties;

    @Mock
    private CacheProperties.Department department;

    @Mock
    private CacheProperties.DepartmentTree tree;

    private DepartmentCache departmentCache;

    private static final String CACHE_KEY = "dept:tree";
    private static final long EXPIRATION = 1800L;
    private static final long NULL_EXPIRATION = 300L;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(cacheProperties.getDepartment()).willReturn(department);
        given(department.getTree()).willReturn(tree);
        given(tree.getExpiration()).willReturn(EXPIRATION);
        given(tree.getNullExpiration()).willReturn(NULL_EXPIRATION);

        departmentCache = new DepartmentCache(redisTemplate, cacheProperties);
    }

    @Nested
    @DisplayName("获取缓存测试")
    class GetCacheTests {

        @Test
        @DisplayName("应该从 Redis 获取缓存的部门树")
        void shouldGetCachedDepartmentTreeFromRedis() {
            // Given
            DepartmentTreeResponse cachedResponse = new DepartmentTreeResponse();
            cachedResponse.setTree(List.of());
            cachedResponse.setTotal(0L);

            given(valueOperations.get(CACHE_KEY)).willReturn(cachedResponse);

            // When
            DepartmentTreeResponse result = departmentCache.getDepartmentTree();

            // Then
            assertThat(result).isSameAs(cachedResponse);
            then(valueOperations).should().get(CACHE_KEY);
        }

        @Test
        @DisplayName("缓存不存在时返回 null")
        void shouldReturnNullWhenCacheNotExists() {
            // Given
            given(valueOperations.get(CACHE_KEY)).willReturn(null);

            // When
            DepartmentTreeResponse result = departmentCache.getDepartmentTree();

            // Then
            assertThat(result).isNull();
            then(valueOperations).should().get(CACHE_KEY);
        }

        @Test
        @DisplayName("Redis 异常时降级返回 null")
        void shouldReturnNullWhenRedisThrowsException() {
            // Given
            doThrow(new RuntimeException("Redis connection failed"))
                .when(valueOperations).get(CACHE_KEY);

            // When
            DepartmentTreeResponse result = departmentCache.getDepartmentTree();

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("设置缓存测试")
    class SetCacheTests {

        @Test
        @DisplayName("应该缓存部门树并设置过期时间")
        void shouldCacheDepartmentTreeWithExpiration() {
            // Given
            DepartmentTreeResponse response = new DepartmentTreeResponse();
            response.setTree(List.of());
            response.setTotal(1L);

            // When
            departmentCache.setDepartmentTree(response);

            // Then
            then(valueOperations).should().set(eq(CACHE_KEY), eq(response), EXPIRATION, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("应该缓存空部门树并使用空值过期时间")
        void shouldCacheEmptyTreeWithNullExpiration() {
            // Given
            DepartmentTreeResponse response = new DepartmentTreeResponse();
            response.setTree(null);
            response.setTotal(0L);

            // When
            departmentCache.setDepartmentTree(response);

            // Then
            then(valueOperations).should().set(eq(CACHE_KEY), eq(response), NULL_EXPIRATION, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("Redis 异常时不抛出异常")
        void shouldNotThrowExceptionWhenRedisFails() {
            // Given
            DepartmentTreeResponse response = new DepartmentTreeResponse();
            doThrow(new RuntimeException("Redis connection failed"))
                .when(valueOperations).set(any(), any(), any(), any());

            // When & Then - 不应该抛出异常
            departmentCache.setDepartmentTree(response);
        }

        @Test
        @DisplayName("响应为 null 时不缓存")
        void shouldNotCacheWhenResponseIsNull() {
            // When
            departmentCache.setDepartmentTree(null);

            // Then
            then(valueOperations).should(never()).set(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("清除缓存测试")
    class EvictCacheTests {

        @Test
        @DisplayName("应该清除部门树缓存")
        void shouldEvictDepartmentTreeCache() {
            // Given
            given(valueOperations.get(CACHE_KEY)).willReturn(new DepartmentTreeResponse());

            // When
            departmentCache.evictCache();

            // Then
            then(redisTemplate).should().delete(CACHE_KEY);
        }

        @Test
        @DisplayName("缓存不存在时不抛出异常")
        void shouldNotThrowExceptionWhenCacheNotExists() {
            // Given
            given(valueOperations.get(CACHE_KEY)).willReturn(null);

            // When & Then - 不应该抛出异常
            departmentCache.evictCache();
        }

        @Test
        @DisplayName("Redis 异常时不抛出异常")
        void shouldNotThrowExceptionWhenRedisFails() {
            // Given
            doThrow(new RuntimeException("Redis connection failed"))
                .when(redisTemplate).delete(CACHE_KEY);

            // When & Then - 不应该抛出异常
            departmentCache.evictCache();
        }
    }

    @Nested
    @DisplayName("缓存是否存在测试")
    class HasCacheTests {

        @Test
        @DisplayName("应该返回缓存存在")
        void shouldReturnTrueWhenCacheExists() {
            // Given
            given(valueOperations.get(CACHE_KEY)).willReturn(new DepartmentTreeResponse());

            // When
            boolean hasCache = departmentCache.hasCache();

            // Then
            assertThat(hasCache).isTrue();
        }

        @Test
        @DisplayName("应该返回缓存不存在")
        void shouldReturnFalseWhenCacheNotExists() {
            // Given
            given(valueOperations.get(CACHE_KEY)).willReturn(null);

            // When
            boolean hasCache = departmentCache.hasCache();

            // Then
            assertThat(hasCache).isFalse();
        }

        @Test
        @DisplayName("Redis 异常时返回 false")
        void shouldReturnFalseWhenRedisThrowsException() {
            // Given
            doThrow(new RuntimeException("Redis connection failed"))
                .when(valueOperations).get(CACHE_KEY);

            // When
            boolean hasCache = departmentCache.hasCache();

            // Then
            assertThat(hasCache).isFalse();
        }
    }
}
