package com.cricket.controller;

import com.cricket.dto.AutoSimRequest;
import com.cricket.dto.CreateMatchRequest;
import com.cricket.dto.ScoreUpdateRequest;
import com.cricket.model.Match;
import com.cricket.model.MatchSummary;
import com.cricket.service.CricketMatchService;
import com.cricket.service.CricketSimulationEngine;
import com.cricket.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
@Tag(name = "Match Management & Live Scoring", description = "Endpoints for managing matches, live scores, simulation, and SSE streaming")
public class MatchRestController {

    private final CricketMatchService matchService;
    private final CricketSimulationEngine simulationEngine;
    private final SseEmitterService sseEmitterService;

    public MatchRestController(CricketMatchService matchService,
                               CricketSimulationEngine simulationEngine,
                               SseEmitterService sseEmitterService) {
        this.matchService = matchService;
        this.simulationEngine = simulationEngine;
        this.sseEmitterService = sseEmitterService;
    }

    @GetMapping
    @Operation(summary = "Get all matches", description = "Retrieves all ongoing, upcoming, and completed cricket matches")
    public ResponseEntity<List<Match>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get match by ID", description = "Retrieves complete match data, innings, active batsmen and bowlers")
    public ResponseEntity<Match> getMatchById(
            @Parameter(description = "ID of the match (e.g. match-1)") @PathVariable String id) {
        Match match = matchService.getMatchById(id);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(match);
    }

    @PostMapping
    @Operation(summary = "Create a new match", description = "Creates a new cricket fixture and saves it to persistent database storage")
    public ResponseEntity<Match> createMatch(@Valid @RequestBody CreateMatchRequest request) {
        Match created = matchService.createMatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete match", description = "Removes a match and its ball history from persistent storage")
    public ResponseEntity<Map<String, Object>> deleteMatch(@PathVariable String id) {
        simulationEngine.stopAutoSimulation(id);
        boolean deleted = matchService.deleteMatch(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Match deleted successfully", "id", id));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update match status", description = "Updates match state (e.g. LIVE, PAUSED, COMPLETED)")
    public ResponseEntity<Match> updateMatchStatus(@PathVariable String id, @RequestParam String status) {
        Match match = matchService.updateMatchStatus(id, status);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(match);
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Get match summary", description = "Retrieves analytical match summary, awards, top performers, and highlights")
    public ResponseEntity<MatchSummary> getMatchSummary(@PathVariable String id) {
        MatchSummary summary = matchService.getMatchSummary(id);
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/{id}/simulate-ball")
    @Operation(summary = "Simulate next ball", description = "Generates a realistic probabilistic delivery outcome for the match")
    public ResponseEntity<Match> simulateBall(@PathVariable String id) {
        Match match = simulationEngine.simulateNextBall(id);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(match);
    }

    @PostMapping("/{id}/auto-simulation")
    @Operation(summary = "Toggle auto-simulation", description = "Starts or stops continuous background ball-by-ball simulation at specified interval")
    public ResponseEntity<Match> toggleAutoSimulation(@PathVariable String id, @RequestBody AutoSimRequest req) {
        boolean active = simulationEngine.toggleAutoSimulation(id, req.isEnabled(), req.getIntervalMs());
        Match match = matchService.getMatchById(id);
        if (match != null) {
            match.setAutoSimulating(active);
            return ResponseEntity.ok(match);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/score-update")
    @Operation(summary = "Manual score update", description = "Manually records runs, wickets, extras, and editorial commentary for a delivery")
    public ResponseEntity<Match> manualScoreUpdate(@PathVariable String id, @RequestBody ScoreUpdateRequest request) {
        Match match = matchService.processBallUpdate(id, request);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(match);
    }

    @PostMapping("/{id}/reset")
    @Operation(summary = "Reset match", description = "Resets match scores and state back to starting scenario for demonstration")
    public ResponseEntity<Match> resetMatch(@PathVariable String id) {
        simulationEngine.stopAutoSimulation(id);
        Match match = matchService.resetMatch(id);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(match);
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Live SSE Stream", description = "Subscribes client to Server-Sent Events (SSE) for zero-latency live score broadcast")
    public SseEmitter streamMatchUpdates(@PathVariable String id) {
        return sseEmitterService.subscribe(id);
    }
}
