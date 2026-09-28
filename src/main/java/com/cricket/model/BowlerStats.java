package com.cricket.model;

public class BowlerStats {
    private String id;
    private String name;
    private int legalBalls;
    private int maidens;
    private int runsConceded;
    private int wickets;
    private double economy;
    private boolean isCurrent;

    public BowlerStats() {}

    public BowlerStats(String id, String name) {
        this.id = id;
        this.name = name;
        this.legalBalls = 0;
        this.maidens = 0;
        this.runsConceded = 0;
        this.wickets = 0;
        this.economy = 0.0;
        this.isCurrent = false;
    }

    public void addBall(int runs, boolean isWicket, boolean isExtraWideOrNoBall) {
        if (!isExtraWideOrNoBall) {
            this.legalBalls++;
        }
        this.runsConceded += runs;
        if (isWicket) {
            this.wickets++;
        }
        recalculateEconomy();
    }

    public void recalculateEconomy() {
        if (this.legalBalls > 0) {
            double totalOvers = (this.legalBalls / 6) + ((this.legalBalls % 6) / 6.0);
            this.economy = Math.round((this.runsConceded / totalOvers) * 10.0) / 10.0;
        } else {
            this.economy = 0.0;
        }
    }

    public String getOversDisplay() {
        return (legalBalls / 6) + "." + (legalBalls % 6);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLegalBalls() { return legalBalls; }
    public void setLegalBalls(int legalBalls) { this.legalBalls = legalBalls; recalculateEconomy(); }

    public int getMaidens() { return maidens; }
    public void setMaidens(int maidens) { this.maidens = maidens; }

    public int getRunsConceded() { return runsConceded; }
    public void setRunsConceded(int runsConceded) { this.runsConceded = runsConceded; recalculateEconomy(); }

    public int getWickets() { return wickets; }
    public void setWickets(int wickets) { this.wickets = wickets; }

    public double getEconomy() { return economy; }
    public void setEconomy(double economy) { this.economy = economy; }

    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }
}
