package com.gultiecode.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPlan extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "level_id")
    private Level level;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String goal;

    private String targetAudienceLevel; // Beginner, Intermediate, Advanced

    private int recommendedHoursPerDay;

    private int durationWeeks;

    @OneToMany(mappedBy = "learningPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PlanTask> tasks = new ArrayList<>();
}
