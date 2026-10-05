// Cricket Score Management - Frontend Application Logic

let currentMatchId = "match-1";
let currentMatchData = null;
let eventSource = null;
let pollInterval = null;
let isAutoSimulating = false;
let teamsCache = [];

document.addEventListener("DOMContentLoaded", () => {
  initTabs();
  loadMatchList();
  setupEventListeners();
  setupModalAndForms();
});

// Tab switching logic
function initTabs() {
  const tabs = document.querySelectorAll(".tab-btn");
  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      tabs.forEach(t => t.classList.remove("active"));
      document.querySelectorAll(".tab-pane").forEach(p => p.classList.remove("active"));

      tab.classList.add("active");
      const targetPane = document.getElementById(tab.dataset.tab);
      if (targetPane) targetPane.classList.add("active");

      if (tab.dataset.tab === "summary-tab") {
        loadMatchSummary();
      } else if (tab.dataset.tab === "teams-tab") {
        loadTeamsAndPlayers();
      }
    });
  });
}

// Fetch all matches and render chips in header
async function loadMatchList() {
  try {
    const res = await fetch("/api/matches");
    if (!res.ok) throw new Error("Failed to load matches");
    const matches = await res.json();

    const bar = document.getElementById("matches-bar");
    bar.innerHTML = "";

    matches.forEach(m => {
      const chip = document.createElement("div");
      chip.className = `match-chip ${m.id === currentMatchId ? "active" : ""}`;
      chip.id = `chip-${m.id}`;
      chip.innerHTML = `
        <span class="chip-status" style="background-color: ${m.status === 'LIVE' ? 'var(--accent-green)' : (m.status === 'COMPLETED' ? 'var(--text-dim)' : 'var(--accent-gold)')}"></span>
        <span>${m.team1.shortName} vs ${m.team2.shortName}</span>
      `;
      chip.addEventListener("click", () => switchMatch(m.id));
      bar.appendChild(chip);
    });

    if (matches.length > 0) {
      if (!matches.some(m => m.id === currentMatchId)) {
        currentMatchId = matches[0].id;
      }
      loadMatchDetails(currentMatchId);
      connectSse(currentMatchId);
    }
  } catch (err) {
    console.error("Error loading match list:", err);
  }
}

// Switch current match
function switchMatch(matchId) {
  if (currentMatchId === matchId && currentMatchData) return;
  currentMatchId = matchId;

  document.querySelectorAll(".match-chip").forEach(c => c.classList.remove("active"));
  const activeChip = document.getElementById(`chip-${matchId}`);
  if (activeChip) activeChip.classList.add("active");

  loadMatchDetails(matchId);
  connectSse(matchId);
}

// Load match details
async function loadMatchDetails(matchId) {
  try {
    const res = await fetch(`/api/matches/${matchId}`);
    if (!res.ok) throw new Error("Match not found");
    const data = await res.json();
    renderMatch(data);
  } catch (err) {
    console.error("Error fetching match details:", err);
  }
}

// Server-Sent Events (SSE) Real-Time Connection
function connectSse(matchId) {
  if (eventSource) {
    eventSource.close();
  }
  if (pollInterval) {
    clearInterval(pollInterval);
  }

  try {
    eventSource = new EventSource(`/api/matches/${matchId}/stream`);

    eventSource.addEventListener("match-update", (event) => {
      const data = JSON.parse(event.data);
      if (data.id === currentMatchId) {
        renderMatch(data);
      }
    });

    eventSource.onerror = (err) => {
      console.warn("SSE disconnected, activating fallback polling", err);
      eventSource.close();
      startPolling(matchId);
    };
  } catch (e) {
    startPolling(matchId);
  }
}

// Fallback Polling if SSE isn't supported or dropped
function startPolling(matchId) {
  if (pollInterval) clearInterval(pollInterval);
  pollInterval = setInterval(() => {
    loadMatchDetails(matchId);
  }, 2500);
}

