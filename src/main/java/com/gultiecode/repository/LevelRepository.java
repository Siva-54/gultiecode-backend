package com.gultiecode.repository;

import com.gultiecode.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    List<Level> findByDomainIdAndStatusOrderByLevelNumberAsc(Long domainId, String status);
    List<Level> findByDomainIdOrderByLevelNumberAsc(Long domainId);
    Optional<Level> findByDomainIdAndLevelNumberAndStatus(Long domainId, int levelNumber, String status);
    Optional<Level> findFirstByDomainIdAndStatusOrderByLevelNumberAsc(Long domainId, String status);
    long countByDomainIdAndStatus(Long domainId, String status);
}
