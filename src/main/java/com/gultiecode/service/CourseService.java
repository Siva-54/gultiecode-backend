package com.gultiecode.service;

import com.gultiecode.dto.CourseRecords.*;
import com.gultiecode.entity.*;
import com.gultiecode.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final DomainRepository domainRepository;
    private final LevelRepository levelRepository;
    private final LevelTaskRepository taskRepository;
    private final UserLevelProgressRepository progressRepository;
    private final UserLevelTaskCompletionRepository taskCompletionRepository;
    private final UserCoursePointsRepository coursePointsRepository;
    private final UserRepository userRepository;
    private final AdminActivityLogRepository auditLogRepository;

    public CourseService(DomainRepository domainRepository,
                         LevelRepository levelRepository,
                         LevelTaskRepository taskRepository,
                         UserLevelProgressRepository progressRepository,
                         UserLevelTaskCompletionRepository taskCompletionRepository,
                         UserCoursePointsRepository coursePointsRepository,
                         UserRepository userRepository,
                         AdminActivityLogRepository auditLogRepository) {
        this.domainRepository = domainRepository;
        this.levelRepository = levelRepository;
        this.taskRepository = taskRepository;
        this.progressRepository = progressRepository;
        this.taskCompletionRepository = taskCompletionRepository;
        this.coursePointsRepository = coursePointsRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<CourseResponse> getAllActiveCourses(Long userId) {
        List<Domain> domains = domainRepository.findByStatus("ACTIVE");
        return domains.stream()
                .map(d -> mapToCourseResponse(d, userId))
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getAllCoursesForAdmin() {
        List<Domain> domains = domainRepository.findAll();
        return domains.stream()
                .map(d -> mapToCourseResponse(d, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseResponse getCourseById(Long courseId, Long userId) {
        Domain domain = domainRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        if (userId != null) {
            initializeUserProgressionForCourse(userId, courseId);
        }

        return mapToCourseResponse(domain, userId);
    }

    @Transactional
    public void initializeUserProgressionForCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Domain domain = domainRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Initialize UserCoursePoints if not existing
        coursePointsRepository.findByUserIdAndDomainId(userId, courseId).orElseGet(() -> {
            UserCoursePoints cp = new UserCoursePoints(null, user, domain, 0, 0);
            cp.setCreatedBy(user.getUsername());
            return coursePointsRepository.save(cp);
        });

        // Initialize Level 1 as UNLOCKED if no progress exists
        List<UserLevelProgress> existingProgress = progressRepository.findByUserIdAndDomainId(userId, courseId);
        if (existingProgress.isEmpty()) {
            Optional<Level> firstLevelOpt = levelRepository.findFirstByDomainIdAndStatusOrderByLevelNumberAsc(courseId, "ACTIVE");
            if (firstLevelOpt.isPresent()) {
                Level firstLevel = firstLevelOpt.get();
                UserLevelProgress p = new UserLevelProgress(null, user, firstLevel, domain, "UNLOCKED", false, 0, false, null);
                p.setCreatedBy(user.getUsername());
                progressRepository.save(p);
            }
        }
    }

    public LevelResponse getLevelById(Long levelId, Long userId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        String userStatus = "LOCKED";
        boolean tasksCompleted = false;
        int taskCount = 0;
        int completedTaskCount = 0;
        List<TaskResponse> tasks = new ArrayList<>();

        if (userId != null) {
            initializeUserProgressionForCourse(userId, level.getDomain().getId());
            UserLevelProgress progress = progressRepository.findByUserIdAndLevelId(userId, levelId).orElse(null);
            if (progress != null) {
                userStatus = progress.getStatus();
            } else {
                // If Level 1 is first active level
                Optional<Level> firstLvl = levelRepository.findFirstByDomainIdAndStatusOrderByLevelNumberAsc(level.getDomain().getId(), "ACTIVE");
                if (firstLvl.isPresent() && firstLvl.get().getId().equals(levelId)) {
                    userStatus = "UNLOCKED";
                }
            }

            List<LevelTask> activeTasks = taskRepository.findByLevelIdAndStatusOrderByOrderIndexAsc(levelId, "ACTIVE");
            taskCount = activeTasks.size();

            for (LevelTask t : activeTasks) {
                boolean completed = taskCompletionRepository.findByUserIdAndTaskId(userId, t.getId())
                        .map(UserLevelTaskCompletion::isCompleted)
                        .orElse(false);
                if (completed) completedTaskCount++;
                tasks.add(new TaskResponse(t.getId(), levelId, t.getTitle(), t.getDescription(), t.getOrderIndex(), completed, t.getStatus()));
            }

            tasksCompleted = (taskCount == 0) || (completedTaskCount == taskCount);
        } else {
            List<LevelTask> activeTasks = taskRepository.findByLevelIdAndStatusOrderByOrderIndexAsc(levelId, "ACTIVE");
            taskCount = activeTasks.size();
            tasks = activeTasks.stream()
                    .map(t -> new TaskResponse(t.getId(), levelId, t.getTitle(), t.getDescription(), t.getOrderIndex(), false, t.getStatus()))
                    .collect(Collectors.toList());
        }

        return new LevelResponse(
                level.getId(),
                level.getDomain().getId(),
                level.getDomain().getName(),
                level.getLevelNumber(),
                level.getName(),
                level.getDescription(),
                level.getDuration(),
                level.getLearningPace(),
                level.getPoints(),
                level.getTopicsCsv(),
                level.getInstructions(),
                level.getExternalLinksJson(),
                level.getStatus(),
                userStatus,
                tasksCompleted,
                taskCount,
                completedTaskCount,
                tasks
        );
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request, String adminUsername) {
        Domain domain = new Domain();
        domain.setName(request.name());
        domain.setDescription(request.description());
        domain.setPrerequisites(request.prerequisites());
        domain.setLearningPace(request.learningPace() != null ? request.learningPace() : "2 hours/day");
        domain.setMinimumDurationWeeks(request.minimumDurationWeeks() > 0 ? request.minimumDurationWeeks() : 8);
        domain.setIconName(request.iconName() != null ? request.iconName() : "book-open");
        domain.setStatus("ACTIVE");
        domain.setCreatedBy(adminUsername);

        Domain saved = domainRepository.save(domain);
        logAdminAction(adminUsername, "CREATE_COURSE", "Course", saved.getId().toString(), null, saved.getName(), "Created course");
        return mapToCourseResponse(saved, null);
    }

    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseRequest request, String adminUsername) {
        Domain domain = domainRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        String prevVal = domain.getName();
        domain.setName(request.name());
        domain.setDescription(request.description());
        domain.setPrerequisites(request.prerequisites());
        if (request.learningPace() != null) domain.setLearningPace(request.learningPace());
        if (request.minimumDurationWeeks() > 0) domain.setMinimumDurationWeeks(request.minimumDurationWeeks());
        if (request.iconName() != null) domain.setIconName(request.iconName());
        domain.setUpdatedBy(adminUsername);

        Domain updated = domainRepository.save(domain);
        logAdminAction(adminUsername, "UPDATE_COURSE", "Course", courseId.toString(), prevVal, updated.getName(), "Updated course");
        return mapToCourseResponse(updated, null);
    }

    @Transactional
    public CourseResponse softDeleteCourse(Long courseId, String adminUsername) {
        Domain domain = domainRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        domain.setStatus("DELETED");
        domain.setUpdatedBy(adminUsername);
        Domain saved = domainRepository.save(domain);

        logAdminAction(adminUsername, "DELETE_COURSE", "Course", courseId.toString(), "ACTIVE", "DELETED", "Soft deleted course");
        return mapToCourseResponse(saved, null);
    }

    @Transactional
    public CourseResponse restoreCourse(Long courseId, String adminUsername) {
        Domain domain = domainRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        domain.setStatus("ACTIVE");
        domain.setUpdatedBy(adminUsername);
        Domain saved = domainRepository.save(domain);

        logAdminAction(adminUsername, "RESTORE_COURSE", "Course", courseId.toString(), "DELETED", "ACTIVE", "Restored course");
        return mapToCourseResponse(saved, null);
    }

    @Transactional
    public LevelResponse createLevel(Long courseId, LevelRequest request, String adminUsername) {
        Domain domain = domainRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        int levelNum = request.levelNumber() > 0 ? request.levelNumber() : (domain.getLevels().size() + 1);

        Level level = new Level();
        level.setDomain(domain);
        level.setLevelNumber(levelNum);
        level.setName(request.name());
        level.setDescription(request.description());
        level.setDuration(request.duration() != null ? request.duration() : "1 Week");
        level.setLearningPace(request.learningPace() != null ? request.learningPace() : "2 hrs/day");
        level.setPoints(request.points() > 0 ? request.points() : 100);
        level.setTopicsCsv(request.topicsCsv());
        level.setInstructions(request.instructions());
        level.setExternalLinksJson(request.externalLinksJson());
        level.setStatus("ACTIVE");
        level.setCreatedBy(adminUsername);

        Level saved = levelRepository.save(level);

        logAdminAction(adminUsername, "CREATE_LEVEL", "Level", saved.getId().toString(), null, saved.getName(), "Created level under course " + courseId);
        return getLevelById(saved.getId(), null);
    }

    @Transactional
    public LevelResponse updateLevel(Long levelId, LevelRequest request, String adminUsername) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        String prevName = level.getName();
        if (request.levelNumber() > 0) level.setLevelNumber(request.levelNumber());
        level.setName(request.name());
        level.setDescription(request.description());
        if (request.duration() != null) level.setDuration(request.duration());
        if (request.learningPace() != null) level.setLearningPace(request.learningPace());
        if (request.points() > 0) level.setPoints(request.points());
        level.setTopicsCsv(request.topicsCsv());
        level.setInstructions(request.instructions());
        level.setExternalLinksJson(request.externalLinksJson());
        level.setUpdatedBy(adminUsername);

        Level saved = levelRepository.save(level);

        logAdminAction(adminUsername, "UPDATE_LEVEL", "Level", levelId.toString(), prevName, saved.getName(), "Updated level");
        return getLevelById(saved.getId(), null);
    }

    @Transactional
    public LevelResponse softDeleteLevel(Long levelId, String adminUsername) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        level.setStatus("DELETED");
        level.setUpdatedBy(adminUsername);
        Level saved = levelRepository.save(level);

        logAdminAction(adminUsername, "DELETE_LEVEL", "Level", levelId.toString(), "ACTIVE", "DELETED", "Soft deleted level");
        return getLevelById(saved.getId(), null);
    }

    @Transactional
    public LevelResponse restoreLevel(Long levelId, String adminUsername) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        level.setStatus("ACTIVE");
        level.setUpdatedBy(adminUsername);
        Level saved = levelRepository.save(level);

        logAdminAction(adminUsername, "RESTORE_LEVEL", "Level", levelId.toString(), "DELETED", "ACTIVE", "Restored level");
        return getLevelById(saved.getId(), null);
    }

    private CourseResponse mapToCourseResponse(Domain domain, Long userId) {
        List<Level> levels = levelRepository.findByDomainIdOrderByLevelNumberAsc(domain.getId());
        List<LevelSummaryResponse> levelSummaries = new ArrayList<>();

        Map<Long, UserLevelProgress> progressMap = new HashMap<>();
        if (userId != null) {
            List<UserLevelProgress> userProgressList = progressRepository.findByUserIdAndDomainId(userId, domain.getId());
            for (UserLevelProgress p : userProgressList) {
                progressMap.put(p.getLevel().getId(), p);
            }
        }

        Level firstActiveLevel = levels.stream().filter(l -> "ACTIVE".equals(l.getStatus())).findFirst().orElse(null);

        int totalCalculatedPoints = levels.stream()
            .filter(level -> "ACTIVE".equals(level.getStatus()))
            .mapToInt(Level::getPoints)
            .sum();

        for (Level lvl : levels) {

            String userStatus = "LOCKED";
            boolean tasksCompleted = false;
            int taskCount = 0;
            int completedTaskCount = 0;

            if (userId != null) {
                UserLevelProgress p = progressMap.get(lvl.getId());
                if (p != null) {
                    userStatus = p.getStatus();
                } else if (firstActiveLevel != null && firstActiveLevel.getId().equals(lvl.getId())) {
                    userStatus = "UNLOCKED";
                }

                List<LevelTask> tasks = taskRepository.findByLevelIdAndStatusOrderByOrderIndexAsc(lvl.getId(), "ACTIVE");
                taskCount = tasks.size();
                for (LevelTask t : tasks) {
                    boolean isComp = taskCompletionRepository.findByUserIdAndTaskId(userId, t.getId())
                            .map(UserLevelTaskCompletion::isCompleted)
                            .orElse(false);
                    if (isComp) completedTaskCount++;
                }
                tasksCompleted = (taskCount == 0) || (completedTaskCount == taskCount);
            }

            levelSummaries.add(new LevelSummaryResponse(
                    lvl.getId(),
                    lvl.getLevelNumber(),
                    lvl.getName(),
                    lvl.getDescription(),
                    lvl.getDuration(),
                    lvl.getLearningPace(),
                    lvl.getPoints(),
                    lvl.getStatus(),
                    userStatus,
                    tasksCompleted,
                    taskCount,
                    completedTaskCount
            ));
        }

        return new CourseResponse(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                domain.getPrerequisites(),
                domain.getLearningPace(),
                domain.getMinimumDurationWeeks(),
                totalCalculatedPoints,
                levels.size(),
                domain.getIconName(),
                domain.getStatus(),
                levelSummaries
        );
    }

    private void logAdminAction(String adminUsername, String action, String entityType, String entityId, String prevVal, String newVal, String reason) {
        AdminActivityLog log = new AdminActivityLog(
                null,
                adminUsername,
                null,
                action,
                entityType,
                entityId,
                null,
                prevVal,
                newVal,
                reason,
                java.time.LocalDateTime.now()
        );
        auditLogRepository.save(log);
    }
}
