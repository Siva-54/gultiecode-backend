package com.gultiecode.controller;

import com.gultiecode.dto.AssessmentRecords.*;
import com.gultiecode.security.UserPrincipal;
import com.gultiecode.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping("/start/{levelId}")
    public ResponseEntity<AssessmentResponse> startAssessment(
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.startAssessment(levelId, currentUser.getId()));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<AssessmentResultResponse> submitAssessment(
            @PathVariable Long id,
            @RequestBody SubmitAssessmentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.submitAssessment(request, currentUser.getId()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AssessmentResultResponse>> getUserAssessmentHistory(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.getUserAssessmentHistory(currentUser.getId()));
    }
}
