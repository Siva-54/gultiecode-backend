package com.gultiecode.controller;

import com.gultiecode.dto.PlanDtos.*;
import com.gultiecode.security.UserPrincipal;
import com.gultiecode.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<List<LearningPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/domain/{domainId}")
    public ResponseEntity<List<LearningPlanResponse>> getPlansByDomain(@PathVariable Long domainId) {
        return ResponseEntity.ok(planService.getPlansByDomain(domainId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearningPlanResponse> getPlanById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(planService.getPlanById(id, userId));
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<UserPlanProgressDto> enrollInPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(planService.enrollInPlan(id, currentUser.getId()));
    }

    @GetMapping("/my-plans")
    public ResponseEntity<List<UserPlanProgressDto>> getUserEnrolledPlans(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(planService.getUserEnrolledPlans(currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        planService.deleteUserPlan(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
