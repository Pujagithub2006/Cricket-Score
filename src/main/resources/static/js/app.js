// Cricket Score Management - Frontend Application Logic

let currentMatchId = "match-1";
let currentMatchData = null;
let eventSource = null;
let pollInterval = null;
let isAutoSimulating = false;

document.addEventListener("DOMContentLoaded", () => {
  initTabs();
  loadMatchList();
  setupEventListeners();
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
        <span class="chip-status"></span>
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
  if (currentMatchId === matchId) return;
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
  document.getElementById("match-status-badge").textContent = match.status;

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
    document.getElementById("team1-score").textContent = `Yet to bat`;
    document.getElementById("team1-overs").textContent = ``;
  }

  if (inn2) {
    document.getElementById("team2-score").textContent = `${inn2.totalRuns}/${inn2.wickets}`;
    document.getElementById("team2-overs").textContent = `(${inn2.oversDisplay} ov)`;
  } else {
    document.getElementById("team2-score").textContent = match.innings.length === 1 ? `Yet to bat` : `0/0`;
    document.getElementById("team2-overs").textContent = ``;
  }

  // Match Equation & Toss
  document.getElementById("match-equation").textContent = match.resultMessage || match.tossDetails;

  // Current Innings Metrics
  const currInn = match.currentInningsIndex === 1 && inn2 ? inn2 : inn1;
  if (currInn) {
    document.getElementById("metric-crr").textContent = currInn.runRate.toFixed(2);
  } else {
    document.getElementById("metric-crr").textContent = "0.00";
  }

  if (match.requiredRunRate != null) {
    document.getElementById("metric-rrr-box").style.display = "flex";
    document.getElementById("metric-rrr").textContent = match.requiredRunRate.toFixed(2);
  } else {
    document.getElementById("metric-rrr-box").style.display = "none";
  }

  if (match.target != null) {
    document.getElementById("metric-target-box").style.display = "flex";
    document.getElementById("metric-target").textContent = match.target;
  } else {
    document.getElementById("metric-target-box").style.display = "none";
  }

  if (match.runsNeeded != null) {
    document.getElementById("metric-needed-box").style.display = "flex";
    document.getElementById("metric-needed").textContent = `${match.runsNeeded} in ${match.ballsRemaining}b`;
  } else {
    document.getElementById("metric-needed-box").style.display = "none";
  }

  // Over Ball Ticker
  renderBallTicker(currInn ? currInn.recentBalls : []);

  // Batsmen & Bowlers HUD
  renderPlayerHUD(currInn);

  // Tab 1: Commentary Feed
  renderCommentary(currInn ? currInn.allBallEvents : []);

  // Tab 2: Full Scorecard
  renderScorecard(match);

  // Auto-Sim Controls
  updateSimButtons(match.autoSimulating);
}

// Render ball bubbles
function renderBallTicker(recentBalls) {
  const container = document.getElementById("balls-reel");
  container.innerHTML = "";

  if (!recentBalls || recentBalls.length === 0) {
    container.innerHTML = `<span style="color:var(--text-dim);font-size:0.85rem;">No balls bowled yet</span>`;
    return;
  }

  recentBalls.forEach(ball => {
    const bubble = document.createElement("div");
    bubble.className = "ball-bubble";

    if (ball.wicket) {
      bubble.classList.add("wicket");
    } else if (ball.extra) {
      bubble.classList.add("extra");
    } else if (ball.runs === 6) {
      bubble.classList.add("run-6");
    } else if (ball.runs === 4) {
      bubble.classList.add("run-4");
    } else if (ball.runs > 0) {
      bubble.classList.add(`run-${ball.runs}`);
    } else {
      bubble.classList.add("run-0");
    }

    bubble.textContent = ball.display;
    bubble.title = `${ball.bowlerName} to ${ball.batsmanName}: ${ball.commentary}`;
    container.appendChild(bubble);
  });
}

