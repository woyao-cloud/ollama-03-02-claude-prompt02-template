package com.usermanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步执行器配置
 *
 * 用于审计日志异步写入等场景
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncExecutorConfig implements AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncExecutorConfig.class);

    @Value("${async.executor.core-pool-size:20}")
    private int corePoolSize;

    @Value("${async.executor.max-pool-size:100}")
    private int maxPoolSize;

    @Value("${async.executor.queue-capacity:1000}")
    private int queueCapacity;

    @Value("${async.executor.keep-alive:60}")
    private int keepAliveSeconds;

    @Override
    @Bean(name = "auditExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(corePoolSize);

        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);

        // 队列容量
        executor.setQueueCapacity(queueCapacity);

        // 线程存活时间
        executor.setKeepAliveSeconds(keepAliveSeconds);

        // 线程名前缀
        executor.setThreadNamePrefix("audit-async-");

        // 拒绝策略：调用者运行（避免丢失审计日志）
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejected(Runnable runnable, ThreadPoolExecutor executor) {
                logger.warn("异步任务被拒绝，由调用者线程执行：queueSize={}", executor.getQueue().size());
                // 由调用者线程直接执行，避免丢失
                runnable.run();
            }
        });

        // 等待任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 关闭时等待时间
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        logger.info("异步执行器已初始化：corePoolSize={}, maxPoolSize={}, queueCapacity={}",
            corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
                logger.error("异步任务执行异常：method={}, error={}", method.getName(), throwable.getMessage(), throwable);
            }
        };
    }
}
