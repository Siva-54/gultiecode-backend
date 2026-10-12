package com.gultiecode.controller;

import com.gultiecode.dto.QuizDtos.*;
import com.gultiecode.security.UserPrincipal;
import com.gultiecode.service.QuizEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizEvaluationService quizEvaluationService;

    @GetMapping("/generate/{taskId}")
    public ResponseEntity<QuizQuestionResponse> generateQuizQuestion(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(quizEvaluationService.generateQuizQuestion(taskId, currentUser.getId()));
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizResultResponse> submitQuizEvaluation(
            @RequestBody QuizSubmitRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(quizEvaluationService.submitQuizEvaluation(request, currentUser.getId()));
    }
}
