package com.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.Objects;
import java.util.UUID;

/**
 * 权限实体 - 细粒度权限定义
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
@Table(name = "permission")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE permission SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Permission extends BaseEntity {

    /**
     * 权限名称
     */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /**
     * 权限代码 - 如 user:create
     */
    @Column(name = "code", length = 100, nullable = false, unique = true)
    private String code;

    /**
     * 权限类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private PermissionType type;

    /**
     * 资源名称 - 如 user, department, role
     */
    @Column(name = "resource", length = 50, nullable = false)
    private String resource;

    /**
     * 操作 - 如 create/update/delete/read
     */
    @Column(name = "action", length = 50)
    private String action;

    /**
     * 父权限 ID - 用于权限树
     */
    @Column(name = "parent_id")
    private UUID parentId;

    /**
     * 图标 - 菜单权限使用
     */
    @Column(name = "icon", length = 100)
    private String icon;

    /**
     * 前端路由 - 菜单权限使用
     */
    @Column(name = "route", length = 200)
    private String route;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 权限状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private PermissionStatus status = PermissionStatus.ACTIVE;

    /**
     * 判断是否为菜单权限
     */
    public boolean isMenu() {
        return this.type == PermissionType.MENU;
    }

    /**
     * 判断是否为激活状态
     */
    public boolean isActive() {
        return this.status == PermissionStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Permission that = (Permission) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", type=" + type +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", status=" + status +
                '}';
    }
}
