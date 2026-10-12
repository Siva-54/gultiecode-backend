package com.gultiecode.repository;

import com.gultiecode.entity.UserCoursePoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCoursePointsRepository extends JpaRepository<UserCoursePoints, Long> {
    Optional<UserCoursePoints> findByUserIdAndDomainId(Long userId, Long domainId);
    List<UserCoursePoints> findByDomainIdOrderByPointsDescCompletedLevelsCountDesc(Long domainId);
    List<UserCoursePoints> findByUserId(Long userId);
}
