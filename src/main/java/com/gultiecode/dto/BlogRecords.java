package com.gultiecode.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class BlogRecords {

    public record BlogRequest(
            @NotBlank String title,
            @NotBlank String content,
            String tags
    ) {}

    public record CommentRequest(
            @NotBlank String content
    ) {}

    public record CommentResponse(
            Long id,
            Long blogId,
            Long authorId,
            String authorName,
            String content,
            LocalDateTime createdAt
    ) {}

    public record BlogResponse(
            Long id,
            String title,
            String content,
            String snippet,
            String tags,
            int likesCount,
            boolean likedByCurrentUser,
            Long authorId,
            String authorName,
            String authorAvatar,
            LocalDateTime createdAt,
            List<CommentResponse> comments
    ) {}

    public record BlogPageResponse(
            List<BlogResponse> blogs,
            int currentPage,
            int totalPages,
            long totalElements
    ) {}
}
