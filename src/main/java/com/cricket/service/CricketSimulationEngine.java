package com.cricket.service;

import com.cricket.dto.ScoreUpdateRequest;
import com.cricket.model.Innings;
import com.cricket.model.Match;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

@Service
public class CricketSimulationEngine {
    private static final Logger log = LoggerFactory.getLogger(CricketSimulationEngine.class);
    private final CricketMatchService matchService;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> activeSimulations = new ConcurrentHashMap<>();

    public CricketSimulationEngine(CricketMatchService matchService) {
        this.matchService = matchService;
    }

    public Match simulateNextBall(String matchId) {
        Match match = matchService.getMatchById(matchId);
        if (match == null || "COMPLETED".equals(match.getStatus())) {
            stopAutoSimulation(matchId);
            return match;
        }

        ScoreUpdateRequest request = generateRealisticBallOutcome(match);
        return matchService.processBallUpdate(matchId, request);
    }

    public synchronized boolean toggleAutoSimulation(String matchId, boolean enable, Integer intervalMs) {
        Match match = matchService.getMatchById(matchId);
        if (match == null) return false;

        int interval = (intervalMs != null && intervalMs >= 500) ? intervalMs : match.getSimulationIntervalMs();
        match.setSimulationIntervalMs(interval);

        if (!enable || "COMPLETED".equals(match.getStatus())) {
            stopAutoSimulation(matchId);
            match.setAutoSimulating(false);
            return false;
        }

        stopAutoSimulation(matchId);
        match.setAutoSimulating(true);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                Match m = matchService.getMatchById(matchId);
                if (m == null || "COMPLETED".equals(m.getStatus()) || !m.isAutoSimulating()) {
                    stopAutoSimulation(matchId);
                    return;
                }
                simulateNextBall(matchId);
            } catch (Exception e) {
                log.error("Simulation error for match {}: {}", matchId, e.getMessage());
            }
        }, 500, interval, TimeUnit.MILLISECONDS);

        activeSimulations.put(matchId, future);
        return true;
    }

    public synchronized void stopAutoSimulation(String matchId) {
        ScheduledFuture<?> future = activeSimulations.remove(matchId);
        if (future != null) {
            future.cancel(false);
        }
        Match match = matchService.getMatchById(matchId);
        if (match != null) {
            match.setAutoSimulating(false);
        }
    }

    private ScoreUpdateRequest generateRealisticBallOutcome(Match match) {
        ScoreUpdateRequest req = new ScoreUpdateRequest();
        Innings current = match.getCurrentInnings();
        if (current == null) {
            req.setRuns(0);
            return req;
        }

        int ballsBowled = current.getLegalBalls();
        boolean isDeathOvers = (ballsBowled >= (match.getMaxOvers() - 4) * 6);
        boolean isHighPressureChase = false;

        if (match.getCurrentInningsIndex() == 1 && match.getTarget() != null) {
            Double rrr = match.getRequiredRunRate();
            if (rrr != null && rrr > 10.0) {
                isHighPressureChase = true;
            }
        }

        int roll = random.nextInt(100);

        // Standard distribution vs Death overs / aggressive chase
        if (isDeathOvers || isHighPressureChase) {
            // Higher boundary & wicket chance
            if (roll < 22) { // 22% dot
                req.setRuns(0);
            } else if (roll < 48) { // 26% single
                req.setRuns(1);
            } else if (roll < 60) { // 12% double
                req.setRuns(2);
            } else if (roll < 76) { // 16% boundary (four)
                req.setRuns(4);
            } else if (roll < 88) { // 12% maximum (six)
                req.setRuns(6);
            } else if (roll < 96) { // 8% wicket
                req.setWicket(true);
                req.setRuns(0);
                req.setWicketType(randomWicketType());
            } else { // 4% extra
                req.setExtra(true);
                req.setRuns(1);
                req.setExtraType(random.nextBoolean() ? "wide" : "noball");
            }
        } else {
            // Normal middle overs distribution
            if (roll < 34) { // 34% dot ball
                req.setRuns(0);
            } else if (roll < 68) { // 34% single
                req.setRuns(1);
            } else if (roll < 80) { // 12% double
                req.setRuns(2);
            } else if (roll < 82) { // 2% triple
                req.setRuns(3);
            } else if (roll < 91) { // 9% four
                req.setRuns(4);
            } else if (roll < 95) { // 4% six
                req.setRuns(6);
            } else if (roll < 98) { // 3% wicket
                req.setWicket(true);
                req.setRuns(0);
                req.setWicketType(randomWicketType());
            } else { // 2% extra
                req.setExtra(true);
                req.setRuns(1);
                req.setExtraType("wide");
            }
        }

        return req;
    }

    private String randomWicketType() {
        String[] types = {"bowled", "caught", "lbw", "caught", "caught", "run_out", "stumped"};
        return types[random.nextInt(types.length)];
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdownNow();
    }
}
