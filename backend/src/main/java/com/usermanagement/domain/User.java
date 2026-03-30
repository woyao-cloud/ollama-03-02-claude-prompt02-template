package com.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 用户实体 - 系统用户账户
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "\"user\"")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE \"user\" SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class User extends BaseEntity {

    /**
     * 邮箱 - 唯一标识
     */
    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    /**
     * 密码哈希
     */
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    /**
     * 名
     */
    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    /**
     * 姓
     */
    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 头像 URL
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * 所属部门 ID
     */
    @Column(name = "department_id")
    private UUID departmentId;

    /**
     * 用户状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    /**
     * 邮箱是否已验证
     */
    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    /**
     * 登录失败次数
     */
    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    /**
     * 锁定截止时间
     */
    @Column(name = "locked_until")
    private Instant lockedUntil;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /**
     * 最后登录 IP
     */
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    /**
     * 密码修改时间
     */
    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    /**
     * 所属角色 (多对多关系)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * 判断用户是否被锁定
     */
    public boolean isLocked() {
        return this.status == UserStatus.LOCKED ||
                (this.lockedUntil != null && this.lockedUntil.isAfter(Instant.now()));
    }

    /**
     * 判断用户是否激活
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    /**
     * 获取全名
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + getId() + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", status=" + status +
                '}';
    }
}
