package com.gultiecode.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizEvaluation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    private PlanTask task;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String optionsJson;

    private String correctAnswer;

    private String userAnswer;

    private int timeLimitSeconds; // 20 - 30 seconds

    private int timeTakenSeconds;

    private int score; // 0 - 100

    private boolean passed;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    private boolean flagged;

    private String flagReason;

    @Builder.Default
    private boolean adminOverridden = false;

    private String adminNotes;
}
