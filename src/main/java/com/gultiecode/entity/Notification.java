package com.gultiecode.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false, length = 50)
    private String type = "ANNOUNCEMENT"; // ANNOUNCEMENT, MEETING, DEADLINE, PHYSICAL_TEST, GENERAL

    @Column(nullable = false, length = 50)
    private String targetType = "ALL"; // ALL, BATCH, SPECIFIC_USERS

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_batch_id")
    private Batch targetBatch;

    @Column(columnDefinition = "TEXT")
    private String targetUserIdsJson; // JSON array of user IDs "[1, 2, 3]"

    private LocalDateTime eventDate; // optional upcoming meeting/deadline/test date

    private String senderAdminUsername;

    public Notification() {
    }

    public Notification(Long id, String title, String message, String type, String targetType,
                        Batch targetBatch, String targetUserIdsJson, LocalDateTime eventDate, String senderAdminUsername) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.targetType = targetType;
        this.targetBatch = targetBatch;
        this.targetUserIdsJson = targetUserIdsJson;
        this.eventDate = eventDate;
        this.senderAdminUsername = senderAdminUsername;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Batch getTargetBatch() {
        return targetBatch;
    }

    public void setTargetBatch(Batch targetBatch) {
        this.targetBatch = targetBatch;
    }

    public String getTargetUserIdsJson() {
        return targetUserIdsJson;
    }

    public void setTargetUserIdsJson(String targetUserIdsJson) {
        this.targetUserIdsJson = targetUserIdsJson;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getSenderAdminUsername() {
        return senderAdminUsername;
    }

    public void setSenderAdminUsername(String senderAdminUsername) {
        this.senderAdminUsername = senderAdminUsername;
    }
}
