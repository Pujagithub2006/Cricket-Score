package com.cricket.model;

public class BallEvent {
    private String id;
    private int inningsNumber;
    private int overNumber;
    private int ballInOver;
    private String batsmanName;
    private String bowlerName;
    private int runs;
    private boolean isWicket;
    private String wicketType;
    private boolean isExtra;
    private String extraType;
    private String display;
    private String commentary;
    private String timestamp;

    public BallEvent() {}

    public BallEvent(String id, int inningsNumber, int overNumber, int ballInOver,
                     String batsmanName, String bowlerName, int runs,
                     boolean isWicket, String wicketType,
                     boolean isExtra, String extraType,
                     String display, String commentary, String timestamp) {
        this.id = id;
        this.inningsNumber = inningsNumber;
        this.overNumber = overNumber;
        this.ballInOver = ballInOver;
        this.batsmanName = batsmanName;
        this.bowlerName = bowlerName;
        this.runs = runs;
        this.isWicket = isWicket;
        this.wicketType = wicketType;
        this.isExtra = isExtra;
        this.extraType = extraType;
        this.display = display;
        this.commentary = commentary;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getInningsNumber() { return inningsNumber; }
    public void setInningsNumber(int inningsNumber) { this.inningsNumber = inningsNumber; }

    public int getOverNumber() { return overNumber; }
    public void setOverNumber(int overNumber) { this.overNumber = overNumber; }

    public int getBallInOver() { return ballInOver; }
    public void setBallInOver(int ballInOver) { this.ballInOver = ballInOver; }

    public String getBatsmanName() { return batsmanName; }
    public void setBatsmanName(String batsmanName) { this.batsmanName = batsmanName; }

    public String getBowlerName() { return bowlerName; }
    public void setBowlerName(String bowlerName) { this.bowlerName = bowlerName; }

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

    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }

    public String getCommentary() { return commentary; }
    public void setCommentary(String commentary) { this.commentary = commentary; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
