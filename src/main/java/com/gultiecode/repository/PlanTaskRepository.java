package com.gultiecode.repository;

import com.gultiecode.entity.PlanTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanTaskRepository extends JpaRepository<PlanTask, Long> {
    List<PlanTask> findByLearningPlanIdOrderByWeekNumberAsc(Long planId);
}
