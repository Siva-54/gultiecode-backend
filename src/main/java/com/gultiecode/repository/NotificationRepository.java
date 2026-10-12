package com.gultiecode.repository;

import com.gultiecode.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatusOrderByCreatedAtDesc(String status);
    List<Notification> findAllByOrderByCreatedAtDesc();

    @Query("SELECT n FROM Notification n WHERE n.status = 'ACTIVE' AND (" +
            "n.targetType = 'ALL' OR " +
            "(n.targetType = 'BATCH' AND n.targetBatch.id IN :batchIds) OR " +
            "(n.targetType = 'SPECIFIC_USERS' AND n.targetUserIdsJson LIKE %:userIdStr%)" +
            ") ORDER BY n.createdAt DESC")
    List<Notification> findActiveNotificationsForUser(
            @Param("batchIds") List<Long> batchIds,
            @Param("userIdStr") String userIdStr);

    @Query("SELECT n FROM Notification n WHERE n.status = 'ACTIVE' AND n.targetType = 'ALL' ORDER BY n.createdAt DESC")
    List<Notification> findActiveBroadcastNotifications();
}