// Render Batter & Bowler HUD
function renderPlayerHUD(innings) {
  const batsmenList = document.getElementById("active-batsmen-list");
  batsmenList.innerHTML = "";

  if (!innings || !innings.batsmen) return;

  const activeBatsmen = innings.batsmen.filter(b => b.batting && !b.out);

  if (activeBatsmen.length === 0) {
    batsmenList.innerHTML = `<div style="padding:0.5rem;color:var(--text-dim);">No active batsmen</div>`;
  } else {
    activeBatsmen.forEach(b => {
      const row = document.createElement("div");
      row.className = `batsman-row ${b.onStrike ? "on-strike" : ""}`;
      row.innerHTML = `
        <div class="player-name-col">
          ${b.onStrike ? `<span class="strike-badge">🏏</span>` : `<span style="width:14px;display:inline-block;"></span>`}
          <span class="player-name">${b.name}</span>
        </div>
        <div class="player-stats-cols">
          <span class="player-runs-main">${b.runs}</span>
          <span class="player-balls">(${b.balls})</span>
          <span style="color:var(--text-muted);font-size:0.8rem;">${b.fours}x4 • ${b.sixes}x6</span>
          <span class="player-sr">SR ${b.strikeRate.toFixed(1)}</span>
        </div>
      `;
      batsmenList.appendChild(row);
    });
  }

  // Bowler
  const bowlerContainer = document.getElementById("active-bowler-container");
  const currentBowler = innings.bowlers ? innings.bowlers.find(b => b.current) : null;

  if (currentBowler) {
    bowlerContainer.innerHTML = `
      <div class="bowler-highlight">
        <span class="player-name">🎯 ${currentBowler.name}</span>
        <div class="player-stats-cols">
          <span class="player-runs-main">${currentBowler.wickets}-${currentBowler.runsConceded}</span>
          <span class="player-balls">(${currentBowler.oversDisplay} ov)</span>
          <span class="player-sr">Econ ${currentBowler.economy.toFixed(1)}</span>
        </div>
      </div>
    `;
  } else {
    bowlerContainer.innerHTML = `<div style="padding:0.5rem;color:var(--text-dim);">No current bowler</div>`;
  }

  // Partnership
  const pBox = document.getElementById("partnership-info");
  const p = innings.partnerships && innings.partnerships.length > 0 ? innings.partnerships[innings.partnerships.length - 1] : null;
  if (p) {
    pBox.innerHTML = `<span>🤝 Current Partnership</span> <strong style="color:var(--accent-gold);">${p.runs} runs (${p.balls} balls)</strong>`;
  } else {
    pBox.innerHTML = `<span>🤝 Partnership</span> <strong>0 (0)</strong>`;
  }
}

// Render Commentary Feed
function renderCommentary(events) {
  const container = document.getElementById("commentary-container");
  container.innerHTML = "";

  if (!events || events.length === 0) {
    container.innerHTML = `<div style="color:var(--text-dim);padding:1rem;">Ball-by-ball commentary will appear here once the match begins.</div>`;
    return;
  }

  events.forEach(evt => {
    const item = document.createElement("div");
    item.className = "commentary-item";

    let badgeClass = "run-0";
    if (evt.wicket) badgeClass = "wicket";
    else if (evt.extra) badgeClass = "extra";
    else if (evt.runs === 6) badgeClass = "run-6";
    else if (evt.runs === 4) badgeClass = "run-4";
    else if (evt.runs > 0) badgeClass = `run-${evt.runs}`;

    item.innerHTML = `
      <div class="commentary-over">Ov ${evt.overNumber}.${evt.ballInOver}</div>
      <div class="commentary-text">
        <strong>${evt.bowlerName}</strong> to <strong>${evt.batsmanName}</strong>: ${evt.commentary}
      </div>
      <div>
        <div class="ball-bubble ${badgeClass}">${evt.display}</div>
      </div>
    `;
    container.appendChild(item);
  });
}

