package com.gultiecode.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class BlogDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlogRequest {
        @NotBlank
        private String title;

        @NotBlank
        private String content;

        private String tags;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BlogResponse {
        private Long id;
        private String title;
        private String content;
        private String snippet; // First 200-300 chars (~40-50 words)
        private String tags;
        private int likesCount;
        private boolean likedByCurrentUser;
        private Long authorId;
        private String authorName;
        private String authorAvatar;
        private LocalDateTime createdAt;
        private List<CommentResponse> comments;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BlogPageResponse {
        private List<BlogResponse> blogs;
        private int currentPage;
        private int totalPages;
        private long totalElements;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentRequest {
        @NotBlank
        private String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentResponse {
        private Long id;
        private Long blogId;
        private Long authorId;
        private String authorName;
        private String content;
        private LocalDateTime createdAt;
    }
}
