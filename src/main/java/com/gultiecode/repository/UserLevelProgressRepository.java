package com.gultiecode.repository;

import com.gultiecode.entity.UserLevelProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLevelProgressRepository extends JpaRepository<UserLevelProgress, Long> {
    Optional<UserLevelProgress> findByUserIdAndLevelId(Long userId, Long levelId);
    List<UserLevelProgress> findByUserIdAndDomainId(Long userId, Long domainId);
    List<UserLevelProgress> findByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, String status);
    long countByUserIdAndDomainIdAndStatus(Long userId, Long domainId, String status);
}
