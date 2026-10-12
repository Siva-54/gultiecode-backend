package com.gultiecode.dto;

import com.gultiecode.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public class AuthDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank
        private String usernameOrEmail;

        @NotBlank
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank
        @Size(min = 3, max = 30)
        private String username;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 6, max = 40)
        private String password;

        @NotBlank
        private String fullName;

        private String contactNumber;
        private String highestQualification;

        @NotBlank
        private String userType; // STUDENT, PROFESSIONAL

        // Student Specific
        private String educationalInstitute;
        private String course;
        private String yearOfStudy;

        // Professional Specific
        private String occupation;

        private String bio;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyOtpRequest {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String otpCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgotPasswordRequest {
        @NotBlank
        @Email
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String otpCode;

        @NotBlank
        @Size(min = 6, max = 40)
        private String newPassword;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AuthResponse {
        private String accessToken;
        private String tokenType = "Bearer";
        private UserDto user;
        private String message;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String contactNumber;
        private String highestQualification;
        private String userType;
        private String educationalInstitute;
        private String course;
        private String yearOfStudy;
        private String occupation;
        private String bio;
        private Role role;
        private boolean approved;
        private String approvalStatus; // PENDING_APPROVAL, APPROVED, REJECTED
        private String avatarUrl;
        private int totalPoints;
        private int streakDays;
        private int longestStreakDays;
        private boolean emailVerified;
    }
}
