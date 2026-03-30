package com.usermanagement.config;

import com.usermanagement.service.cache.DepartmentCache;
import com.usermanagement.service.TreeBuilder;
import com.usermanagement.web.dto.DepartmentTreeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 缓存预加载组件
 *
 * 应用启动后异步预热热点缓存数据
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class CachePreloader {

    private static final Logger logger = LoggerFactory.getLogger(CachePreloader.class);

    private final DepartmentCache departmentCache;
    private final TreeBuilder treeBuilder;
    private final CacheProperties cacheProperties;

    // 专用线程池用于缓存预热，避免阻塞主应用启动
    private final ExecutorService preloaderExecutor = Executors.newFixedThreadPool(
        2,
        r -> {
            Thread thread = new Thread(r);
            thread.setName("cache-preloader");
            thread.setDaemon(true);
            return thread;
        }
    );

    public CachePreloader(
        DepartmentCache departmentCache,
        TreeBuilder treeBuilder,
        CacheProperties cacheProperties
    ) {
        this.departmentCache = departmentCache;
        this.treeBuilder = treeBuilder;
        this.cacheProperties = cacheProperties;
    }

    /**
     * 应用启动完成后执行缓存预热
     */
    @EventListener(ApplicationReadyEvent.class)
    public void preloadCache() {
        logger.info("开始缓存预热...");

        // 异步预热部门树缓存
        preloaderExecutor.submit(() -> {
            try {
                preloadDepartmentTree();
            } catch (Exception e) {
                logger.error("预热部门树缓存失败：{}", e.getMessage(), e);
            }
        });

        // 可在此添加其他缓存预热任务
        // 例如：预热权限列表、角色列表等

        logger.info("缓存预热任务已启动");
    }

    /**
     * 预热部门树缓存
     */
    private void preloadDepartmentTree() {
        logger.info("预热部门树缓存...");

        long startTime = System.currentTimeMillis();

        try {
            // 检查缓存是否已存在
            if (departmentCache.hasCache()) {
                logger.info("部门树缓存已存在，跳过预热");
                return;
            }

            // 构建部门树
            DepartmentTreeResponse tree = treeBuilder.buildTree();

            if (tree != null) {
                // 缓存到 Redis
                departmentCache.setDepartmentTree(tree);

                long duration = System.currentTimeMillis() - startTime;
                logger.info("部门树缓存预热完成，耗时={}ms, 节点数={}",
                    duration,
                    tree.getTree() != null ? tree.getTree().size() : 0);
            } else {
                logger.warn("部门树为空，跳过缓存");
            }
        } catch (Exception e) {
            logger.error("预热部门树缓存异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 关闭线程池
     */
    @org.springframework.beans.factory.annotation.PreDestroy
    public void shutdown() {
        try {
            preloaderExecutor.shutdown();
            if (!preloaderExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                preloaderExecutor.shutdownNow();
            }
            logger.info("缓存预热线程池已关闭");
        } catch (InterruptedException e) {
            preloaderExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
