package com.cricket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ball_events")
public class BallEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String matchId;

    private int inningsNumber;
    private int overNumber;
    private int ballInOver;

    private String batsman;
    private String bowler;
    private int runs;
    private boolean isWicket;
    private String wicketType;
    private boolean isExtra;
    private String extraType;
    private String display;

    @Column(length = 500)
    private String commentary;

    private String timeDisplay;
    private LocalDateTime recordedAt = LocalDateTime.now();

    public BallEventEntity() {}

    public BallEventEntity(String matchId, int inningsNumber, int overNumber, int ballInOver,
                           String batsman, String bowler, int runs, boolean isWicket,
                           String wicketType, boolean isExtra, String extraType,
                           String display, String commentary, String timeDisplay) {
        this.matchId = matchId;
        this.inningsNumber = inningsNumber;
        this.overNumber = overNumber;
        this.ballInOver = ballInOver;
        this.batsman = batsman;
        this.bowler = bowler;
        this.runs = runs;
        this.isWicket = isWicket;
        this.wicketType = wicketType;
        this.isExtra = isExtra;
        this.extraType = extraType;
        this.display = display;
        this.commentary = commentary;
        this.timeDisplay = timeDisplay;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public int getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(int inningsNumber) {
        this.inningsNumber = inningsNumber;
    }

    public int getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(int overNumber) {
        this.overNumber = overNumber;
    }

    public int getBallInOver() {
        return ballInOver;
    }

    public void setBallInOver(int ballInOver) {
        this.ballInOver = ballInOver;
    }

    public String getBatsman() {
        return batsman;
    }

    public void setBatsman(String batsman) {
        this.batsman = batsman;
    }

    public String getBowler() {
        return bowler;
    }

    public void setBowler(String bowler) {
        this.bowler = bowler;
    }

    public int getRuns() {
        return runs;
    }

    public void setRuns(int runs) {
        this.runs = runs;
    }

    public boolean isWicket() {
        return isWicket;
    }

    public void setWicket(boolean wicket) {
        isWicket = wicket;
    }

    public String getWicketType() {
        return wicketType;
    }

    public void setWicketType(String wicketType) {
        this.wicketType = wicketType;
    }

    public boolean isExtra() {
        return isExtra;
    }

    public void setExtra(boolean extra) {
        isExtra = extra;
    }

    public String getExtraType() {
        return extraType;
    }

    public void setExtraType(String extraType) {
        this.extraType = extraType;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public String getTimeDisplay() {
        return timeDisplay;
    }

    public void setTimeDisplay(String timeDisplay) {
        this.timeDisplay = timeDisplay;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
