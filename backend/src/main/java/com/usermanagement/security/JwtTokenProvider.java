package com.usermanagement.security;

import com.usermanagement.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Token Provider - 负责生成、验证和解析 JWT Token
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";
    private static final String USER_ID_KEY = "userId";
    private static final String EMAIL_KEY = "email";

    private final SecretKey secretKey;
    private final long expiration;
    private final long refreshExpiration;
    private final String issuer;

    public JwtTokenProvider(AppProperties appProperties) {
        this.secretKey = Keys.hmacShaKeyFor(
            appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8)
        );
        this.expiration = appProperties.getJwt().getExpiration();
        this.refreshExpiration = appProperties.getJwt().getRefreshExpiration();
        this.issuer = appProperties.getJwt().getIssuer();
    }

    /**
     * 生成 Access Token
     *
     * @param authentication 认证信息
     * @return JWT Access Token
     */
    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, expiration);
    }

    /**
     * 生成 Refresh Token
     *
     * @param authentication 认证信息
     * @return JWT Refresh Token
     */
    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, refreshExpiration);
    }

    /**
     * 生成 JWT Token
     *
     * @param authentication 认证信息
     * @param expirationTime 过期时间（毫秒）
     * @return JWT Token
     */
    private String generateToken(Authentication authentication, long expirationTime) {
        String userId = authentication.getName();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
            .subject(userId)
            .claim(USER_ID_KEY, userId)
            .claim(EMAIL_KEY, userDetails.getEmail())
            .claim(AUTHORITIES_KEY, authorities)
            .issuer(issuer)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact();
    }

    /**
     * 从 Token 中获取用户 ID
     *
     * @param token JWT Token
     * @return 用户 ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 从 Token 中获取邮箱
     *
     * @param token JWT Token
     * @return 邮箱
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(EMAIL_KEY, String.class);
    }

    /**
     * 从 Token 中获取权限列表
     *
     * @param token JWT Token
     * @return 权限列表
     */
    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String authorities = claims.get(AUTHORITIES_KEY, String.class);
        if (authorities == null || authorities.isEmpty()) {
            return List.of();
        }
        return List.of(authorities.split(","))
            .stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    /**
     * 从 Token 中获取 Claims
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * 获取 Token 过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从 Token 获取认证信息
     *
     * @param token JWT Token
     * @return Authentication
     */
    public Authentication getAuthenticationFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String userId = claims.getSubject();
        String email = claims.get(EMAIL_KEY, String.class);
        List<GrantedAuthority> authorities = getAuthoritiesFromToken(token);

        CustomUserDetails userDetails = new CustomUserDetails(userId, email, null, authorities);
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            authorities
        );
    }
}