// Render complete Match state into UI
function renderMatch(match) {
  currentMatchData = match;
  isAutoSimulating = match.autoSimulating;

  // Title and Meta
  document.getElementById("match-title").textContent = match.title;
  document.getElementById("match-venue").textContent = `${match.series} • ${match.venue}`;
  
  const statusBadge = document.getElementById("match-status-badge");
  statusBadge.textContent = match.status;
  if (match.status === "COMPLETED") {
    statusBadge.style.color = "var(--text-muted)";
  } else if (match.status === "PAUSED") {
    statusBadge.style.color = "var(--accent-gold)";
  } else {
    statusBadge.style.color = "var(--accent-green)";
  }

  // Teams Banner
  document.getElementById("team1-flag").textContent = match.team1.flagEmoji || "🏏";
  document.getElementById("team1-name").textContent = match.team1.name;
  document.getElementById("team1-code").textContent = match.team1.shortName;

  document.getElementById("team2-flag").textContent = match.team2.flagEmoji || "🏏";
  document.getElementById("team2-name").textContent = match.team2.name;
  document.getElementById("team2-code").textContent = match.team2.shortName;

  // Innings Scores
  const inn1 = match.innings && match.innings.length > 0 ? match.innings[0] : null;
  const inn2 = match.innings && match.innings.length > 1 ? match.innings[1] : null;

  if (inn1) {
    document.getElementById("team1-score").textContent = `${inn1.totalRuns}/${inn1.wickets}`;
    document.getElementById("team1-overs").textContent = `(${inn1.oversDisplay} ov)`;
  } else {
    document.getElementById("team1-score").textContent = "Yet to bat";
    document.getElementById("team1-overs").textContent = "";
  }

  if (inn2) {
    document.getElementById("team2-score").textContent = `${inn2.totalRuns}/${inn2.wickets}`;
    document.getElementById("team2-overs").textContent = `(${inn2.oversDisplay} ov)`;
  } else {
    document.getElementById("team2-score").textContent = "Yet to bat";
    document.getElementById("team2-overs").textContent = "";
  }

  // Match Equation / Result Banner
  document.getElementById("match-equation").textContent = match.resultMessage || "Match in progress";

  // Current Active Innings
  const currentInnings = match.currentInnings;
  if (currentInnings) {
    document.getElementById("metric-crr").textContent = currentInnings.runRate;

    const rrrBox = document.getElementById("metric-rrr-box");
    const targetBox = document.getElementById("metric-target-box");
    const neededBox = document.getElementById("metric-needed-box");

    if (match.target != null && match.currentInningsIndex === 1) {
      rrrBox.style.display = "flex";
      targetBox.style.display = "flex";
      neededBox.style.display = "flex";

      document.getElementById("metric-rrr").textContent = match.requiredRunRate != null ? match.requiredRunRate : "N/A";
      document.getElementById("metric-target").textContent = match.target;
      document.getElementById("metric-needed").textContent = `${match.runsNeeded} in ${match.ballsRemaining}b`;
    } else {
      rrrBox.style.display = "none";
      targetBox.style.display = "none";
      neededBox.style.display = "none";
    }

    renderBallReel(currentInnings.recentBalls || []);
    renderActiveBatsmen(currentInnings);
    renderActiveBowler(currentInnings);
    renderCommentary(currentInnings.allBallEvents || []);
  }

  renderScorecards(match);
  updateSimButtons(isAutoSimulating);
}

// Render the 12-ball over ticker
function renderBallReel(balls) {
  const reel = document.getElementById("balls-reel");
  reel.innerHTML = "";

  if (balls.length === 0) {
    reel.innerHTML = `<span style="color:var(--text-dim); font-size:0.8rem;">Awaiting first delivery...</span>`;
    return;
  }

  balls.forEach(b => {
    const ballBubble = document.createElement("div");
    ballBubble.className = `ball ${getBallClass(b)}`;
    ballBubble.textContent = b.display;
    ballBubble.title = `${b.striker} vs ${b.bowler} - ${b.commentary || ''}`;
    reel.appendChild(ballBubble);
  });
}

