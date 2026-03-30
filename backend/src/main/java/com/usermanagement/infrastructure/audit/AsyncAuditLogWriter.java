package com.usermanagement.infrastructure.audit;

import com.usermanagement.domain.AuditLog;
import com.usermanagement.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步审计日志写入器
 *
 * 性能优化:
 * - 使用内存队列缓冲审计日志
 * - 批量写入数据库，减少 IO 次数
 * - 支持定时刷新和队列满时强制刷新
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class AsyncAuditLogWriter {

    private static final Logger logger = LoggerFactory.getLogger(AsyncAuditLogWriter.class);

    /**
     * 审计日志队列 (有界队列，防止内存溢出)
     */
    private final BlockingQueue<AuditLog> auditLogQueue;

    /**
     * 批量写入缓冲区
     */
    private final List<AuditLog> batchBuffer;

    /**
     * 队列中待处理的日志数量
     */
    private final AtomicInteger queueSize = new AtomicInteger(0);

    private final AuditLogRepository auditLogRepository;

    /**
     * 批量写入大小
     */
    private final int batchSize;

    /**
     * 批量写入间隔 (毫秒)
     */
    private final long batchInterval;

    public AsyncAuditLogWriter(
        AuditLogRepository auditLogRepository,
        @Value("${audit.async.batch-size:100}") int batchSize,
        @Value("${audit.async.batch-interval:1000}") long batchInterval,
        @Value("${audit.async.queue-capacity:5000}") int queueCapacity
    ) {
        this.auditLogRepository = auditLogRepository;
        this.batchSize = batchSize;
        this.batchInterval = batchInterval;
        this.auditLogQueue = new ArrayBlockingQueue<>(queueCapacity);
        this.batchBuffer = new ArrayList<>(batchSize);
        logger.info("异步审计日志写入器已初始化：batchSize={}, batchInterval={}ms, queueCapacity={}",
            batchSize, batchInterval, queueCapacity);
    }

    /**
     * 异步提交审计日志
     *
     * @param auditLog 审计日志
     * @return 是否提交成功
     */
    @Async("auditExecutor")
    public boolean submit(AuditLog auditLog) {
        if (auditLog == null) {
            return false;
        }

        try {
            // 尝试将日志添加到队列 (不阻塞)
            if (auditLogQueue.offer(auditLog)) {
                queueSize.incrementAndGet();
                logger.trace("审计日志已加入队列：operation={}, userId={}",
                    auditLog.getOperation(), auditLog.getUserId());

                // 如果队列接近满，触发紧急刷新
                if (queueSize.get() >= auditLogQueue.remainingCapacity() * 0.8) {
                    logger.warn("审计日志队列接近满载，触发紧急刷新：size={}", queueSize.get());
                    flushImmediately();
                }
                return true;
            } else {
                // 队列已满，强制刷新后重试
                logger.warn("审计日志队列已满，触发强制刷新");
                flushImmediately();

                // 重试一次
                if (auditLogQueue.offer(auditLog)) {
                    queueSize.incrementAndGet();
                    return true;
                } else {
                    logger.error("审计日志队列仍然已满，丢弃日志：operation={}", auditLog.getOperation());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("提交审计日志失败：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 定时刷新：将队列中的日志批量写入数据库
     */
    @Scheduled(fixedDelayString = "${audit.async.batch-interval:1000}")
    public void scheduledFlush() {
        if (queueSize.get() > 0) {
            flushBatch();
        }
    }

    /**
     * 立即刷新（紧急情况下使用）
     */
    public void flushImmediately() {
        flushBatch();
    }

    /**
     * 批量刷新
     */
    private synchronized void flushBatch() {
        if (batchBuffer.isEmpty() && queueSize.get() == 0) {
            return;
        }

        try {
            // 从队列中取出日志到缓冲区
            int count = 0;
            while (count < batchSize) {
                AuditLog log = auditLogQueue.poll();
                if (log == null) {
                    break;
                }
                batchBuffer.add(log);
                count++;
                queueSize.decrementAndGet();
            }

            if (batchBuffer.isEmpty()) {
                return;
            }

            // 批量写入数据库
            long startTime = System.currentTimeMillis();
            try {
                auditLogRepository.saveAll(batchBuffer);
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("批量写入审计日志：count={}, duration={}ms", batchBuffer.size(), duration);
            } catch (Exception e) {
                logger.error("批量写入审计日志失败：{}", e.getMessage(), e);
                // 失败时将日志重新加入队列头部（简单重试策略）
                for (int i = batchBuffer.size() - 1; i >= 0; i--) {
                    auditLogQueue.offer(batchBuffer.get(i));
                    queueSize.incrementAndGet();
                }
            } finally {
                batchBuffer.clear();
            }
        } catch (Exception e) {
            logger.error("批量刷新审计日志异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 获取队列大小
     *
     * @return 队列中待处理的日志数量
     */
    public int getQueueSize() {
        return queueSize.get();
    }

    /**
     * 获取队列容量
     *
     * @return 队列总容量
     */
    public int getQueueCapacity() {
        return auditLogQueue.size();
    }

    /**
     * 获取队列使用率
     *
     * @return 使用率百分比
     */
    public double getQueueUsage() {
        return (double) queueSize.get() / auditLogQueue.remainingCapacity() * 100;
    }
}
