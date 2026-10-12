package com.gultiecode.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_level_task_completions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "task_id"})
})
public class UserLevelTaskCompletion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    private LevelTask task;

    @Column(nullable = false)
    private boolean completed = false;

    private LocalDateTime completedAt;

    public UserLevelTaskCompletion() {
    }

    public UserLevelTaskCompletion(Long id, User user, LevelTask task, boolean completed, LocalDateTime completedAt) {
        this.id = id;
        this.user = user;
        this.task = task;
        this.completed = completed;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LevelTask getTask() {
        return task;
    }

    public void setTask(LevelTask task) {
        this.task = task;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
