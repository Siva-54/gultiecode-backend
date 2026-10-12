package com.gultiecode.dto;

import java.util.List;

public class LeaderboardRecords {

    public record CourseLeaderboardEntry(
            int rank,
            Long userId,
            String username,
            String fullName,
            String avatarUrl,
            int points,
            int completedLevelsCount
    ) {}

    public record CourseLeaderboardResponse(
            Long courseId,
            String courseName,
            List<CourseLeaderboardEntry> entries
    ) {}
}
