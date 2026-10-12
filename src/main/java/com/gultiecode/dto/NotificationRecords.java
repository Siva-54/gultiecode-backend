package com.gultiecode.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationRecords {

    public record CreateBatchRequest(
            @NotBlank String name,
            String description,
            List<Long> userIds
    ) {}

    public record BatchMemberDto(
            Long id,
            String username,
            String fullName,
            String email,
            String avatarUrl
    ) {}

    public record BatchResponse(
            Long id,
            String name,
            String description,
            int memberCount,
            List<BatchMemberDto> members,
            String status
    ) {}

    public record SendNotificationRequest(
            @NotBlank String title,
            @NotBlank String message,
            String type, // ANNOUNCEMENT, MEETING, DEADLINE, PHYSICAL_TEST, GENERAL
            String targetType, // ALL, BATCH, SPECIFIC_USERS
            Long targetBatchId,
            List<Long> targetUserIds,
            LocalDateTime eventDate
    ) {}

    public record NotificationResponse(
            Long id,
            String title,
            String message,
            String type,
            String targetType,
            Long targetBatchId,
            String targetBatchName,
            List<Long> targetUserIds,
            LocalDateTime eventDate,
            String senderAdminUsername,
            boolean isRead,
            LocalDateTime createdAt
    ) {}

    public record UnreadCountResponse(
            long unreadCount
    ) {}
}
