package com.gultiecode.repository;

import com.gultiecode.entity.QuizEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizEvaluationRepository extends JpaRepository<QuizEvaluation, Long> {
    List<QuizEvaluation> findByUserId(Long userId);
    List<QuizEvaluation> findByTaskId(Long taskId);
    List<QuizEvaluation> findByFlaggedTrue();
}
