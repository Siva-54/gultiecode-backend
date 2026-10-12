package com.gultiecode.dto;

import com.gultiecode.dto.AuthDtos.UserDto;
import com.gultiecode.dto.PlanDtos.UserPlanProgressDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class AdminDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminActivityLogDto {
        private Long id;
        private String adminUsername;
        private String action;
        private String entityType;
        private String entityId;
        private String previousValuesJson;
        private String newValuesJson;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDetailInspectionDto {
        private UserDto user;
        private LocalDateTime registrationDate;
        private String approvalStatus;
        private List<UserPlanProgressDto> enrolledPlans;
        private long completedTasksCount;
        private long blogsWrittenCount;
        private long commentsCount;
        private int currentStreak;
        private int longestStreak;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DomainLeaderboardItem {
        private int rank;
        private Long userId;
        private String username;
        private String fullName;
        private String avatarUrl;
        private int totalPoints;
        private int completedTasksCount;
        private double progressPercentage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DomainLeaderboardGroup {
        private Long domainId;
        private String domainName;
        private List<DomainLeaderboardItem> leaderboard;
    }
}
