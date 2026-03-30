package com.usermanagement.security;

import com.usermanagement.config.AppProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JWT Token Provider 单元测试
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private AppProperties appProperties;

    private static final String TEST_SECRET = "test-secret-key-for-unit-testing-must-be-long-enough";
    private static final String TEST_ISSUER = "test-issuer";
    private static final Long TEST_EXPIRATION = 3600000L; // 1 hour
    private static final Long TEST_REFRESH_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.getJwt().setSecret(TEST_SECRET);
        appProperties.getJwt().setExpiration(TEST_EXPIRATION);
        appProperties.getJwt().setRefreshExpiration(TEST_REFRESH_EXPIRATION);
        appProperties.getJwt().setIssuer(TEST_ISSUER);

        jwtTokenProvider = new JwtTokenProvider(appProperties);
    }

    @Test
    void shouldGenerateAccessToken_whenValidAuthentication() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication authentication = createAuthentication(userId, email, authorities);

        // When
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    void shouldGenerateRefreshToken_whenValidAuthentication() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication authentication = createAuthentication(userId, email, authorities);

        // When
        String token = jwtTokenProvider.generateRefreshToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    @Test
    void shouldExtractUserIdFromToken_whenValidToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication authentication = createAuthentication(userId, email, authorities);
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // When
        String extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(extractedUserId).isEqualTo(userId.toString());
    }

    @Test
    void shouldExtractEmailFromToken_whenValidToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication authentication = createAuthentication(userId, email, authorities);
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // When
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void shouldExtractAuthoritiesFromToken_whenValidToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("user:read")
        );
        Authentication authentication = createAuthentication(userId, email, authorities);
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // When
        Collection<? extends GrantedAuthority> extractedAuthorities =
            jwtTokenProvider.getAuthoritiesFromToken(token);

        // Then
        assertThat(extractedAuthorities).hasSize(2);
        assertThat(extractedAuthorities).extracting(GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ROLE_USER", "user:read");
    }

    @Test
    void shouldValidateToken_whenValidToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication authentication = createAuthentication(userId, email, authorities);
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTokenIsExpired() {
        // Given
        AppProperties expiredProps = new AppProperties();
        expiredProps.getJwt().setSecret(TEST_SECRET);
        expiredProps.getJwt().setExpiration(100L); // 100ms
        expiredProps.getJwt().setIssuer(TEST_ISSUER);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);

        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication authentication = createAuthentication(userId, email, authorities);
        String token = expiredProvider.generateAccessToken(authentication);

        // Wait for token to expire
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isValid = expiredProvider.validateToken(token);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenGetUserIdFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(invalidToken))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldGetExpirationDateFromToken_whenValidToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication authentication = createAuthentication(userId, email, authorities);
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // When
        java.util.Date expiration = jwtTokenProvider.getExpirationDateFromToken(token);

        // Then
        assertThat(expiration).isAfter(java.util.Date.from(java.time.Instant.now()));
    }

    @Test
    void shouldGetClaimsFromToken_whenValidToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication authentication = createAuthentication(userId, email, authorities);
        String token = jwtTokenProvider.generateAccessToken(authentication);

        // When
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email")).isEqualTo(email);
        assertThat(claims.getIssuer()).isEqualTo(TEST_ISSUER);
    }

    @Test
    void refreshTokenShouldHaveLongerExpirationThanAccessToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        Authentication authentication = createAuthentication(userId, email, authorities);

        // When
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        java.util.Date accessExpiration = jwtTokenProvider.getExpirationDateFromToken(accessToken);
        java.util.Date refreshExpiration = jwtTokenProvider.getExpirationDateFromToken(refreshToken);

        // Then
        assertThat(refreshExpiration).isAfter(accessExpiration);
    }

    private Authentication createAuthentication(
        UUID userId,
        String email,
        Collection<? extends GrantedAuthority> authorities
    ) {
        CustomUserDetails userDetails = new CustomUserDetails(userId.toString(), email, null, authorities);

        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            authorities
        );
    }
}
