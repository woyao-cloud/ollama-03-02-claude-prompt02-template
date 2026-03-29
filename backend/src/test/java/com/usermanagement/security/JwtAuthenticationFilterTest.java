package com.usermanagement.security;

import com.usermanagement.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * JwtAuthenticationFilter 单元测试
 */
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "test-secret-key-for-unit-testing-must-be-long-enough";
    private static final String TEST_ISSUER = "test-issuer";
    private static final Long TEST_EXPIRATION = 3600000L;
    private static final Long TEST_REFRESH_EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getJwt().setSecret(TEST_SECRET);
        appProperties.getJwt().setExpiration(TEST_EXPIRATION);
        appProperties.getJwt().setRefreshExpiration(TEST_REFRESH_EXPIRATION);
        appProperties.getJwt().setIssuer(TEST_ISSUER);

        jwtTokenProvider = new JwtTokenProvider(appProperties);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("过滤器设置认证 - 有效 Token")
    void shouldSetAuthentication_whenValidToken() throws ServletException, IOException {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        CustomUserDetails userDetails = new CustomUserDetails(userId.toString(), email, null, List.of());
        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            List.of()
        );
        String token = jwtTokenProvider.generateAccessToken(authentication);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(userId.toString());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("过滤器不设置认证 - 无 Token")
    void shouldNotSetAuthentication_whenNoToken() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertThat(result).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("过滤器不设置认证 - 无效 Token")
    void shouldNotSetAuthentication_whenInvalidToken() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertThat(result).isNull();
        assertThat(request.getAttribute("jwt_invalid")).isEqualTo(true);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("过滤器不设置认证 - Token 过期")
    void shouldNotSetAuthentication_whenExpiredToken() throws ServletException, IOException, InterruptedException {
        // Given
        AppProperties expiredProps = new AppProperties();
        expiredProps.getJwt().setSecret(TEST_SECRET);
        expiredProps.getJwt().setExpiration(100L);
        expiredProps.getJwt().setIssuer(TEST_ISSUER);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);

        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        CustomUserDetails userDetails = new CustomUserDetails(userId.toString(), email, null, List.of());
        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            List.of()
        );
        String token = expiredProvider.generateAccessToken(authentication);

        // Wait for token to expire
        Thread.sleep(200);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertThat(result).isNull();
        assertThat(request.getAttribute("jwt_expired")).isEqualTo(true);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("过滤器不设置认证 - 无 Bearer 前缀")
    void shouldNotSetAuthentication_whenNoBearerPrefix() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "invalid-prefix token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication result = SecurityContextHolder.getContext().getAuthentication();
        assertThat(result).isNull();

        verify(filterChain).doFilter(request, response);
    }
}
