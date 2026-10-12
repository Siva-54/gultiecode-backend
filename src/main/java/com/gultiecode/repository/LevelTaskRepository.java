package com.gultiecode.repository;

import com.gultiecode.entity.LevelTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelTaskRepository extends JpaRepository<LevelTask, Long> {
    List<LevelTask> findByLevelIdAndStatusOrderByOrderIndexAsc(Long levelId, String status);
    List<LevelTask> findByLevelIdOrderByOrderIndexAsc(Long levelId);
    long countByLevelIdAndStatus(Long levelId, String status);
}
