package com.usermanagement.web.mapper;

import com.usermanagement.domain.AuditLog;
import com.usermanagement.web.dto.AuditLogDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 审计日志 Mapper
 */
@Component
public class AuditLogMapper {

    /**
     * 将 AuditLog 转换为 AuditLogDTO
     */
    public AuditLogDTO toDto(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(auditLog.getId() != null ? auditLog.getId().toString() : null);
        dto.setUserId(auditLog.getUserId() != null ? auditLog.getUserId().toString() : null);
        dto.setUserEmail(auditLog.getUserEmail());
        dto.setOperationType(auditLog.getOperationType());
        dto.setResourceType(auditLog.getResourceType());
        dto.setResourceId(auditLog.getResourceId() != null ? auditLog.getResourceId().toString() : null);
        dto.setOperationDescription(auditLog.getOperationDescription());
        dto.setOldValue(convertJsonMap(auditLog.getOldValue()));
        dto.setNewValue(convertJsonMap(auditLog.getNewValue()));
        dto.setClientIp(auditLog.getClientIp());
        dto.setUserAgent(auditLog.getUserAgent());
        dto.setOperationResult(auditLog.getOperationResult());
        dto.setErrorMessage(auditLog.getErrorMessage());
        dto.setCreatedAt(auditLog.getCreatedAt());

        return dto;
    }

    /**
     * 确保 Map 可序列化
     */
    private Map<String, Object> convertJsonMap(Map<String, Object> original) {
        if (original == null) {
            return null;
        }

        Map<String, Object> converted = new HashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof UUID) {
                converted.put(entry.getKey(), ((UUID) value).toString());
            } else {
                converted.put(entry.getKey(), value);
            }
        }
        return converted;
    }
}