function getBallClass(b) {
  if (b.isWicket) return "ball-wicket";
  if (b.isExtra) return "ball-extra";
  if (b.runs === 6) return "ball-6";
  if (b.runs === 4) return "ball-4";
  if (b.runs > 0) return "ball-run";
  return "ball-0";
}

// Render active striker & non-striker
function renderActiveBatsmen(innings) {
  const container = document.getElementById("active-batsmen-list");
  container.innerHTML = "";

  const striker = innings.striker;
  const nonStriker = innings.nonStriker;

  const batsmenToRender = [striker, nonStriker].filter(Boolean);

  if (batsmenToRender.length === 0) {
    container.innerHTML = `<div style="color:var(--text-dim); font-size:0.85rem; padding:0.5rem 0;">No active batsmen</div>`;
  } else {
    batsmenToRender.forEach(b => {
      const row = document.createElement("div");
      row.className = "batsman-row";
      row.innerHTML = `
        <div class="batsman-name">
          ${b.onStrike ? '<span class="strike-marker">🏏</span>' : ''}
          <span style="font-weight: 600;">${b.name}</span>
          ${b.onStrike ? '<span style="font-size:0.7rem; color:var(--accent-cyan); font-weight:700;">*</span>' : ''}
        </div>
        <div class="batsman-stats">
          <strong>${b.runs}</strong>
          <span class="sub">(${b.balls})</span>
          <span class="sub">${b.fours}x4</span>
          <span class="sub">${b.sixes}x6</span>
          <span class="sr">${b.strikeRate}</span>
        </div>
      `;
      container.appendChild(row);
    });
  }

  // Partnership
  const pBox = document.getElementById("partnership-info");
  const p = innings.currentPartnership;
  if (p) {
    pBox.innerHTML = `
      <span>🤝 Partnership (${p.batsman1} & ${p.batsman2})</span>
      <strong>${p.runs} runs (${p.balls} balls)</strong>
    `;
  } else {
    pBox.innerHTML = `<span>🤝 Partnership</span><strong>0 runs (0 balls)</strong>`;
  }
}

// Render current bowler
function renderActiveBowler(innings) {
  const container = document.getElementById("active-bowler-container");
  container.innerHTML = "";

  const bowler = innings.currentBowler;
  if (!bowler) {
    container.innerHTML = `<div style="color:var(--text-dim); font-size:0.85rem; padding:0.5rem 0;">No active bowler</div>`;
    return;
  }

  const row = document.createElement("div");
  row.className = "bowler-row";
  row.innerHTML = `
    <div class="bowler-name">
      <span style="font-weight: 600;">${bowler.name}</span>
      <span class="overs-count">(${bowler.oversDisplay} ov)</span>
    </div>
    <div class="bowler-stats">
      <span style="color:var(--accent-red); font-weight:700;">${bowler.wickets}</span>
      <span>-</span>
      <span>${bowler.runsConceded}</span>
      <span class="econ">Econ: ${bowler.economyRate}</span>
    </div>
  `;
  container.appendChild(row);
}

// Render chronological commentary
function renderCommentary(events) {
  const container = document.getElementById("commentary-container");
  container.innerHTML = "";

  if (!events || events.length === 0) {
    container.innerHTML = `<p style="color:var(--text-dim); text-align:center; padding:2rem;">No ball commentary recorded yet.</p>`;
    return;
  }

  events.slice(0, 30).forEach(ev => {
    const item = document.createElement("div");
    item.className = "commentary-item";

    item.innerHTML = `
      <div class="comm-over">${ev.overNumber}.${ev.ballInOver}</div>
      <div class="comm-ball ${getBallClass(ev)}">${ev.display}</div>
      <div class="comm-body">
        <div class="comm-text">${ev.commentary}</div>
        <div class="comm-meta">${ev.bowler} to ${ev.striker} • ${ev.timestamp}</div>
      </div>
    `;
    container.appendChild(item);
  });
}

