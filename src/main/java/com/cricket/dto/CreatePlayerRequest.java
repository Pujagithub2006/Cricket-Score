package com.cricket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreatePlayerRequest {

    @NotBlank(message = "Player name is required")
    private String name;

    private String role = "ALL_ROUNDER"; // BATSMAN, BOWLER, ALL_ROUNDER, WICKET_KEEPER
    private String battingStyle = "Right-hand bat";
    private String bowlingStyle = "Right-arm medium";

    @NotNull(message = "Team ID is required")
    private Long teamId;

    public CreatePlayerRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBattingStyle() {
        return battingStyle;
    }

    public void setBattingStyle(String battingStyle) {
        this.battingStyle = battingStyle;
    }

    public String getBowlingStyle() {
        return bowlingStyle;
    }

    public void setBowlingStyle(String bowlingStyle) {
        this.bowlingStyle = bowlingStyle;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}
