package com.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 角色实体 - RBAC 权限模型
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
@Table(name = "role")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE role SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Role extends BaseEntity {

    /**
     * 角色名称
     */
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    /**
     * 角色代码 - 如 ROLE_ADMIN
     */
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 数据权限范围
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_scope", length = 20, nullable = false)
    @Builder.Default
    private DataScope dataScope = DataScope.ALL;

    /**
     * 角色状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private RoleStatus status = RoleStatus.ACTIVE;

    /**
     * 拥有权限 (多对多关系)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * 判断角色是否激活
     */
    public boolean isActive() {
        return this.status == RoleStatus.ACTIVE;
    }

    /**
     * 判断是否为管理员角色
     */
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(this.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Role role = (Role) o;
        return Objects.equals(getId(), role.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }

    @Override
    public String toString() {
        return "Role{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", dataScope=" + dataScope +
                ", status=" + status +
                '}';
    }
}
