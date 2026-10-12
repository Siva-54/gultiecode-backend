package com.gultiecode.controller;

import com.gultiecode.dto.AdminDtos.*;
import com.gultiecode.dto.AuditLogRecords.AuditLogResponse;
import com.gultiecode.dto.AuditLogRecords.ManualUnlockRequest;
import com.gultiecode.dto.AuthDtos.UserDto;
import com.gultiecode.dto.ProfileRecords.RoleChangeRequest;
import com.gultiecode.security.UserPrincipal;
import com.gultiecode.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Management & Operations", description = "Admin endpoints for user approvals, role allocations, user inspection, manual level unlock overrides, and audit logs")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Get all registered users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{userId}/approve")
    @Operation(summary = "Approve user account", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDto> approveUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(adminService.approveUser(userId, currentUser.getUsername()));
    }

    @PutMapping("/users/{userId}/reject")
    @Operation(summary = "Reject/suspend user account", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDto> rejectUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(adminService.rejectUser(userId, currentUser.getUsername()));
    }

    @PutMapping("/users/{userId}/role")
    @Operation(summary = "Allocate role to user (ROLE_USER, ROLE_EDITOR, ROLE_ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDto> changeUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody RoleChangeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(adminService.changeUserRole(userId, request.role(), currentUser.getUsername()));
    }

    @GetMapping("/users/{userId}/inspect")
    @Operation(summary = "Inspect detailed user profile & statistics", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDetailInspectionDto> inspectUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.inspectUser(userId));
    }

    @PostMapping("/users/{userId}/courses/{courseId}/levels/{levelId}/unlock")
    @Operation(summary = "Manually unlock level for a user (override sequential lock)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> unlockLevelForUser(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            @PathVariable Long levelId,
            @RequestBody(required = false) ManualUnlockRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        String reason = request != null ? request.reason() : "Admin manual unlock override";
        adminService.unlockLevelForUser(userId, courseId, levelId, currentUser.getUsername(), reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/leaderboard/by-domain")
    @Operation(summary = "Get domain leaderboards", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<DomainLeaderboardGroup>> getDomainLeaderboards() {
        return ResponseEntity.ok(adminService.getDomainLeaderboards());
    }

    @GetMapping("/activity-logs")
    @Operation(summary = "Get admin activity audit logs", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<AuditLogResponse>> getActivityLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }
}
