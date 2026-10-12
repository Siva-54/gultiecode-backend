package com.gultiecode.controller;

import com.gultiecode.dto.NotificationRecords.*;
import com.gultiecode.security.UserPrincipal;
import com.gultiecode.service.BatchService;
import com.gultiecode.service.NotificationService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Notifications & Batches", description = "Endpoints for broadcasting announcements, meetings, deadlines, and managing user batches")
public class NotificationController {

    private final NotificationService notificationService;
    private final BatchService batchService;

    // User Notification Endpoints
    @GetMapping("/notifications")
    @Operation(summary = "Get notifications for current user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(currentUser.getId()));
    }

    @GetMapping("/notifications/unread-count")
    @Operation(summary = "Get unread notifications count for badge", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(notificationService.getUnreadCountForUser(currentUser.getId()));
    }

    @PutMapping("/notifications/{notificationId}/read")
    @Operation(summary = "Mark a notification as read", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.markNotificationAsRead(notificationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notifications/mark-all-read")
    @Operation(summary = "Mark all user notifications as read", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    // Admin Notification Management
    @GetMapping("/admin/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Get all broadcast notifications", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<NotificationResponse>> getAllAdminNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotificationsForAdmin());
    }

    @PostMapping("/admin/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Broadcast notification to All, Batch, or Specific Users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(notificationService.sendNotification(request, currentUser.getUsername()));
    }

    @DeleteMapping("/admin/notifications/{notificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Delete notification", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.softDeleteNotification(notificationId, currentUser.getUsername());
        return ResponseEntity.ok().build();
    }

    // Admin Batch Management
    @GetMapping("/admin/batches")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Get all batches", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<BatchResponse>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @PostMapping("/admin/batches")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Create a new user batch", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BatchResponse> createBatch(
            @Valid @RequestBody CreateBatchRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(batchService.createBatch(request, currentUser.getUsername()));
    }

    @PutMapping("/admin/batches/{batchId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Update batch details & member assignment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BatchResponse> updateBatch(
            @PathVariable Long batchId,
            @Valid @RequestBody CreateBatchRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(batchService.updateBatch(batchId, request, currentUser.getUsername()));
    }

    @DeleteMapping("/admin/batches/{batchId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: Delete batch", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteBatch(
            @PathVariable Long batchId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        batchService.softDeleteBatch(batchId, currentUser.getUsername());
        return ResponseEntity.ok().build();
    }
}
