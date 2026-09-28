package com.cricket.controller;

import com.cricket.dto.AutoSimRequest;
import com.cricket.dto.ScoreUpdateRequest;
import com.cricket.model.Match;
import com.cricket.model.MatchSummary;
import com.cricket.service.CricketMatchService;
import com.cricket.service.CricketSimulationEngine;
import com.cricket.service.SseEmitterService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<List<Match>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatchById(@PathVariable String id) {
        Match match = matchService.getMatchById(id);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(match);
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<MatchSummary> getMatchSummary(@PathVariable String id) {
        MatchSummary summary = matchService.getMatchSummary(id);
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/{id}/simulate-ball")
    public ResponseEntity<Match> simulateBall(@PathVariable String id) {
        Match match = simulationEngine.simulateNextBall(id);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(match);
    }

    @PostMapping("/{id}/auto-simulation")
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
    public ResponseEntity<Match> manualScoreUpdate(@PathVariable String id, @RequestBody ScoreUpdateRequest request) {
        Match match = matchService.processBallUpdate(id, request);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(match);
    }

    @PostMapping("/{id}/reset")
    public ResponseEntity<Match> resetMatch(@PathVariable String id) {
        simulationEngine.stopAutoSimulation(id);
        Match match = matchService.resetMatch(id);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(match);
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMatchUpdates(@PathVariable String id) {
        return sseEmitterService.subscribe(id);
    }
}
