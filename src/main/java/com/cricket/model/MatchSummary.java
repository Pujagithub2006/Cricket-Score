package com.cricket.model;

import java.util.ArrayList;
import java.util.List;

public class MatchSummary {
    private String matchId;
    private String title;
    private String venue;
    private String result;
    private String playerOfTheMatch;
    private String topScorer;
    private String bestBowler;
    private int team1Total;
    private int team1Wickets;
    private String team1Overs;
    private int team2Total;
    private int team2Wickets;
    private String team2Overs;
    private List<String> highlights = new ArrayList<>();

    public MatchSummary() {}

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getPlayerOfTheMatch() { return playerOfTheMatch; }
    public void setPlayerOfTheMatch(String playerOfTheMatch) { this.playerOfTheMatch = playerOfTheMatch; }

    public String getTopScorer() { return topScorer; }
    public void setTopScorer(String topScorer) { this.topScorer = topScorer; }

    public String getBestBowler() { return bestBowler; }
    public void setBestBowler(String bestBowler) { this.bestBowler = bestBowler; }

    public int getTeam1Total() { return team1Total; }
    public void setTeam1Total(int team1Total) { this.team1Total = team1Total; }

    public int getTeam1Wickets() { return team1Wickets; }
    public void setTeam1Wickets(int team1Wickets) { this.team1Wickets = team1Wickets; }

    public String getTeam1Overs() { return team1Overs; }
    public void setTeam1Overs(String team1Overs) { this.team1Overs = team1Overs; }

    public int getTeam2Total() { return team2Total; }
    public void setTeam2Total(int team2Total) { this.team2Total = team2Total; }

    public int getTeam2Wickets() { return team2Wickets; }
    public void setTeam2Wickets(int team2Wickets) { this.team2Wickets = team2Wickets; }

    public String getTeam2Overs() { return team2Overs; }
    public void setTeam2Overs(String team2Overs) { this.team2Overs = team2Overs; }

    public List<String> getHighlights() { return highlights; }
    public void setHighlights(List<String> highlights) { this.highlights = highlights; }
}
