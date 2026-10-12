package com.gultiecode.repository;

import com.gultiecode.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByLevelIdAndStatusOrderByOrderIndexAsc(Long levelId, String status);
    List<Question> findByLevelIdOrderByOrderIndexAsc(Long levelId);
    long countByLevelIdAndStatus(Long levelId, String status);
}
