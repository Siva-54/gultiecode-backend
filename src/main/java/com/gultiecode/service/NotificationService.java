package com.gultiecode.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gultiecode.dto.NotificationRecords.*;
import com.gultiecode.entity.*;
import com.gultiecode.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationStatusRepository statusRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final AdminActivityLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationService(NotificationRepository notificationRepository,
                               UserNotificationStatusRepository statusRepository,
                               BatchRepository batchRepository,
                               UserRepository userRepository,
                               AdminActivityLogRepository auditLogRepository) {
        this.notificationRepository = notificationRepository;
        this.statusRepository = statusRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request, String adminUsername) {
        Notification notification = new Notification();
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setType(request.type() != null ? request.type().toUpperCase() : "ANNOUNCEMENT");
        notification.setTargetType(request.targetType() != null ? request.targetType().toUpperCase() : "ALL");
        notification.setEventDate(request.eventDate());
        notification.setSenderAdminUsername(adminUsername);
        notification.setStatus("ACTIVE");
        notification.setCreatedBy(adminUsername);

        if ("BATCH".equalsIgnoreCase(request.targetType()) && request.targetBatchId() != null) {
            Batch batch = batchRepository.findById(request.targetBatchId())
                    .orElseThrow(() -> new RuntimeException("Batch not found with id: " + request.targetBatchId()));
            notification.setTargetBatch(batch);
        } else if ("SPECIFIC_USERS".equalsIgnoreCase(request.targetType()) && request.targetUserIds() != null) {
            try {
                notification.setTargetUserIdsJson(objectMapper.writeValueAsString(request.targetUserIds()));
            } catch (Exception e) {
                notification.setTargetUserIdsJson("[]");
            }
        }

        Notification saved = notificationRepository.save(notification);
        logAdminAction(adminUsername, "SEND_NOTIFICATION", "Notification", saved.getId().toString(), null, saved.getTitle(),
                "Broadcasted " + saved.getType() + " to " + saved.getTargetType());

        return mapToNotificationResponse(saved, false);
    }

    public List<NotificationResponse> getNotificationsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Batch> userBatches = batchRepository.findBatchesByUserId(userId);
        List<Long> batchIds = userBatches.stream().map(Batch::getId).collect(Collectors.toList());

        List<Notification> allActive = notificationRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");
        List<NotificationResponse> userNotifications = new ArrayList<>();

        Map<Long, UserNotificationStatus> statusMap = new HashMap<>();
        statusRepository.findByUserId(userId).forEach(s -> statusMap.put(s.getNotification().getId(), s));

        for (Notification n : allActive) {
            boolean isTargeted = false;

            if ("ALL".equalsIgnoreCase(n.getTargetType())) {
                isTargeted = true;
            } else if ("BATCH".equalsIgnoreCase(n.getTargetType()) && n.getTargetBatch() != null) {
                if (batchIds.contains(n.getTargetBatch().getId())) {
                    isTargeted = true;
                }
            } else if ("SPECIFIC_USERS".equalsIgnoreCase(n.getTargetType()) && n.getTargetUserIdsJson() != null) {
                try {
                    List<Long> targetIds = objectMapper.readValue(n.getTargetUserIdsJson(), new TypeReference<List<Long>>() {});
                    if (targetIds.contains(userId)) {
                        isTargeted = true;
                    }
                } catch (Exception ignored) {}
            }

            if (isTargeted) {
                UserNotificationStatus st = statusMap.get(n.getId());
                boolean isRead = (st != null && st.isRead());
                userNotifications.add(mapToNotificationResponse(n, isRead));
            }
        }

        return userNotifications;
    }

    public UnreadCountResponse getUnreadCountForUser(Long userId) {
        List<NotificationResponse> notifications = getNotificationsForUser(userId);
        long unread = notifications.stream().filter(n -> !n.isRead()).count();
        return new UnreadCountResponse(unread);
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        UserNotificationStatus status = statusRepository.findByUserIdAndNotificationId(userId, notificationId)
                .orElseGet(() -> new UserNotificationStatus(null, user, notification, false, null));

        status.setRead(true);
        status.setReadAt(LocalDateTime.now());
        statusRepository.save(status);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<NotificationResponse> userNotifications = getNotificationsForUser(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        for (NotificationResponse nr : userNotifications) {
            if (!nr.isRead()) {
                Notification n = notificationRepository.findById(nr.id()).orElse(null);
                if (n != null) {
                    UserNotificationStatus status = statusRepository.findByUserIdAndNotificationId(userId, n.getId())
                            .orElseGet(() -> new UserNotificationStatus(null, user, n, false, null));
                    status.setRead(true);
                    status.setReadAt(LocalDateTime.now());
                    statusRepository.save(status);
                }
            }
        }
    }

    public List<NotificationResponse> getAllNotificationsForAdmin() {
        return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(n -> mapToNotificationResponse(n, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public void softDeleteNotification(Long id, String adminUsername) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        notification.setStatus("DELETED");
        notification.setUpdatedBy(adminUsername);
        notificationRepository.save(notification);

        logAdminAction(adminUsername, "DELETE_NOTIFICATION", "Notification", id.toString(), "ACTIVE", "DELETED", "Soft deleted notification");
    }

    private NotificationResponse mapToNotificationResponse(Notification n, boolean isRead) {
        List<Long> targetIds = new ArrayList<>();
        if (n.getTargetUserIdsJson() != null) {
            try {
                targetIds = objectMapper.readValue(n.getTargetUserIdsJson(), new TypeReference<List<Long>>() {});
            } catch (Exception ignored) {}
        }

        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getTargetType(),
                n.getTargetBatch() != null ? n.getTargetBatch().getId() : null,
                n.getTargetBatch() != null ? n.getTargetBatch().getName() : null,
                targetIds,
                n.getEventDate(),
                n.getSenderAdminUsername(),
                isRead,
                n.getCreatedAt()
        );
    }

    private void logAdminAction(String adminUsername, String action, String entityType, String entityId, String prevVal, String newVal, String reason) {
        AdminActivityLog log = new AdminActivityLog(
                null,
                adminUsername,
                null,
                action,
                entityType,
                entityId,
                null,
                prevVal,
                newVal,
                reason,
                LocalDateTime.now()
        );
        auditLogRepository.save(log);
    }
}
