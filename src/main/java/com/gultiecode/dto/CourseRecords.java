package com.gultiecode.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CourseRecords {

    public record CourseRequest(
            @NotBlank String name,
            String description,
            String prerequisites,
            String learningPace,
            int minimumDurationWeeks,
            String iconName
    ) {}

    public record LevelSummaryResponse(
            Long id,
            int levelNumber,
            String name,
            String description,
            String duration,
            String learningPace,
            int points,
            String status,
            String userStatus, // LOCKED, UNLOCKED, COMPLETED
            boolean tasksCompleted,
            int taskCount,
            int completedTaskCount
    ) {}

    public record CourseResponse(
            Long id,
            String name,
            String description,
            String prerequisites,
            String learningPace,
            int minimumDurationWeeks,
            int totalPoints,
            int numberOfLevels,
            String iconName,
            String status,
            List<LevelSummaryResponse> levels
    ) {}

    public record LevelRequest(
            int levelNumber,
            @NotBlank String name,
            String description,
            String duration,
            String learningPace,
            int points,
            String topicsCsv,
            String instructions,
            String externalLinksJson
    ) {}

    public record TaskRequest(
            @NotBlank String title,
            String description,
            int orderIndex
    ) {}

    public record TaskResponse(
            Long id,
            Long levelId,
            String title,
            String description,
            int orderIndex,
            boolean completed,
            String status
    ) {}

    public record LevelResponse(
            Long id,
            Long courseId,
            String courseName,
            int levelNumber,
            String name,
            String description,
            String duration,
            String learningPace,
            int points,
            String topicsCsv,
            String instructions,
            String externalLinksJson,
            String status,
            String userStatus, // LOCKED, UNLOCKED, COMPLETED
            boolean tasksCompleted,
            int taskCount,
            int completedTaskCount,
            List<TaskResponse> tasks
    ) {}
}
