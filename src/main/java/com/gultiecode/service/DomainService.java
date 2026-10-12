package com.gultiecode.service;

import com.gultiecode.dto.DomainDtos.*;
import com.gultiecode.entity.AdminActivityLog;
import com.gultiecode.entity.Domain;
import com.gultiecode.entity.Level;
import com.gultiecode.repository.AdminActivityLogRepository;
import com.gultiecode.repository.DomainRepository;
import com.gultiecode.repository.LevelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DomainService {

    private final DomainRepository domainRepository;
    private final LevelRepository levelRepository;
    private final AdminActivityLogRepository auditLogRepository;

    public DomainService(DomainRepository domainRepository,
                         LevelRepository levelRepository,
                         AdminActivityLogRepository auditLogRepository) {
        this.domainRepository = domainRepository;
        this.levelRepository = levelRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public List<DomainResponse> getAllActiveDomains() {
        return domainRepository.findByStatus("ACTIVE").stream()
                .map(this::mapToDomainResponse)
                .collect(Collectors.toList());
    }

    public List<DomainResponse> getAllDomainsForAdmin() {
        return domainRepository.findAll().stream()
                .map(this::mapToDomainResponse)
                .collect(Collectors.toList());
    }

    public DomainResponse getDomainById(Long id) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Domain not found with id: " + id));
        return mapToDomainResponse(domain);
    }

    @Transactional
    public DomainResponse createDomain(DomainRequest request, String adminUsername) {
        Domain domain = new Domain();
        domain.setName(request.getName());
        domain.setDescription(request.getDescription());
        domain.setMinimumDurationWeeks(request.getMinimumDurationWeeks() > 0 ? request.getMinimumDurationWeeks() : 4);
        domain.setIconName(request.getIconName() != null ? request.getIconName() : "code");
        domain.setStatus("ACTIVE");
        domain.setCreatedBy(adminUsername);

        Domain savedDomain = domainRepository.save(domain);

        logAdminAction(adminUsername, "CREATE_DOMAIN", "Domain", savedDomain.getId().toString(), null, savedDomain.getName());

        return mapToDomainResponse(savedDomain);
    }

    @Transactional
    public DomainResponse updateDomain(Long id, DomainRequest request, String adminUsername) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Domain not found with id: " + id));

        String prevName = domain.getName();

        domain.setName(request.getName());
        domain.setDescription(request.getDescription());
        domain.setMinimumDurationWeeks(request.getMinimumDurationWeeks());
        if (request.getIconName() != null) domain.setIconName(request.getIconName());
        domain.setUpdatedBy(adminUsername);

        Domain updatedDomain = domainRepository.save(domain);

        logAdminAction(adminUsername, "UPDATE_DOMAIN", "Domain", id.toString(), prevName, updatedDomain.getName());

        return mapToDomainResponse(updatedDomain);
    }

    @Transactional
    public DomainResponse softDeleteDomain(Long id, String adminUsername) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Domain not found with id: " + id));

        domain.setStatus("DELETED");
        domain.setUpdatedBy(adminUsername);
        Domain saved = domainRepository.save(domain);

        logAdminAction(adminUsername, "DELETE_DOMAIN", "Domain", id.toString(), "ACTIVE", "DELETED");

        return mapToDomainResponse(saved);
    }

    @Transactional
    public DomainResponse restoreDomain(Long id, String adminUsername) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Domain not found with id: " + id));

        domain.setStatus("ACTIVE");
        domain.setUpdatedBy(adminUsername);
        Domain saved = domainRepository.save(domain);

        logAdminAction(adminUsername, "RESTORE_DOMAIN", "Domain", id.toString(), "DELETED", "ACTIVE");

        return mapToDomainResponse(saved);
    }

    @Transactional
    public LevelDto createLevel(Long domainId, LevelDto levelDto, String adminUsername) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new RuntimeException("Domain not found with id: " + domainId));

        Level level = new Level();
        level.setDomain(domain);
        level.setLevelNumber(levelDto.getLevelNumber());
        level.setName(levelDto.getName());
        level.setDescription(levelDto.getDescription());
        level.setTopicsCsv(levelDto.getTopicsCsv());
        level.setInstructions(levelDto.getInstructions());
        level.setExternalLinksJson(levelDto.getExternalLinksJson());
        level.setStatus("ACTIVE");
        level.setCreatedBy(adminUsername);

        Level savedLevel = levelRepository.save(level);

        logAdminAction(adminUsername, "CREATE_LEVEL", "Level", savedLevel.getId().toString(), null, savedLevel.getName());

        return mapToLevelDto(savedLevel);
    }

    @Transactional
    public void deleteLevel(Long levelId, String adminUsername) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found with id: " + levelId));

        levelRepository.delete(level);

        logAdminAction(adminUsername, "DELETE_LEVEL", "Level", levelId.toString(), level.getName(), null);
    }

    private void logAdminAction(String adminUsername, String action, String entityType, String entityId, String prevVal, String newVal) {
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
                "Domain Operation",
                LocalDateTime.now()
        );
        auditLogRepository.save(log);
    }

    public DomainResponse mapToDomainResponse(Domain domain) {
        List<LevelDto> levels = levelRepository.findByDomainIdOrderByLevelNumberAsc(domain.getId()).stream()
                .map(this::mapToLevelDto)
                .collect(Collectors.toList());

        return DomainResponse.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .minimumDurationWeeks(domain.getMinimumDurationWeeks())
                .iconName(domain.getIconName())
                .status(domain.getStatus() != null ? domain.getStatus() : "ACTIVE")
                .levels(levels)
                .build();
    }

    public LevelDto mapToLevelDto(Level level) {
        return LevelDto.builder()
                .id(level.getId())
                .domainId(level.getDomain().getId())
                .levelNumber(level.getLevelNumber())
                .name(level.getName())
                .description(level.getDescription())
                .topicsCsv(level.getTopicsCsv())
                .instructions(level.getInstructions())
                .externalLinksJson(level.getExternalLinksJson())
                .build();
    }
}
