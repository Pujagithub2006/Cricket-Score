package com.cricket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateMatchRequest {

    @NotBlank(message = "Match title is required")
    private String title;

    private String series = "International Championship 2026";
    private String matchType = "T20";

    @NotBlank(message = "Venue is required")
    private String venue;

    @Min(value = 1, message = "Overs must be at least 1")
    private int maxOvers = 20;

    @NotBlank(message = "Team 1 name is required")
    private String team1Name;
    private String team1ShortName;
    private String team1Flag = "🏏";
    private String team1Color = "#0078FF";
    private List<String> team1Squad;

    @NotBlank(message = "Team 2 name is required")
    private String team2Name;
    private String team2ShortName;
    private String team2Flag = "🏆";
    private String team2Color = "#FFD700";
    private List<String> team2Squad;

    private String tossDetails;

    public CreateMatchRequest() {}

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

    public int getMaxOvers() {
        return maxOvers;
    }

    public void setMaxOvers(int maxOvers) {
        this.maxOvers = maxOvers;
    }

    public String getTeam1Name() {
        return team1Name;
    }

    public void setTeam1Name(String team1Name) {
        this.team1Name = team1Name;
    }

    public String getTeam1ShortName() {
        return team1ShortName;
    }

    public void setTeam1ShortName(String team1ShortName) {
        this.team1ShortName = team1ShortName;
    }

    public String getTeam1Flag() {
        return team1Flag;
    }

    public void setTeam1Flag(String team1Flag) {
        this.team1Flag = team1Flag;
    }

    public String getTeam1Color() {
        return team1Color;
    }

    public void setTeam1Color(String team1Color) {
        this.team1Color = team1Color;
    }

    public List<String> getTeam1Squad() {
        return team1Squad;
    }

    public void setTeam1Squad(List<String> team1Squad) {
        this.team1Squad = team1Squad;
    }

    public String getTeam2Name() {
        return team2Name;
    }

    public void setTeam2Name(String team2Name) {
        this.team2Name = team2Name;
    }

    public String getTeam2ShortName() {
        return team2ShortName;
    }

    public void setTeam2ShortName(String team2ShortName) {
        this.team2ShortName = team2ShortName;
    }

    public String getTeam2Flag() {
        return team2Flag;
    }

    public void setTeam2Flag(String team2Flag) {
        this.team2Flag = team2Flag;
    }

    public String getTeam2Color() {
        return team2Color;
    }

    public void setTeam2Color(String team2Color) {
        this.team2Color = team2Color;
    }

    public List<String> getTeam2Squad() {
        return team2Squad;
    }

    public void setTeam2Squad(List<String> team2Squad) {
        this.team2Squad = team2Squad;
    }

    public String getTossDetails() {
        return tossDetails;
    }

    public void setTossDetails(String tossDetails) {
        this.tossDetails = tossDetails;
    }
}
