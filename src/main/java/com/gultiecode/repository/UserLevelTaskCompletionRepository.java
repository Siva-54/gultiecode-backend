package com.gultiecode.repository;

import com.gultiecode.entity.UserLevelTaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLevelTaskCompletionRepository extends JpaRepository<UserLevelTaskCompletion, Long> {
    Optional<UserLevelTaskCompletion> findByUserIdAndTaskId(Long userId, Long taskId);
    List<UserLevelTaskCompletion> findByUserIdAndTask_Level_Id(Long userId, Long levelId);
    long countByUserIdAndTask_Level_IdAndCompletedTrue(Long userId, Long levelId);
}
