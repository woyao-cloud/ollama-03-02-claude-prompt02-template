package com.usermanagement.aspect;

import com.usermanagement.annotation.Audit;
import com.usermanagement.domain.AuditOperationType;
import com.usermanagement.security.CustomUserDetails;
import com.usermanagement.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 审计日志切面 - 拦截带有@Audit 注解的方法
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogService auditLogService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * 定义切点 - 匹配带有@Audit 注解的方法
     */
    @Pointcut("@annotation(com.usermanagement.annotation.Audit)")
    public void auditPointcut() {
    }

    /**
     * 环绕通知 - 记录审计日志
     */
    @Around("auditPointcut()")
    public Object aroundAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Audit auditAnnotation = method.getAnnotation(Audit.class);

        // 获取当前用户
        CustomUserDetails userDetails = getCurrentUser();
        UUID userId = userDetails != null ? UUID.fromString(userDetails.getUserId()) : null;
        String userEmail = userDetails != null ? userDetails.getEmail() : "anonymous";

        // 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        String clientIp = getClientIp(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : "unknown";

        // 获取审计参数
        AuditOperationType operationType = auditAnnotation.operationType();
        String resourceType = auditAnnotation.resourceType();
        String description = auditAnnotation.description();
        boolean includeOldValue = auditAnnotation.includeOldValue();
        boolean includeNewValue = auditAnnotation.includeNewValue();

        // 解析资源 ID
        UUID resourceId = parseResourceId(auditAnnotation.resourceId(), joinPoint, null);

        // 操作前的数据
        Map<String, Object> oldValue = null;

        Object result = null;
        String operationResult = "SUCCESS";
        String errorMessage = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();

            // 重新解析资源 ID（如果使用方法返回值）
            if (auditAnnotation.resourceId().startsWith("#result")) {
                resourceId = parseResourceId(auditAnnotation.resourceId(), joinPoint, result);
            }

            // 操作后的数据
            Map<String, Object> newValue = null;
            if (includeNewValue && result != null) {
                newValue = convertToMap(result);
            }

            return result;
        } catch (Throwable t) {
            operationResult = "FAILURE";
            errorMessage = t.getMessage();
            logger.error("审计操作失败：{}", description, t);
            throw t;
        } finally {
            // 记录审计日志
            try {
                auditLogService.logAudit(
                    userId,
                    userEmail,
                    operationType,
                    resourceType,
                    resourceId,
                    description,
                    oldValue,
                    includeNewValue ? convertToMap(result) : null,
                    clientIp,
                    userAgent,
                    operationResult,
                    errorMessage
                );
            } catch (Exception e) {
                logger.error("记录审计日志失败", e);
            }
        }
    }

    /**
     * 获取当前用户
     */
    private CustomUserDetails getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }
        } catch (Exception e) {
            // 无认证信息
        }
        return null;
    }

    /**
     * 获取当前 HTTP 请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            // 非 Web 环境
        }
        return null;
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 解析资源 ID
     */
    private UUID parseResourceId(String expression, ProceedingJoinPoint joinPoint, Object result) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }

        // 简单的 SpEL 解析
        if (expression.startsWith("#result") && result != null) {
            // 从返回值中获取 ID
            return extractIdFromResult(result);
        } else if (expression.startsWith("#")) {
            // 从参数中获取
            String paramName = expression.substring(1);
            Object[] args = joinPoint.getArgs();
            String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equals(paramName)) {
                    Object arg = args[i];
                    if (arg instanceof UUID) {
                        return (UUID) arg;
                    } else if (arg != null) {
                        return extractIdFromResult(arg);
                    }
                }
            }
        }

        return null;
    }

    /**
     * 从对象中提取 ID
     */
    private UUID extractIdFromResult(Object result) {
        if (result == null) {
            return null;
        }

        try {
            Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);
            if (id instanceof UUID) {
                return (UUID) id;
            } else if (id instanceof String) {
                return UUID.fromString((String) id);
            }
        } catch (Exception e) {
            logger.debug("无法从结果中提取 ID", e);
        }

        return null;
    }

    /**
     * 将对象转换为 Map
     */
    private Map<String, Object> convertToMap(Object obj) {
        if (obj == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        try {
            Class<?> clazz = obj.getClass();
            for (Method method : clazz.getMethods()) {
                if (method.getName().startsWith("get") &&
                    method.getParameterCount() == 0 &&
                    !method.getReturnType().equals(void.class)) {

                    String propertyName = method.getName().substring(3);
                    propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);

                    // 跳过 getClass
                    if ("class".equals(propertyName)) {
                        continue;
                    }

                    Object value = method.invoke(obj);
                    if (value != null) {
                        map.put(propertyName, value);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("转换对象为 Map 失败", e);
        }

        return map;
    }
}
