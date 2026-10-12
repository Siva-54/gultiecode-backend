package com.gultiecode.dto;

import java.time.LocalDateTime;

public class AuditLogRecords {

    public record ManualUnlockRequest(
            String reason
    ) {}

    public record AuditLogResponse(
            Long id,
            String adminUsername,
            Long adminId,
            String action,
            String entityType,
            String entityId,
            Long targetUserId,
            String previousValuesJson,
            String newValuesJson,
            String reason,
            LocalDateTime createdAt
    ) {}
}
