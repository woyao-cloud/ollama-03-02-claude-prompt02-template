package com.usermanagement.service.cache;

import com.usermanagement.config.CacheProperties;
import com.usermanagement.web.dto.DepartmentTreeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * 部门树缓存服务
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class DepartmentCache {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentCache.class);
    private static final String CACHE_KEY = "dept:tree";

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties cacheProperties;

    public DepartmentCache(RedisTemplate<String, Object> redisTemplate,
                          CacheProperties cacheProperties) {
        this.redisTemplate = redisTemplate;
        this.cacheProperties = cacheProperties;
    }

    /**
     * 从 Redis 获取部门树缓存
     *
     * @return 部门树响应，缓存不存在或 Redis 异常时返回 null
     */
    public DepartmentTreeResponse getDepartmentTree() {
        try {
            return (DepartmentTreeResponse) redisTemplate.opsForValue().get(CACHE_KEY);
        } catch (Exception e) {
            log.warn("获取部门树缓存失败，降级到数据库查询：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 缓存部门树到 Redis
     *
     * @param response 部门树响应
     */
    public void setDepartmentTree(DepartmentTreeResponse response) {
        if (response == null) {
            return;
        }

        try {
            long expiration = getExpiration(response);
            redisTemplate.opsForValue().set(CACHE_KEY, response, expiration, TimeUnit.SECONDS);
            log.debug("部门树缓存已设置，过期时间：{}秒", expiration);
        } catch (Exception e) {
            log.warn("设置部门树缓存失败：{}", e.getMessage());
        }
    }

    /**
     * 清除部门树缓存
     */
    public void evictCache() {
        try {
            redisTemplate.delete(CACHE_KEY);
            log.debug("部门树缓存已清除");
        } catch (Exception e) {
            log.warn("清除部门树缓存失败：{}", e.getMessage());
        }
    }

    /**
     * 检查缓存是否存在
     *
     * @return 缓存是否存在
     */
    public boolean hasCache() {
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
            return cached != null;
        } catch (Exception e) {
            log.warn("检查缓存状态失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取缓存过期时间
     * 空树使用较短的过期时间（缓存穿透保护）
     */
    private long getExpiration(DepartmentTreeResponse response) {
        boolean isEmpty = response.getTree() == null || response.getTree().isEmpty();
        return isEmpty
            ? cacheProperties.getDepartment().getTree().getNullExpiration()
            : cacheProperties.getDepartment().getTree().getExpiration();
    }
}
