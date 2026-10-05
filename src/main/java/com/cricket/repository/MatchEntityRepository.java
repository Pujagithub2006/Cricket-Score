package com.cricket.repository;

import com.cricket.entity.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchEntityRepository extends JpaRepository<MatchEntity, String> {
    List<MatchEntity> findByStatusIgnoreCase(String status);
}
