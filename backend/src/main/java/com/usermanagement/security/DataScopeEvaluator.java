package com.usermanagement.security;

import com.usermanagement.domain.DataScope;
import com.usermanagement.domain.Department;
import com.usermanagement.domain.RoleDataScope;
import com.usermanagement.repository.DepartmentRepository;
import com.usermanagement.repository.RoleDataScopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 数据范围评估器 - 根据用户数据权限范围计算可访问的部门 ID 列表
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class DataScopeEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(DataScopeEvaluator.class);

    private final DepartmentRepository departmentRepository;
    private final RoleDataScopeRepository roleDataScopeRepository;

    public DataScopeEvaluator(DepartmentRepository departmentRepository,
                              RoleDataScopeRepository roleDataScopeRepository) {
        this.departmentRepository = departmentRepository;
        this.roleDataScopeRepository = roleDataScopeRepository;
    }

    /**
     * 评估数据范围，返回可访问的部门 ID 列表
     * 空列表表示：
     * - ALL: 不需要过滤（访问全部）
     * - SELF: 按用户 ID 过滤（在 SQL 中处理）
     * - CUSTOM: 待扩展
     *
     * @param dataScope 数据范围
     * @param currentUser 当前用户
     * @return 可访问的部门 ID 列表
     */
    public List<UUID> evaluateDataScope(DataScope dataScope, CustomUserDetails currentUser) {
        if (currentUser == null || currentUser.getDepartmentId() == null) {
            logger.warn("当前用户或部门 ID 为空，返回空列表");
            return List.of();
        }

        switch (dataScope) {
            case ALL:
                // 全部数据，返回空列表表示不过滤
                return List.of();
            case SELF:
                // 个人数据，返回空列表，由 SQL WHERE user_id = ? 处理
                return List.of();
            case DEPT:
                // 本部门 + 下级部门
                return getDeptAndChildDeptIds(currentUser);
            case CUSTOM:
                // 自定义范围：从 role_data_scope 表读取配置
                return getCustomDeptIds(currentUser);
            default:
                logger.warn("未知数据范围：{}", dataScope);
                return List.of();
        }
    }

    /**
     * 获取用户有权限的部门 ID 列表（包含本部门及所有下级部门）
     *
     * @param currentUser 当前用户
     * @param dataScope 数据范围
     * @return 部门 ID 列表
     */
    public List<UUID> getUserScopedDepartmentIds(CustomUserDetails currentUser, DataScope dataScope) {
        return evaluateDataScope(dataScope, currentUser);
    }

    /**
     * 获取本部门及下级部门 ID 列表
     *
     * @param currentUser 当前用户
     * @return 部门 ID 列表
     */
    private List<UUID> getDeptAndChildDeptIds(CustomUserDetails currentUser) {
        UUID deptId = currentUser.getDepartmentId();

        // 获取当前部门
        return departmentRepository.findById(deptId)
            .map(dept -> {
                List<UUID> result = new ArrayList<>();
                result.add(deptId);

                // 构建路径前缀，查找所有下级部门
                String pathPrefix = buildPathPrefix(dept.getPath());
                if (!pathPrefix.isEmpty()) {
                    List<Department> childDepts = departmentRepository.findByPathStartingWith(pathPrefix);
                    childDepts.forEach(d -> result.add(d.getId()));
                }

                return result;
            })
            .orElseGet(() -> {
                logger.warn("部门不存在：{}", deptId);
                return List.of();
            });
    }

    /**
     * 构建路径前缀用于查询下级部门
     *
     * @param currentPath 当前部门路径
     * @return 路径前缀
     */
    private String buildPathPrefix(String currentPath) {
        if (currentPath == null || currentPath.isEmpty()) {
            return "";
        }
        // 确保路径以 / 结尾，例如 /1/2/ 用于匹配 /1/2/3, /1/2/4 等
        return currentPath.endsWith("/") ? currentPath : currentPath + "/";
    }

    /**
     * 构建部门路径前缀（公开方法，供其他组件使用）
     *
     * @param currentUser 当前用户
     * @return 路径前缀
     */
    public String buildDeptPathPrefix(CustomUserDetails currentUser) {
        if (currentUser == null || currentUser.getDepartmentId() == null) {
            return "";
        }

        return departmentRepository.findById(currentUser.getDepartmentId())
            .map(dept -> buildPathPrefix(dept.getPath()))
            .orElse("");
    }

    /**
     * 获取自定义数据权限范围的部门 ID 列表
     * 从 role_data_scope 表读取用户的角色配置的自定义部门范围
     *
     * @param currentUser 当前用户
     * @return 自定义范围的部门 ID 列表
     */
    private List<UUID> getCustomDeptIds(CustomUserDetails currentUser) {
        // 需要从用户角色中读取自定义配置
        // 这里简化实现：返回空列表，实际应该在调用前根据用户角色加载配置
        logger.debug("CUSTOM 数据范围：需要从角色配置中加载自定义部门范围");
        return List.of();
    }

    /**
     * 根据角色 ID 列表和自定义配置，获取可访问的部门 ID 列表
     *
     * @param roleIds 用户的角色 ID 列表
     * @return 自定义范围的部门 ID 列表
     */
    public List<UUID> getCustomDeptIdsByRoles(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        List<UUID> result = new ArrayList<>();
        for (UUID roleId : roleIds) {
            // 读取该角色的 CUSTOM 范围配置
            List<RoleDataScope> scopeConfigs = roleDataScopeRepository.findByRoleIdAndScopeType(
                roleId, RoleDataScope.ScopeType.DEPT);

            for (RoleDataScope config : scopeConfigs) {
                // scopeValue 格式：逗号分隔的部门 ID 列表
                String[] deptIdStrings = config.getScopeValue().split(",");
                for (String deptIdStr : deptIdStrings) {
                    try {
                        UUID deptId = UUID.fromString(deptIdStr.trim());
                        if (!result.contains(deptId)) {
                            result.add(deptId);
                        }
                    } catch (IllegalArgumentException e) {
                        logger.warn("无效的部门 ID 格式：{}", deptIdStr);
                    }
                }
            }
        }

        logger.debug("CUSTOM 数据范围：解析出 {} 个部门", result.size());
        return result;
    }
}
