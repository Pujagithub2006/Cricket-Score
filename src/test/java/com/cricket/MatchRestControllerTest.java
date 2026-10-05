package com.cricket;

import com.cricket.dto.CreateMatchRequest;
import com.cricket.dto.ScoreUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MatchRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/matches - Retrieve all matches")
    void testGetAllMatches() throws Exception {
        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].title", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/matches/{id} - Retrieve match by valid ID")
    void testGetMatchById() throws Exception {
        mockMvc.perform(get("/api/matches/match-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("match-1")))
                .andExpect(jsonPath("$.team1.name", notNullValue()))
                .andExpect(jsonPath("$.team2.name", notNullValue()))
                .andExpect(jsonPath("$.innings", not(empty())));
    }

    @Test
    @DisplayName("GET /api/matches/{id} - Return 404 for unknown ID")
    void testGetMatchByUnknownId() throws Exception {
        mockMvc.perform(get("/api/matches/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/matches - Successfully create a new cricket match")
    void testCreateNewMatch() throws Exception {
        CreateMatchRequest request = new CreateMatchRequest();
        request.setTitle("New Zealand vs South Africa - Semi Final");
        request.setSeries("ICC Champions Trophy");
        request.setMatchType("T20");
        request.setVenue("Eden Park, Auckland");
        request.setMaxOvers(20);
        request.setTeam1Name("New Zealand");
        request.setTeam1ShortName("NZ");
        request.setTeam1Squad(List.of("Kane Williamson", "Devon Conway", "Glenn Phillips", "Daryl Mitchell", "Trent Boult", "Tim Southee"));
        request.setTeam2Name("South Africa");
        request.setTeam2ShortName("SA");
        request.setTeam2Squad(List.of("Quinton de Kock", "Aiden Markram", "Heinrich Klaasen", "David Miller", "Kagiso Rabada", "Anrich Nortje"));
        request.setTossDetails("New Zealand won the toss and elected to bat");

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("New Zealand vs South Africa - Semi Final")))
                .andExpect(jsonPath("$.team1.name", is("New Zealand")))
                .andExpect(jsonPath("$.team2.name", is("South Africa")))
                .andExpect(jsonPath("$.status", is("LIVE")));
    }

    @Test
    @DisplayName("POST /api/matches/{id}/score-update - Record runs for a delivery")
    void testRecordBallRuns() throws Exception {
        ScoreUpdateRequest request = new ScoreUpdateRequest();
        request.setRuns(4);
        request.setWicket(false);
        request.setExtra(false);
        request.setCustomCommentary("Glorious boundary through mid-wicket!");

        mockMvc.perform(post("/api/matches/match-1/score-update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("match-1")));
    }

    @Test
    @DisplayName("POST /api/matches/{id}/simulate-ball - Simulate delivery")
    void testSimulateBall() throws Exception {
        mockMvc.perform(post("/api/matches/match-1/simulate-ball"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("match-1")));
    }

    @Test
    @DisplayName("GET /api/matches/{id}/summary - Retrieve match summary")
    void testGetMatchSummary() throws Exception {
        mockMvc.perform(get("/api/matches/match-1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchId", is("match-1")))
                .andExpect(jsonPath("$.title", notNullValue()))
                .andExpect(jsonPath("$.topScorer", notNullValue()))
                .andExpect(jsonPath("$.bestBowler", notNullValue()));
    }

    @Test
    @DisplayName("PUT /api/matches/{id}/status - Update match status")
    void testUpdateMatchStatus() throws Exception {
        mockMvc.perform(put("/api/matches/match-2/status?status=PAUSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PAUSED")));
    }

    @Test
    @DisplayName("POST /api/matches/{id}/reset - Reset match")
    void testResetMatch() throws Exception {
        mockMvc.perform(post("/api/matches/match-1/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("match-1")));
    }
}
