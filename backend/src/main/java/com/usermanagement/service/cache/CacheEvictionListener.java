package com.usermanagement.service.cache;

import com.usermanagement.domain.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 缓存失效监听器
 * 监听部门变更事件，自动清除缓存
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class CacheEvictionListener {

    private static final Logger logger = LoggerFactory.getLogger(CacheEvictionListener.class);

    private final DepartmentCache departmentCache;

    public CacheEvictionListener(DepartmentCache departmentCache) {
        this.departmentCache = departmentCache;
    }

    /**
     * 部门创建后清除缓存
     */
    public void onDepartmentCreated(Department department) {
        if (department == null) {
            return;
        }
        evictCacheSafely("创建");
    }

    /**
     * 部门更新后清除缓存
     */
    public void onDepartmentUpdated(Department department) {
        if (department == null) {
            return;
        }
        evictCacheSafely("更新");
    }

    /**
     * 部门删除后清除缓存
     */
    public void onDepartmentDeleted(Department department) {
        if (department == null) {
            return;
        }
        evictCacheSafely("删除");
    }

    /**
     * 部门移动后清除缓存
     */
    public void onDepartmentMoved(Department department) {
        if (department == null) {
            return;
        }
        evictCacheSafely("移动");
    }

    /**
     * 安全地清除缓存，异常时不抛出
     */
    private void evictCacheSafely(String operation) {
        try {
            departmentCache.evictCache();
            log.debug("部门{}操作后缓存已清除", operation);
        } catch (Exception e) {
            log.warn("部门{}后清除缓存失败：{}", operation, e.getMessage());
        }
    }
}
