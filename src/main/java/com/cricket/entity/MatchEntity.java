package com.cricket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    private String series;
    private String matchType;
    private String venue;
    private String status; // LIVE, UPCOMING, COMPLETED, PAUSED
    private String tossDetails;

    private int maxOvers = 20;
    private Integer target;
    private String resultMessage;
    private int simulationIntervalMs = 2500;
    private boolean autoSimulating = false;
    private int currentInningsIndex = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team1_id")
    private TeamEntity team1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team2_id")
    private TeamEntity team2;

    @Column(columnDefinition = "TEXT")
    private String matchDataJson; // stores full snapshot for instant retrieval and rehydration

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public MatchEntity() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTossDetails() {
        return tossDetails;
    }

    public void setTossDetails(String tossDetails) {
        this.tossDetails = tossDetails;
    }

    public int getMaxOvers() {
        return maxOvers;
    }

    public void setMaxOvers(int maxOvers) {
        this.maxOvers = maxOvers;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public int getSimulationIntervalMs() {
        return simulationIntervalMs;
    }

    public void setSimulationIntervalMs(int simulationIntervalMs) {
        this.simulationIntervalMs = simulationIntervalMs;
    }

    public boolean isAutoSimulating() {
        return autoSimulating;
    }

    public void setAutoSimulating(boolean autoSimulating) {
        this.autoSimulating = autoSimulating;
    }

    public int getCurrentInningsIndex() {
        return currentInningsIndex;
    }

    public void setCurrentInningsIndex(int currentInningsIndex) {
        this.currentInningsIndex = currentInningsIndex;
    }

    public TeamEntity getTeam1() {
        return team1;
    }

    public void setTeam1(TeamEntity team1) {
        this.team1 = team1;
    }

    public TeamEntity getTeam2() {
        return team2;
    }

    public void setTeam2(TeamEntity team2) {
        this.team2 = team2;
    }

    public String getMatchDataJson() {
        return matchDataJson;
    }

    public void setMatchDataJson(String matchDataJson) {
        this.matchDataJson = matchDataJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
