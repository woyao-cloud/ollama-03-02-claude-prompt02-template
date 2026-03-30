package com.usermanagement.service;

import com.usermanagement.domain.AuditLog;
import com.usermanagement.domain.AuditOperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.timeout;

/**
 * AuditLogProducer 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuditLogProducerTest {

    @Mock
    private AuditLogService auditLogService;

    private AuditLogProducer auditLogProducer;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final String TEST_CLIENT_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "Mozilla/5.0";

    @BeforeEach
    void setUp() {
        auditLogProducer = new AuditLogProducer(auditLogService);
    }

    @Nested
    @DisplayName("异步审计日志生产测试")
    class AsyncAuditLogProducerTests {

        @Test
        @DisplayName("应该将审计日志放入队列")
        void shouldPutAuditLogToQueue() throws InterruptedException {
            // Given
            AuditLog auditLog = createAuditLog(AuditOperationType.CREATE);

            // When
            auditLogProducer.queueAuditLog(auditLog);

            // Then
            BlockingQueue<AuditLog> queue = getQueue();
            assertThat(queue).isNotEmpty();
            assertThat(queue.poll().getOperationType()).isEqualTo(AuditOperationType.CREATE);
        }

        @Test
        @DisplayName("应该异步消费队列并保存审计日志")
        void shouldConsumeQueueAndSaveAuditLog() throws InterruptedException {
            // Given
            AuditLog auditLog = createAuditLog(AuditOperationType.LOGIN);
            auditLogProducer.start();

            // When
            auditLogProducer.queueAuditLog(auditLog);

            // Then
            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            then(auditLogService).should(timeout(5000).times(1)).logAudit(
                captor.capture(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            );

            assertThat(captor.getValue().getOperationType()).isEqualTo(AuditOperationType.LOGIN);
        }

        @Test
        @DisplayName("应该处理多个审计日志")
        void shouldProcessMultipleAuditLogs() throws InterruptedException {
            // Given
            AuditLog log1 = createAuditLog(AuditOperationType.CREATE);
            AuditLog log2 = createAuditLog(AuditOperationType.UPDATE);
            AuditLog log3 = createAuditLog(AuditOperationType.DELETE);
            auditLogProducer.start();

            // When
            auditLogProducer.queueAuditLog(log1);
            auditLogProducer.queueAuditLog(log2);
            auditLogProducer.queueAuditLog(log3);

            // Then
            then(auditLogService).should(timeout(5000).times(3)).logAudit(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("应该优雅关闭")
        void shouldShutdownGracefully() throws InterruptedException {
            // Given
            auditLogProducer.start();
            AuditLog auditLog = createAuditLog(AuditOperationType.LOGOUT);
            auditLogProducer.queueAuditLog(auditLog);

            // When
            auditLogProducer.shutdown();

            // Then
            // 等待关闭完成
            Thread.sleep(1000);
            assertThat(auditLogProducer.isRunning()).isFalse();
        }

        @Test
        @DisplayName("队列满时应该阻塞")
        void shouldBlockWhenQueueIsFull() throws InterruptedException {
            // Given - 创建一个容量较小的生产者
            AuditLogProducer smallProducer = new AuditLogProducer(auditLogService, 5);
            smallProducer.start();

            // When - 填满队列
            for (int i = 0; i < 5; i++) {
                smallProducer.queueAuditLog(createAuditLog(AuditOperationType.CREATE));
            }

            // Then - 队列应该已满
            Thread.sleep(500);
            assertThat(smallProducer.getQueueSize()).isGreaterThanOrEqualTo(1);

            smallProducer.shutdown();
        }
    }

    // 辅助方法
    private AuditLog createAuditLog(AuditOperationType operationType) {
        return AuditLog.builder()
            .id(UUID.randomUUID())
            .userId(TEST_USER_ID)
            .userEmail(TEST_USER_EMAIL)
            .operationType(operationType)
            .resourceType("USER")
            .resourceId(UUID.randomUUID())
            .operationDescription("测试操作")
            .oldValue(null)
            .newValue(Map.of("test", "value"))
            .clientIp(TEST_CLIENT_IP)
            .userAgent(TEST_USER_AGENT)
            .operationResult("SUCCESS")
            .errorMessage(null)
            .createdAt(Instant.now())
            .build();
    }

    // 测试辅助方法
    private BlockingQueue<AuditLog> getQueue() throws Exception {
        java.lang.reflect.Field field = AuditLogProducer.class.getDeclaredField("auditLogQueue");
        field.setAccessible(true);
        return (BlockingQueue<AuditLog>) field.get(auditLogProducer);
    }
}