// Render Full Scorecard Tables
function renderScorecard(match) {
  const container = document.getElementById("scorecard-container");
  container.innerHTML = "";

  if (!match.innings || match.innings.length === 0) {
    container.innerHTML = `<p>No scorecard data available.</p>`;
    return;
  }

  match.innings.forEach(inn => {
    const section = document.createElement("div");
    section.className = "scorecard-section";

    let batsmenRows = "";
    inn.batsmen.forEach(b => {
      batsmenRows += `
        <tr>
          <td class="bold">${b.name}</td>
          <td style="color:var(--text-muted);">${b.out ? b.dismissal : (b.batting ? "Batting" : "Did not bat")}</td>
          <td class="text-right bold" style="color:var(--accent-gold);">${b.runs}</td>
          <td class="text-right">${b.balls}</td>
          <td class="text-right">${b.fours}</td>
          <td class="text-right">${b.sixes}</td>
          <td class="text-right" style="color:var(--accent-cyan);">${b.strikeRate.toFixed(1)}</td>
        </tr>
      `;
    });

    let bowlerRows = "";
    inn.bowlers.forEach(bw => {
      bowlerRows += `
        <tr>
          <td class="bold">${bw.name}</td>
          <td class="text-right">${bw.oversDisplay}</td>
          <td class="text-right">${bw.maidens}</td>
          <td class="text-right bold">${bw.runsConceded}</td>
          <td class="text-right bold" style="color:var(--accent-red);">${bw.wickets}</td>
          <td class="text-right" style="color:var(--accent-cyan);">${bw.economy.toFixed(1)}</td>
        </tr>
      `;
    });

    const fowText = inn.fallOfWickets && inn.fallOfWickets.length > 0
      ? inn.fallOfWickets.join(" • ")
      : "None";

    section.innerHTML = `
      <div class="scorecard-header">
        <h4>${inn.battingTeam} Innings</h4>
        <div style="font-size:1.15rem;font-weight:700;color:var(--accent-cyan);">
          ${inn.totalRuns}/${inn.wickets} <span style="font-size:0.9rem;color:var(--text-muted);">(${inn.oversDisplay} ov, RR ${inn.runRate.toFixed(2)})</span>
        </div>
      </div>

      <table class="score-table">
        <thead>
          <tr>
            <th>Batter</th>
            <th>Dismissal</th>
            <th class="text-right">R</th>
            <th class="text-right">B</th>
            <th class="text-right">4s</th>
            <th class="text-right">6s</th>
            <th class="text-right">SR</th>
          </tr>
        </thead>
        <tbody>${batsmenRows}</tbody>
      </table>

      <div class="extras-summary">
        <span>Extras: <strong>${inn.extras ? inn.extras.total : 0}</strong> (b ${inn.extras ? inn.extras.byes : 0}, lb ${inn.extras ? inn.extras.legByes : 0}, w ${inn.extras ? inn.extras.wides : 0}, nb ${inn.extras ? inn.extras.noBalls : 0})</span>
        <span>Total: <strong>${inn.totalRuns}/${inn.wickets}</strong> (${inn.oversDisplay} Overs)</span>
      </div>

      <div class="fow-list">
        <strong>Fall of Wickets:</strong> ${fowText}
      </div>

      <h4 style="margin: 1.25rem 0 0.75rem 0; font-size:0.95rem; text-transform:uppercase; color:var(--text-muted);">Bowling</h4>
      <table class="score-table">
        <thead>
          <tr>
            <th>Bowler</th>
            <th class="text-right">O</th>
            <th class="text-right">M</th>
            <th class="text-right">R</th>
            <th class="text-right">W</th>
            <th class="text-right">Econ</th>
          </tr>
        </thead>
        <tbody>${bowlerRows}</tbody>
      </table>
    `;

    container.appendChild(section);
  });
}

// Load Match Summary for Tab 3
async function loadMatchSummary() {
  try {
    const res = await fetch(`/api/matches/${currentMatchId}/summary`);
    if (!res.ok) return;
    const summary = await res.json();

    document.getElementById("sum-winner").textContent = summary.result;
    document.getElementById("sum-potm").textContent = summary.playerOfTheMatch || "TBD";
    document.getElementById("sum-top-scorer").textContent = summary.topScorer || "TBD";
    document.getElementById("sum-best-bowler").textContent = summary.bestBowler || "TBD";

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
  const speed = parseInt(document.getElementById("sim-speed").value) || 2000;
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
