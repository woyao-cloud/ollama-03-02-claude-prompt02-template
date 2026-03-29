package com.usermanagement.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * Spring Security UserDetails 实现 - 用于认证流程
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
public class CustomUserDetails implements UserDetails {

    private final String userId;
    private final String email;
    private final UUID departmentId;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String userId, String email, UUID departmentId, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.departmentId = departmentId;
        this.authorities = authorities;
    }

    public CustomUserDetails(String userId, String email, Collection<? extends GrantedAuthority> authorities) {
        this(userId, email, null, authorities);
    }

    /**
     * 获取用户 ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 获取邮箱
     */
    public String getEmail() {
        return email;
    }

    /**
     * 获取部门 ID
     */
    public UUID getDepartmentId() {
        return departmentId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    /**
     * 从 UUID 创建用户 ID 字符串
     */
    public static String createUserId(UUID uuid) {
        return uuid.toString();
    }

    /**
     * 从字符串解析 UUID
     */
    public static UUID parseUserId(String userId) {
        return UUID.fromString(userId);
    }
}
