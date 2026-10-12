package com.gultiecode.repository;

import com.gultiecode.entity.UserNotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationStatusRepository extends JpaRepository<UserNotificationStatus, Long> {
    Optional<UserNotificationStatus> findByUserIdAndNotificationId(Long userId, Long notificationId);
    List<UserNotificationStatus> findByUserId(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
}
