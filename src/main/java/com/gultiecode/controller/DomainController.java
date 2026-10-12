package com.gultiecode.controller;

import com.gultiecode.dto.DomainDtos.*;
import com.gultiecode.security.UserPrincipal;
import com.gultiecode.service.DomainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domains")
@RequiredArgsConstructor
public class DomainController {

    private final DomainService domainService;

    @GetMapping
    public ResponseEntity<List<DomainResponse>> getAllActiveDomains() {
        return ResponseEntity.ok(domainService.getAllActiveDomains());
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DomainResponse>> getAllDomainsForAdmin() {
        return ResponseEntity.ok(domainService.getAllDomainsForAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomainResponse> getDomainById(@PathVariable Long id) {
        return ResponseEntity.ok(domainService.getDomainById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DomainResponse> createDomain(
            @Valid @RequestBody DomainRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(domainService.createDomain(request, currentUser.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DomainResponse> updateDomain(
            @PathVariable Long id,
            @Valid @RequestBody DomainRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(domainService.updateDomain(id, request, currentUser.getUsername()));
    }

    @PutMapping("/{id}/soft-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DomainResponse> softDeleteDomain(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(domainService.softDeleteDomain(id, currentUser.getUsername()));
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DomainResponse> restoreDomain(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(domainService.restoreDomain(id, currentUser.getUsername()));
    }

    @PostMapping("/{domainId}/levels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LevelDto> createLevel(
            @PathVariable Long domainId,
            @RequestBody LevelDto levelDto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(domainService.createLevel(domainId, levelDto, currentUser.getUsername()));
    }

    @DeleteMapping("/levels/{levelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLevel(
            @PathVariable Long levelId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        domainService.deleteLevel(levelId, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }
}
