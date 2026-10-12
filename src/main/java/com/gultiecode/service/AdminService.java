package com.gultiecode.service;

import com.gultiecode.dto.AdminDtos.*;
import com.gultiecode.dto.AuditLogRecords.AuditLogResponse;
import com.gultiecode.dto.AuthDtos.UserDto;
import com.gultiecode.dto.PlanDtos.UserPlanProgressDto;
import com.gultiecode.entity.*;
import com.gultiecode.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DomainRepository domainRepository;
    private final LevelRepository levelRepository;
    private final UserLevelProgressRepository progressRepository;
    private final UserLevelTaskCompletionRepository taskCompletionRepository;
    private final UserPlanProgressRepository planProgressRepository;
    private final BlogRepository blogRepository;
    private final AdminActivityLogRepository auditLogRepository;
    private final AuthService authService;
    private final PlanService planService;

    public AdminService(UserRepository userRepository,
                        DomainRepository domainRepository,
                        LevelRepository levelRepository,
                        UserLevelProgressRepository progressRepository,
                        UserLevelTaskCompletionRepository taskCompletionRepository,
                        UserPlanProgressRepository planProgressRepository,
                        BlogRepository blogRepository,
                        AdminActivityLogRepository auditLogRepository,
                        AuthService authService,
                        PlanService planService) {
        this.userRepository = userRepository;
        this.domainRepository = domainRepository;
        this.levelRepository = levelRepository;
        this.progressRepository = progressRepository;
        this.taskCompletionRepository = taskCompletionRepository;
        this.planProgressRepository = planProgressRepository;
        this.blogRepository = blogRepository;
        this.auditLogRepository = auditLogRepository;
        this.authService = authService;
        this.planService = planService;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto approveUser(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setApproved(true);
        user.setApprovalStatus("APPROVED");
        User updated = userRepository.save(user);

        logAdminAction(adminUsername, "APPROVE_USER", "User", userId.toString(), userId, "PENDING_APPROVAL", "APPROVED", "Approved user account");

        return authService.mapToUserDto(updated);
    }

    @Transactional
    public UserDto rejectUser(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setApproved(false);
        user.setApprovalStatus("REJECTED");
        User updated = userRepository.save(user);

        logAdminAction(adminUsername, "REJECT_USER", "User", userId.toString(), userId, "APPROVED", "REJECTED", "Suspended user account");

        return authService.mapToUserDto(updated);
    }

    @Transactional
    public UserDto changeUserRole(Long userId, String roleName, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Role newRole;
        try {
            newRole = Role.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + roleName + ". Must be ROLE_USER, ROLE_EDITOR, or ROLE_ADMIN.");
        }

        String prevRole = user.getRole().name();
        user.setRole(newRole);
        user.setUpdatedBy(adminUsername);
        User saved = userRepository.save(user);

        logAdminAction(adminUsername, "CHANGE_USER_ROLE", "User", userId.toString(), userId, prevRole, newRole.name(), "Changed role from " + prevRole + " to " + newRole.name());

        return authService.mapToUserDto(saved);
    }

    @Transactional
    public void unlockLevelForUser(Long userId, Long courseId, Long levelId, String adminUsername, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Domain domain = domainRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        UserLevelProgress progress = progressRepository.findByUserIdAndLevelId(userId, levelId)
                .orElseGet(() -> {
                    UserLevelProgress p = new UserLevelProgress(null, user, level, domain, "LOCKED", false, 0, false, null);
                    p.setCreatedBy(adminUsername);
                    return p;
                });

        String prevStatus = progress.getStatus();
        progress.setStatus("UNLOCKED");
        progress.setManuallyUnlocked(true);
        progress.setUpdatedBy(adminUsername);
        progressRepository.save(progress);

        String logReason = (reason != null && !reason.trim().isEmpty()) ? reason : "Admin manual level unlock override";
        logAdminAction(adminUsername, "MANUAL_LEVEL_UNLOCK", "Level", levelId.toString(), userId, prevStatus, "UNLOCKED", logReason);
    }

    public UserDetailInspectionDto inspectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserPlanProgressDto> enrolledPlans = planProgressRepository.findByUserId(userId).stream()
                .map(planService::mapToProgressDto)
                .collect(Collectors.toList());

        long completedTasks = taskCompletionRepository.countByUserIdAndTask_Level_IdAndCompletedTrue(userId, 1L);
        long blogsWritten = blogRepository.countByAuthorId(userId);

        return UserDetailInspectionDto.builder()
                .user(authService.mapToUserDto(user))
                .registrationDate(user.getCreatedAt())
                .approvalStatus(user.getApprovalStatus())
                .enrolledPlans(enrolledPlans)
                .completedTasksCount(completedTasks)
                .blogsWrittenCount(blogsWritten)
                .commentsCount(0)
                .currentStreak(user.getStreakDays())
                .longestStreak(user.getLongestStreakDays())
                .build();
    }

    public List<AuditLogResponse> getAuditLogs() {
        return auditLogRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getAdminUsername(),
                        log.getAdminId(),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.getTargetUserId(),
                        log.getPreviousValuesJson(),
                        log.getNewValuesJson(),
                        log.getReason(),
                        log.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<DomainLeaderboardGroup> getDomainLeaderboards() {
        List<Domain> domains = domainRepository.findAll();
        List<DomainLeaderboardGroup> groups = new ArrayList<>();

        for (Domain domain : domains) {
            List<User> users = userRepository.findAll();
            List<DomainLeaderboardItem> items = new ArrayList<>();

            int rank = 1;
            for (User u : users) {
                if (u.getTotalPoints() > 0) {
                    items.add(DomainLeaderboardItem.builder()
                            .rank(rank++)
                            .userId(u.getId())
                            .username(u.getUsername())
                            .fullName(u.getFullName() != null ? u.getFullName() : u.getUsername())
                            .avatarUrl(u.getAvatarUrl())
                            .totalPoints(u.getTotalPoints())
                            .completedTasksCount(0)
                            .progressPercentage(100.0)
                            .build());
                }
            }

            items.sort((a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()));
            for (int i = 0; i < items.size(); i++) {
                items.get(i).setRank(i + 1);
            }

            groups.add(DomainLeaderboardGroup.builder()
                    .domainId(domain.getId())
                    .domainName(domain.getName())
                    .leaderboard(items)
                    .build());
        }

        return groups;
    }

    private void logAdminAction(String adminUsername, String action, String entityType, String entityId, Long targetUserId, String prevVal, String newVal, String reason) {
        AdminActivityLog log = new AdminActivityLog(
                null,
                adminUsername,
                null,
                action,
                entityType,
                entityId,
                targetUserId,
                prevVal,
                newVal,
                reason,
                LocalDateTime.now()
        );
        auditLogRepository.save(log);
    }
}
