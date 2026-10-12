package com.gultiecode.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "levels")
public class Level extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    @JsonIgnore
    private Domain domain;

    @Column(nullable = false)
    private int levelNumber;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(length = 100)
    private String duration = "1 Week";

    @Column(length = 100)
    private String learningPace = "2 hrs/day";

    @Column(nullable = false)
    private int points = 100;

    @Column(length = 2000)
    private String topicsCsv;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(columnDefinition = "TEXT")
    private String externalLinksJson; // JSON array string [{"title":"...", "url":"..."}]

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<LevelTask> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Question> questions = new ArrayList<>();

    public Level() {
    }

    public Level(Long id, Domain domain, int levelNumber, String name, String description, String duration,
                 String learningPace, int points, String topicsCsv, String instructions, String externalLinksJson) {
        this.id = id;
        this.domain = domain;
        this.levelNumber = levelNumber;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.learningPace = learningPace;
        this.points = points;
        this.topicsCsv = topicsCsv;
        this.instructions = instructions;
        this.externalLinksJson = externalLinksJson;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLearningPace() {
        return learningPace;
    }

    public void setLearningPace(String learningPace) {
        this.learningPace = learningPace;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getTopicsCsv() {
        return topicsCsv;
    }

    public void setTopicsCsv(String topicsCsv) {
        this.topicsCsv = topicsCsv;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getExternalLinksJson() {
        return externalLinksJson;
    }

    public void setExternalLinksJson(String externalLinksJson) {
        this.externalLinksJson = externalLinksJson;
    }

    public List<LevelTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<LevelTask> tasks) {
        this.tasks = tasks;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
