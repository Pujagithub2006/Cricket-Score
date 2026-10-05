package com.cricket.service;

import com.cricket.dto.CreateMatchRequest;
import com.cricket.dto.ScoreUpdateRequest;
import com.cricket.entity.BallEventEntity;
import com.cricket.entity.MatchEntity;
import com.cricket.entity.TeamEntity;
import com.cricket.model.*;
import com.cricket.repository.BallEventEntityRepository;
import com.cricket.repository.MatchEntityRepository;
import com.cricket.repository.TeamRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CricketMatchService {

    private static final Logger log = LoggerFactory.getLogger(CricketMatchService.class);

    private final Map<String, Match> matchStore = new ConcurrentHashMap<>();
    private final SseEmitterService sseEmitterService;
    private final MatchEntityRepository matchEntityRepository;
    private final BallEventEntityRepository ballEventEntityRepository;
    private final TeamRepository teamRepository;
    private final ObjectMapper objectMapper;

    public CricketMatchService(SseEmitterService sseEmitterService,
                               MatchEntityRepository matchEntityRepository,
                               BallEventEntityRepository ballEventEntityRepository,
                               TeamRepository teamRepository,
                               ObjectMapper objectMapper) {
        this.sseEmitterService = sseEmitterService;
        this.matchEntityRepository = matchEntityRepository;
        this.ballEventEntityRepository = ballEventEntityRepository;
        this.teamRepository = teamRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        loadOrInitializeMatches();
    }

    public synchronized void loadOrInitializeMatches() {
        matchStore.clear();
        List<MatchEntity> persistedMatches = matchEntityRepository.findAll();

        if (persistedMatches.isEmpty()) {
            log.info("No matches found in database. Initializing default sample matches...");
            createIndVsAusMatch();
            createEngVsPakMatch();
            createCskVsMiMatch();
        } else {
            log.info("Loading {} matches from persistent database storage...", persistedMatches.size());
            for (MatchEntity entity : persistedMatches) {
                try {
                    if (entity.getMatchDataJson() != null && !entity.getMatchDataJson().isEmpty()) {
                        Match match = objectMapper.readValue(entity.getMatchDataJson(), Match.class);
                        matchStore.put(match.getId(), match);
                    }
                } catch (Exception e) {
                    log.error("Failed to rehydrate match {} from database: {}", entity.getId(), e.getMessage());
                }
            }
            // If rehydration was empty for any reason, reseed
            if (matchStore.isEmpty()) {
                createIndVsAusMatch();
                createEngVsPakMatch();
                createCskVsMiMatch();
            }
        }
    }

    public List<Match> getAllMatches() {
        return new ArrayList<>(matchStore.values());
    }

    public Match getMatchById(String id) {
        return matchStore.get(id);
    }

    @Transactional
    public synchronized Match createMatch(CreateMatchRequest request) {
        String matchId = "match-" + System.currentTimeMillis();

        Match match = new Match();
        match.setId(matchId);
        match.setTitle(request.getTitle());
        match.setSeries(request.getSeries() != null ? request.getSeries() : "T20 Championship 2026");
        match.setMatchType(request.getMatchType() != null ? request.getMatchType() : "T20");
        match.setVenue(request.getVenue());
        match.setStatus("LIVE");
        match.setMaxOvers(request.getMaxOvers() > 0 ? request.getMaxOvers() : 20);
        match.setTossDetails(request.getTossDetails() != null ? request.getTossDetails() : request.getTeam1Name() + " won the toss and elected to bat");
        match.setResultMessage(request.getTeam1Name() + " batting in 1st Innings");
        match.setAutoSimulating(false);

        // Team 1
        List<String> squad1 = (request.getTeam1Squad() != null && !request.getTeam1Squad().isEmpty())
                ? request.getTeam1Squad()
                : defaultSquadForTeam(request.getTeam1Name());
        String t1Short = request.getTeam1ShortName() != null ? request.getTeam1ShortName() : deriveShortName(request.getTeam1Name());
        Team team1 = new Team(request.getTeam1Name(), t1Short, t1Short, request.getTeam1Flag(), request.getTeam1Color(), squad1);

        // Team 2
        List<String> squad2 = (request.getTeam2Squad() != null && !request.getTeam2Squad().isEmpty())
                ? request.getTeam2Squad()
                : defaultSquadForTeam(request.getTeam2Name());
        String t2Short = request.getTeam2ShortName() != null ? request.getTeam2ShortName() : deriveShortName(request.getTeam2Name());
        Team team2 = new Team(request.getTeam2Name(), t2Short, t2Short, request.getTeam2Flag(), request.getTeam2Color(), squad2);

        match.setTeam1(team1);
        match.setTeam2(team2);

        // Innings 1
        Innings inn1 = new Innings(1, team1.getShortName(), team2.getShortName());
        setupInningsPlayers(inn1, team1, team2);
        match.getInnings().add(inn1);
        match.setCurrentInningsIndex(0);

        matchStore.put(match.getId(), match);
        persistMatch(match);
        sseEmitterService.broadcastMatchUpdate(match);

        log.info("Created new match: {} [{}]", match.getTitle(), match.getId());
        return match;
    }

    @Transactional
    public synchronized boolean deleteMatch(String id) {
        Match removed = matchStore.remove(id);
        if (removed != null) {
            try {
                matchEntityRepository.deleteById(id);
                ballEventEntityRepository.deleteByMatchId(id);
                log.info("Deleted match {} from persistent storage", id);
                return true;
            } catch (Exception e) {
                log.error("Error deleting match {}: {}", id, e.getMessage());
            }
        }
        return false;
    }

    @Transactional
    public synchronized Match updateMatchStatus(String id, String status) {
        Match match = matchStore.get(id);
        if (match != null) {
            match.setStatus(status.toUpperCase());
            if ("COMPLETED".equalsIgnoreCase(status)) {
                match.setAutoSimulating(false);
            }
            persistMatch(match);
            sseEmitterService.broadcastMatchUpdate(match);
        }
        return match;
    }

    public synchronized Match resetMatch(String id) {
        if ("match-1".equals(id)) {
            createIndVsAusMatch();
        } else if ("match-2".equals(id)) {
            createEngVsPakMatch();
        } else if ("match-3".equals(id)) {
            createCskVsMiMatch();
        } else {
            Match m = matchStore.get(id);
            if (m != null) {
                m.getInnings().clear();
                Innings inn1 = new Innings(1, m.getTeam1().getShortName(), m.getTeam2().getShortName());
                setupInningsPlayers(inn1, m.getTeam1(), m.getTeam2());
                m.getInnings().add(inn1);
                m.setCurrentInningsIndex(0);
                m.setStatus("LIVE");
                m.setTarget(null);
                m.setResultMessage(m.getTeam1().getShortName() + " batting in 1st Innings");
                persistMatch(m);
            }
        }
        Match m = matchStore.get(id);
        if (m != null) {
            sseEmitterService.broadcastMatchUpdate(m);
        }
        return m;
    }

    @Transactional
    public synchronized Match processBallUpdate(String matchId, ScoreUpdateRequest request) {
        Match match = matchStore.get(matchId);
        if (match == null || "COMPLETED".equals(match.getStatus())) {
            return match;
        }

        Innings innings = match.getCurrentInnings();
        if (innings == null || innings.isCompleted()) {
            return match;
        }

        BatsmanStats striker = innings.getStriker();
        BatsmanStats nonStriker = innings.getNonStriker();
        BowlerStats bowler = innings.getCurrentBowler();

        if (striker == null || bowler == null) {
            return match;
        }

        int runs = request.getRuns();
        boolean isWicket = request.isWicket();
        boolean isExtra = request.isExtra();
        String extraType = request.getExtraType() != null ? request.getExtraType().toLowerCase() : "";
        String wicketType = request.getWicketType() != null ? request.getWicketType() : "caught";

        boolean isWideOrNoBall = isExtra && (extraType.contains("wide") || extraType.contains("noball"));
        boolean isLegalBall = !isWideOrNoBall;

        // Update Innings score
        innings.setTotalRuns(innings.getTotalRuns() + runs);

        // Update Extras
        if (isExtra) {
            Extras extras = innings.getExtras();
            if (extraType.contains("wide")) {
                extras.setWides(extras.getWides() + runs);
            } else if (extraType.contains("noball")) {
                extras.setNoBalls(extras.getNoBalls() + runs);
            } else if (extraType.contains("legbye")) {
                extras.setLegByes(extras.getLegByes() + runs);
            } else {
                extras.setByes(extras.getByes() + runs);
            }
        }

        // Update Bowler
        bowler.addBall(runs, isWicket && !wicketType.equalsIgnoreCase("run_out"), isWideOrNoBall);

        // Update Striker Batsman
        if (!isExtra || extraType.contains("noball") || extraType.contains("bye")) {
            if (!isExtra) {
                striker.addRuns(runs);
            } else if (extraType.contains("noball")) {
                striker.addRuns(Math.max(0, runs - 1));
            }
        } else if (isLegalBall && runs == 0) {
            striker.addDotBall();
        }

        // Handle Wicket
        if (isWicket) {
            innings.setWickets(innings.getWickets() + 1);
            striker.setOut(true);
            striker.setBatting(false);
            striker.setOnStrike(false);
            String dismissalDesc = formatDismissal(wicketType, bowler.getName());
            striker.setDismissal(dismissalDesc);

            String fow = innings.getTotalRuns() + "/" + innings.getWickets() + " (" + striker.getName() + ", " + innings.getOversDisplay() + " ov)";
            innings.getFallOfWickets().add(fow);

            // Bring in next batsman if available
            Team battingTeamObj = innings.getBattingTeam().equalsIgnoreCase(match.getTeam1().getShortName()) ||
                    innings.getBattingTeam().equalsIgnoreCase(match.getTeam1().getName()) ? match.getTeam1() : match.getTeam2();

            BatsmanStats nextBatsman = findNextBatsman(innings, battingTeamObj);
            if (nextBatsman != null) {
                nextBatsman.setBatting(true);
                nextBatsman.setOnStrike(true);
                innings.setCurrentStrikerId(nextBatsman.getId());
            } else {
                innings.setCurrentStrikerId(null);
            }
        }

        // Display string and commentary
        String display = buildDisplayString(runs, isWicket, isExtra, extraType);
        String commentary = request.getCustomCommentary();
        if (commentary == null || commentary.trim().isEmpty()) {
            commentary = generateCommentary(striker.getName(), bowler.getName(), runs, isWicket, wicketType, isExtra, extraType);
        }

        int overNum = innings.getLegalBalls() / 6;
        int ballInOver = (innings.getLegalBalls() % 6) + (isLegalBall ? 1 : 0);

        BallEvent ballEvent = new BallEvent(
                UUID.randomUUID().toString(),
                innings.getInningsNumber(),
                overNum,
                ballInOver,
                striker.getName(),
                bowler.getName(),
                runs,
                isWicket,
                wicketType,
                isExtra,
                extraType,
                display,
                commentary,
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        );

        innings.getRecentBalls().add(ballEvent);
        if (innings.getRecentBalls().size() > 12) {
            innings.getRecentBalls().remove(0);
        }
        innings.getAllBallEvents().add(0, ballEvent); // Latest first for commentary

        // Persist BallEventEntity in database
        try {
            BallEventEntity ballEntity = new BallEventEntity(
                    matchId,
                    innings.getInningsNumber(),
                    overNum,
                    ballInOver,
                    striker.getName(),
                    bowler.getName(),
                    runs,
                    isWicket,
                    wicketType,
                    isExtra,
                    extraType,
                    display,
                    commentary,
                    ballEvent.getTimestamp()
            );
            ballEventEntityRepository.save(ballEntity);
        } catch (Exception e) {
            log.error("Failed to persist ball event: {}", e.getMessage());
        }

        // Partnership
        Partnership partnership = innings.getCurrentPartnership();
        if (partnership != null) {
            partnership.addBall(runs, isLegalBall);
        }

        // If legal ball, increment legal ball count
        if (isLegalBall) {
            innings.setLegalBalls(innings.getLegalBalls() + 1);
        }

        // Rotate Strike on Odd Runs (1, 3, 5)
        if (runs % 2 != 0 && !isWicket) {
            swapStrike(innings);
        }

        // Check Over Completion
        if (isLegalBall && innings.getLegalBalls() % 6 == 0) {
            // End of over: switch strike
            swapStrike(innings);
            // Switch Bowler
            rotateBowler(innings, match);
        }

        // Check Chase completion (2nd Innings)
        if (match.getCurrentInningsIndex() == 1 && match.getTarget() != null) {
            if (innings.getTotalRuns() >= match.getTarget()) {
                match.setStatus("COMPLETED");
                match.setAutoSimulating(false);
                innings.setCompleted(true);
                int wicketsLeft = 10 - innings.getWickets();
                match.setResultMessage(innings.getBattingTeam() + " won by " + wicketsLeft + " wicket" + (wicketsLeft > 1 ? "s" : "") + "!");
            }
        }

        // Check 1st Innings Completion or All Out
        if (innings.getWickets() >= 10 || innings.getLegalBalls() >= match.getMaxOvers() * 6) {
            innings.setCompleted(true);
            if (match.getCurrentInningsIndex() == 0) {
                // Transition to 2nd innings
                startSecondInnings(match);
            } else {
                // 2nd innings completed
                match.setStatus("COMPLETED");
                match.setAutoSimulating(false);
                int firstInningsScore = match.getInnings().get(0).getTotalRuns();
                int secondInningsScore = innings.getTotalRuns();
                if (secondInningsScore >= match.getTarget()) {
                    int wLeft = 10 - innings.getWickets();
                    match.setResultMessage(innings.getBattingTeam() + " won by " + wLeft + " wicket" + (wLeft > 1 ? "s" : "") + "!");
                } else if (secondInningsScore == firstInningsScore) {
                    match.setResultMessage("Match Tied!");
                } else {
                    int runDiff = firstInningsScore - secondInningsScore;
                    match.setResultMessage(match.getInnings().get(0).getBattingTeam() + " won by " + runDiff + " runs!");
                }
            }
        }

        // Update live result message if still LIVE
        if ("LIVE".equals(match.getStatus())) {
            if (match.getCurrentInningsIndex() == 1 && match.getTarget() != null) {
                int needed = match.getTarget() - innings.getTotalRuns();
                int ballsLeft = (match.getMaxOvers() * 6) - innings.getLegalBalls();
                match.setResultMessage(innings.getBattingTeam() + " need " + needed + " runs in " + ballsLeft + " balls");
            } else {
                match.setResultMessage(innings.getBattingTeam() + " batting in 1st Innings");
            }
        }

        // Persist updated match state in DB
        persistMatch(match);

        // Broadcast to SSE clients
        sseEmitterService.broadcastMatchUpdate(match);

        return match;
    }

    public void persistMatch(Match match) {
        try {
            MatchEntity entity = matchEntityRepository.findById(match.getId()).orElse(new MatchEntity());
            entity.setId(match.getId());
            entity.setTitle(match.getTitle());
            entity.setSeries(match.getSeries());
            entity.setMatchType(match.getMatchType());
            entity.setVenue(match.getVenue());
            entity.setStatus(match.getStatus());
            entity.setTossDetails(match.getTossDetails());
            entity.setMaxOvers(match.getMaxOvers());
            entity.setTarget(match.getTarget());
            entity.setResultMessage(match.getResultMessage());
            entity.setSimulationIntervalMs(match.getSimulationIntervalMs());
            entity.setAutoSimulating(match.isAutoSimulating());
            entity.setCurrentInningsIndex(match.getCurrentInningsIndex());

            // Link TeamEntities if available
            if (match.getTeam1() != null) {
                teamRepository.findByNameIgnoreCase(match.getTeam1().getName()).ifPresent(entity::setTeam1);
            }
            if (match.getTeam2() != null) {
                teamRepository.findByNameIgnoreCase(match.getTeam2().getName()).ifPresent(entity::setTeam2);
            }

            entity.setMatchDataJson(objectMapper.writeValueAsString(match));
            matchEntityRepository.save(entity);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize match for persistence: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to save match entity: {}", e.getMessage());
        }
    }

    private void swapStrike(Innings innings) {
        String temp = innings.getCurrentStrikerId();
        innings.setCurrentStrikerId(innings.getCurrentNonStrikerId());
        innings.setCurrentNonStrikerId(temp);

        for (BatsmanStats b : innings.getBatsmen()) {
            if (b.getId().equals(innings.getCurrentStrikerId())) {
                b.setOnStrike(true);
            } else if (b.getId().equals(innings.getCurrentNonStrikerId())) {
                b.setOnStrike(false);
            }
        }
    }

    private void rotateBowler(Innings innings, Match match) {
        List<BowlerStats> bowlers = innings.getBowlers();
        if (bowlers.size() < 2) return;

        String currentId = innings.getCurrentBowlerId();
        BowlerStats current = innings.getCurrentBowler();
        if (current != null) current.setCurrent(false);

        for (BowlerStats b : bowlers) {
            if (!b.getId().equals(currentId) && b.getLegalBalls() < (match.getMaxOvers() / 5) * 6) {
                b.setCurrent(true);
                innings.setCurrentBowlerId(b.getId());
                return;
            }
        }
        for (BowlerStats b : bowlers) {
            if (!b.getId().equals(currentId)) {
                b.setCurrent(true);
                innings.setCurrentBowlerId(b.getId());
                return;
            }
        }
    }

    private BatsmanStats findNextBatsman(Innings innings, Team battingTeam) {
        for (BatsmanStats b : innings.getBatsmen()) {
            if (!b.isBatting() && !b.isOut()) {
                return b;
            }
        }
        int nextIndex = innings.getBatsmen().size();
        if (nextIndex < battingTeam.getSquad().size()) {
            String playerName = battingTeam.getSquad().get(nextIndex);
            BatsmanStats newBatsman = new BatsmanStats("bat-" + (nextIndex + 1), playerName);
            innings.getBatsmen().add(newBatsman);
            return newBatsman;
        }
        return null;
    }

    private void startSecondInnings(Match match) {
        Innings firstInnings = match.getInnings().get(0);
        int target = firstInnings.getTotalRuns() + 1;
        match.setTarget(target);

        Team battingTeam = match.getTeam2();
        Team bowlingTeam = match.getTeam1();

        if (firstInnings.getBattingTeam().equalsIgnoreCase(battingTeam.getShortName())) {
            battingTeam = match.getTeam1();
            bowlingTeam = match.getTeam2();
        }

        Innings secondInnings = new Innings(2, battingTeam.getShortName(), bowlingTeam.getShortName());
        setupInningsPlayers(secondInnings, battingTeam, bowlingTeam);

        match.getInnings().add(secondInnings);
        match.setCurrentInningsIndex(1);
        match.setResultMessage(battingTeam.getShortName() + " need " + target + " runs to win (" + match.getMaxOvers() + " ov)");
    }

    private void setupInningsPlayers(Innings innings, Team battingTeam, Team bowlingTeam) {
        if (battingTeam.getSquad().size() >= 2) {
            BatsmanStats b1 = new BatsmanStats("bat-1", battingTeam.getSquad().get(0));
            b1.setBatting(true);
            b1.setOnStrike(true);
            BatsmanStats b2 = new BatsmanStats("bat-2", battingTeam.getSquad().get(1));
            b2.setBatting(true);
            b2.setOnStrike(false);

            innings.getBatsmen().add(b1);
            innings.getBatsmen().add(b2);
            innings.setCurrentStrikerId(b1.getId());
            innings.setCurrentNonStrikerId(b2.getId());
            innings.getPartnerships().add(new Partnership(b1.getName(), b2.getName()));
        }

        List<String> squad = bowlingTeam.getSquad();
        int start = Math.max(0, squad.size() - 5);
        for (int i = start; i < squad.size(); i++) {
            BowlerStats bw = new BowlerStats("bowl-" + (i + 1), squad.get(i));
            innings.getBowlers().add(bw);
        }
        if (!innings.getBowlers().isEmpty()) {
            innings.getBowlers().get(0).setCurrent(true);
            innings.setCurrentBowlerId(innings.getBowlers().get(0).getId());
        }
    }

    private String buildDisplayString(int runs, boolean isWicket, boolean isExtra, String extraType) {
        if (isWicket) return "W";
        if (isExtra) {
            if (extraType.contains("wide")) return runs + "wd";
            if (extraType.contains("noball")) return runs + "nb";
            if (extraType.contains("bye")) return runs + "b";
            if (extraType.contains("legbye")) return runs + "lb";
            return runs + "ex";
        }
        return String.valueOf(runs);
    }

    private String formatDismissal(String wicketType, String bowlerName) {
        switch (wicketType.toLowerCase()) {
            case "bowled":
                return "b " + bowlerName;
            case "lbw":
                return "lbw b " + bowlerName;
            case "run_out":
                return "run out";
            case "stumped":
                return "st b " + bowlerName;
            default:
                return "c Field b " + bowlerName;
        }
    }

    private String generateCommentary(String batsman, String bowler, int runs, boolean isWicket, String wicketType, boolean isExtra, String extraType) {
        if (isWicket) {
            return "OUT! " + bowler + " strikes! " + batsman + " is " + wicketType.replace("_", " ") + " for a crucial breakthrough!";
        }
        if (isExtra) {
            return bowler + " strays in line, called " + extraType.toUpperCase() + ", " + runs + " extra run(s) awarded.";
        }
        if (runs == 6) {
            return "SIX! Massive hit by " + batsman + "! Lofted high and clean over deep midwicket into the stands!";
        }
        if (runs == 4) {
            return "FOUR! Glorious stroke by " + batsman + "! Pierces the gap through covers with pure elegance.";
        }
        if (runs == 0) {
            return bowler + " bowls a sharp delivery, defended solidly on the backfoot by " + batsman + ". No run.";
        }
        if (runs == 1) {
            return batsman + " taps it gently into the vacant pocket on the on-side and rotates the strike.";
        }
        return batsman + " clips it smartly into the deep, running hard between the wickets for " + runs + " runs.";
    }

    public MatchSummary getMatchSummary(String matchId) {
        Match match = matchStore.get(matchId);
        if (match == null) return null;

        MatchSummary summary = new MatchSummary();
        summary.setMatchId(match.getId());
        summary.setTitle(match.getTitle());
        summary.setVenue(match.getVenue());
        summary.setResult(match.getResultMessage() != null ? match.getResultMessage() : "In Progress");

        if (match.getInnings().size() > 0) {
            Innings inn1 = match.getInnings().get(0);
            summary.setTeam1Total(inn1.getTotalRuns());
            summary.setTeam1Wickets(inn1.getWickets());
            summary.setTeam1Overs(inn1.getOversDisplay());
        }

        if (match.getInnings().size() > 1) {
            Innings inn2 = match.getInnings().get(1);
            summary.setTeam2Total(inn2.getTotalRuns());
            summary.setTeam2Wickets(inn2.getWickets());
            summary.setTeam2Overs(inn2.getOversDisplay());
        }

        String topScorer = "N/A";
        int maxRuns = -1;
        String bestBowler = "N/A";
        int maxWickets = -1;

        for (Innings inn : match.getInnings()) {
            for (BatsmanStats b : inn.getBatsmen()) {
                if (b.getRuns() > maxRuns) {
                    maxRuns = b.getRuns();
                    topScorer = b.getName() + " (" + b.getRuns() + " off " + b.getBalls() + "b)";
                }
            }
            for (BowlerStats bw : inn.getBowlers()) {
                if (bw.getWickets() > maxWickets) {
                    maxWickets = bw.getWickets();
                    bestBowler = bw.getName() + " (" + bw.getWickets() + "/" + bw.getRunsConceded() + " in " + bw.getOversDisplay() + " ov)";
                }
            }
        }

        summary.setTopScorer(topScorer);
        summary.setBestBowler(bestBowler);
        summary.setPlayerOfTheMatch(topScorer.split(" \\(")[0]);

        List<String> highlights = new ArrayList<>();
        highlights.add("High intensity clash hosted at " + match.getVenue());
        highlights.add(match.getTossDetails() != null ? match.getTossDetails() : "Toss conducted");
        if (match.getTarget() != null) {
            highlights.add("Target set: " + match.getTarget() + " runs in " + match.getMaxOvers() + " overs");
        }
        highlights.add("Outstanding boundary percentage & death-over power hitting showcased.");
        summary.setHighlights(highlights);

        return summary;
    }

    private List<String> defaultSquadForTeam(String teamName) {
        Optional<TeamEntity> opt = teamRepository.findByNameIgnoreCase(teamName);
        if (opt.isPresent() && !opt.get().getPlayers().isEmpty()) {
            return opt.get().getPlayers().stream().map(p -> p.getName()).toList();
        }
        return List.of(
                teamName + " Player 1", teamName + " Player 2", teamName + " Player 3",
                teamName + " Player 4", teamName + " Player 5", teamName + " Player 6",
                teamName + " Player 7", teamName + " Player 8", teamName + " Player 9",
                teamName + " Player 10", teamName + " Player 11"
        );
    }

    private String deriveShortName(String name) {
        if (name == null || name.isEmpty()) return "TM";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, Math.min(2, parts[1].length()))).toUpperCase();
        }
        return name.substring(0, Math.min(3, name.length())).toUpperCase();
    }

    private void createIndVsAusMatch() {
        Match match = new Match();
        match.setId("match-1");
        match.setTitle("India vs Australia - 3rd T20I Decider");
        match.setSeries("Mastercard T20 Series 2026");
        match.setMatchType("T20");
        match.setVenue("Wankhede Stadium, Mumbai");
        match.setStatus("LIVE");
        match.setTossDetails("India won the toss and elected to bowl first");
        match.setMaxOvers(20);
        match.setTarget(189);
        match.setResultMessage("India need 31 runs in 18 balls");
        match.setAutoSimulating(false);

        Team ind = new Team("India", "IND", "IN", "🇮🇳", "#0078FF",
                List.of("Rohit Sharma (c)", "Yashasvi Jaiswal", "Virat Kohli", "Suryakumar Yadav",
                        "Hardik Pandya", "Rinku Singh", "Axar Patel", "Kuldeep Yadav",
                        "Jasprit Bumrah", "Mohammed Siraj", "Arshdeep Singh"));

        Team aus = new Team("Australia", "AUS", "AU", "🇦🇺", "#FFD700",
                List.of("Travis Head", "David Warner", "Mitchell Marsh (c)", "Glenn Maxwell",
                        "Marcus Stoinis", "Tim David", "Matthew Wade", "Pat Cummins",
                        "Mitchell Starc", "Adam Zampa", "Josh Hazlewood"));

        match.setTeam1(aus);
        match.setTeam2(ind);

        // 1st Innings: Australia (Completed 188/5 in 20.0 overs)
        Innings inn1 = new Innings(1, "AUS", "IND");
        inn1.setTotalRuns(188);
        inn1.setWickets(5);
        inn1.setLegalBalls(120);
        inn1.setCompleted(true);
        inn1.setExtras(new Extras(6, 2, 1, 2, 0));

        BatsmanStats aus1 = new BatsmanStats("bat-a1", "Travis Head");
        aus1.setRuns(48); aus1.setBalls(28); aus1.setFours(6); aus1.setSixes(2); aus1.setOut(true); aus1.setDismissal("c Kohli b Bumrah");
        BatsmanStats aus2 = new BatsmanStats("bat-a2", "David Warner");
        aus2.setRuns(22); aus2.setBalls(14); aus2.setFours(3); aus2.setSixes(1); aus2.setOut(true); aus2.setDismissal("b Arshdeep");
        BatsmanStats aus3 = new BatsmanStats("bat-a3", "Mitchell Marsh (c)");
        aus3.setRuns(54); aus3.setBalls(35); aus3.setFours(4); aus3.setSixes(3); aus3.setOut(true); aus3.setDismissal("c Suryakumar b Axar");
        BatsmanStats aus4 = new BatsmanStats("bat-a4", "Glenn Maxwell");
        aus4.setRuns(38); aus4.setBalls(21); aus4.setFours(3); aus4.setSixes(3); aus4.setOut(true); aus4.setDismissal("c Rinku b Bumrah");
        BatsmanStats aus5 = new BatsmanStats("bat-a5", "Marcus Stoinis");
        aus5.setRuns(15); aus5.setBalls(12); aus5.setFours(1); aus5.setSixes(1); aus5.setOut(false);
        BatsmanStats aus6 = new BatsmanStats("bat-a6", "Tim David");
        aus6.setRuns(9); aus6.setBalls(6); aus6.setFours(1); aus6.setSixes(0); aus6.setOut(false);

        inn1.getBatsmen().addAll(List.of(aus1, aus2, aus3, aus4, aus5, aus6));

        BowlerStats indB1 = new BowlerStats("ind-b1", "Jasprit Bumrah");
        indB1.setLegalBalls(24); indB1.setRunsConceded(26); indB1.setWickets(2); indB1.setMaidens(1);
        BowlerStats indB2 = new BowlerStats("ind-b2", "Arshdeep Singh");
        indB2.setLegalBalls(24); indB2.setRunsConceded(39); indB2.setWickets(1);
        BowlerStats indB3 = new BowlerStats("ind-b3", "Axar Patel");
        indB3.setLegalBalls(24); indB3.setRunsConceded(34); indB3.setWickets(1);
        BowlerStats indB4 = new BowlerStats("ind-b4", "Kuldeep Yadav");
        indB4.setLegalBalls(24); indB4.setRunsConceded(42); indB4.setWickets(0);
        BowlerStats indB5 = new BowlerStats("ind-b5", "Hardik Pandya");
        indB5.setLegalBalls(24); indB5.setRunsConceded(44); indB5.setWickets(1);

        inn1.getBowlers().addAll(List.of(indB1, indB2, indB3, indB4, indB5));
        inn1.getFallOfWickets().addAll(List.of("36/1 (Warner, 3.4 ov)", "92/2 (Head, 9.2 ov)", "148/3 (Marsh, 15.1 ov)", "171/4 (Maxwell, 18.2 ov)"));

        // 2nd Innings: India Chasing 189
        Innings inn2 = new Innings(2, "IND", "AUS");
        inn2.setTotalRuns(158);
        inn2.setWickets(4);
        inn2.setLegalBalls(102);
        inn2.setExtras(new Extras(4, 1, 0, 1, 0));

        BatsmanStats ind1 = new BatsmanStats("bat-i1", "Rohit Sharma (c)");
        ind1.setRuns(34); ind1.setBalls(19); ind1.setFours(4); ind1.setSixes(2); ind1.setOut(true); ind1.setDismissal("c Wade b Starc");
        BatsmanStats ind2 = new BatsmanStats("bat-i2", "Yashasvi Jaiswal");
        ind2.setRuns(28); ind2.setBalls(16); ind2.setFours(5); ind2.setSixes(1); ind2.setOut(true); ind2.setDismissal("c Warner b Hazlewood");
        BatsmanStats ind3 = new BatsmanStats("bat-i3", "Virat Kohli");
        ind3.setRuns(62); ind3.setBalls(41); ind3.setFours(5); ind3.setSixes(2); ind3.setOut(false); ind3.setBatting(true); ind3.setOnStrike(true);
        BatsmanStats ind4 = new BatsmanStats("bat-i4", "Suryakumar Yadav");
        ind4.setRuns(16); ind4.setBalls(11); ind4.setFours(2); ind4.setSixes(0); ind4.setOut(true); ind4.setDismissal("c Maxwell b Zampa");
        BatsmanStats ind5 = new BatsmanStats("bat-i5", "Hardik Pandya");
        ind5.setRuns(17); ind5.setBalls(9); ind5.setFours(1); ind5.setSixes(1); ind5.setOut(false); ind5.setBatting(true); ind5.setOnStrike(false);

        inn2.getBatsmen().addAll(List.of(ind1, ind2, ind3, ind4, ind5));
        inn2.setCurrentStrikerId(ind3.getId());
        inn2.setCurrentNonStrikerId(ind5.getId());

        BowlerStats ausB1 = new BowlerStats("aus-b1", "Mitchell Starc");
        ausB1.setLegalBalls(18); ausB1.setRunsConceded(32); ausB1.setWickets(1); ausB1.setCurrent(true);
        BowlerStats ausB2 = new BowlerStats("aus-b2", "Josh Hazlewood");
        ausB2.setLegalBalls(24); ausB2.setRunsConceded(35); ausB2.setWickets(1);
        BowlerStats ausB3 = new BowlerStats("aus-b3", "Pat Cummins");
        ausB3.setLegalBalls(24); ausB3.setRunsConceded(38); ausB3.setWickets(1);
        BowlerStats ausB4 = new BowlerStats("aus-b4", "Adam Zampa");
        ausB4.setLegalBalls(24); ausB4.setRunsConceded(31); ausB4.setWickets(1);
        BowlerStats ausB5 = new BowlerStats("aus-b5", "Marcus Stoinis");
        ausB5.setLegalBalls(12); ausB5.setRunsConceded(21); ausB5.setWickets(0);

        inn2.getBowlers().addAll(List.of(ausB1, ausB2, ausB3, ausB4, ausB5));
        inn2.setCurrentBowlerId(ausB1.getId());

        inn2.getFallOfWickets().addAll(List.of("52/1 (Jaiswal, 4.3 ov)", "68/2 (Rohit, 6.2 ov)", "119/3 (Suryakumar, 12.5 ov)"));

        List<BallEvent> recents = List.of(
                new BallEvent("b1", 2, 16, 1, "Virat Kohli", "Pat Cummins", 1, false, null, false, null, "1", "Kohli taps it towards deep point for a quick single.", "19:42:01"),
                new BallEvent("b2", 2, 16, 2, "Hardik Pandya", "Pat Cummins", 4, false, null, false, null, "4", "FOUR! Hardik punches it sweetly through extra cover.", "19:42:24"),
                new BallEvent("b3", 2, 16, 3, "Hardik Pandya", "Pat Cummins", 0, false, null, false, null, "0", "Beaten outside off stump as Cummins cuts it away.", "19:42:50"),
                new BallEvent("b4", 2, 16, 4, "Hardik Pandya", "Pat Cummins", 6, false, null, false, null, "6", "SIX! Into the stands! Pandya deposits the short ball into square leg!", "19:43:18"),
                new BallEvent("b5", 2, 16, 5, "Hardik Pandya", "Pat Cummins", 2, false, null, false, null, "2", "Worked away behind square, superb running for a couple.", "19:43:40"),
                new BallEvent("b6", 2, 16, 6, "Hardik Pandya", "Pat Cummins", 1, false, null, false, null, "1", "Driven down to long on, strike retained for Kohli as over ends.", "19:44:05")
        );
        inn2.getRecentBalls().addAll(recents);
        inn2.getAllBallEvents().addAll(recents);

        Partnership part = new Partnership("Virat Kohli", "Hardik Pandya");
        part.setRuns(39);
        part.setBalls(21);
        inn2.getPartnerships().add(part);

        match.getInnings().add(inn1);
        match.getInnings().add(inn2);
        match.setCurrentInningsIndex(1);

        matchStore.put(match.getId(), match);
        persistMatch(match);
    }

    private void createEngVsPakMatch() {
        Match match = new Match();
        match.setId("match-2");
        match.setTitle("England vs Pakistan - T20 Super Clash");
        match.setSeries("International T20 Championship 2026");
        match.setMatchType("T20");
        match.setVenue("Melbourne Cricket Ground, Melbourne");
        match.setStatus("LIVE");
        match.setTossDetails("Pakistan won the toss and elected to bat first");
        match.setMaxOvers(20);
        match.setTarget(166);
        match.setResultMessage("England need 54 runs in 40 balls");
        match.setAutoSimulating(false);

        Team pak = new Team("Pakistan", "PAK", "PK", "🇵🇰", "#01411C",
                List.of("Babar Azam (c)", "Mohammad Rizwan", "Fakhar Zaman", "Iftikhar Ahmed",
                        "Shadab Khan", "Imad Wasim", "Shaheen Afridi", "Naseem Shah",
                        "Haris Rauf", "Mohammad Amir", "Abrar Ahmed"));

        Team eng = new Team("England", "ENG", "EN", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "#CF081F",
                List.of("Jos Buttler (c)", "Phil Salt", "Will Jacks", "Jonny Bairstow",
                        "Harry Brook", "Liam Livingstone", "Moeen Ali", "Sam Curran",
                        "Jofra Archer", "Adil Rashid", "Reece Topley"));

        match.setTeam1(pak);
        match.setTeam2(eng);

        Innings inn1 = new Innings(1, "PAK", "ENG");
        inn1.setTotalRuns(165);
        inn1.setWickets(7);
        inn1.setLegalBalls(120);
        inn1.setCompleted(true);
        inn1.setExtras(new Extras(5, 1, 0, 1, 0));

        inn1.getBatsmen().add(new BatsmanStats("p1", "Babar Azam (c)"));
        inn1.getBatsmen().get(0).setRuns(52); inn1.getBatsmen().get(0).setBalls(42); inn1.getBatsmen().get(0).setOut(true); inn1.getBatsmen().get(0).setDismissal("b Archer");
        inn1.getBatsmen().add(new BatsmanStats("p2", "Mohammad Rizwan"));
        inn1.getBatsmen().get(1).setRuns(38); inn1.getBatsmen().get(1).setBalls(28); inn1.getBatsmen().get(1).setOut(true); inn1.getBatsmen().get(1).setDismissal("c Buttler b Rashid");

        BowlerStats engB1 = new BowlerStats("eb1", "Jofra Archer");
        engB1.setLegalBalls(24); engB1.setRunsConceded(24); engB1.setWickets(3);
        inn1.getBowlers().add(engB1);

        Innings inn2 = new Innings(2, "ENG", "PAK");
        inn2.setTotalRuns(112);
        inn2.setWickets(3);
        inn2.setLegalBalls(80);
        inn2.setExtras(new Extras(3, 0, 0, 1, 0));

        BatsmanStats eng1 = new BatsmanStats("e1", "Jos Buttler (c)");
        eng1.setRuns(46); eng1.setBalls(32); eng1.setBatting(true); eng1.setOnStrike(true);
        BatsmanStats eng2 = new BatsmanStats("e2", "Harry Brook");
        eng2.setRuns(24); eng2.setBalls(16); eng2.setBatting(true); eng2.setOnStrike(false);
        inn2.getBatsmen().addAll(List.of(eng1, eng2));
        inn2.setCurrentStrikerId(eng1.getId());
        inn2.setCurrentNonStrikerId(eng2.getId());

        BowlerStats pakB1 = new BowlerStats("pb1", "Shaheen Afridi");
        pakB1.setLegalBalls(20); pakB1.setRunsConceded(26); pakB1.setWickets(2); pakB1.setCurrent(true);
        inn2.getBowlers().add(pakB1);
        inn2.setCurrentBowlerId(pakB1.getId());

        match.getInnings().add(inn1);
        match.getInnings().add(inn2);
        match.setCurrentInningsIndex(1);

        matchStore.put(match.getId(), match);
        persistMatch(match);
    }

    private void createCskVsMiMatch() {
        Match match = new Match();
        match.setId("match-3");
        match.setTitle("Chennai Super Kings vs Mumbai Indians");
        match.setSeries("Indian Premier League 2026");
        match.setMatchType("T20");
        match.setVenue("MA Chidambaram Stadium, Chepauk");
        match.setStatus("LIVE");
        match.setTossDetails("CSK won the toss and elected to bat first");
        match.setMaxOvers(20);
        match.setTarget(null);
        match.setResultMessage("CSK 142/2 (15.0 ov) - Projected: 198");
        match.setAutoSimulating(false);

        Team csk = new Team("Chennai Super Kings", "CSK", "CSK", "🦁", "#F9CD05",
                List.of("Ruturaj Gaikwad (c)", "Rachin Ravindra", "Shivam Dube", "MS Dhoni (wk)",
                        "Ravindra Jadeja", "Daryl Mitchell", "Sameer Rizvi", "Shardul Thakur",
                        "Deepak Chahar", "Mustafizur Rahman", "Matheesha Pathirana"));

        Team mi = new Team("Mumbai Indians", "MI", "MI", "💙", "#004BA0",
                List.of("Rohit Sharma", "Ishan Kishan (wk)", "Suryakumar Yadav", "Hardik Pandya (c)",
                        "Tilak Varma", "Tim David", "Romario Shepherd", "Gerald Coetzee",
                        "Jasprit Bumrah", "Piyush Chawla", "Akash Madhwal"));

        match.setTeam1(csk);
        match.setTeam2(mi);

        Innings inn1 = new Innings(1, "CSK", "MI");
        inn1.setTotalRuns(142);
        inn1.setWickets(2);
        inn1.setLegalBalls(90);
        inn1.setCompleted(false);
        inn1.setExtras(new Extras(4, 1, 0, 0, 0));

        BatsmanStats csk1 = new BatsmanStats("c1", "Ruturaj Gaikwad (c)");
        csk1.setRuns(68); csk1.setBalls(44); csk1.setFours(7); csk1.setSixes(3); csk1.setBatting(true); csk1.setOnStrike(true);
        BatsmanStats csk2 = new BatsmanStats("c2", "Shivam Dube");
        csk2.setRuns(41); csk2.setBalls(22); csk2.setFours(2); csk2.setSixes(4); csk2.setBatting(true); csk2.setOnStrike(false);
        inn1.getBatsmen().addAll(List.of(csk1, csk2));
        inn1.setCurrentStrikerId(csk1.getId());
        inn1.setCurrentNonStrikerId(csk2.getId());

        BowlerStats miB1 = new BowlerStats("mb1", "Jasprit Bumrah");
        miB1.setLegalBalls(18); miB1.setRunsConceded(16); miB1.setWickets(1); miB1.setCurrent(true);
        inn1.getBowlers().add(miB1);
        inn1.setCurrentBowlerId(miB1.getId());

        match.getInnings().add(inn1);
        match.setCurrentInningsIndex(0);

        matchStore.put(match.getId(), match);
        persistMatch(match);
    }
}