// Render full detailed scorecards
function renderScorecards(match) {
  const container = document.getElementById("scorecard-container");
  container.innerHTML = "";

  if (!match.innings || match.innings.length === 0) {
    container.innerHTML = `<p style="color:var(--text-dim); padding:2rem; text-align:center;">Scorecard details not yet generated.</p>`;
    return;
  }

  match.innings.forEach(inn => {
    const card = document.createElement("div");
    card.className = "hud-card";
    card.style.marginBottom = "1.5rem";

    let batsmenRows = "";
    inn.batsmen.forEach(b => {
      batsmenRows += `
        <tr>
          <td>
            <strong>${b.name}</strong>
            <div style="font-size:0.75rem; color:var(--text-dim);">${b.dismissal || (b.batting ? "batting" : "not out")}</div>
          </td>
          <td class="text-right"><strong>${b.runs}</strong></td>
          <td class="text-right">${b.balls}</td>
          <td class="text-right">${b.fours}</td>
          <td class="text-right">${b.sixes}</td>
          <td class="text-right" style="color:var(--accent-cyan); font-weight:600;">${b.strikeRate}</td>
        </tr>
      `;
    });

    let bowlerRows = "";
    inn.bowlers.forEach(bw => {
      bowlerRows += `
        <tr>
          <td><strong>${bw.name}</strong></td>
          <td class="text-right">${bw.oversDisplay}</td>
          <td class="text-right">${bw.maidens}</td>
          <td class="text-right">${bw.runsConceded}</td>
          <td class="text-right"><strong style="color:var(--accent-red);">${bw.wickets}</strong></td>
          <td class="text-right" style="color:var(--accent-gold);">${bw.economyRate}</td>
        </tr>
      `;
    });

    let extrasText = "0";
    if (inn.extras) {
      extrasText = `${inn.extras.total} (w ${inn.extras.wides}, nb ${inn.extras.noBalls}, b ${inn.extras.byes}, lb ${inn.extras.legByes})`;
    }

    let fowText = inn.fallOfWickets && inn.fallOfWickets.length > 0 ? inn.fallOfWickets.join(", ") : "None";

    card.innerHTML = `
      <div class="hud-card-header">
        <h3>${inn.battingTeam} Innings</h3>
        <span style="font-weight:700; color:var(--accent-cyan); font-size:1.1rem;">
          ${inn.totalRuns}/${inn.wickets} (${inn.oversDisplay} ov)
        </span>
      </div>

      <div style="overflow-x:auto;">
        <table class="scorecard-table">
          <thead>
            <tr>
              <th style="min-width: 160px;">Batter</th>
              <th class="text-right">R</th>
              <th class="text-right">B</th>
              <th class="text-right">4s</th>
              <th class="text-right">6s</th>
              <th class="text-right">SR</th>
            </tr>
          </thead>
          <tbody>
            ${batsmenRows}
          </tbody>
        </table>
      </div>

      <div class="scorecard-summary-strip">
        <span><strong>Extras:</strong> ${extrasText}</span>
        <span><strong>Total:</strong> ${inn.totalRuns}/${inn.wickets} (CRR: ${inn.runRate})</span>
      </div>

      <div class="scorecard-fow-strip">
        <strong>Fall of Wickets:</strong> ${fowText}
      </div>

      <div style="overflow-x:auto; margin-top:0.5rem;">
        <table class="scorecard-table">
          <thead>
            <tr>
              <th style="min-width: 160px;">Bowler</th>
              <th class="text-right">O</th>
              <th class="text-right">M</th>
              <th class="text-right">R</th>
              <th class="text-right">W</th>
              <th class="text-right">ECON</th>
            </tr>
          </thead>
          <tbody>
            ${bowlerRows}
          </tbody>
        </table>
      </div>
    `;
    container.appendChild(card);
  });
}

