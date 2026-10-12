package com.gultiecode.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_activity_logs")
public class AdminActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String adminUsername;

    private Long adminId;

    @Column(nullable = false)
    private String action; // CREATE_COURSE, UPDATE_COURSE, DELETE_COURSE, RESTORE_COURSE, CREATE_LEVEL, UPDATE_LEVEL, DELETE_LEVEL, RESTORE_LEVEL, CREATE_TASK, UPDATE_TASK, DELETE_TASK, RESTORE_TASK, CREATE_QUESTION, UPDATE_QUESTION, DELETE_QUESTION, RESTORE_QUESTION, MANUAL_LEVEL_UNLOCK, APPROVE_USER, REJECT_USER

    private String entityType; // Course, Level, Task, Question, User

    private String entityId;

    private Long targetUserId;

    @Column(columnDefinition = "TEXT")
    private String previousValuesJson;

    @Column(columnDefinition = "TEXT")
    private String newValuesJson;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private LocalDateTime createdAt = LocalDateTime.now();

    public AdminActivityLog() {
    }

    public AdminActivityLog(Long id, String adminUsername, Long adminId, String action, String entityType,
                            String entityId, Long targetUserId, String previousValuesJson, String newValuesJson,
                            String reason, LocalDateTime createdAt) {
        this.id = id;
        this.adminUsername = adminUsername;
        this.adminId = adminId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.targetUserId = targetUserId;
        this.previousValuesJson = previousValuesJson;
        this.newValuesJson = newValuesJson;
        this.reason = reason;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getPreviousValuesJson() {
        return previousValuesJson;
    }

    public void setPreviousValuesJson(String previousValuesJson) {
        this.previousValuesJson = previousValuesJson;
    }

    public String getNewValuesJson() {
        return newValuesJson;
    }

    public void setNewValuesJson(String newValuesJson) {
        this.newValuesJson = newValuesJson;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
