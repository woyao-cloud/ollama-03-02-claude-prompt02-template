package com.usermanagement.security;

import com.usermanagement.domain.DataScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 数据权限拦截器 - 用于构建数据权限 SQL 参数
 * 可在 MyBatis Interceptor 或 JPA Hibernate Filter 中使用
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class DataPermissionInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(DataPermissionInterceptor.class);

    private final DataScopeEvaluator evaluator;

    public DataPermissionInterceptor(DataScopeEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * 构建数据范围 SQL 参数
     * 返回的参数可用于 MyBatis 动态 SQL 或 JPA 查询
     *
     * @param dataScope 数据范围
     * @param currentUser 当前用户
     * @return SQL 参数 Map
     */
    public Map<String, Object> buildDataScopeParams(DataScope dataScope, CustomUserDetails currentUser) {
        Map<String, Object> params = new HashMap<>();

        if (currentUser == null) {
            logger.warn("当前用户为空，不应用数据权限");
            return params;
        }

        List<UUID> departmentIds = evaluator.evaluateDataScope(dataScope, currentUser);

        switch (dataScope) {
            case ALL:
                // 全部数据，不需要额外参数
                break;
            case SELF:
                // 个人数据，传入 userId
                params.put("userId", currentUser.getUserId());
                break;
            case DEPT:
                // 本部门及下级部门，传入部门 ID 列表
                params.put("departmentIds", departmentIds);
                break;
            case CUSTOM:
                // 自定义范围，待扩展
                logger.debug("CUSTOM 数据范围暂未实现");
                break;
            default:
                logger.warn("未知数据范围：{}", dataScope);
        }

        return params;
    }

    /**
     * 从 SecurityContext 获取当前用户
     *
     * @return 当前用户，无法获取时返回 null
     */
    public CustomUserDetails getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                logger.debug("SecurityContext 中认证信息为空");
                return null;
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }

            logger.debug("Principal 不是 CustomUserDetails 类型：{}",
                principal != null ? principal.getClass().getSimpleName() : "null");
            return null;
        } catch (Exception e) {
            logger.warn("获取当前用户失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断是否应该应用数据权限
     *
     * @param dataScope 数据范围（从注解获取）
     * @return 是否应用
     */
    public boolean shouldApplyDataScope(DataScope dataScope) {
        return dataScope != null;
    }

    /**
     * 生成数据权限 SQL WHERE 子句
     * 用于动态拼接到查询 SQL 中
     *
     * @param dataScope 数据范围
     * @param tableName 表名
     * @param userIdColumn 用户 ID 列名
     * @param deptIdColumn 部门 ID 列名
     * @return SQL WHERE 子句（不含 WHERE 关键字）
     */
    public String generateDataScopeSql(
        DataScope dataScope,
        String tableName,
        String userIdColumn,
        String deptIdColumn
    ) {
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser == null) {
            return "1=1"; // 无权限限制
        }

        switch (dataScope) {
            case ALL:
                return "1=1";
            case SELF:
                return tableName + "." + userIdColumn + " = '" + currentUser.getUserId() + "'";
            case DEPT:
                List<UUID> deptIds = evaluator.evaluateDataScope(dataScope, currentUser);
                if (deptIds.isEmpty()) {
                    return "1=0"; // 无权限
                }
                String ids = String.join(",",
                    deptIds.stream().map(UUID::toString).toArray(String[]::new));
                return tableName + "." + deptIdColumn + " IN (" + ids + ")";
            case CUSTOM:
                return "1=1"; // 待扩展
            default:
                return "1=1";
        }
    }
}