// Load Post-Match Analytical Summary
async function loadMatchSummary() {
  try {
    const res = await fetch(`/api/matches/${currentMatchId}/summary`);
    if (!res.ok) return;
    const summary = await res.json();

    document.getElementById("sum-winner").textContent = summary.result || "Match in progress";
    document.getElementById("sum-potm").textContent = summary.playerOfTheMatch || "TBD";
    document.getElementById("sum-top-scorer").textContent = summary.topScorer || "N/A";
    document.getElementById("sum-best-bowler").textContent = summary.bestBowler || "N/A";

    const highlightsList = document.getElementById("highlights-list");
    highlightsList.innerHTML = "";
    if (summary.highlights) {
      summary.highlights.forEach(h => {
        const li = document.createElement("li");
        li.textContent = h;
        highlightsList.appendChild(li);
      });
    }
  } catch (e) {
    console.error("Error loading summary:", e);
  }
}

// Load Teams & Squads (Tab 5)
async function loadTeamsAndPlayers() {
  try {
    const res = await fetch("/api/teams");
    if (!res.ok) throw new Error("Failed to fetch teams");
    const teams = await res.json();
    teamsCache = teams;

    // Populate Teams Grid
    const grid = document.getElementById("teams-grid");
    grid.innerHTML = "";

    const teamSelect = document.getElementById("new-player-team-select");
    teamSelect.innerHTML = "";

    teams.forEach(t => {
      // Add option to select dropdown
      const opt = document.createElement("option");
      opt.value = t.id;
      opt.textContent = `${t.flag || '🏏'} ${t.name} (${t.shortName})`;
      teamSelect.appendChild(opt);

      // Render Team Card
      const card = document.createElement("div");
      card.className = "team-card";

      let playersHtml = "";
      if (t.players && t.players.length > 0) {
        t.players.forEach(p => {
          const roleClass = `role-${(p.role || 'all_rounder').toLowerCase()}`;
          playersHtml += `
            <div class="player-item-row">
              <div>
                <strong>${p.name}</strong>
                <div style="font-size:0.7rem; color:var(--text-dim);">${p.battingStyle || ''} • ${p.bowlingStyle || ''}</div>
              </div>
              <span class="player-role-badge ${roleClass}">${p.role || 'ALL_ROUNDER'}</span>
            </div>
          `;
        });
      } else {
        playersHtml = `<p style="font-size:0.8rem; color:var(--text-dim); padding:0.5rem 0;">No players in squad</p>`;
      }

      card.innerHTML = `
        <div class="team-card-header">
          <div class="team-card-flag">${t.flag || '🏏'}</div>
          <div class="team-card-title">
            <h4>${t.name}</h4>
            <span>${t.shortName} • ${t.players ? t.players.length : 0} Players</span>
          </div>
        </div>
        <div class="squad-roster-list">
          ${playersHtml}
        </div>
      `;
      grid.appendChild(card);
    });
  } catch (err) {
    console.error("Error loading teams:", err);
  }
}

// Setup Event Listeners for Controls
function setupEventListeners() {
  // Step Ball Button
  document.getElementById("btn-step-ball").addEventListener("click", () => {
    simulateSingleBall();
  });

  // Auto Simulation Toggle
  document.getElementById("btn-toggle-auto").addEventListener("click", () => {
    toggleAutoSim();
  });

  // Reset Match Button
  document.getElementById("btn-reset-match").addEventListener("click", () => {
    resetMatch();
  });

  // Match Status Buttons
  document.getElementById("btn-status-live").addEventListener("click", () => updateMatchStatus("LIVE"));
  document.getElementById("btn-status-paused").addEventListener("click", () => updateMatchStatus("PAUSED"));
  document.getElementById("btn-status-completed").addEventListener("click", () => updateMatchStatus("COMPLETED"));
  document.getElementById("btn-delete-match").addEventListener("click", () => deleteCurrentMatch());

  // Manual Quick Scoring Buttons
  document.querySelectorAll(".score-btn[data-run]").forEach(btn => {
    btn.addEventListener("click", () => {
      const runs = parseInt(btn.dataset.run);
      submitManualScore({ runs, isWicket: false, isExtra: false });
    });
  });

  document.querySelectorAll(".score-btn[data-wicket]").forEach(btn => {
    btn.addEventListener("click", () => {
      const wicketType = btn.dataset.wicket;
      submitManualScore({ runs: 0, isWicket: true, wicketType, isExtra: false });
    });
  });

  document.querySelectorAll(".score-btn[data-extra]").forEach(btn => {
    btn.addEventListener("click", () => {
      const extraType = btn.dataset.extra;
      submitManualScore({ runs: 1, isWicket: false, isExtra: true, extraType });
    });
  });

  // Custom commentary form submit
  document.getElementById("btn-custom-score").addEventListener("click", () => {
    const runs = parseInt(document.getElementById("custom-runs").value) || 0;
    const commentary = document.getElementById("custom-commentary").value;
    submitManualScore({ runs, isWicket: false, isExtra: false, customCommentary: commentary });
    document.getElementById("custom-commentary").value = "";
  });
}

