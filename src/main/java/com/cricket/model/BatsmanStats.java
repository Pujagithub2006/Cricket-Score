package com.cricket.model;

public class BatsmanStats {
    private String id;
    private String name;
    private int runs;
    private int balls;
    private int fours;
    private int sixes;
    private double strikeRate;
    private boolean isOut;
    private String dismissal;
    private boolean isBatting;
    private boolean isOnStrike;

    public BatsmanStats() {
        this.dismissal = "Not out";
    }

    public BatsmanStats(String id, String name) {
        this.id = id;
        this.name = name;
        this.runs = 0;
        this.balls = 0;
        this.fours = 0;
        this.sixes = 0;
        this.strikeRate = 0.0;
        this.isOut = false;
        this.dismissal = "Not out";
        this.isBatting = false;
        this.isOnStrike = false;
    }

    public void recalculateStrikeRate() {
        if (this.balls > 0) {
            this.strikeRate = Math.round(((double) this.runs / this.balls * 100.0) * 10.0) / 10.0;
        } else {
            this.strikeRate = 0.0;
        }
    }

    public void addRuns(int scoredRuns) {
        this.runs += scoredRuns;
        this.balls += 1;
        if (scoredRuns == 4) this.fours += 1;
        if (scoredRuns == 6) this.sixes += 1;
        recalculateStrikeRate();
    }

    public void addDotBall() {
        this.balls += 1;
        recalculateStrikeRate();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; recalculateStrikeRate(); }

    public int getBalls() { return balls; }
    public void setBalls(int balls) { this.balls = balls; recalculateStrikeRate(); }

    public int getFours() { return fours; }
    public void setFours(int fours) { this.fours = fours; }

    public int getSixes() { return sixes; }
    public void setSixes(int sixes) { this.sixes = sixes; }

    public double getStrikeRate() { return strikeRate; }
    public void setStrikeRate(double strikeRate) { this.strikeRate = strikeRate; }

    public boolean isOut() { return isOut; }
    public void setOut(boolean out) { isOut = out; }

    public String getDismissal() { return dismissal; }
    public void setDismissal(String dismissal) { this.dismissal = dismissal; }

    public boolean isBatting() { return isBatting; }
    public void setBatting(boolean batting) { isBatting = batting; }

    public boolean isOnStrike() { return isOnStrike; }
    public void setOnStrike(boolean onStrike) { isOnStrike = onStrike; }
}
