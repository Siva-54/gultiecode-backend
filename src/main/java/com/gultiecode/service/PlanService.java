package com.gultiecode.service;

import com.gultiecode.dto.PlanDtos.*;
import com.gultiecode.entity.*;
import com.gultiecode.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final LearningPlanRepository planRepository;
    private final PlanTaskRepository taskRepository;
    private final UserPlanProgressRepository progressRepository;
    private final UserTaskCompletionRepository completionRepository;
    private final UserRepository userRepository;

    public List<LearningPlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::mapToPlanResponse)
                .collect(Collectors.toList());
    }

    public List<LearningPlanResponse> getPlansByDomain(Long domainId) {
        return planRepository.findByDomainId(domainId).stream()
                .map(this::mapToPlanResponse)
                .collect(Collectors.toList());
    }

    public LearningPlanResponse getPlanById(Long planId, Long currentUserId) {
        LearningPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + planId));
        
        LearningPlanResponse response = mapToPlanResponse(plan);

        if (currentUserId != null) {
            // Populate user-specific completion status for each task
            for (PlanTaskDto taskDto : response.getTasks()) {
                Optional<UserTaskCompletion> completion = completionRepository.findByUserIdAndTaskId(currentUserId, taskDto.getId());
                if (completion.isPresent()) {
                    taskDto.setCompleted(completion.get().isCompleted());
                    taskDto.setUserScore(completion.get().getScore());
                } else {
                    taskDto.setCompleted(false);
                }
            }
        }

        return response;
    }

    @Transactional
    public UserPlanProgressDto enrollInPlan(Long planId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LearningPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Learning plan not found"));

        Optional<UserPlanProgress> existing = progressRepository.findByUserIdAndLearningPlanId(userId, planId);
        if (existing.isPresent()) {
            return mapToProgressDto(existing.get());
        }

        int totalTasks = plan.getTasks().size();
        UserPlanProgress progress = UserPlanProgress.builder()
                .user(user)
                .learningPlan(plan)
                .currentWeek(1)
                .progressPercentage(0.0)
                .completedTasksCount(0)
                .totalTasksCount(totalTasks)
                .enrollmentStatus("IN_PROGRESS")
                .build();

        UserPlanProgress saved = progressRepository.save(progress);
        return mapToProgressDto(saved);
    }

    public List<UserPlanProgressDto> getUserEnrolledPlans(Long userId) {
        return progressRepository.findByUserId(userId).stream()
                .map(this::mapToProgressDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUserPlan(Long planId, Long userId) {
        progressRepository.deleteByUserIdAndLearningPlanId(userId, planId);
    }

    @Transactional
    public void updateProgressStats(Long userId, Long planId) {
        LearningPlan plan = planRepository.findById(planId).orElse(null);
        if (plan == null) return;

        Optional<UserPlanProgress> progressOpt = progressRepository.findByUserIdAndLearningPlanId(userId, planId);
        if (progressOpt.isEmpty()) return;

        UserPlanProgress progress = progressOpt.get();
        List<PlanTask> tasks = plan.getTasks();

        int completedCount = 0;
        for (PlanTask task : tasks) {
            Optional<UserTaskCompletion> comp = completionRepository.findByUserIdAndTaskId(userId, task.getId());
            if (comp.isPresent() && comp.get().isCompleted()) {
                completedCount++;
            }
        }

        int total = tasks.size();
        double percentage = total > 0 ? ((double) completedCount / total) * 100.0 : 0.0;

        progress.setCompletedTasksCount(completedCount);
        progress.setTotalTasksCount(total);
        progress.setProgressPercentage(Math.round(percentage * 10.0) / 10.0);

        if (completedCount == total && total > 0) {
            progress.setEnrollmentStatus("COMPLETED");
        }

        progressRepository.save(progress);
    }

    public LearningPlanResponse mapToPlanResponse(LearningPlan plan) {
        List<PlanTaskDto> taskDtos = plan.getTasks().stream()
                .map(task -> PlanTaskDto.builder()
                        .id(task.getId())
                        .weekNumber(task.getWeekNumber())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .checkpoint(task.getCheckpoint())
                        .complexity(task.getComplexity() != null ? task.getComplexity() : "MEDIUM")
                        .points(task.getPoints())
                        .completed(false)
                        .build())
                .collect(Collectors.toList());

        return LearningPlanResponse.builder()
                .id(plan.getId())
                .domainId(plan.getDomain().getId())
                .domainName(plan.getDomain().getName())
                .levelId(plan.getLevel() != null ? plan.getLevel().getId() : null)
                .levelName(plan.getLevel() != null ? plan.getLevel().getName() : "General")
                .title(plan.getTitle())
                .goal(plan.getGoal())
                .targetAudienceLevel(plan.getTargetAudienceLevel())
                .recommendedHoursPerDay(plan.getRecommendedHoursPerDay())
                .durationWeeks(plan.getDurationWeeks())
                .totalTasks(plan.getTasks().size())
                .tasks(taskDtos)
                .build();
    }

    public UserPlanProgressDto mapToProgressDto(UserPlanProgress progress) {
        return UserPlanProgressDto.builder()
                .id(progress.getId())
                .planId(progress.getLearningPlan().getId())
                .planTitle(progress.getLearningPlan().getTitle())
                .domainName(progress.getLearningPlan().getDomain().getName())
                .currentWeek(progress.getCurrentWeek())
                .progressPercentage(progress.getProgressPercentage())
                .completedTasksCount(progress.getCompletedTasksCount())
                .totalTasksCount(progress.getTotalTasksCount())
                .enrollmentStatus(progress.getEnrollmentStatus())
                .build();
    }
}
