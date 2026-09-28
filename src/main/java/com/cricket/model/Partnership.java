package com.cricket.model;

public class Partnership {
    private String batsman1Name;
    private String batsman2Name;
    private int runs;
    private int balls;

    public Partnership() {}

    public Partnership(String batsman1Name, String batsman2Name) {
        this.batsman1Name = batsman1Name;
        this.batsman2Name = batsman2Name;
        this.runs = 0;
        this.balls = 0;
    }

    public void addBall(int run, boolean isLegal) {
        this.runs += run;
        if (isLegal) {
            this.balls++;
        }
    }

    public String getBatsman1Name() { return batsman1Name; }
    public void setBatsman1Name(String batsman1Name) { this.batsman1Name = batsman1Name; }

    public String getBatsman2Name() { return batsman2Name; }
    public void setBatsman2Name(String batsman2Name) { this.batsman2Name = batsman2Name; }

    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }

    public int getBalls() { return balls; }
    public void setBalls(int balls) { this.balls = balls; }
}
