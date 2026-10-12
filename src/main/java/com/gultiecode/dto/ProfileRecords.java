package com.gultiecode.dto;

import jakarta.validation.constraints.NotBlank;

public class ProfileRecords {

    public record UpdateProfileRequest(
            String fullName,
            String contactNumber,
            String highestQualification,
            String userType, // STUDENT, PROFESSIONAL
            String educationalInstitute,
            String course,
            String yearOfStudy,
            String occupation,
            String bio,
            String avatarUrl
    ) {}

    public record RoleChangeRequest(
            @NotBlank String role // ROLE_USER, ROLE_EDITOR, ROLE_ADMIN
    ) {}
}
