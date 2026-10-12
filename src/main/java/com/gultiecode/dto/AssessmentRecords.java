package com.gultiecode.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class AssessmentRecords {

    public record QuestionDto(
            Long id,
            Long levelId,
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctAnswer,
            int points
    ) {}

    public record QuestionRequest(
            @NotBlank String questionText,
            @NotBlank String optionA,
            @NotBlank String optionB,
            @NotBlank String optionC,
            @NotBlank String optionD,
            @NotBlank String correctAnswer,
            int points
    ) {}

    public record AssessmentResponse(
            Long assessmentId,
            Long levelId,
            String levelName,
            Long domainId,
            String domainName,
            List<QuestionDto> questions
    ) {}

    public record AnswerSubmission(
            Long questionId,
            String selectedAnswer
    ) {}

    public record SubmitAssessmentRequest(
            Long assessmentId,
            List<AnswerSubmission> answers
    ) {}

    public record AssessmentResultResponse(
            Long assessmentId,
            int score,
            boolean passed,
            int totalQuestions,
            int correctCount,
            int pointsEarned,
            LocalDateTime completedAt
    ) {}
}
