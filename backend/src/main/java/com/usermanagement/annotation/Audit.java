package com.usermanagement.annotation;

import com.usermanagement.domain.AuditOperationType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解 - 用于标记需要记录审计日志的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {

    /**
     * 操作类型
     */
    AuditOperationType operationType();

    /**
     * 资源类型 (如：USER, ROLE, PERMISSION, DEPARTMENT)
     */
    String resourceType();

    /**
     * 资源 ID 的 SpEL 表达式
     * 例如："#id", "#request.id", "#result.id"
     */
    String resourceId() default "";

    /**
     * 操作描述的 SpEL 表达式
     * 例如："'创建用户：' + #request.email"
     */
    String description() default "";

    /**
     * 是否记录旧值
     */
    boolean includeOldValue() default true;

    /**
     * 是否记录新值
     */
    boolean includeNewValue() default true;
}
