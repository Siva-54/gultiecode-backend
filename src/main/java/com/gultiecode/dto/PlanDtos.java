package com.gultiecode.dto;

import lombok.*;

import java.util.List;

public class PlanDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LearningPlanResponse {
        private Long id;
        private Long domainId;
        private String domainName;
        private Long levelId;
        private String levelName;
        private String title;
        private String goal;
        private String targetAudienceLevel;
        private int recommendedHoursPerDay;
        private int durationWeeks;
        private int totalTasks;
        private List<PlanTaskDto> tasks;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanTaskDto {
        private Long id;
        private int weekNumber;
        private String title;
        private String description;
        private String checkpoint;
        private String complexity;
        private int points;
        private boolean completed;
        private Integer userScore;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserPlanProgressDto {
        private Long id;
        private Long planId;
        private String planTitle;
        private String domainName;
        private int currentWeek;
        private double progressPercentage;
        private int completedTasksCount;
        private int totalTasksCount;
        private String enrollmentStatus;
    }
}
