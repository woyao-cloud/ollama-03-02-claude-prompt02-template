package com.usermanagement.service;

import com.usermanagement.domain.Department;
import com.usermanagement.repository.DepartmentRepository;
import com.usermanagement.web.dto.DepartmentDTO;
import com.usermanagement.web.dto.DepartmentMoveRequest;
import com.usermanagement.web.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 部门移动服务实现类
 * <p>
 * 负责处理部门层级调整，包含循环依赖检查
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
@Transactional
public class DepartmentMoveServiceImpl implements DepartmentMoveService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentMoveServiceImpl(DepartmentRepository departmentRepository,
                                     DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    @Override
    public DepartmentDTO moveDepartment(UUID id, DepartmentMoveRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在：" + id));

        // 检查是否移动到自身
        if (request.getParentId() != null && request.getParentId().equals(id.toString())) {
            throw new IllegalArgumentException("不能将部门移动到自身");
        }

        // 处理移动到根节点的情况
        if (request.getParentId() == null) {
            department.setParentId(null);
            department.setLevel(1);
            department.setPath("/" + department.getId());
            if (request.getSortOrder() != null) {
                department.setSortOrder(request.getSortOrder());
            }
            Department saved = departmentRepository.save(department);
            return departmentMapper.toDto(saved);
        }

        UUID newParentId = UUID.fromString(request.getParentId());
        Department newParent = departmentRepository.findById(newParentId)
                .orElseThrow(() -> new IllegalArgumentException("父部门不存在：" + request.getParentId()));

        // 循环依赖检查：不能移动到其子部门下
        checkCircularDependency(department, newParent);

        // 计算新层级
        int newLevel = newParent.getLevel() + 1;
        if (newLevel > 5) {
            throw new IllegalArgumentException("部门层级不能超过 5 级");
        }

        // 更新部门信息
        department.setParentId(newParentId);
        department.setLevel(newLevel);
        department.setPath(newParent.getPath() + "/" + department.getId());

        if (request.getSortOrder() != null) {
            department.setSortOrder(request.getSortOrder());
        }

        Department saved = departmentRepository.save(department);
        return departmentMapper.toDto(saved);
    }

    /**
     * 检查循环依赖
     * <p>
     * 确保不能将部门移动到其子部门下（包括间接子部门）
     *
     * @param department 要移动的部门
     * @param newParent  新父部门
     */
    private void checkCircularDependency(Department department, Department newParent) {
        // 获取新父部门的所有子部门（包括间接子部门）
        String parentPathPrefix = newParent.getPath() + "/";
        List<Department> descendants = departmentRepository.findByPathStartingWith(parentPathPrefix);

        // 检查要移动的部门是否在新父部门的子树中
        for (Department descendant : descendants) {
            if (descendant.getId().equals(department.getId())) {
                throw new IllegalArgumentException("不能将部门移动到其子部门下");
            }
        }
    }
}
