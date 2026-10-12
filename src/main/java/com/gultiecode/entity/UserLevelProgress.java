package com.gultiecode.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_level_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "level_id"})
})
public class UserLevelProgress extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @Column(nullable = false, length = 30)
    private String status = "LOCKED"; // LOCKED, UNLOCKED, COMPLETED

    private boolean manuallyUnlocked = false;

    private Integer bestScore = 0;

    private boolean pointsAwarded = false;

    private LocalDateTime completedAt;

    public UserLevelProgress() {
    }

    public UserLevelProgress(Long id, User user, Level level, Domain domain, String status,
                             boolean manuallyUnlocked, Integer bestScore, boolean pointsAwarded, LocalDateTime completedAt) {
        this.id = id;
        this.user = user;
        this.level = level;
        this.domain = domain;
        this.status = status;
        this.manuallyUnlocked = manuallyUnlocked;
        this.bestScore = bestScore;
        this.pointsAwarded = pointsAwarded;
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

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isManuallyUnlocked() {
        return manuallyUnlocked;
    }

    public void setManuallyUnlocked(boolean manuallyUnlocked) {
        this.manuallyUnlocked = manuallyUnlocked;
    }

    public Integer getBestScore() {
        return bestScore;
    }

    public void setBestScore(Integer bestScore) {
        this.bestScore = bestScore;
    }

    public boolean isPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(boolean pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
