package com.cricket.controller;

import com.cricket.dto.CreatePlayerRequest;
import com.cricket.entity.PlayerEntity;
import com.cricket.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*")
@Tag(name = "Player Management", description = "APIs for maintaining player statistics, roles, and profiles")
public class PlayerRestController {

    private final TeamService teamService;

    public PlayerRestController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    @Operation(summary = "Get all players", description = "Retrieves all players across all teams")
    public ResponseEntity<List<PlayerEntity>> getAllPlayers() {
        return ResponseEntity.ok(teamService.getAllPlayers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID", description = "Retrieves a player's profile and stats")
    public ResponseEntity<PlayerEntity> getPlayerById(@PathVariable Long id) {
        return teamService.getPlayerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Add a new player", description = "Creates a player and associates them with a team in the database")
    public ResponseEntity<PlayerEntity> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        PlayerEntity created = teamService.createPlayer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
