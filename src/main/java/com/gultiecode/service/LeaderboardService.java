package com.gultiecode.service;

import com.gultiecode.dto.LeaderboardRecords.*;
import com.gultiecode.entity.Domain;
import com.gultiecode.entity.UserCoursePoints;
import com.gultiecode.repository.DomainRepository;
import com.gultiecode.repository.UserCoursePointsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    private final UserCoursePointsRepository coursePointsRepository;
    private final DomainRepository domainRepository;

    public LeaderboardService(UserCoursePointsRepository coursePointsRepository,
                              DomainRepository domainRepository) {
        this.coursePointsRepository = coursePointsRepository;
        this.domainRepository = domainRepository;
    }

    public CourseLeaderboardResponse getCourseLeaderboard(Long courseId) {
        Domain domain = domainRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        List<UserCoursePoints> pointsList = coursePointsRepository.findByDomainIdOrderByPointsDescCompletedLevelsCountDesc(courseId);
        List<CourseLeaderboardEntry> entries = new ArrayList<>();

        int rank = 1;
        for (UserCoursePoints cp : pointsList) {
            entries.add(new CourseLeaderboardEntry(
                    rank++,
                    cp.getUser().getId(),
                    cp.getUser().getUsername(),
                    cp.getUser().getFullName() != null ? cp.getUser().getFullName() : cp.getUser().getUsername(),
                    cp.getUser().getAvatarUrl(),
                    cp.getPoints(),
                    cp.getCompletedLevelsCount()
            ));
        }

        return new CourseLeaderboardResponse(domain.getId(), domain.getName(), entries);
    }

    public List<CourseLeaderboardResponse> getAllCourseLeaderboards() {
        List<Domain> domains = domainRepository.findByStatus("ACTIVE");
        List<CourseLeaderboardResponse> responses = new ArrayList<>();
        for (Domain d : domains) {
            responses.add(getCourseLeaderboard(d.getId()));
        }
        return responses;
    }
}
