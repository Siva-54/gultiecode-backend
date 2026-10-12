package com.gultiecode.service;

import com.gultiecode.dto.AssessmentRecords.*;
import com.gultiecode.entity.*;
import com.gultiecode.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final LevelRepository levelRepository;
    private final UserRepository userRepository;

    public AssessmentService(AssessmentRepository assessmentRepository,
                             QuestionRepository questionRepository,
                             LevelRepository levelRepository,
                             UserRepository userRepository) {
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.levelRepository = levelRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AssessmentResponse startAssessment(Long levelId, Long userId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Question> questions = questionRepository.findByLevelIdOrderByOrderIndexAsc(levelId);
        if (questions.isEmpty()) {
            throw new RuntimeException("No assessment questions available for this level yet.");
        }

        Assessment assessment = new Assessment();
        assessment.setUser(user);
        assessment.setDomain(level.getDomain());
        assessment.setLevel(level);
        assessment.setScore(0);
        assessment.setPassed(false);
        assessment.setTotalQuestions(questions.size());
        assessment.setCorrectCount(0);
        assessment.setStartedAt(LocalDateTime.now());
        assessment.setCreatedBy(user.getUsername());

        Assessment saved = assessmentRepository.save(assessment);

        List<QuestionDto> questionDtos = questions.stream()
                .map(q -> new QuestionDto(
                        q.getId(),
                        q.getLevel().getId(),
                        q.getQuestionText(),
                        q.getOptionA(),
                        q.getOptionB(),
                        q.getOptionC(),
                        q.getOptionD(),
                        q.getCorrectAnswer(),
                        q.getPoints()
                ))
                .collect(Collectors.toList());

        return new AssessmentResponse(
                saved.getId(),
                level.getId(),
                level.getName(),
                level.getDomain().getId(),
                level.getDomain().getName(),
                questionDtos
        );
    }

    @Transactional
    public AssessmentResultResponse submitAssessment(SubmitAssessmentRequest request, Long userId) {
        Assessment assessment = assessmentRepository.findById(request.assessmentId())
                .orElseThrow(() -> new RuntimeException("Assessment attempt not found with id: " + request.assessmentId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Question> questions = questionRepository.findByLevelIdOrderByOrderIndexAsc(assessment.getLevel().getId());
        Map<Long, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        int correctCount = 0;
        int totalQuestions = questions.size();
        int totalPointsEarned = 0;

        List<AssessmentAnswer> answersList = new ArrayList<>();

        if (request.answers() != null) {
            for (AnswerSubmission sub : request.answers()) {
                Question q = questionMap.get(sub.questionId());
                if (q != null) {
                    boolean isCorrect = sub.selectedAnswer() != null && sub.selectedAnswer().trim().equalsIgnoreCase(q.getCorrectAnswer().trim());
                    if (isCorrect) {
                        correctCount++;
                        totalPointsEarned += q.getPoints();
                    }

                    AssessmentAnswer ans = new AssessmentAnswer();
                    ans.setAssessment(assessment);
                    ans.setQuestion(q);
                    ans.setUserAnswer(sub.selectedAnswer());
                    ans.setCorrect(isCorrect);
                    answersList.add(ans);
                }
            }
        }

        int score = totalQuestions > 0 ? (int) Math.round(((double) correctCount / totalQuestions) * 100.0) : 0;
        boolean passed = score >= 60;

        assessment.setScore(score);
        assessment.setPassed(passed);
        assessment.setTotalQuestions(totalQuestions);
        assessment.setCorrectCount(correctCount);
        assessment.setCompletedAt(LocalDateTime.now());
        assessment.getAnswers().clear();
        assessment.getAnswers().addAll(answersList);

        Assessment saved = assessmentRepository.save(assessment);

        if (passed) {
            user.setTotalPoints(user.getTotalPoints() + totalPointsEarned);
            userRepository.save(user);
        }

        return new AssessmentResultResponse(
                saved.getId(),
                score,
                passed,
                totalQuestions,
                correctCount,
                passed ? totalPointsEarned : 0,
                LocalDateTime.now()
        );
    }

    public List<AssessmentResultResponse> getUserAssessmentHistory(Long userId) {
        return assessmentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(a -> new AssessmentResultResponse(
                        a.getId(),
                        a.getScore(),
                        a.isPassed(),
                        a.getTotalQuestions(),
                        a.getCorrectCount(),
                        a.isPassed() ? a.getScore() : 0,
                        a.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
