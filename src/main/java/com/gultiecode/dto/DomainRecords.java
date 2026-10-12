package com.gultiecode.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class DomainRecords {

    public record DomainRequest(
            @NotBlank String name,
            String description,
            int minimumDurationWeeks,
            String iconName
    ) {}

    public record LevelDto(
            Long id,
            Long domainId,
            int levelNumber,
            String name,
            String description,
            String topicsCsv,
            String instructions,
            String externalLinksJson,
            List<AssessmentRecords.QuestionDto> questions
    ) {}

    public record DomainResponse(
            Long id,
            String name,
            String description,
            int minimumDurationWeeks,
            String iconName,
            String status,
            List<LevelDto> levels
    ) {}
}
