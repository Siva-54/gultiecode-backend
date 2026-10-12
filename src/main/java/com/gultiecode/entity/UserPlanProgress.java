package com.gultiecode.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_plan_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPlanProgress extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private LearningPlan learningPlan;

    @Builder.Default
    private int currentWeek = 1;

    @Builder.Default
    private double progressPercentage = 0.0;

    @Builder.Default
    private int completedTasksCount = 0;

    @Builder.Default
    private int totalTasksCount = 0;

    private String enrollmentStatus; // ENROLLED, IN_PROGRESS, COMPLETED
}
