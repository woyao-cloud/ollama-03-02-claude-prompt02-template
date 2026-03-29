package com.usermanagement.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CustomUserDetails 单元测试
 */
class CustomUserDetailsTest {

    @Test
    @DisplayName("创建 CustomUserDetails")
    void shouldCreateCustomUserDetails() {
        // Given
        String userId = UUID.randomUUID().toString();
        String email = "test@example.com";
        UUID departmentId = UUID.randomUUID();
        var authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("user:read")
        );

        // When
        CustomUserDetails userDetails = new CustomUserDetails(userId, email, departmentId, authorities);

        // Then
        assertThat(userDetails.getUserId()).isEqualTo(userId);
        assertThat(userDetails.getEmail()).isEqualTo(email);
        assertThat(userDetails.getDepartmentId()).isEqualTo(departmentId);
        assertThat(userDetails.getUsername()).isEqualTo(userId);
        assertThat(userDetails.getAuthorities()).hasSize(2);
    }

    @Test
    @DisplayName("创建 CustomUserDetails - 不带部门 ID")
    void shouldCreateCustomUserDetailsWithoutDepartmentId() {
        // Given
        String userId = UUID.randomUUID().toString();
        String email = "test@example.com";
        var authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );

        // When
        CustomUserDetails userDetails = new CustomUserDetails(userId, email, authorities);

        // Then
        assertThat(userDetails.getUserId()).isEqualTo(userId);
        assertThat(userDetails.getEmail()).isEqualTo(email);
        assertThat(userDetails.getDepartmentId()).isNull();
        assertThat(userDetails.getAuthorities()).hasSize(1);
    }

    @Test
    @DisplayName("UserDetails 默认启用")
    void shouldBeEnabledByDefault() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(
            UUID.randomUUID().toString(),
            "test@example.com",
            null,
            List.of()
        );

        // Then
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("UserDetails 密码为空")
    void shouldHaveNullPassword() {
        // Given
        CustomUserDetails userDetails = new CustomUserDetails(
            UUID.randomUUID().toString(),
            "test@example.com",
            null,
            List.of()
        );

        // Then
        assertThat(userDetails.getPassword()).isNull();
    }

    @Test
    @DisplayName("UUID 转换工具方法")
    void shouldConvertUUID() {
        // Given
        UUID uuid = UUID.randomUUID();

        // When
        String userId = CustomUserDetails.createUserId(uuid);
        UUID parsed = CustomUserDetails.parseUserId(userId);

        // Then
        assertThat(userId).isEqualTo(uuid.toString());
        assertThat(parsed).isEqualTo(uuid);
    }
}
