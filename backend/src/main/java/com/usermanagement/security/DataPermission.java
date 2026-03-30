package com.usermanagement.security;

import java.lang.annotation.*;

/**
 * 数据权限注解 - 用于方法级数据权限控制
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 数据权限范围，不指定时从角色中获取
     */
    DataScope value() default DataScope.ALL;

    /**
     * 用户参数名称，用于获取当前用户
     * 默认从 SecurityContext 获取当前用户
     */
    String userParam() default "";
}
