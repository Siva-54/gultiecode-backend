package com.gultiecode.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    @Column(nullable = false)
    private boolean approved = false;

    @Builder.Default
    private String approvalStatus = "PENDING_APPROVAL"; // PENDING_APPROVAL, APPROVED, REJECTED

    private String fullName;

    private String contactNumber;

    private String highestQualification;

    private String userType; // STUDENT, PROFESSIONAL

    // Conditional Student Fields
    private String educationalInstitute;
    private String course;
    private String yearOfStudy;

    // Conditional Professional Fields
    private String occupation;

    @Column(length = 500)
    private String bio;

    private String avatarUrl;

    @Builder.Default
    private int totalPoints = 0;

    @Builder.Default
    private int streakDays = 0;

    @Builder.Default
    private int longestStreakDays = 0;

    private LocalDate lastLoginDate;

    // OTP Email Verification
    @Builder.Default
    private boolean emailVerified = false;

    private String otpCode;

    private LocalDateTime otpExpiry;
}
