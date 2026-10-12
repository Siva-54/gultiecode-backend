package com.gultiecode.repository;

import com.gultiecode.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Assessment> findByDomainId(Long domainId);
    long countByUserIdAndPassed(Long userId, boolean passed);
}
