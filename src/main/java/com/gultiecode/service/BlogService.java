package com.gultiecode.service;

import com.gultiecode.dto.BlogDtos.*;
import com.gultiecode.entity.*;
import com.gultiecode.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogCommentRepository commentRepository;
    private final BlogLikeRepository likeRepository;
    private final UserRepository userRepository;

    public BlogPageResponse searchBlogs(String searchQuery, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        String searchParam = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : "";

        Page<Blog> blogPage = blogRepository.searchBlogs(searchParam, pageable);

        List<BlogResponse> blogResponses = blogPage.getContent().stream()
                .map(blog -> mapToBlogResponse(blog, currentUserId))
                .collect(Collectors.toList());

        return BlogPageResponse.builder()
                .blogs(blogResponses)
                .currentPage(blogPage.getNumber())
                .totalPages(blogPage.getTotalPages())
                .totalElements(blogPage.getTotalElements())
                .build();
    }

    public BlogResponse getBlogById(Long id, Long currentUserId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));
        return mapToBlogResponse(blog, currentUserId);
    }

    @Transactional
    public BlogResponse createBlog(BlogRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Blog blog = Blog.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .tags(request.getTags() != null ? request.getTags() : "Tech,Learning")
                .author(author)
                .likesCount(0)
                .build();
        blog.setCreatedBy(author.getUsername());

        Blog saved = blogRepository.save(blog);
        return mapToBlogResponse(saved, authorId);
    }

    @Transactional
    public BlogResponse updateBlog(Long id, BlogRequest request, Long authorId, boolean isAdmin) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!isAdmin && !blog.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("Not authorized to edit this blog");
        }

        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        if (request.getTags() != null)
            blog.setTags(request.getTags());
        blog.setUpdatedBy("user-" + authorId);

        Blog updated = blogRepository.save(blog);
        return mapToBlogResponse(updated, authorId);
    }

    @Transactional
    public void deleteBlog(Long id, Long authorId, boolean isAdmin) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!isAdmin && !blog.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("Not authorized to delete this blog");
        }

        blogRepository.delete(blog);
    }

    @Transactional
    public BlogResponse toggleLike(Long blogId, Long userId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<BlogLike> existingLike = likeRepository.findByBlogIdAndUserId(blogId, userId);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            blog.setLikesCount(Math.max(0, blog.getLikesCount() - 1));
        } else {
            BlogLike like = BlogLike.builder().blog(blog).user(user).build();
            likeRepository.save(like);
            blog.setLikesCount(blog.getLikesCount() + 1);
        }

        Blog saved = blogRepository.save(blog);
        return mapToBlogResponse(saved, userId);
    }

    @Transactional
    public CommentResponse addComment(Long blogId, CommentRequest request, Long authorId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BlogComment comment = BlogComment.builder()
                .blog(blog)
                .author(author)
                .content(request.getContent())
                .build();
        comment.setCreatedBy(author.getUsername());

        BlogComment saved = commentRepository.save(comment);
        return mapToCommentResponse(saved);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        BlogComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!isAdmin && !comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    public BlogResponse mapToBlogResponse(Blog blog, Long currentUserId) {
        List<CommentResponse> comments = commentRepository.findByBlogIdOrderByCreatedAtAsc(blog.getId()).stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());

        boolean liked = currentUserId != null && likeRepository.existsByBlogIdAndUserId(blog.getId(), currentUserId);

        // Generate ~40-50 words (200-300 chars) snippet
        String content = blog.getContent() != null ? blog.getContent() : "";
        String snippet = content.length() > 250 ? content.substring(0, 250) + "..." : content;

        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .content(content)
                .snippet(snippet)
                .tags(blog.getTags())
                .likesCount(blog.getLikesCount())
                .likedByCurrentUser(liked)
                .authorId(blog.getAuthor().getId())
                .authorName(blog.getAuthor().getUsername())
                .authorAvatar(blog.getAuthor().getAvatarUrl())
                .createdAt(blog.getCreatedAt())
                .comments(comments)
                .build();
    }

    public CommentResponse mapToCommentResponse(BlogComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .blogId(comment.getBlog().getId())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
