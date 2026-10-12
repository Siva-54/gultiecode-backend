package com.gultiecode.controller;

import com.gultiecode.dto.AssessmentRecords.QuestionDto;
import com.gultiecode.dto.AssessmentRecords.QuestionRequest;
import com.gultiecode.security.UserPrincipal;
import com.gultiecode.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/levels/{levelId}/questions")
    public ResponseEntity<List<QuestionDto>> getQuestionsByLevel(@PathVariable Long levelId) {
        return ResponseEntity.ok(questionService.getQuestionsByLevel(levelId));
    }

    @PostMapping("/levels/{levelId}/questions")
    public ResponseEntity<QuestionDto> createQuestion(
            @PathVariable Long levelId,
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(questionService.createQuestion(levelId, request, currentUser.getUsername()));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<QuestionDto> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request, currentUser.getUsername()));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
