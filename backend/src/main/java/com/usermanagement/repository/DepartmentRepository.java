package com.usermanagement.repository;

import com.usermanagement.domain.Department;
import com.usermanagement.domain.DepartmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 部门数据访问接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    /**
     * 根据部门代码查找
     *
     * @param code 部门代码
     * @return 部门 Optional
     */
    Optional<Department> findByCode(String code);

    /**
     * 根据层级查找部门
     *
     * @param level 层级
     * @return 部门列表
     */
    List<Department> findByLevel(Integer level);

    /**
     * 根据状态查找部门
     *
     * @param status 状态
     * @return 部门列表
     */
    List<Department> findByStatus(DepartmentStatus status);

    /**
     * 根据路径前缀查找子部门
     *
     * @param pathPrefix 路径前缀
     * @return 子部门列表
     */
    List<Department> findByPathStartingWith(String pathPrefix);

    /**
     * 根据父部门 ID 查找子部门
     *
     * @param parentId 父部门 ID
     * @return 子部门列表
     */
    List<Department> findByParentId(UUID parentId);

    /**
     * 检查代码是否已存在
     *
     * @param code 部门代码
     * @return 是否存在
     */
    boolean existsByCode(String code);

    /**
     * 查询所有部门（包括已删除的）
     *
     * @return 所有部门列表
     */
    @Query("SELECT d FROM Department d ORDER BY d.path")
    List<Department> findAllOrderedByPath();
}
