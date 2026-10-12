package com.gultiecode.controller;

import com.gultiecode.dto.AuthDtos.UserDto;
import com.gultiecode.dto.ProfileRecords.UpdateProfileRequest;
import com.gultiecode.security.UserPrincipal;
import com.gultiecode.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile Management", description = "Endpoints for viewing and editing personal and professional user profile information")
public class UserController {

    private final AuthService authService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(authService.getCurrentUser(currentUser.getId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update personal & professional profile information (email is immutable)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(currentUser.getId(), request));
    }
}
