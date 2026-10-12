package com.gultiecode.service;

import com.gultiecode.dto.NotificationRecords.*;
import com.gultiecode.entity.AdminActivityLog;
import com.gultiecode.entity.Batch;
import com.gultiecode.entity.User;
import com.gultiecode.repository.AdminActivityLogRepository;
import com.gultiecode.repository.BatchRepository;
import com.gultiecode.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BatchService {

    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final AdminActivityLogRepository auditLogRepository;

    public BatchService(BatchRepository batchRepository,
                        UserRepository userRepository,
                        AdminActivityLogRepository auditLogRepository) {
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<BatchResponse> getAllBatches() {
        return batchRepository.findAll().stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    public List<BatchResponse> getActiveBatches() {
        return batchRepository.findByStatus("ACTIVE").stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    public BatchResponse getBatchById(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));
        return mapToBatchResponse(batch);
    }

    @Transactional
    public BatchResponse createBatch(CreateBatchRequest request, String adminUsername) {
        if (batchRepository.findByName(request.name()).isPresent()) {
            throw new RuntimeException("Batch with name '" + request.name() + "' already exists");
        }

        Batch batch = new Batch();
        batch.setName(request.name());
        batch.setDescription(request.description());
        batch.setStatus("ACTIVE");
        batch.setCreatedBy(adminUsername);

        if (request.userIds() != null && !request.userIds().isEmpty()) {
            List<User> members = userRepository.findAllById(request.userIds());
            batch.setMembers(new HashSet<>(members));
        }

        Batch saved = batchRepository.save(batch);
        logAdminAction(adminUsername, "CREATE_BATCH", "Batch", saved.getId().toString(), null, saved.getName(), "Created batch with " + saved.getMembers().size() + " members");
        return mapToBatchResponse(saved);
    }

    @Transactional
    public BatchResponse updateBatch(Long id, CreateBatchRequest request, String adminUsername) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));

        String prevName = batch.getName();
        batch.setName(request.name());
        batch.setDescription(request.description());

        if (request.userIds() != null) {
            List<User> members = userRepository.findAllById(request.userIds());
            batch.setMembers(new HashSet<>(members));
        }
        batch.setUpdatedBy(adminUsername);

        Batch updated = batchRepository.save(batch);
        logAdminAction(adminUsername, "UPDATE_BATCH", "Batch", id.toString(), prevName, updated.getName(), "Updated batch");
        return mapToBatchResponse(updated);
    }

    @Transactional
    public BatchResponse softDeleteBatch(Long id, String adminUsername) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));

        batch.setStatus("DELETED");
        batch.setUpdatedBy(adminUsername);
        Batch saved = batchRepository.save(batch);

        logAdminAction(adminUsername, "DELETE_BATCH", "Batch", id.toString(), "ACTIVE", "DELETED", "Soft deleted batch");
        return mapToBatchResponse(saved);
    }

    @Transactional
    public BatchResponse restoreBatch(Long id, String adminUsername) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));

        batch.setStatus("ACTIVE");
        batch.setUpdatedBy(adminUsername);
        Batch saved = batchRepository.save(batch);

        logAdminAction(adminUsername, "RESTORE_BATCH", "Batch", id.toString(), "DELETED", "ACTIVE", "Restored batch");
        return mapToBatchResponse(saved);
    }

    public BatchResponse mapToBatchResponse(Batch batch) {
        List<BatchMemberDto> members = batch.getMembers().stream()
                .map(u -> new BatchMemberDto(
                        u.getId(),
                        u.getUsername(),
                        u.getFullName() != null ? u.getFullName() : u.getUsername(),
                        u.getEmail(),
                        u.getAvatarUrl()
                ))
                .collect(Collectors.toList());

        return new BatchResponse(
                batch.getId(),
                batch.getName(),
                batch.getDescription(),
                batch.getMembers().size(),
                members,
                batch.getStatus() != null ? batch.getStatus() : "ACTIVE"
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