// Setup Modals and Secondary Forms
function setupModalAndForms() {
  const modal = document.getElementById("create-match-modal");
  const btnOpenModal = document.getElementById("btn-open-create-match");
  const btnCloseModal = document.getElementById("btn-close-modal");
  const btnCancelModal = document.getElementById("btn-cancel-create-match");

  btnOpenModal.addEventListener("click", () => {
    modal.style.display = "flex";
  });

  const closeModal = () => {
    modal.style.display = "none";
  };

  btnCloseModal.addEventListener("click", closeModal);
  btnCancelModal.addEventListener("click", closeModal);
  modal.addEventListener("click", (e) => {
    if (e.target === modal) closeModal();
  });

  // Form: Create Match
  document.getElementById("form-create-match").addEventListener("submit", async (e) => {
    e.preventDefault();
    const title = document.getElementById("match-input-title").value;
    const series = document.getElementById("match-input-series").value;
    const matchType = document.getElementById("match-input-type").value;
    const venue = document.getElementById("match-input-venue").value;
    const maxOvers = parseInt(document.getElementById("match-input-overs").value) || 20;
    const team1Name = document.getElementById("match-input-team1").value;
    const team2Name = document.getElementById("match-input-team2").value;
    const toss = document.getElementById("match-input-toss").value;

    try {
      const res = await fetch("/api/matches", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title,
          series,
          matchType,
          venue,
          maxOvers,
          team1Name,
          team2Name,
          tossDetails: toss
        })
      });

      if (!res.ok) throw new Error("Failed to create match");
      const newMatch = await res.json();
      closeModal();
      currentMatchId = newMatch.id;
      await loadMatchList();
      alert(`Match "${newMatch.title}" created successfully and saved to database!`);
    } catch (err) {
      alert("Error creating match: " + err.message);
    }
  });

  // Teams & Players form toggles
  const playerPanel = document.getElementById("add-player-panel");
  const teamPanel = document.getElementById("add-team-panel");

  document.getElementById("btn-toggle-add-player").addEventListener("click", () => {
    playerPanel.style.display = playerPanel.style.display === "none" ? "block" : "none";
    teamPanel.style.display = "none";
  });

  document.getElementById("btn-cancel-player").addEventListener("click", () => {
    playerPanel.style.display = "none";
  });

  document.getElementById("btn-toggle-add-team").addEventListener("click", () => {
    teamPanel.style.display = teamPanel.style.display === "none" ? "block" : "none";
    playerPanel.style.display = "none";
  });

  document.getElementById("btn-cancel-team").addEventListener("click", () => {
    teamPanel.style.display = "none";
  });

  // Form: Add Player
  document.getElementById("form-add-player").addEventListener("submit", async (e) => {
    e.preventDefault();
    const name = document.getElementById("new-player-name").value;
    const teamId = parseInt(document.getElementById("new-player-team-select").value);
    const role = document.getElementById("new-player-role").value;
    const battingStyle = document.getElementById("new-player-batting").value;

    try {
      const res = await fetch("/api/players", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, teamId, role, battingStyle })
      });
      if (!res.ok) throw new Error("Failed to add player");
      playerPanel.style.display = "none";
      document.getElementById("form-add-player").reset();
      loadTeamsAndPlayers();
      alert(`Player "${name}" added to squad successfully!`);
    } catch (err) {
      alert("Error adding player: " + err.message);
    }
  });

  // Form: Add Team
  document.getElementById("form-add-team").addEventListener("submit", async (e) => {
    e.preventDefault();
    const name = document.getElementById("new-team-name").value;
    const shortName = document.getElementById("new-team-short").value;
    const flag = document.getElementById("new-team-flag").value;
    const primaryColor = document.getElementById("new-team-color").value;

    try {
      const res = await fetch("/api/teams", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, shortName, flag, primaryColor, playerNames: [] })
      });
      if (!res.ok) throw new Error("Failed to add team");
      teamPanel.style.display = "none";
      document.getElementById("form-add-team").reset();
      loadTeamsAndPlayers();
      alert(`Team "${name}" registered successfully in database!`);
    } catch (err) {
      alert("Error adding team: " + err.message);
    }
  });
}

