package com.cricket.repository;

import com.cricket.entity.BallEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BallEventEntityRepository extends JpaRepository<BallEventEntity, Long> {
    List<BallEventEntity> findByMatchIdOrderByRecordedAtDesc(String matchId);
    List<BallEventEntity> findByMatchIdAndInningsNumberOrderByRecordedAtAsc(String matchId, int inningsNumber);
    void deleteByMatchId(String matchId);
}
