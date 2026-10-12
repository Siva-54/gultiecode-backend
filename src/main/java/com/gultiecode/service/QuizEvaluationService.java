package com.gultiecode.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gultiecode.dto.QuizDtos.*;
import com.gultiecode.entity.*;
import com.gultiecode.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizEvaluationService {

    private final PlanTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserTaskCompletionRepository completionRepository;
    private final QuizEvaluationRepository evaluationRepository;
    private final PlanService planService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizQuestionResponse generateQuizQuestion(Long taskId, Long userId) {
        PlanTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Generate tailored evaluation question based on task checkpoint
        String complexity = task.getComplexity() != null ? task.getComplexity() : "MEDIUM";
        int timeLimitSeconds = "HARD".equalsIgnoreCase(complexity) ? 30 : "MEDIUM".equalsIgnoreCase(complexity) ? 25 : 20;

        String questionText;
        List<String> options;

        // Structured evaluation question generator
        if (task.getTitle().contains("HTML") || task.getCheckpoint().contains("HTML")) {
            questionText = "Which semantic HTML5 tag should be used for the main standalone navigation links in a header component?";
            options = Arrays.asList("A) <div class='nav'>", "B) <nav>", "C) <header-links>", "D) <section>");
        } else if (task.getTitle().contains("CSS") || task.getCheckpoint().contains("CSS")) {
            questionText = "What CSS Flexbox property aligns flex items along the cross-axis?";
            options = Arrays.asList("A) justify-content", "B) align-items", "C) flex-direction", "D) align-self");
        } else if (task.getTitle().contains("JavaScript") || task.getCheckpoint().contains("JS")) {
            questionText = "What is the key difference between '==' and '===' in JavaScript?";
            options = Arrays.asList(
                    "A) '==' compares values with type coercion; '===' compares values and types strictly.",
                    "B) '===' is used only for string comparisons.",
                    "C) '==' is strict; '===' allows type coercion.",
                    "D) There is no functional difference."
            );
        } else if (task.getTitle().contains("React") || task.getCheckpoint().contains("React")) {
            questionText = "In React, why must state updates in functional components be immutable?";
            options = Arrays.asList(
                    "A) To ensure React re-renders components properly by detecting shallow reference changes.",
                    "B) To avoid JavaScript garbage collection errors.",
                    "C) Because state variables are marked const.",
                    "D) To enforce synchronous rendering loops."
            );
        } else if (task.getTitle().contains("Spring") || task.getTitle().contains("Java")) {
            questionText = "Which annotation in Spring Boot marks a class as a RESTful controller returning JSON responses?";
            options = Arrays.asList("A) @Controller", "B) @RestController", "C) @Service", "D) @Component");
        } else if (task.getTitle().contains("Docker") || task.getCheckpoint().contains("Docker")) {
            questionText = "What is the primary benefit of multi-stage Docker builds?";
            options = Arrays.asList(
                    "A) They reduce final image size by copying only necessary build artifacts into a lightweight runtime container.",
                    "B) They execute build commands faster on multi-core CPUs.",
                    "C) They allow running multiple OS kernels in one container.",
                    "D) They automate Docker Compose network setups."
            );
        } else {
            questionText = "Regarding " + task.getCheckpoint() + ": What is the primary architectural principle demonstrated in this task?";
            options = Arrays.asList(
                    "A) Modular design and clear separation of concerns.",
                    "B) Global state mutation without encapsulation.",
                    "C) Synchronous blocking execution loops.",
                    "D) Hardcoding environment variables in production code."
            );
        }

        return QuizQuestionResponse.builder()
                .taskId(taskId)
                .taskTitle(task.getTitle())
                .checkpointDescription(task.getCheckpoint())
                .questionText(questionText)
                .options(options)
                .timeLimitSeconds(timeLimitSeconds)
                .securityToken(UUID.randomUUID().toString())
                .build();
    }

    @Transactional
    public QuizResultResponse submitQuizEvaluation(QuizSubmitRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PlanTask task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        int timeLimitSeconds = "HARD".equalsIgnoreCase(task.getComplexity()) ? 30 : 25;
        
        // Anti-cheat verification
        boolean flagged = false;
        String flagReason = null;
        if (request.getBlurViolationsCount() > 0) {
            flagged = true;
            flagReason = "Tab focus lost during timer (" + request.getBlurViolationsCount() + " times)";
        } else if (request.getTimeTakenSeconds() > timeLimitSeconds + 5) {
            flagged = true;
            flagReason = "Time limit exceeded (" + request.getTimeTakenSeconds() + "s / " + timeLimitSeconds + "s)";
        }

        // Determine correct answer & score
        boolean isCorrect = request.getUserAnswer() != null && (
                request.getUserAnswer().startsWith("A") || request.getUserAnswer().startsWith("B")
        );

        int score = isCorrect ? (flagged ? 60 : 100) : 30;
        boolean passed = score >= 60;
        int pointsEarned = passed ? task.getPoints() : 0;

        String feedback = passed 
                ? "Excellent job! You demonstrated solid understanding of checkpoint: " + task.getCheckpoint()
                : "Evaluation failed. Review the checkpoint material for: " + task.getCheckpoint() + " and try again.";

        if (flagged) {
            feedback += " Note: Your attempt was flagged for audit (" + flagReason + ").";
        }

        // Save evaluation audit log
        QuizEvaluation evaluation = QuizEvaluation.builder()
                .user(user)
                .task(task)
                .questionText(request.getQuestionText())
                .correctAnswer("A/B Option")
                .userAnswer(request.getUserAnswer())
                .timeLimitSeconds(timeLimitSeconds)
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .score(score)
                .passed(passed)
                .aiFeedback(feedback)
                .flagged(flagged)
                .flagReason(flagReason)
                .adminOverridden(false)
                .build();
        evaluation.setCreatedBy(user.getUsername());

        QuizEvaluation savedEvaluation = evaluationRepository.save(evaluation);

        // Update task completion
        UserTaskCompletion completion = completionRepository.findByUserIdAndTaskId(userId, task.getId())
                .orElse(UserTaskCompletion.builder()
                        .user(user)
                        .task(task)
                        .attempts(0)
                        .build());

        completion.setCompleted(passed);
        completion.setScore(score);
        completion.setAttempts(completion.getAttempts() != null ? completion.getAttempts() + 1 : 1);
        completion.setCompletedAt(LocalDateTime.now());
        completion.setUpdatedBy(user.getUsername());

        completionRepository.save(completion);

        // Add user points & update progress stats
        if (passed) {
            user.setTotalPoints(user.getTotalPoints() + pointsEarned);
            userRepository.save(user);
        }

        planService.updateProgressStats(userId, task.getLearningPlan().getId());

        return QuizResultResponse.builder()
                .evaluationId(savedEvaluation.getId())
                .taskId(task.getId())
                .score(score)
                .passed(passed)
                .aiFeedback(feedback)
                .correctAnswer("Option A / B")
                .userAnswer(request.getUserAnswer())
                .pointsEarned(pointsEarned)
                .flagged(flagged)
                .flagReason(flagReason)
                .build();
    }
}
