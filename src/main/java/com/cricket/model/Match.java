package com.cricket.model;

import java.util.ArrayList;
import java.util.List;

public class Match {
    private String id;
    private String title;
    private String series;
    private String matchType; // T20, ODI, TEST
    private String venue;
    private String status; // LIVE, COMPLETED, UPCOMING
    private String tossDetails;
    private String resultMessage;
    private int maxOvers;
    private Team team1;
    private Team team2;
    private List<Innings> innings = new ArrayList<>();
    private int currentInningsIndex;
    private Integer target;
    private boolean autoSimulating;
    private int simulationIntervalMs = 2500;

    public Match() {}

    public Innings getCurrentInnings() {
        if (innings == null || innings.isEmpty()) return null;
        if (currentInningsIndex < 0 || currentInningsIndex >= innings.size()) {
            return innings.get(innings.size() - 1);
        }
        return innings.get(currentInningsIndex);
    }

    public Double getRequiredRunRate() {
        if (target == null) return null;
        Innings curr = getCurrentInnings();
        if (curr == null) return null;

        int runsNeeded = target - curr.getTotalRuns();
        if (runsNeeded <= 0) return 0.0;

        int totalLegalBallsAllowed = maxOvers * 6;
        int ballsRemaining = totalLegalBallsAllowed - curr.getLegalBalls();
        if (ballsRemaining <= 0) return 99.9;

        double oversRemaining = ballsRemaining / 6.0;
        return Math.round((runsNeeded / oversRemaining) * 100.0) / 100.0;
    }

    public Integer getRunsNeeded() {
        if (target == null) return null;
        Innings curr = getCurrentInnings();
        if (curr == null) return null;
        return Math.max(0, target - curr.getTotalRuns());
    }

    public Integer getBallsRemaining() {
        Innings curr = getCurrentInnings();
        if (curr == null) return null;
        return Math.max(0, (maxOvers * 6) - curr.getLegalBalls());
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTossDetails() { return tossDetails; }
    public void setTossDetails(String tossDetails) { this.tossDetails = tossDetails; }

    public String getResultMessage() { return resultMessage; }
    public void setResultMessage(String resultMessage) { this.resultMessage = resultMessage; }

    public int getMaxOvers() { return maxOvers; }
    public void setMaxOvers(int maxOvers) { this.maxOvers = maxOvers; }

    public Team getTeam1() { return team1; }
    public void setTeam1(Team team1) { this.team1 = team1; }

    public Team getTeam2() { return team2; }
    public void setTeam2(Team team2) { this.team2 = team2; }

    public List<Innings> getInnings() { return innings; }
    public void setInnings(List<Innings> innings) { this.innings = innings; }

    public int getCurrentInningsIndex() { return currentInningsIndex; }
    public void setCurrentInningsIndex(int currentInningsIndex) { this.currentInningsIndex = currentInningsIndex; }

    public Integer getTarget() { return target; }
    public void setTarget(Integer target) { this.target = target; }

    public boolean isAutoSimulating() { return autoSimulating; }
    public void setAutoSimulating(boolean autoSimulating) { this.autoSimulating = autoSimulating; }

    public int getSimulationIntervalMs() { return simulationIntervalMs; }
    public void setSimulationIntervalMs(int simulationIntervalMs) { this.simulationIntervalMs = simulationIntervalMs; }
}
