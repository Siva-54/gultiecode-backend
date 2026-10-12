package com.gultiecode.controller;

import com.gultiecode.dto.CourseRecords.TaskRequest;
import com.gultiecode.dto.CourseRecords.TaskResponse;
import com.gultiecode.security.UserPrincipal;
import com.gultiecode.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Level Tasks & Progress", description = "Endpoints for managing and completing level learning tasks")
public class LevelTaskController {

    private final TaskService taskService;

    public LevelTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/levels/{levelId}/tasks")
    @Operation(summary = "Get tasks for a level", description = "Returns active learning tasks and user checkbox completion state",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TaskResponse>> getTasksForLevel(
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(taskService.getTasksForLevel(levelId, userId));
    }

    @PostMapping("/levels/{levelId}/tasks/{taskId}/toggle")
    @Operation(summary = "Toggle task completion", description = "Marks a task completed or incomplete for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponse> toggleTaskCompletion(
            @PathVariable Long levelId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(taskService.toggleTaskCompletion(levelId, taskId, currentUser.getId()));
    }

    @PostMapping("/admin/levels/{levelId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Create task under level", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long levelId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(taskService.createTask(levelId, request, currentUser.getUsername()));
    }

    @PutMapping("/admin/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Update task", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request, currentUser.getUsername()));
    }

    @PutMapping("/admin/tasks/{taskId}/soft-delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Soft delete task", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponse> softDeleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(taskService.softDeleteTask(taskId, currentUser.getUsername()));
    }

    @PutMapping("/admin/tasks/{taskId}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Restore task", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponse> restoreTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(taskService.restoreTask(taskId, currentUser.getUsername()));
    }
}
