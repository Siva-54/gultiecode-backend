package com.gultiecode.repository;

import com.gultiecode.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findTop10ByOrderByCreatedAtDesc();
    List<Blog> findByAuthorId(Long authorId);
    long countByAuthorId(Long authorId);

    @Query("SELECT b FROM Blog b WHERE (:search IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.author.username) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY b.createdAt DESC")
    Page<Blog> searchBlogs(@Param("search") String search, Pageable pageable);
}
