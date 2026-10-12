package com.gultiecode.repository;

import com.gultiecode.entity.UserPlanProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPlanProgressRepository extends JpaRepository<UserPlanProgress, Long> {
    List<UserPlanProgress> findByUserId(Long userId);
    Optional<UserPlanProgress> findByUserIdAndLearningPlanId(Long userId, Long learningPlanId);
    void deleteByUserIdAndLearningPlanId(Long userId, Long learningPlanId);
}
