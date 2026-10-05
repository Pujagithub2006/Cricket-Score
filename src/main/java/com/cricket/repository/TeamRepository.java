package com.cricket.repository;

import com.cricket.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Long> {
    Optional<TeamEntity> findByNameIgnoreCase(String name);
    Optional<TeamEntity> findByShortNameIgnoreCase(String shortName);
}
