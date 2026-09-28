package com.cricket.model;

import java.util.ArrayList;
import java.util.List;

public class Innings {
    private int inningsNumber;
    private String battingTeam;
    private String bowlingTeam;
    private int totalRuns;
    private int wickets;
    private int legalBalls;
    private Extras extras = new Extras();
    private List<BatsmanStats> batsmen = new ArrayList<>();
    private List<BowlerStats> bowlers = new ArrayList<>();
    private List<BallEvent> recentBalls = new ArrayList<>();
    private List<BallEvent> allBallEvents = new ArrayList<>();
    private List<Partnership> partnerships = new ArrayList<>();
    private List<String> fallOfWickets = new ArrayList<>();
    private String currentStrikerId;
    private String currentNonStrikerId;
    private String currentBowlerId;
    private boolean isCompleted;

    public Innings() {}

    public Innings(int inningsNumber, String battingTeam, String bowlingTeam) {
        this.inningsNumber = inningsNumber;
        this.battingTeam = battingTeam;
        this.bowlingTeam = bowlingTeam;
        this.totalRuns = 0;
        this.wickets = 0;
        this.legalBalls = 0;
        this.isCompleted = false;
    }

    public String getOversDisplay() {
        return (legalBalls / 6) + "." + (legalBalls % 6);
    }

    public double getRunRate() {
        if (legalBalls == 0) return 0.0;
        double oversDecimal = (legalBalls / 6) + ((legalBalls % 6) / 6.0);
        return Math.round((totalRuns / oversDecimal) * 100.0) / 100.0;
    }

    public BatsmanStats getStriker() {
        return batsmen.stream()
                .filter(b -> b.getId().equals(currentStrikerId))
                .findFirst()
                .orElse(null);
    }

    public BatsmanStats getNonStriker() {
        return batsmen.stream()
                .filter(b -> b.getId().equals(currentNonStrikerId))
                .findFirst()
                .orElse(null);
    }

    public BowlerStats getCurrentBowler() {
        return bowlers.stream()
                .filter(b -> b.getId().equals(currentBowlerId))
                .findFirst()
                .orElse(null);
    }

    public Partnership getCurrentPartnership() {
        if (partnerships.isEmpty()) return null;
        return partnerships.get(partnerships.size() - 1);
    }

    // Getters and Setters
    public int getInningsNumber() { return inningsNumber; }
    public void setInningsNumber(int inningsNumber) { this.inningsNumber = inningsNumber; }

    public String getBattingTeam() { return battingTeam; }
    public void setBattingTeam(String battingTeam) { this.battingTeam = battingTeam; }

    public String getBowlingTeam() { return bowlingTeam; }
    public void setBowlingTeam(String bowlingTeam) { this.bowlingTeam = bowlingTeam; }

    public int getTotalRuns() { return totalRuns; }
    public void setTotalRuns(int totalRuns) { this.totalRuns = totalRuns; }

    public int getWickets() { return wickets; }
    public void setWickets(int wickets) { this.wickets = wickets; }

    public int getLegalBalls() { return legalBalls; }
    public void setLegalBalls(int legalBalls) { this.legalBalls = legalBalls; }

    public Extras getExtras() { return extras; }
    public void setExtras(Extras extras) { this.extras = extras; }

    public List<BatsmanStats> getBatsmen() { return batsmen; }
    public void setBatsmen(List<BatsmanStats> batsmen) { this.batsmen = batsmen; }

    public List<BowlerStats> getBowlers() { return bowlers; }
    public void setBowlers(List<BowlerStats> bowlers) { this.bowlers = bowlers; }

    public List<BallEvent> getRecentBalls() { return recentBalls; }
    public void setRecentBalls(List<BallEvent> recentBalls) { this.recentBalls = recentBalls; }

    public List<BallEvent> getAllBallEvents() { return allBallEvents; }
    public void setAllBallEvents(List<BallEvent> allBallEvents) { this.allBallEvents = allBallEvents; }

    public List<Partnership> getPartnerships() { return partnerships; }
    public void setPartnerships(List<Partnership> partnerships) { this.partnerships = partnerships; }

    public List<String> getFallOfWickets() { return fallOfWickets; }
    public void setFallOfWickets(List<String> fallOfWickets) { this.fallOfWickets = fallOfWickets; }

    public String getCurrentStrikerId() { return currentStrikerId; }
    public void setCurrentStrikerId(String currentStrikerId) { this.currentStrikerId = currentStrikerId; }

    public String getCurrentNonStrikerId() { return currentNonStrikerId; }
    public void setCurrentNonStrikerId(String currentNonStrikerId) { this.currentNonStrikerId = currentNonStrikerId; }

    public String getCurrentBowlerId() { return currentBowlerId; }
    public void setCurrentBowlerId(String currentBowlerId) { this.currentBowlerId = currentBowlerId; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
