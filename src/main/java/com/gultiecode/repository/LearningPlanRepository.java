package com.gultiecode.repository;

import com.gultiecode.entity.LearningPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPlanRepository extends JpaRepository<LearningPlan, Long> {
    List<LearningPlan> findByDomainId(Long domainId);
    Optional<LearningPlan> findByDomainIdAndLevelId(Long domainId, Long levelId);
}
