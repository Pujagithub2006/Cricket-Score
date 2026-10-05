package com.cricket;

import com.cricket.dto.CreatePlayerRequest;
import com.cricket.dto.CreateTeamRequest;
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
public class TeamRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/teams - Retrieve all teams and squads")
    void testGetAllTeams() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].shortName", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/teams - Create a new team")
    void testCreateTeam() throws Exception {
        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("West Indies");
        request.setShortName("WI");
        request.setCode("WI");
        request.setFlag("🌴");
        request.setPrimaryColor("#7B1113");
        request.setPlayerNames(List.of("Nicholas Pooran", "Andre Russell", "Shimron Hetmyer", "Akeal Hosein", "Alzarri Joseph"));

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("West Indies")))
                .andExpect(jsonPath("$.shortName", is("WI")));
    }

    @Test
    @DisplayName("GET /api/players - Retrieve all players across teams")
    void testGetAllPlayers() throws Exception {
        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].role", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/players - Add a player to a team")
    void testAddPlayer() throws Exception {
        CreatePlayerRequest request = new CreatePlayerRequest();
        request.setName("Shubman Gill");
        request.setRole("BATSMAN");
        request.setBattingStyle("Right-hand bat");
        request.setBowlingStyle("Right-arm offbreak");
        request.setTeamId(1L);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Shubman Gill")))
                .andExpect(jsonPath("$.role", is("BATSMAN")));
    }
}
