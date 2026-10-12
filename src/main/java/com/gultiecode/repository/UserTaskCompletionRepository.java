package com.gultiecode.repository;

import com.gultiecode.entity.UserTaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTaskCompletionRepository extends JpaRepository<UserTaskCompletion, Long> {
    List<UserTaskCompletion> findByUserId(Long userId);
    Optional<UserTaskCompletion> findByUserIdAndTaskId(Long userId, Long taskId);
    long countByUserIdAndCompleted(Long userId, boolean completed);
}
