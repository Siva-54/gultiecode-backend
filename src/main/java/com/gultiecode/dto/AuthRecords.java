package com.gultiecode.dto;

import com.gultiecode.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRecords {

    public record LoginRequest(
            @NotBlank String usernameOrEmail,
            @NotBlank String password
    ) {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 30) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 40) String password,
            @NotBlank String fullName,
            String contactNumber,
            String highestQualification,
            @NotBlank String userType, // STUDENT, PROFESSIONAL
            String educationalInstitute,
            String course,
            String yearOfStudy,
            String occupation,
            String bio
    ) {}

    public record ProfileUpdateRequest(
            @NotBlank String fullName,
            String contactNumber,
            String userType,
            String educationalInstitute,
            String course,
            String yearOfStudy,
            String occupation,
            String highestQualification,
            String bio
    ) {}

    public record VerifyOtpRequest(
            @NotBlank @Email String email,
            @NotBlank String otpCode
    ) {}

    public record ForgotPasswordRequest(
            @NotBlank @Email String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank @Email String email,
            @NotBlank String otpCode,
            @NotBlank @Size(min = 6, max = 40) String newPassword
    ) {}

    public record UserResponse(
            Long id,
            String username,
            String email,
            String fullName,
            String contactNumber,
            String highestQualification,
            String userType,
            String educationalInstitute,
            String course,
            String yearOfStudy,
            String occupation,
            String bio,
            Role role,
            boolean approved,
            String approvalStatus,
            String avatarUrl,
            int totalPoints,
            int streakDays,
            int longestStreakDays,
            boolean emailVerified
    ) {}

    public record AuthResponse(
            String accessToken,
            String tokenType,
            UserResponse user,
            String message
    ) {}
}
