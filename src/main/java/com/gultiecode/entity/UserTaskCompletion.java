package com.gultiecode.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_task_completions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTaskCompletion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    private PlanTask task;

    @Builder.Default
    private boolean completed = false;

    private Integer score;

    private Integer attempts;

    private LocalDateTime completedAt;
}
