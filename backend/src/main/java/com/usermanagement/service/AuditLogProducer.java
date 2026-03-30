package com.usermanagement.service;

import com.usermanagement.domain.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 审计日志生产者 - 异步写入审计日志
 *
 * 使用队列和后台线程异步处理审计日志，降低业务操作的延迟
 */
@Component
public class AuditLogProducer {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogProducer.class);

    private static final int DEFAULT_QUEUE_CAPACITY = 1000;
    private static final int BATCH_SIZE = 10;
    private static final long FLUSH_INTERVAL_MS = 1000;

    private final BlockingQueue<AuditLog> auditLogQueue;
    private final AuditLogService auditLogService;
    private final ExecutorService executorService;
    private final AtomicBoolean running;

    /**
     * 默认构造函数
     */
    public AuditLogProducer(AuditLogService auditLogService) {
        this(auditLogService, DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * 指定队列容量的构造函数
     */
    public AuditLogProducer(AuditLogService auditLogService, int queueCapacity) {
        this.auditLogService = auditLogService;
        this.auditLogQueue = new ArrayBlockingQueue<>(queueCapacity);
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "audit-log-producer");
            thread.setDaemon(true);
            return thread;
        });
        this.running = new AtomicBoolean(false);

        // 启动消费者线程
        start();
    }

    /**
     * 将审计日志放入队列
     *
     * @param auditLog 审计日志
     */
    public void queueAuditLog(AuditLog auditLog) {
        if (!running.get()) {
            logger.warn("AuditLogProducer 未运行，直接写入数据库");
            auditLogService.logAudit(
                auditLog.getUserId(),
                auditLog.getUserEmail(),
                auditLog.getOperationType(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getOperationDescription(),
                auditLog.getOldValue(),
                auditLog.getNewValue(),
                auditLog.getClientIp(),
                auditLog.getUserAgent(),
                auditLog.getOperationResult(),
                auditLog.getErrorMessage()
            );
            return;
        }

        try {
            // 如果队列满，阻塞等待
            auditLogQueue.put(auditLog);
            logger.debug("审计日志入队：{} - {}", auditLog.getUserEmail(), auditLog.getOperationType());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("审计日志入队中断", e);
            // 降级为同步写入
            fallbackWrite(auditLog);
        }
    }

    /**
     * 启动消费者线程
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            executorService.submit(this::consumeQueue);
            logger.info("AuditLogProducer 启动成功");
        }
    }

    /**
     * 关闭生产者
     */
    @PreDestroy
    public void shutdown() {
        logger.info("正在关闭 AuditLogProducer...");
        running.set(false);

        // 等待队列清空
        int remaining = auditLogQueue.size();
        int waitCount = 0;
        while (remaining > 0 && waitCount < 10) {
            try {
                Thread.sleep(100);
                remaining = auditLogQueue.size();
                waitCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // 处理剩余日志
        drainQueue();

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("AuditLogProducer 关闭完成，处理了 {} 条剩余日志", remaining);
    }

    /**
     * 消费队列中的审计日志
     */
    private void consumeQueue() {
        while (running.get() || !auditLogQueue.isEmpty()) {
            try {
                // 批量处理
                for (int i = 0; i < BATCH_SIZE; i++) {
                    AuditLog auditLog = auditLogQueue.poll(FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
                    if (auditLog != null) {
                        writeAuditLog(auditLog);
                    } else {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("消费队列中断", e);
                break;
            } catch (Exception e) {
                logger.error("消费队列异常", e);
            }
        }
    }

    /**
     * 写入单条审计日志
     */
    private void writeAuditLog(AuditLog auditLog) {
        try {
            auditLogService.logAudit(
                auditLog.getUserId(),
                auditLog.getUserEmail(),
                auditLog.getOperationType(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getOperationDescription(),
                auditLog.getOldValue(),
                auditLog.getNewValue(),
                auditLog.getClientIp(),
                auditLog.getUserAgent(),
                auditLog.getOperationResult(),
                auditLog.getErrorMessage()
            );
        } catch (Exception e) {
            logger.error("写入审计日志失败：{}", auditLog, e);
        }
    }

    /**
     * 清空队列
     */
    private void drainQueue() {
        AuditLog auditLog;
        while ((auditLog = auditLogQueue.poll()) != null) {
            try {
                writeAuditLog(auditLog);
            } catch (Exception e) {
                logger.error("清空队列时写入失败", e);
            }
        }
    }

    /**
     * 降级写入
     */
    private void fallbackWrite(AuditLog auditLog) {
        try {
            auditLogService.logAudit(
                auditLog.getUserId(),
                auditLog.getUserEmail(),
                auditLog.getOperationType(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getOperationDescription(),
                auditLog.getOldValue(),
                auditLog.getNewValue(),
                auditLog.getClientIp(),
                auditLog.getUserAgent(),
                auditLog.getOperationResult(),
                auditLog.getErrorMessage()
            );
        } catch (Exception e) {
            logger.error("降级写入失败", e);
        }
    }

    /**
     * 获取队列大小（用于测试）
     */
    public int getQueueSize() {
        return auditLogQueue.size();
    }

    /**
     * 检查是否正在运行（用于测试）
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 获取队列（用于测试）
     */
    BlockingQueue<AuditLog> getQueue() {
        return auditLogQueue;
    }
}
