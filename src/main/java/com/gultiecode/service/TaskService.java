package com.gultiecode.service;

import com.gultiecode.dto.CourseRecords.TaskRequest;
import com.gultiecode.dto.CourseRecords.TaskResponse;
import com.gultiecode.entity.*;
import com.gultiecode.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final LevelTaskRepository taskRepository;
    private final LevelRepository levelRepository;
    private final UserLevelProgressRepository progressRepository;
    private final UserLevelTaskCompletionRepository taskCompletionRepository;
    private final UserRepository userRepository;
    private final AdminActivityLogRepository auditLogRepository;

    public TaskService(LevelTaskRepository taskRepository,
                       LevelRepository levelRepository,
                       UserLevelProgressRepository progressRepository,
                       UserLevelTaskCompletionRepository taskCompletionRepository,
                       UserRepository userRepository,
                       AdminActivityLogRepository auditLogRepository) {
        this.taskRepository = taskRepository;
        this.levelRepository = levelRepository;
        this.progressRepository = progressRepository;
        this.taskCompletionRepository = taskCompletionRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<TaskResponse> getTasksForLevel(Long levelId, Long userId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        if (userId != null) {
            UserLevelProgress progress = progressRepository.findByUserIdAndLevelId(userId, levelId).orElse(null);
            boolean isLevel1 = levelRepository.findFirstByDomainIdAndStatusOrderByLevelNumberAsc(level.getDomain().getId(), "ACTIVE")
                    .map(l -> l.getId().equals(levelId)).orElse(false);

            if (!isLevel1 && (progress == null || "LOCKED".equalsIgnoreCase(progress.getStatus()))) {
                throw new RuntimeException("Level is locked. Please complete previous levels first.");
            }
        }

        List<LevelTask> tasks = taskRepository.findByLevelIdAndStatusOrderByOrderIndexAsc(levelId, "ACTIVE");
        return tasks.stream()
                .map(t -> {
                    boolean completed = false;
                    if (userId != null) {
                        completed = taskCompletionRepository.findByUserIdAndTaskId(userId, t.getId())
                                .map(UserLevelTaskCompletion::isCompleted)
                                .orElse(false);
                    }
                    return new TaskResponse(t.getId(), levelId, t.getTitle(), t.getDescription(), t.getOrderIndex(), completed, t.getStatus());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse toggleTaskCompletion(Long levelId, Long taskId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found"));
        LevelTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getLevel().getId().equals(levelId)) {
            throw new RuntimeException("Task does not belong to specified level");
        }

        // Verify level is not locked
        UserLevelProgress progress = progressRepository.findByUserIdAndLevelId(userId, levelId).orElse(null);
        boolean isLevel1 = levelRepository.findFirstByDomainIdAndStatusOrderByLevelNumberAsc(level.getDomain().getId(), "ACTIVE")
                .map(l -> l.getId().equals(levelId)).orElse(false);

        if (!isLevel1 && (progress == null || "LOCKED".equalsIgnoreCase(progress.getStatus()))) {
            throw new RuntimeException("Level is locked. Cannot complete tasks for locked levels.");
        }

        Optional<UserLevelTaskCompletion> completionOpt = taskCompletionRepository.findByUserIdAndTaskId(userId, taskId);
        UserLevelTaskCompletion completion;
        if (completionOpt.isPresent()) {
            completion = completionOpt.get();
            completion.setCompleted(!completion.isCompleted());
            completion.setCompletedAt(completion.isCompleted() ? LocalDateTime.now() : null);
        } else {
            completion = new UserLevelTaskCompletion(null, user, task, true, LocalDateTime.now());
            completion.setCreatedBy(user.getUsername());
        }

        UserLevelTaskCompletion saved = taskCompletionRepository.save(completion);
        return new TaskResponse(task.getId(), levelId, task.getTitle(), task.getDescription(), task.getOrderIndex(), saved.isCompleted(), task.getStatus());
    }

    public boolean areAllTasksCompleted(Long levelId, Long userId) {
        List<LevelTask> activeTasks = taskRepository.findByLevelIdAndStatusOrderByOrderIndexAsc(levelId, "ACTIVE");
        if (activeTasks.isEmpty()) return true;

        long completedCount = taskCompletionRepository.countByUserIdAndTask_Level_IdAndCompletedTrue(userId, levelId);
        return completedCount >= activeTasks.size();
    }

    @Transactional
    public TaskResponse createTask(Long levelId, TaskRequest request, String adminUsername) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        int orderIdx = request.orderIndex() > 0 ? request.orderIndex() : (int) (taskRepository.countByLevelIdAndStatus(levelId, "ACTIVE") + 1);

        LevelTask task = new LevelTask();
        task.setLevel(level);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setOrderIndex(orderIdx);
        task.setStatus("ACTIVE");
        task.setCreatedBy(adminUsername);

        LevelTask saved = taskRepository.save(task);
        logAdminAction(adminUsername, "CREATE_TASK", "Task", saved.getId().toString(), null, saved.getTitle(), "Created task under level " + levelId);
        return new TaskResponse(saved.getId(), levelId, saved.getTitle(), saved.getDescription(), saved.getOrderIndex(), false, saved.getStatus());
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskRequest request, String adminUsername) {
        LevelTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        String prevTitle = task.getTitle();
        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.orderIndex() > 0) task.setOrderIndex(request.orderIndex());
        task.setUpdatedBy(adminUsername);

        LevelTask saved = taskRepository.save(task);
        logAdminAction(adminUsername, "UPDATE_TASK", "Task", taskId.toString(), prevTitle, saved.getTitle(), "Updated task");
        return new TaskResponse(saved.getId(), saved.getLevel().getId(), saved.getTitle(), saved.getDescription(), saved.getOrderIndex(), false, saved.getStatus());
    }

    @Transactional
    public TaskResponse softDeleteTask(Long taskId, String adminUsername) {
        LevelTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        task.setStatus("DELETED");
        task.setUpdatedBy(adminUsername);
        LevelTask saved = taskRepository.save(task);

        logAdminAction(adminUsername, "DELETE_TASK", "Task", taskId.toString(), "ACTIVE", "DELETED", "Soft deleted task");
        return new TaskResponse(saved.getId(), saved.getLevel().getId(), saved.getTitle(), saved.getDescription(), saved.getOrderIndex(), false, saved.getStatus());
    }

    @Transactional
    public TaskResponse restoreTask(Long taskId, String adminUsername) {
        LevelTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        task.setStatus("ACTIVE");
        task.setUpdatedBy(adminUsername);
        LevelTask saved = taskRepository.save(task);

        logAdminAction(adminUsername, "RESTORE_TASK", "Task", taskId.toString(), "DELETED", "ACTIVE", "Restored task");
        return new TaskResponse(saved.getId(), saved.getLevel().getId(), saved.getTitle(), saved.getDescription(), saved.getOrderIndex(), false, saved.getStatus());
    }

    private void logAdminAction(String adminUsername, String action, String entityType, String entityId, String prevVal, String newVal, String reason) {
        AdminActivityLog log = new AdminActivityLog(
                null,
                adminUsername,
                null,
                action,
                entityType,
                entityId,
                null,
                prevVal,
                newVal,
                reason,
                LocalDateTime.now()
        );
        auditLogRepository.save(log);
    }
}
