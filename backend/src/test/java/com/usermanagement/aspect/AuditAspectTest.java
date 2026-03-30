package com.usermanagement.aspect;

import com.usermanagement.annotation.Audit;
import com.usermanagement.domain.AuditOperationType;
import com.usermanagement.security.CustomUserDetails;
import com.usermanagement.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * AuditAspect 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditLogService auditLogService;

    private AuditAspect auditAspect;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String TEST_USER_ID_STR = TEST_USER_ID.toString();
    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final String TEST_CLIENT_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "Mozilla/5.0";

    @BeforeEach
    void setUp() {
        auditAspect = new AuditAspect(auditLogService);
    }

    @Nested
    @DisplayName("审计日志拦截器测试")
    class AuditInterceptorTests {

        @Test
        @DisplayName("应该拦截带有@Audit 注解的方法并记录日志")
        void shouldInterceptMethodWithAuditAnnotation() throws Throwable {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr(TEST_CLIENT_IP);
            request.addHeader("User-Agent", TEST_USER_AGENT);
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

            CustomUserDetails userDetails = new CustomUserDetails(
                TEST_USER_ID_STR, TEST_USER_EMAIL, List.of(new SimpleGrantedAuthority("USER"))
            );

            SecurityContext securityContext = mock(SecurityContext.class);
            given(securityContext.getAuthentication()).willReturn(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                    userDetails, null, userDetails.getAuthorities()
                )
            );

            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                TestService testService = new TestService();

                // When
                testService.createResource("test-resource");

                // Then
                then(auditLogService).should().logAudit(
                    any(UUID.class),
                    any(String.class),
                    any(AuditOperationType.class),
                    any(String.class),
                    any(UUID.class),
                    any(String.class),
                    any(Map.class),
                    any(Map.class),
                    any(String.class),
                    any(String.class),
                    any(String.class),
                    any(String.class)
                );
            }
        }

        @Test
        @DisplayName("应该记录操作成功时的审计日志")
        void shouldLogSuccessAudit() throws Throwable {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr(TEST_CLIENT_IP);
            request.addHeader("User-Agent", TEST_USER_AGENT);
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

            CustomUserDetails userDetails = new CustomUserDetails(
                TEST_USER_ID_STR, TEST_USER_EMAIL, List.of(new SimpleGrantedAuthority("USER"))
            );

            SecurityContext securityContext = mock(SecurityContext.class);
            given(securityContext.getAuthentication()).willReturn(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                    userDetails, null, userDetails.getAuthorities()
                )
            );

            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                TestService testService = new TestService();

                // When
                testService.createResource("test-resource");

                // Then
                ArgumentCaptor<String> resultCaptor = ArgumentCaptor.forClass(String.class);
                ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
                then(auditLogService).should().logAudit(
                    any(UUID.class),
                    any(String.class),
                    eq(AuditOperationType.CREATE),
                    eq("TEST"),
                    any(UUID.class),
                    eq("创建测试资源"),
                    any(Map.class),
                    any(Map.class),
                    eq(TEST_CLIENT_IP),
                    eq(TEST_USER_AGENT),
                    resultCaptor.capture(),
                    errorCaptor.capture()
                );

                assertThat(resultCaptor.getValue()).isEqualTo("SUCCESS");
                assertThat(errorCaptor.getValue()).isNull();
            }
        }

        @Test
        @DisplayName("应该记录操作失败时的审计日志")
        void shouldLogFailedAudit() throws Throwable {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr(TEST_CLIENT_IP);
            request.addHeader("User-Agent", TEST_USER_AGENT);
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

            CustomUserDetails userDetails = new CustomUserDetails(
                TEST_USER_ID_STR, TEST_USER_EMAIL, List.of(new SimpleGrantedAuthority("USER"))
            );

            SecurityContext securityContext = mock(SecurityContext.class);
            given(securityContext.getAuthentication()).willReturn(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                    userDetails, null, userDetails.getAuthorities()
                )
            );

            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                TestService testService = new TestService();

                // When & Then
                try {
                    testService.createResourceWithFailure();
                } catch (RuntimeException e) {
                    assertThat(e.getMessage()).contains("模拟失败");
                }

                ArgumentCaptor<String> resultCaptor = ArgumentCaptor.forClass(String.class);
                ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
                then(auditLogService).should().logAudit(
                    any(UUID.class),
                    any(String.class),
                    eq(AuditOperationType.CREATE),
                    eq("TEST"),
                    any(UUID.class),
                    eq("创建测试资源（失败）"),
                    any(Map.class),
                    any(Map.class),
                    eq(TEST_CLIENT_IP),
                    eq(TEST_USER_AGENT),
                    resultCaptor.capture(),
                    errorCaptor.capture()
                );

                assertThat(resultCaptor.getValue()).isEqualTo("FAILURE");
                assertThat(errorCaptor.getValue()).contains("模拟失败");
            }
        }

        @Test
        @DisplayName("应该从返回值中解析资源 ID")
        void shouldParseResourceIdFromResult() throws Throwable {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr(TEST_CLIENT_IP);
            request.addHeader("User-Agent", TEST_USER_AGENT);
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

            CustomUserDetails userDetails = new CustomUserDetails(
                TEST_USER_ID_STR, TEST_USER_EMAIL, List.of(new SimpleGrantedAuthority("USER"))
            );

            SecurityContext securityContext = mock(SecurityContext.class);
            given(securityContext.getAuthentication()).willReturn(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                    userDetails, null, userDetails.getAuthorities()
                )
            );

            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                TestServiceWithResult testService = new TestServiceWithResult();
                UUID resourceId = UUID.randomUUID();
                testService.setResourceIdToReturn(resourceId);

                // When
                testService.createResourceWithIdInResult();

                // Then
                ArgumentCaptor<UUID> resourceIdCaptor = ArgumentCaptor.forClass(UUID.class);
                then(auditLogService).should().logAudit(
                    any(UUID.class),
                    any(String.class),
                    eq(AuditOperationType.CREATE),
                    eq("TEST"),
                    resourceIdCaptor.capture(),
                    any(String.class),
                    any(Map.class),
                    any(Map.class),
                    any(String.class),
                    any(String.class),
                    any(String.class),
                    any(String.class)
                );

                assertThat(resourceIdCaptor.getValue()).isEqualTo(resourceId);
            }
        }

        @Test
        @DisplayName("无认证用户时使用匿名")
        void shouldUseAnonymousWhenNoAuthentication() throws Throwable {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr(TEST_CLIENT_IP);
            request.addHeader("User-Agent", TEST_USER_AGENT);
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

            SecurityContext securityContext = mock(SecurityContext.class);
            given(securityContext.getAuthentication()).willReturn(null);

            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                mocked.when(SecurityContextHolder::getContext).thenReturn(securityContext);

                TestService testService = new TestService();

                // When
                testService.createResource("test-resource");

                // Then
                then(auditLogService).should().logAudit(
                    any(UUID.class),
                    eq("anonymous"),
                    any(AuditOperationType.class),
                    any(String.class),
                    any(UUID.class),
                    any(String.class),
                    any(Map.class),
                    any(Map.class),
                    any(String.class),
                    any(String.class),
                    any(String.class),
                    any(String.class)
                );
            }
        }
    }

    /**
     * 测试服务类 - 基本测试
     */
    static class TestService {

        @Audit(
            operationType = AuditOperationType.CREATE,
            resourceType = "TEST",
            description = "创建测试资源"
        )
        public void createResource(String name) {
            // 模拟业务逻辑
        }

        @Audit(
            operationType = AuditOperationType.CREATE,
            resourceType = "TEST",
            description = "创建测试资源（失败）"
        )
        public void createResourceWithFailure() {
            throw new RuntimeException("模拟失败");
        }
    }

    /**
     * 测试服务类 - 返回值测试
     */
    static class TestServiceWithResult {

        private UUID resourceIdToReturn;

        public void setResourceIdToReturn(UUID resourceId) {
            this.resourceIdToReturn = resourceId;
        }

        @Audit(
            operationType = AuditOperationType.CREATE,
            resourceType = "TEST",
            resourceId = "#result.id",
            description = "创建测试资源（带返回值 ID）"
        )
        public TestResource createResourceWithIdInResult() {
            TestResource resource = new TestResource();
            resource.setId(resourceIdToReturn);
            return resource;
        }
    }

    /**
     * 测试资源类
     */
    static class TestResource {
        private UUID id;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }
    }
}
