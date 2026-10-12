package com.gultiecode.service;

import com.gultiecode.dto.AuthRecords.ProfileUpdateRequest;
import com.gultiecode.dto.AuthRecords.UserResponse;
import com.gultiecode.entity.User;
import com.gultiecode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.fullName());
        user.setContactNumber(request.contactNumber());
        if (request.userType() != null) user.setUserType(request.userType());
        user.setEducationalInstitute(request.educationalInstitute());
        user.setCourse(request.course());
        user.setYearOfStudy(request.yearOfStudy());
        user.setOccupation(request.occupation());
        user.setHighestQualification(request.highestQualification());
        if (request.bio() != null) user.setBio(request.bio());
        user.setUpdatedBy("user-" + userId);

        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    public UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getContactNumber(),
                user.getHighestQualification(),
                user.getUserType(),
                user.getEducationalInstitute(),
                user.getCourse(),
                user.getYearOfStudy(),
                user.getOccupation(),
                user.getBio(),
                user.getRole(),
                user.isApproved(),
                user.getApprovalStatus(),
                user.getAvatarUrl(),
                user.getTotalPoints(),
                user.getStreakDays(),
                user.getLongestStreakDays(),
                user.isEmailVerified()
        );
    }
}
