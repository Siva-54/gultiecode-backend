package com.gultiecode.repository;

import com.gultiecode.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findByStatus(String status);
    Optional<Batch> findByName(String name);

    @Query("SELECT b FROM Batch b JOIN b.members m WHERE m.id = :userId AND b.status = 'ACTIVE'")
    List<Batch> findBatchesByUserId(@Param("userId") Long userId);
}
