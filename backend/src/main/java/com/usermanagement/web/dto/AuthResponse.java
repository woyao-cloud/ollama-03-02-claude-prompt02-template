package com.usermanagement.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 认证响应 DTO
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;

    /**
     * 创建成功响应
     */
    public static AuthResponse success(
        String accessToken,
        String refreshToken,
        String userId,
        String email,
        String firstName,
        String lastName,
        List<String> roles
    ) {
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(86400000L) // 24 hours in milliseconds
            .userId(userId)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .roles(roles)
            .build();
    }
}
