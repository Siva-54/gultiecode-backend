package com.gultiecode.controller;

import com.gultiecode.dto.AssessmentEngineRecords.*;
import com.gultiecode.security.UserPrincipal;
import com.gultiecode.service.AssessmentEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Assessments & Question Engine", description = "Endpoints for starting, executing, and evaluating level assessments with 80% passing threshold and anti-cheat timer")
public class AssessmentEngineController {

    private final AssessmentEngineService assessmentService;

    public AssessmentEngineController(AssessmentEngineService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/levels/{levelId}/assessment/status")
    @Operation(summary = "Get level assessment unlock status", description = "Checks if user has completed all tasks and level is unlocked to take assessment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AssessmentStatusResponse> getAssessmentStatus(
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.getAssessmentStatus(levelId, currentUser.getId()));
    }

    @PostMapping("/levels/{levelId}/assessment/start")
    @Operation(summary = "Start level assessment", description = "Starts a timed assessment attempt and returns questions without answers", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AssessmentStartResponse> startAssessment(
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.startAssessment(levelId, currentUser.getId()));
    }

    @PostMapping("/levels/{levelId}/assessment/submit")
    @Operation(summary = "Submit level assessment answers", description = "Evaluates submitted answers, calculates score (>=80% pass), awards points, and unlocks next level", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AssessmentResultResponse> submitAssessment(
            @PathVariable Long levelId,
            @RequestBody AssessmentSubmitRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.submitAssessment(request, currentUser.getId()));
    }
}