// Update Match Status
async function updateMatchStatus(status) {
  try {
    const res = await fetch(`/api/matches/${currentMatchId}/status?status=${status}`, { method: "PUT" });
    if (res.ok) {
      const match = await res.json();
      renderMatch(match);
      loadMatchList();
    }
  } catch (err) {
    console.error("Error updating match status:", err);
  }
}

// Delete Match
async function deleteCurrentMatch() {
  if (!confirm(`Are you sure you want to permanently delete match "${currentMatchData ? currentMatchData.title : currentMatchId}" from the database?`)) return;

  try {
    const res = await fetch(`/api/matches/${currentMatchId}`, { method: "DELETE" });
    if (res.ok) {
      alert("Match removed from database.");
      currentMatchId = "match-1";
      loadMatchList();
    } else {
      alert("Failed to delete match.");
    }
  } catch (err) {
    console.error("Error deleting match:", err);
  }
}

// Simulate single ball
async function simulateSingleBall() {
  try {
    const res = await fetch(`/api/matches/${currentMatchId}/simulate-ball`, { method: "POST" });
    if (res.ok) {
      const match = await res.json();
      renderMatch(match);
    }
  } catch (err) {
    console.error("Error simulating ball:", err);
  }
}

// Toggle Auto Simulation
async function toggleAutoSim() {
  const speed = parseInt(document.getElementById("sim-speed").value) || 2500;
  const newActiveState = !isAutoSimulating;

  try {
    const res = await fetch(`/api/matches/${currentMatchId}/auto-simulation`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ enabled: newActiveState, intervalMs: speed })
    });
    if (res.ok) {
      const match = await res.json();
      isAutoSimulating = match.autoSimulating;
      updateSimButtons(isAutoSimulating);
    }
  } catch (err) {
    console.error("Error toggling auto simulation:", err);
  }
}

// Reset Match
async function resetMatch() {
  if (!confirm("Reset match to initial state?")) return;
  try {
    const res = await fetch(`/api/matches/${currentMatchId}/reset`, { method: "POST" });
    if (res.ok) {
      const match = await res.json();
      renderMatch(match);
    }
  } catch (err) {
    console.error("Error resetting match:", err);
  }
}

// Submit Manual Score Update
async function submitManualScore(payload) {
  try {
    const res = await fetch(`/api/matches/${currentMatchId}/score-update`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    if (res.ok) {
      const match = await res.json();
      renderMatch(match);
    }
  } catch (err) {
    console.error("Error submitting score update:", err);
  }
}

function updateSimButtons(active) {
  const btn = document.getElementById("btn-toggle-auto");
  if (active) {
    btn.innerHTML = `<span>⏸ Pause Live Sim</span>`;
    btn.className = "btn btn-danger";
  } else {
    btn.innerHTML = `<span>▶ Start Live Sim</span>`;
    btn.className = "btn btn-success";
  }
}
