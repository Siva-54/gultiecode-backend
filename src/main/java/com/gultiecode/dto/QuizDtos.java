package com.gultiecode.dto;

import lombok.*;

import java.util.List;

public class QuizDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizQuestionResponse {
        private Long taskId;
        private String taskTitle;
        private String checkpointDescription;
        private String questionText;
        private List<String> options;
        private int timeLimitSeconds; // 20 - 30 seconds
        private String securityToken;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizSubmitRequest {
        private Long taskId;
        private String questionText;
        private String userAnswer;
        private int timeTakenSeconds;
        private int blurViolationsCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizResultResponse {
        private Long evaluationId;
        private Long taskId;
        private int score;
        private boolean passed;
        private String aiFeedback;
        private String correctAnswer;
        private String userAnswer;
        private int pointsEarned;
        private boolean flagged;
        private String flagReason;
    }
}
