package com.gultiecode.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "domains")
public class Domain extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(length = 1000)
    private String prerequisites;

    @Column(length = 100)
    private String learningPace = "2 hours/day";

    @Column(nullable = false)
    private int minimumDurationWeeks = 8;

    private String iconName = "book-open";

    @OneToMany(mappedBy = "domain", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("levelNumber ASC")
    private List<Level> levels = new ArrayList<>();

    public Domain() {
    }

    public Domain(Long id, String name, String description, String prerequisites, String learningPace,
                  int minimumDurationWeeks, String iconName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.prerequisites = prerequisites;
        this.learningPace = learningPace;
        this.minimumDurationWeeks = minimumDurationWeeks;
        this.iconName = iconName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String getLearningPace() {
        return learningPace;
    }

    public void setLearningPace(String learningPace) {
        this.learningPace = learningPace;
    }

    public int getMinimumDurationWeeks() {
        return minimumDurationWeeks;
    }

    public void setMinimumDurationWeeks(int minimumDurationWeeks) {
        this.minimumDurationWeeks = minimumDurationWeeks;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }
}
