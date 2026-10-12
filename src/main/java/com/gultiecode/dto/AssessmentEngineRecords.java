package com.gultiecode.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AssessmentEngineRecords {

    public record QuestionAdminRequest(
            @NotBlank String questionText,
            @NotBlank String optionA,
            @NotBlank String optionB,
            @NotBlank String optionC,
            @NotBlank String optionD,
            @NotBlank String correctAnswer,
            int points,
            int orderIndex
    ) {}

    public record QuestionAdminResponse(
            Long id,
            Long levelId,
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctAnswer,
            int points,
            int orderIndex,
            String status
    ) {}

    public record AssessmentQuestionDto(
            Long id,
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            int points
    ) {}

    public record AssessmentStartResponse(
            Long attemptId,
            Long levelId,
            String levelName,
            Long courseId,
            String courseName,
            List<AssessmentQuestionDto> questions,
            int timeLimitSecondsPerQuestion
    ) {}

    public record AnswerSubmissionDto(
            Long questionId,
            String selectedAnswer
    ) {}

    public record AssessmentSubmitRequest(
            Long attemptId,
            List<AnswerSubmissionDto> answers
    ) {}

    public record AssessmentResultResponse(
            Long attemptId,
            int score,
            boolean passed,
            int totalQuestions,
            int correctCount,
            int pointsEarned,
            boolean pointsAlreadyAwarded,
            boolean nextLevelUnlocked,
            Long nextLevelId
    ) {}

    public record AssessmentStatusResponse(
            Long levelId,
            String levelStatus, // LOCKED, UNLOCKED, COMPLETED
            boolean tasksCompleted,
            boolean canTakeAssessment,
            int taskCount,
            int completedTaskCount,
            String message
    ) {}
}
