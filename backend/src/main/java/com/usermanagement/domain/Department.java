package com.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.Objects;
import java.util.UUID;

/**
 * 部门实体 - 支持 5 级层级的树形结构 (Materialized Path)
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
@Table(name = "department")
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE department SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Department extends BaseEntity {

    /**
     * 部门名称
     */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /**
     * 部门代码 - 唯一标识
     */
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    /**
     * 父部门 ID - 根部门为 null
     */
    @Column(name = "parent_id")
    private UUID parentId;

    /**
     * 部门负责人 ID
     */
    @Column(name = "manager_id")
    private UUID managerId;

    /**
     * 部门层级 - 1 到 5 级
     * 1 = 公司，2 = 一级部门，3 = 二级部门，4 = 三级部门，5 = 四级部门
     */
    @Column(name = "level", nullable = false)
    private Integer level;

    /**
     * Materialized Path - 如 /1/2/5/10
     * 用于高效查询子树
     */
    @Column(name = "path", length = 500, nullable = false)
    private String path;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 部门状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private DepartmentStatus status = DepartmentStatus.ACTIVE;

    /**
     * 设置层级 - 验证范围 1-5
     */
    public void setLevel(Integer level) {
        if (level == null || level < 1 || level > 5) {
            throw new IllegalArgumentException("部门层级必须在 1-5 范围内");
        }
        this.level = level;
    }

    /**
     * 判断是否为根部门
     */
    public boolean isRoot() {
        return this.parentId == null;
    }

    /**
     * 判断是否为叶子部门（没有子部门）
     * 注意：实际判断需要查询数据库，此处仅基于层级做简单判断
     */
    public boolean isLeaf() {
        return this.level == 5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Department that = (Department) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }

    @Override
    public String toString() {
        return "Department{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", level=" + level +
                ", path='" + path + '\'' +
                ", status=" + status +
                '}';
    }
}
