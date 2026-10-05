package com.cricket.service;

import com.cricket.dto.CreatePlayerRequest;
import com.cricket.dto.CreateTeamRequest;
import com.cricket.entity.PlayerEntity;
import com.cricket.entity.TeamEntity;
import com.cricket.repository.PlayerRepository;
import com.cricket.repository.TeamRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public TeamService(TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    @PostConstruct
    public void seedInitialTeamsIfEmpty() {
        if (teamRepository.count() == 0) {
            initDefaultSquads();
        }
    }

    @Transactional
    public void initDefaultSquads() {
        // India
        TeamEntity ind = new TeamEntity("India", "IND", "IN", "🇮🇳", "#0078FF");
        List<PlayerEntity> indPlayers = List.of(
                new PlayerEntity("Rohit Sharma (c)", "BATSMAN", "Right-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Yashasvi Jaiswal", "BATSMAN", "Left-hand bat", "Legbreak"),
                new PlayerEntity("Virat Kohli", "BATSMAN", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Suryakumar Yadav", "BATSMAN", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Hardik Pandya", "ALL_ROUNDER", "Right-hand bat", "Right-arm fast-medium"),
                new PlayerEntity("Rinku Singh", "BATSMAN", "Left-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Axar Patel", "ALL_ROUNDER", "Left-hand bat", "Slow left-arm orthodox"),
                new PlayerEntity("Kuldeep Yadav", "BOWLER", "Left-hand bat", "Left-arm wrist-spin"),
                new PlayerEntity("Jasprit Bumrah", "BOWLER", "Right-hand bat", "Right-arm fast"),
                new PlayerEntity("Mohammed Siraj", "BOWLER", "Right-hand bat", "Right-arm fast"),
                new PlayerEntity("Arshdeep Singh", "BOWLER", "Left-hand bat", "Left-arm medium-fast")
        );
        indPlayers.forEach(ind::addPlayer);
        teamRepository.save(ind);

        // Australia
        TeamEntity aus = new TeamEntity("Australia", "AUS", "AU", "🇦🇺", "#FFD700");
        List<PlayerEntity> ausPlayers = List.of(
                new PlayerEntity("Travis Head", "BATSMAN", "Left-hand bat", "Right-arm offbreak"),
                new PlayerEntity("David Warner", "BATSMAN", "Left-hand bat", "Right-arm legbreak"),
                new PlayerEntity("Mitchell Marsh (c)", "ALL_ROUNDER", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Glenn Maxwell", "ALL_ROUNDER", "Right-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Marcus Stoinis", "ALL_ROUNDER", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Tim David", "BATSMAN", "Right-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Matthew Wade (wk)", "WICKET_KEEPER", "Left-hand bat", "Right-arm medium"),
                new PlayerEntity("Pat Cummins", "BOWLER", "Right-hand bat", "Right-arm fast"),
                new PlayerEntity("Mitchell Starc", "BOWLER", "Left-hand bat", "Left-arm fast"),
                new PlayerEntity("Adam Zampa", "BOWLER", "Right-hand bat", "Legbreak googly"),
                new PlayerEntity("Josh Hazlewood", "BOWLER", "Left-hand bat", "Right-arm fast-medium")
        );
        ausPlayers.forEach(aus::addPlayer);
        teamRepository.save(aus);

        // England
        TeamEntity eng = new TeamEntity("England", "ENG", "EN", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "#CF081F");
        List<PlayerEntity> engPlayers = List.of(
                new PlayerEntity("Jos Buttler (c)", "WICKET_KEEPER", "Right-hand bat", "None"),
                new PlayerEntity("Phil Salt", "BATSMAN", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Will Jacks", "ALL_ROUNDER", "Right-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Jonny Bairstow", "BATSMAN", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Harry Brook", "BATSMAN", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Liam Livingstone", "ALL_ROUNDER", "Right-hand bat", "Right-arm leg/off spin"),
                new PlayerEntity("Moeen Ali", "ALL_ROUNDER", "Left-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Sam Curran", "ALL_ROUNDER", "Left-hand bat", "Left-arm medium-fast"),
                new PlayerEntity("Jofra Archer", "BOWLER", "Right-hand bat", "Right-arm fast"),
                new PlayerEntity("Adil Rashid", "BOWLER", "Right-hand bat", "Legbreak googly"),
                new PlayerEntity("Reece Topley", "BOWLER", "Right-hand bat", "Left-arm fast-medium")
        );
        engPlayers.forEach(eng::addPlayer);
        teamRepository.save(eng);

        // Pakistan
        TeamEntity pak = new TeamEntity("Pakistan", "PAK", "PK", "🇵🇰", "#01411C");
        List<PlayerEntity> pakPlayers = List.of(
                new PlayerEntity("Babar Azam (c)", "BATSMAN", "Right-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Mohammad Rizwan (wk)", "WICKET_KEEPER", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Fakhar Zaman", "BATSMAN", "Left-hand bat", "Slow left-arm orthodox"),
                new PlayerEntity("Iftikhar Ahmed", "ALL_ROUNDER", "Right-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Shadab Khan", "ALL_ROUNDER", "Right-hand bat", "Legbreak googly"),
                new PlayerEntity("Imad Wasim", "ALL_ROUNDER", "Left-hand bat", "Slow left-arm orthodox"),
                new PlayerEntity("Shaheen Afridi", "BOWLER", "Left-hand bat", "Left-arm fast"),
                new PlayerEntity("Naseem Shah", "BOWLER", "Right-hand bat", "Right-arm fast"),
                new PlayerEntity("Haris Rauf", "BOWLER", "Right-hand bat", "Right-arm fast"),
                new PlayerEntity("Mohammad Amir", "BOWLER", "Left-hand bat", "Left-arm fast"),
                new PlayerEntity("Abrar Ahmed", "BOWLER", "Right-hand bat", "Legbreak googly")
        );
        pakPlayers.forEach(pak::addPlayer);
        teamRepository.save(pak);

        // Chennai Super Kings
        TeamEntity csk = new TeamEntity("Chennai Super Kings", "CSK", "CSK", "🦁", "#F9CD05");
        List<PlayerEntity> cskPlayers = List.of(
                new PlayerEntity("Ruturaj Gaikwad (c)", "BATSMAN", "Right-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Rachin Ravindra", "ALL_ROUNDER", "Left-hand bat", "Slow left-arm orthodox"),
                new PlayerEntity("Shivam Dube", "ALL_ROUNDER", "Left-hand bat", "Right-arm medium"),
                new PlayerEntity("MS Dhoni (wk)", "WICKET_KEEPER", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Ravindra Jadeja", "ALL_ROUNDER", "Left-hand bat", "Slow left-arm orthodox"),
                new PlayerEntity("Daryl Mitchell", "ALL_ROUNDER", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Sameer Rizvi", "BATSMAN", "Right-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Shardul Thakur", "BOWLER", "Right-hand bat", "Right-arm medium-fast"),
                new PlayerEntity("Deepak Chahar", "BOWLER", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Mustafizur Rahman", "BOWLER", "Left-hand bat", "Left-arm fast-medium"),
                new PlayerEntity("Matheesha Pathirana", "BOWLER", "Right-hand bat", "Right-arm fast")
        );
        cskPlayers.forEach(csk::addPlayer);
        teamRepository.save(csk);

        // Mumbai Indians
        TeamEntity mi = new TeamEntity("Mumbai Indians", "MI", "MI", "💙", "#004BA0");
        List<PlayerEntity> miPlayers = List.of(
                new PlayerEntity("Rohit Sharma", "BATSMAN", "Right-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Ishan Kishan (wk)", "WICKET_KEEPER", "Left-hand bat", "None"),
                new PlayerEntity("Suryakumar Yadav", "BATSMAN", "Right-hand bat", "Right-arm medium"),
                new PlayerEntity("Hardik Pandya (c)", "ALL_ROUNDER", "Right-hand bat", "Right-arm fast-medium"),
                new PlayerEntity("Tilak Varma", "BATSMAN", "Left-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Tim David", "BATSMAN", "Right-hand bat", "Right-arm offbreak"),
                new PlayerEntity("Romario Shepherd", "ALL_ROUNDER", "Right-hand bat", "Right-arm fast-medium"),
                new PlayerEntity("Gerald Coetzee", "BOWLER", "Right-hand bat", "Right-arm fast"),
                new PlayerEntity("Jasprit Bumrah", "BOWLER", "Right-hand bat", "Right-arm fast"),
                new PlayerEntity("Piyush Chawla", "BOWLER", "Left-hand bat", "Legbreak googly"),
                new PlayerEntity("Akash Madhwal", "BOWLER", "Right-hand bat", "Right-arm medium-fast")
        );
        miPlayers.forEach(mi::addPlayer);
        teamRepository.save(mi);
    }

    public List<TeamEntity> getAllTeams() {
        return teamRepository.findAll();
    }

    public Optional<TeamEntity> getTeamById(Long id) {
        return teamRepository.findById(id);
    }

    public Optional<TeamEntity> getTeamByName(String name) {
        return teamRepository.findByNameIgnoreCase(name);
    }

    @Transactional
    public TeamEntity createTeam(CreateTeamRequest request) {
        Optional<TeamEntity> existing = teamRepository.findByNameIgnoreCase(request.getName());
        if (existing.isPresent()) {
            return existing.get();
        }

        TeamEntity team = new TeamEntity(
                request.getName(),
                request.getShortName(),
                request.getCode() != null ? request.getCode() : request.getShortName(),
                request.getFlag() != null ? request.getFlag() : "🏏",
                request.getPrimaryColor() != null ? request.getPrimaryColor() : "#0078FF"
        );

        if (request.getPlayerNames() != null) {
            for (String pName : request.getPlayerNames()) {
                if (pName != null && !pName.trim().isEmpty()) {
                    PlayerEntity player = new PlayerEntity(pName.trim(), "ALL_ROUNDER", "Right-hand bat", "Right-arm medium");
                    team.addPlayer(player);
                }
            }
        }

        return teamRepository.save(team);
    }

    public List<PlayerEntity> getAllPlayers() {
        return playerRepository.findAll();
    }

    public List<PlayerEntity> getPlayersByTeamId(Long teamId) {
        return playerRepository.findByTeamId(teamId);
    }

    public Optional<PlayerEntity> getPlayerById(Long id) {
        return playerRepository.findById(id);
    }

    @Transactional
    public PlayerEntity createPlayer(CreatePlayerRequest request) {
        TeamEntity team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + request.getTeamId()));

        PlayerEntity player = new PlayerEntity(
                request.getName(),
                request.getRole() != null ? request.getRole() : "ALL_ROUNDER",
                request.getBattingStyle() != null ? request.getBattingStyle() : "Right-hand bat",
                request.getBowlingStyle() != null ? request.getBowlingStyle() : "Right-arm medium"
        );
        team.addPlayer(player);
        return playerRepository.save(player);
    }
}
