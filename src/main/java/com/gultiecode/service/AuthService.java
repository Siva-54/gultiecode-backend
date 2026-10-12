package com.gultiecode.service;

import com.gultiecode.dto.AuthDtos.*;
import com.gultiecode.dto.ProfileRecords.UpdateProfileRequest;
import com.gultiecode.entity.Role;
import com.gultiecode.entity.User;
import com.gultiecode.repository.UserRepository;
import com.gultiecode.security.JwtTokenProvider;
import com.gultiecode.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered!");
        }

        // Generate 6-digit OTP code valid for 5 minutes
        String otpCode = String.format("%06d", new Random().nextInt(900000) + 100000);
        LocalDateTime otpExpiry = LocalDateTime.now().plusMinutes(5);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .contactNumber(request.getContactNumber())
                .highestQualification(request.getHighestQualification())
                .userType(request.getUserType())
                .educationalInstitute(request.getEducationalInstitute())
                .course(request.getCourse())
                .yearOfStudy(request.getYearOfStudy())
                .occupation(request.getOccupation())
                .bio(request.getBio())
                .role(Role.ROLE_USER)
                .approved(false)
                .approvalStatus("PENDING_APPROVAL")
                .avatarUrl("https://api.dicebear.com/7.x/bottts/svg?seed=" + request.getUsername())
                .totalPoints(0)
                .streakDays(0)
                .longestStreakDays(0)
                .emailVerified(false)
                .otpCode(otpCode)
                .otpExpiry(otpExpiry)
                .build();
        user.setCreatedBy("self_registration");

        userRepository.save(user);

        return AuthResponse.builder()
                .message("Registration successful. OTP sent to email: " + request.getEmail() + " (Mock OTP: " + otpCode + ")")
                .build();
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtpCode())) {
            throw new RuntimeException("Invalid OTP code!");
        }
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new RuntimeException("OTP has expired (valid for 5 minutes)!");
        }

        user.setEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return AuthResponse.builder()
                .message("Email verified successfully! Your account is awaiting administrator approval.")
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new RuntimeException("Invalid username/email or password!")));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email via OTP before logging in.");
        }

        if (!"APPROVED".equalsIgnoreCase(user.getApprovalStatus()) || !user.isApproved()) {
            throw new RuntimeException("Your account is awaiting administrator approval.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        // Update Daily Login Streak
        updateLoginStreak(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(mapToUserDto(user))
                .message("Login successful")
                .build();
    }

    @Transactional
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account registered with email: " + request.getEmail()));

        String otpCode = String.format("%06d", new Random().nextInt(900000) + 100000);
        user.setOtpCode(otpCode);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        return AuthResponse.builder()
                .message("Password reset OTP sent to email. (Mock OTP: " + otpCode + ")")
                .build();
    }

    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account registered with email: " + request.getEmail()));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtpCode())) {
            throw new RuntimeException("Invalid OTP code!");
        }
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new RuntimeException("OTP has expired!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return AuthResponse.builder()
                .message("Password reset successfully. You can now log in.")
                .build();
    }

    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (request.fullName() != null) user.setFullName(request.fullName());
        if (request.contactNumber() != null) user.setContactNumber(request.contactNumber());
        if (request.highestQualification() != null) user.setHighestQualification(request.highestQualification());
        if (request.userType() != null) user.setUserType(request.userType());
        if (request.educationalInstitute() != null) user.setEducationalInstitute(request.educationalInstitute());
        if (request.course() != null) user.setCourse(request.course());
        if (request.yearOfStudy() != null) user.setYearOfStudy(request.yearOfStudy());
        if (request.occupation() != null) user.setOccupation(request.occupation());
        if (request.bio() != null) user.setBio(request.bio());
        if (request.avatarUrl() != null && !request.avatarUrl().trim().isEmpty()) user.setAvatarUrl(request.avatarUrl());

        user.setUpdatedBy(user.getUsername());
        User saved = userRepository.save(user);
        return mapToUserDto(saved);
    }

    private void updateLoginStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastLogin = user.getLastLoginDate();

        if (lastLogin == null) {
            user.setStreakDays(1);
        } else if (lastLogin.equals(today)) {
            // Already logged in today - do not increment
        } else if (lastLogin.equals(today.minusDays(1))) {
            // Consecutive daily login
            user.setStreakDays(user.getStreakDays() + 1);
        } else {
            // Missed a day - reset streak
            user.setStreakDays(1);
        }

        user.setLastLoginDate(today);
        if (user.getStreakDays() > user.getLongestStreakDays()) {
            user.setLongestStreakDays(user.getStreakDays());
        }
        userRepository.save(user);
    }

    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserDto(user);
    }

    public UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .contactNumber(user.getContactNumber())
                .highestQualification(user.getHighestQualification())
                .userType(user.getUserType())
                .educationalInstitute(user.getEducationalInstitute())
                .course(user.getCourse())
                .yearOfStudy(user.getYearOfStudy())
                .occupation(user.getOccupation())
                .bio(user.getBio())
                .role(user.getRole())
                .approved(user.isApproved())
                .approvalStatus(user.getApprovalStatus() != null ? user.getApprovalStatus() : "PENDING_APPROVAL")
                .avatarUrl(user.getAvatarUrl())
                .totalPoints(user.getTotalPoints())
                .streakDays(user.getStreakDays())
                .longestStreakDays(user.getLongestStreakDays())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
