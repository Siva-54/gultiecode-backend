package com.gultiecode.controller;

import com.gultiecode.dto.CourseRecords.*;
import com.gultiecode.dto.LeaderboardRecords.CourseLeaderboardResponse;
import com.gultiecode.security.UserPrincipal;
import com.gultiecode.service.CourseService;
import com.gultiecode.service.LeaderboardService;
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
@Tag(name = "Courses & Learning Progression", description = "Endpoints for exploring courses, sequential level unlocking, and course leaderboards")
public class CourseController {

    private final CourseService courseService;
    private final LeaderboardService leaderboardService;

    public CourseController(CourseService courseService, LeaderboardService leaderboardService) {
        this.courseService = courseService;
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/courses")
    @Operation(summary = "Get all active courses", description = "Public endpoint returning all available courses with metadata")
    public ResponseEntity<List<CourseResponse>> getAllActiveCourses(@AuthenticationPrincipal UserPrincipal currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(courseService.getAllActiveCourses(userId));
    }

    @GetMapping("/courses/{courseId}")
    @Operation(summary = "Get course details by ID", description = "Returns course information and ordered levels with user unlock statuses",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseResponse> getCourseById(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(courseService.getCourseById(courseId, userId));
    }

    @GetMapping("/courses/{courseId}/levels/{levelId}")
    @Operation(summary = "Get level details", description = "Returns level instructions, resources, tasks, and completion state",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LevelResponse> getLevelById(
            @PathVariable Long courseId,
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(courseService.getLevelById(levelId, userId));
    }

    @GetMapping("/courses/{courseId}/leaderboard")
    @Operation(summary = "Get course leaderboard", description = "Returns leaderboard ranking of users for a specific course",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseLeaderboardResponse> getCourseLeaderboard(@PathVariable Long courseId) {
        return ResponseEntity.ok(leaderboardService.getCourseLeaderboard(courseId));
    }

    // Admin & Editor Course Management Endpoints
    @GetMapping("/admin/courses/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Get all courses (including deleted)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<CourseResponse>> getAllCoursesForAdmin() {
        return ResponseEntity.ok(courseService.getAllCoursesForAdmin());
    }

    @PostMapping("/admin/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Create a new course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(courseService.createCourse(request, currentUser.getUsername()));
    }

    @PutMapping("/admin/courses/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Update an existing course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, request, currentUser.getUsername()));
    }

    @PutMapping("/admin/courses/{courseId}/soft-delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Soft delete a course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseResponse> softDeleteCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(courseService.softDeleteCourse(courseId, currentUser.getUsername()));
    }

    @PutMapping("/admin/courses/{courseId}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Restore a soft-deleted course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CourseResponse> restoreCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(courseService.restoreCourse(courseId, currentUser.getUsername()));
    }

    @PostMapping("/admin/courses/{courseId}/levels")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Create level inside a course", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LevelResponse> createLevel(
            @PathVariable Long courseId,
            @Valid @RequestBody LevelRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(courseService.createLevel(courseId, request, currentUser.getUsername()));
    }

    @PutMapping("/admin/levels/{levelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Update level metadata", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LevelResponse> updateLevel(
            @PathVariable Long levelId,
            @Valid @RequestBody LevelRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(courseService.updateLevel(levelId, request, currentUser.getUsername()));
    }

    @PutMapping("/admin/levels/{levelId}/soft-delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Soft delete a level", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LevelResponse> softDeleteLevel(
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(courseService.softDeleteLevel(levelId, currentUser.getUsername()));
    }

    @PutMapping("/admin/levels/{levelId}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Admin/Editor: Restore a soft-deleted level", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LevelResponse> restoreLevel(
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(courseService.restoreLevel(levelId, currentUser.getUsername()));
    }
}
