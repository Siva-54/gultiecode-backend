package com.gultiecode.service;

import com.gultiecode.dto.AssessmentEngineRecords.*;
import com.gultiecode.entity.*;
import com.gultiecode.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssessmentEngineService {

    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final LevelRepository levelRepository;
    private final LevelTaskRepository taskRepository;
    private final UserLevelProgressRepository progressRepository;
    private final UserLevelTaskCompletionRepository taskCompletionRepository;
    private final UserCoursePointsRepository coursePointsRepository;
    private final UserRepository userRepository;
    private final AdminActivityLogRepository auditLogRepository;

    public AssessmentEngineService(AssessmentRepository assessmentRepository,
                                   QuestionRepository questionRepository,
                                   LevelRepository levelRepository,
                                   LevelTaskRepository taskRepository,
                                   UserLevelProgressRepository progressRepository,
                                   UserLevelTaskCompletionRepository taskCompletionRepository,
                                   UserCoursePointsRepository coursePointsRepository,
                                   UserRepository userRepository,
                                   AdminActivityLogRepository auditLogRepository) {
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.levelRepository = levelRepository;
        this.taskRepository = taskRepository;
        this.progressRepository = progressRepository;
        this.taskCompletionRepository = taskCompletionRepository;
        this.coursePointsRepository = coursePointsRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public AssessmentStatusResponse getAssessmentStatus(Long levelId, Long userId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        String levelStatus = "LOCKED";
        UserLevelProgress progress = progressRepository.findByUserIdAndLevelId(userId, levelId).orElse(null);
        boolean isLevel1 = levelRepository.findFirstByDomainIdAndStatusOrderByLevelNumberAsc(level.getDomain().getId(), "ACTIVE")
                .map(l -> l.getId().equals(levelId)).orElse(false);

        if (progress != null) {
            levelStatus = progress.getStatus();
        } else if (isLevel1) {
            levelStatus = "UNLOCKED";
        }

        List<LevelTask> activeTasks = taskRepository.findByLevelIdAndStatusOrderByOrderIndexAsc(levelId, "ACTIVE");
        int taskCount = activeTasks.size();
        int completedCount = 0;
        for (LevelTask t : activeTasks) {
            boolean isComp = taskCompletionRepository.findByUserIdAndTaskId(userId, t.getId())
                    .map(UserLevelTaskCompletion::isCompleted)
                    .orElse(false);
            if (isComp) completedCount++;
        }

        boolean tasksCompleted = (taskCount == 0) || (completedCount == taskCount);
        boolean canTake = !"LOCKED".equalsIgnoreCase(levelStatus) && tasksCompleted;
        String message = canTake ? "Assessment unlocked and ready" :
                ("LOCKED".equalsIgnoreCase(levelStatus) ? "Level is locked. Complete previous level to unlock." : "Complete all tasks in this level to unlock the assessment.");

        return new AssessmentStatusResponse(levelId, levelStatus, tasksCompleted, canTake, taskCount, completedCount, message);
    }

    @Transactional
    public AssessmentStartResponse startAssessment(Long levelId, Long userId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AssessmentStatusResponse status = getAssessmentStatus(levelId, userId);
        if (!status.canTakeAssessment()) {
            throw new RuntimeException("Assessment is locked: " + status.message());
        }

        List<Question> questions = questionRepository.findByLevelIdAndStatusOrderByOrderIndexAsc(levelId, "ACTIVE");
        if (questions.isEmpty()) {
            throw new RuntimeException("No active assessment questions found for this level.");
        }

        Assessment attempt = new Assessment();
        attempt.setUser(user);
        attempt.setLevel(level);
        attempt.setDomain(level.getDomain());
        attempt.setScore(0);
        attempt.setPassed(false);
        attempt.setTotalQuestions(questions.size());
        attempt.setCorrectCount(0);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setCreatedBy(user.getUsername());

        Assessment savedAttempt = assessmentRepository.save(attempt);

        List<AssessmentQuestionDto> questionDtos = questions.stream()
                .map(q -> new AssessmentQuestionDto(
                        q.getId(),
                        q.getQuestionText(),
                        q.getOptionA(),
                        q.getOptionB(),
                        q.getOptionC(),
                        q.getOptionD(),
                        q.getPoints()
                ))
                .collect(Collectors.toList());

        return new AssessmentStartResponse(
                savedAttempt.getId(),
                level.getId(),
                level.getName(),
                level.getDomain().getId(),
                level.getDomain().getName(),
                questionDtos,
                30 // 30 seconds per question
        );
    }

    @Transactional
    public AssessmentResultResponse submitAssessment(AssessmentSubmitRequest request, Long userId) {
        Assessment attempt = assessmentRepository.findById(request.attemptId())
                .orElseThrow(() -> new RuntimeException("Assessment attempt not found with id: " + request.attemptId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new RuntimeException("Assessment attempt does not belong to user");
        }

        Level level = attempt.getLevel();
        Domain domain = attempt.getDomain();

        List<Question> questions = questionRepository.findByLevelIdAndStatusOrderByOrderIndexAsc(level.getId(), "ACTIVE");
        Map<Long, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        int correctCount = 0;
        int totalQuestions = questions.size();

        List<AssessmentAnswer> answerEntities = new ArrayList<>();

        if (request.answers() != null) {
            for (AnswerSubmissionDto sub : request.answers()) {
                Question q = questionMap.get(sub.questionId());
                if (q != null) {
                    boolean isCorrect = sub.selectedAnswer() != null &&
                            sub.selectedAnswer().trim().equalsIgnoreCase(q.getCorrectAnswer().trim());
                    if (isCorrect) {
                        correctCount++;
                    }

                    AssessmentAnswer ans = new AssessmentAnswer();
                    ans.setAssessment(attempt);
                    ans.setQuestion(q);
                    ans.setUserAnswer(sub.selectedAnswer());
                    ans.setCorrect(isCorrect);
                    answerEntities.add(ans);
                }
            }
        }

        int score = totalQuestions > 0 ? (int) Math.round(((double) correctCount / totalQuestions) * 100.0) : 0;
        boolean passed = score >= 80; // Strict 80% passing threshold!

        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setCorrectCount(correctCount);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.getAnswers().clear();
        attempt.getAnswers().addAll(answerEntities);

        assessmentRepository.save(attempt);

        int pointsEarned = 0;
        boolean pointsAlreadyAwarded = false;
        boolean nextLevelUnlocked = false;
        Long nextLevelId = null;

        // Fetch or create user level progress
        UserLevelProgress progress = progressRepository.findByUserIdAndLevelId(userId, level.getId())
                .orElseGet(() -> {
                    UserLevelProgress p = new UserLevelProgress(null, user, level, domain, "UNLOCKED", false, 0, false, null);
                    p.setCreatedBy(user.getUsername());
                    return progressRepository.save(p);
                });

        if (passed) {
            progress.setStatus("COMPLETED");
            progress.setCompletedAt(LocalDateTime.now());
            if (progress.getBestScore() == null || score > progress.getBestScore()) {
                progress.setBestScore(score);
            }

            // Check duplicate point awards prevention!
            if (!progress.isPointsAwarded()) {
                pointsEarned = level.getPoints();
                user.setTotalPoints(user.getTotalPoints() + pointsEarned);
                userRepository.save(user);

                // Update UserCoursePoints
                UserCoursePoints cp = coursePointsRepository.findByUserIdAndDomainId(userId, domain.getId())
                        .orElseGet(() -> new UserCoursePoints(null, user, domain, 0, 0));
                cp.setPoints(cp.getPoints() + pointsEarned);
                cp.setCompletedLevelsCount(cp.getCompletedLevelsCount() + 1);
                coursePointsRepository.save(cp);

                progress.setPointsAwarded(true);
            } else {
                pointsAlreadyAwarded = true;
            }
            progressRepository.save(progress);

            // Unlock next level in sequence!
            Optional<Level> nextLevelOpt = levelRepository.findByDomainIdAndLevelNumberAndStatus(
                    domain.getId(), level.getLevelNumber() + 1, "ACTIVE");

            if (nextLevelOpt.isPresent()) {
                Level nextLevel = nextLevelOpt.get();
                UserLevelProgress nextProgress = progressRepository.findByUserIdAndLevelId(userId, nextLevel.getId())
                        .orElseGet(() -> {
                            UserLevelProgress np = new UserLevelProgress(null, user, nextLevel, domain, "LOCKED", false, 0, false, null);
                            np.setCreatedBy(user.getUsername());
                            return np;
                        });

                if (!"COMPLETED".equalsIgnoreCase(nextProgress.getStatus())) {
                    nextProgress.setStatus("UNLOCKED");
                    progressRepository.save(nextProgress);
                    nextLevelUnlocked = true;
                    nextLevelId = nextLevel.getId();
                }
            }
        }

        return new AssessmentResultResponse(
                attempt.getId(),
                score,
                passed,
                totalQuestions,
                correctCount,
                pointsEarned,
                pointsAlreadyAwarded,
                nextLevelUnlocked,
                nextLevelId
        );
    }

    public List<QuestionAdminResponse> getQuestionsByLevelForAdmin(Long levelId) {
        List<Question> questions = questionRepository.findByLevelIdOrderByOrderIndexAsc(levelId);
        return questions.stream()
                .map(this::mapToQuestionAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestionAdminResponse createQuestion(Long levelId, QuestionAdminRequest request, String adminUsername) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        int orderIdx = request.orderIndex() > 0 ? request.orderIndex() : (int) (questionRepository.countByLevelIdAndStatus(levelId, "ACTIVE") + 1);

        Question q = new Question();
        q.setLevel(level);
        q.setQuestionText(request.questionText());
        q.setOptionA(request.optionA());
        q.setOptionB(request.optionB());
        q.setOptionC(request.optionC());
        q.setOptionD(request.optionD());
        q.setCorrectAnswer(request.correctAnswer());
        q.setPoints(request.points() > 0 ? request.points() : 100);
        q.setOrderIndex(orderIdx);
        q.setStatus("ACTIVE");
        q.setCreatedBy(adminUsername);

        Question saved = questionRepository.save(q);
        logAdminAction(adminUsername, "CREATE_QUESTION", "Question", saved.getId().toString(), null, saved.getQuestionText(), "Created question under level " + levelId);
        return mapToQuestionAdminResponse(saved);
    }

    @Transactional
    public QuestionAdminResponse updateQuestion(Long questionId, QuestionAdminRequest request, String adminUsername) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        String prevText = q.getQuestionText();
        q.setQuestionText(request.questionText());
        q.setOptionA(request.optionA());
        q.setOptionB(request.optionB());
        q.setOptionC(request.optionC());
        q.setOptionD(request.optionD());
        q.setCorrectAnswer(request.correctAnswer());
        if (request.points() > 0) q.setPoints(request.points());
        if (request.orderIndex() > 0) q.setOrderIndex(request.orderIndex());
        q.setUpdatedBy(adminUsername);

        Question saved = questionRepository.save(q);
        logAdminAction(adminUsername, "UPDATE_QUESTION", "Question", questionId.toString(), prevText, saved.getQuestionText(), "Updated question");
        return mapToQuestionAdminResponse(saved);
    }

    @Transactional
    public QuestionAdminResponse softDeleteQuestion(Long questionId, String adminUsername) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        q.setStatus("DELETED");
        q.setUpdatedBy(adminUsername);
        Question saved = questionRepository.save(q);

        logAdminAction(adminUsername, "DELETE_QUESTION", "Question", questionId.toString(), "ACTIVE", "DELETED", "Soft deleted question");
        return mapToQuestionAdminResponse(saved);
    }

    @Transactional
    public QuestionAdminResponse restoreQuestion(Long questionId, String adminUsername) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        q.setStatus("ACTIVE");
        q.setUpdatedBy(adminUsername);
        Question saved = questionRepository.save(q);

        logAdminAction(adminUsername, "RESTORE_QUESTION", "Question", questionId.toString(), "DELETED", "ACTIVE", "Restored question");
        return mapToQuestionAdminResponse(saved);
    }

    private QuestionAdminResponse mapToQuestionAdminResponse(Question q) {
        return new QuestionAdminResponse(
                q.getId(),
                q.getLevel().getId(),
                q.getQuestionText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                q.getCorrectAnswer(),
                q.getPoints(),
                q.getOrderIndex(),
                q.getStatus()
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
                LocalDateTime.now()
        );
        auditLogRepository.save(log);
    }
}
