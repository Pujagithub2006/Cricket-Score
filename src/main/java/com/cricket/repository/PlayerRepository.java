package com.cricket.repository;

import com.cricket.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    List<PlayerEntity> findByTeamId(Long teamId);
    List<PlayerEntity> findByRoleIgnoreCase(String role);
}
