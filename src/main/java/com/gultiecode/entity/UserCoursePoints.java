package com.gultiecode.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_course_points", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "domain_id"})
})
public class UserCoursePoints extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @Column(nullable = false)
    private int points = 0;

    @Column(nullable = false)
    private int completedLevelsCount = 0;

    public UserCoursePoints() {
    }

    public UserCoursePoints(Long id, User user, Domain domain, int points, int completedLevelsCount) {
        this.id = id;
        this.user = user;
        this.domain = domain;
        this.points = points;
        this.completedLevelsCount = completedLevelsCount;
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

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getCompletedLevelsCount() {
        return completedLevelsCount;
    }

    public void setCompletedLevelsCount(int completedLevelsCount) {
        this.completedLevelsCount = completedLevelsCount;
    }
}
