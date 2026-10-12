package com.gultiecode.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

public class DomainDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainRequest {
        @NotBlank
        private String name;

        private String description;

        private int minimumDurationWeeks;

        private String iconName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DomainResponse {
        private Long id;
        private String name;
        private String description;
        private int minimumDurationWeeks;
        private String iconName;
        private String status; // ACTIVE, DELETED
        private List<LevelDto> levels;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LevelDto {
        private Long id;
        private Long domainId;
        private int levelNumber;
        private String name;
        private String description;
        private String topicsCsv;
        private String instructions;
        private String externalLinksJson;
    }
}
