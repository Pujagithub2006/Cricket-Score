package com.cricket.controller;

import com.cricket.dto.CreateTeamRequest;
import com.cricket.entity.PlayerEntity;
import com.cricket.entity.TeamEntity;
import com.cricket.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
@Tag(name = "Team Management", description = "APIs for maintaining team and squad information")
public class TeamRestController {

    private final TeamService teamService;

    public TeamRestController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    @Operation(summary = "Get all teams", description = "Retrieves all teams along with their squad lists")
    public ResponseEntity<List<TeamEntity>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID", description = "Retrieves specific team details and squad")
    public ResponseEntity<TeamEntity> getTeamById(@PathVariable Long id) {
        return teamService.getTeamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/players")
    @Operation(summary = "Get players of a team", description = "Retrieves all players belonging to a team")
    public ResponseEntity<List<PlayerEntity>> getPlayersByTeamId(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getPlayersByTeamId(id));
    }

    @PostMapping
    @Operation(summary = "Create a new team", description = "Adds a new team to persistent database storage")
    public ResponseEntity<TeamEntity> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        TeamEntity created = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
