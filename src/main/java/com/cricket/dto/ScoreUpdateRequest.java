package com.cricket.dto;

public class ScoreUpdateRequest {
    private int runs;
    private boolean isWicket;
    private String wicketType; // "bowled", "caught", "lbw", "run_out", "stumped"
    private boolean isExtra;
    private String extraType; // "wide", "noball", "bye", "legbye"
    private String customCommentary;

    public ScoreUpdateRequest() {}

    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }

    public boolean isWicket() { return isWicket; }
    public void setWicket(boolean wicket) { isWicket = wicket; }

    public String getWicketType() { return wicketType; }
    public void setWicketType(String wicketType) { this.wicketType = wicketType; }

    public boolean isExtra() { return isExtra; }
    public void setExtra(boolean extra) { isExtra = extra; }

    public String getExtraType() { return extraType; }
    public void setExtraType(String extraType) { this.extraType = extraType; }

    public String getCustomCommentary() { return customCommentary; }
    public void setCustomCommentary(String customCommentary) { this.customCommentary = customCommentary; }
}
