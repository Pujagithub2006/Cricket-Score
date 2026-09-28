# Real-Time Cricket Score Management System

A scalable and real-time cricket scoring platform built with Spring Boot 3, providing live match simulation, RESTful APIs, Server-Sent Events (SSE) streaming, and a responsive web dashboard.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [REST API Documentation](#rest-api-documentation)
- [Real-Time Streaming (SSE)](#real-time-streaming-sse)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation and Setup](#installation-and-setup)
- [Configuration](#configuration)
- [User Interface and Operations](#user-interface-and-operations)

---

## Overview

The Real-Time Cricket Score Management System simulates a live cricket scoring platform capable of managing concurrent matches, calculating dynamic run rates and target equations, updating player statistics, and broadcasting real-time ball-by-ball updates to connected clients without page reloads.

---

## Key Features

1. **Real-Time Score Engine**:
   - Manages match state across innings, overs, wickets, and strike rotations.
   - Calculates Current Run Rate (CRR), Required Run Rate (RRR), balls remaining, and chase equations.
   - Tracks detailed player statistics for batsmen (runs, balls, strike rates, dismissals) and bowlers (overs, maidens, runs conceded, wickets, economy).

2. **Automated Simulation Engine**:
   - Probability-weighted delivery generator reflecting real-world match situations (dot balls, singles, boundaries, maximums, wickets, and extras).
   - Dynamic aggression modeling during death overs and high-pressure run chases.
   - Configurable delivery intervals (1.2 seconds, 2.5 seconds, 4.0 seconds) for automated match progression.

3. **Manual Scoring Console**:
   - Administrative controls for custom delivery scoring: dots, runs (1 to 6), dismissals (bowled, caught, lbw, run out), and extras (wides, no-balls, leg-byes).
   - Editorial support for custom ball-by-ball commentary entry.
   - Match reset mechanism for testing and demonstration workflows.

4. **Server-Sent Events (SSE)**:
   - High-throughput, low-latency live event broadcasting.
   - Automatically pushes match updates to connected clients with graceful polling fallback.

5. **Analytical Dashboard**:
   - Live match summary with Player of the Match, top run-scorer, and best bowling figures.
   - Full scorecard tables covering batting, bowling, extras breakdown, and fall-of-wickets order.
   - Chronological commentary feed with delivery outcomes.

---

## System Architecture

```
+-------------------------------------------------------------+
|                     Client Browser                          |
|  - HTML5 / Vanilla CSS Dashboard                            |
|  - JavaScript EventSource Client (SSE / REST fallback)      |
+------------------------------+------------------------------+
                               |
               HTTP Requests   |   SSE Updates
               (REST APIs)     |   (text/event-stream)
                               v
+-------------------------------------------------------------+
|               Spring Boot Application Layer                 |
|                                                             |
|   +-----------------------------------------------------+   |
|   | MatchRestController                                 |   |
|   | - /api/matches                                      |   |
|   | - /api/matches/{id}/stream                          |   |
|   +--------------------------+--------------------------+   |
|                              |                              |
|   +--------------------------v--------------------------+   |
|   | CricketMatchService       | CricketSimulationEngine |   |
|   | - Domain Rules            | - Scheduled Executor    |   |
|   | - Innings State           | - Weighted Generator    |   |
|   +--------------------------+--------------------------+   |
|                              |                              |
|   +--------------------------v--------------------------+   |
|   | SseEmitterService                                   |   |
|   | - Concurrent Emitter Pool                           |   |
|   | - Push Event Dispatcher                             |   |
|   +-----------------------------------------------------+   |
+-------------------------------------------------------------+
```

---

## Technology Stack

- **Backend Framework**: Spring Boot 3.3.4
- **Language**: Java 17+ (tested with Java 17, 21, and 25)
- **Build Tool**: Apache Maven 3.9+
- **Embedded Web Server**: Apache Tomcat 10.1
- **Real-Time Communication**: Server-Sent Events (SSE)
- **Frontend**: Semantic HTML5, Vanilla CSS3 (Custom Design System), JavaScript (ES6+)

---

## REST API Documentation

### Base URL: `/api/matches`

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/matches` | Retrieve all active and completed matches. |
| `GET` | `/api/matches/{id}` | Retrieve complete match data, current innings, and scorecard. |
| `GET` | `/api/matches/{id}/summary` | Retrieve post-match analytical summary and awards. |
| `POST` | `/api/matches/{id}/simulate-ball` | Trigger a single simulated legal or extra delivery. |
| `POST` | `/api/matches/{id}/auto-simulation` | Enable or disable background live delivery simulation. |
| `POST` | `/api/matches/{id}/score-update` | Manually submit a custom ball outcome or wicket. |
| `POST` | `/api/matches/{id}/reset` | Reset a match back to its initial scenario state. |
| `GET` | `/api/matches/{id}/stream` | Establish an SSE persistent connection for real-time updates. |

### Request Payload Examples

#### 1. Toggle Auto-Simulation (`POST /api/matches/{id}/auto-simulation`)
```json
{
  "enabled": true,
  "intervalMs": 2500
}
```

#### 2. Manual Score Submission (`POST /api/matches/{id}/score-update`)
```json
{
  "runs": 4,
  "isWicket": false,
  "wicketType": null,
  "isExtra": false,
  "extraType": null,
  "customCommentary": "Driven firmly through the covers for four runs."
}
```

#### 3. Wicket Entry (`POST /api/matches/{id}/score-update`)
```json
{
  "runs": 0,
  "isWicket": true,
  "wicketType": "bowled",
  "isExtra": false,
  "extraType": null,
  "customCommentary": "Clean bowled! The off-stump is knocked out of the ground."
}
```

---

## Real-Time Streaming (SSE)

Clients subscribe to live score updates by establishing an EventSource connection:

```javascript
const eventSource = new EventSource('/api/matches/match-1/stream');

eventSource.addEventListener('match-update', (event) => {
  const match = JSON.parse(event.data);
  // Update scoreboard DOM elements
});
```

If an SSE connection is interrupted, the frontend client automatically transitions to a polling fallback every 2.5 seconds until connection recovery.

---

## Project Structure

```
9-Cricket-Score/
|-- pom.xml
|-- README.md
|-- .gitignore
`-- src/
    `-- main/
        |-- java/
        |   `-- com/
        |       `-- cricket/
        |           |-- CricketScoreApplication.java
        |           |-- controller/
        |           |   `-- MatchRestController.java
        |           |-- dto/
        |           |   |-- AutoSimRequest.java
        |           |   `-- ScoreUpdateRequest.java
        |           |-- model/
        |           |   |-- BallEvent.java
        |           |   |-- BatsmanStats.java
        |           |   |-- BowlerStats.java
        |           |   |-- Extras.java
        |           |   |-- Innings.java
        |           |   |-- Match.java
        |           |   |-- MatchSummary.java
        |           |   |-- Partnership.java
        |           |   `-- Team.java
        |           `-- service/
        |               |-- CricketMatchService.java
        |               |-- CricketSimulationEngine.java
        |               `-- SseEmitterService.java
        `-- resources/
            |-- application.properties
            `-- static/
                |-- index.html
                |-- css/
                |   `-- styles.css
                `-- js/
                    `-- app.js
```

---

## Prerequisites

- **Java Development Kit (JDK)**: 17 or higher
- **Apache Maven**: 3.8 or higher
- Modern web browser (Chrome, Edge, Firefox, or Safari)

---

## Installation and Setup

### 1. Clone the Repository
```bash
git clone https://github.com/Pujagithub2006/Cricket-Score.git
cd Cricket-Score
```

### 2. Build the Application
```bash
mvn clean package -DskipTests
```

### 3. Run the Application
Execute the packaged JAR:
```bash
java -jar target/cricket-score-management-1.0.0.jar
```

Alternatively, use the Spring Boot plugin:
```bash
mvn spring-boot:run
```

### 4. Access the Dashboard
Open your web browser and navigate to:
```
http://localhost:8085
```

---

## Configuration

Server properties can be customized in `src/main/resources/application.properties`:

```properties
# Server Port Configuration
server.port=8085

# Application Name
spring.application.name=CricketScoreManagementSystem

# Logging Levels
logging.level.com.cricket=INFO
```

---

## User Interface and Operations

1. **Match Selection**:
   - The top header contains match chips to switch between concurrent fixtures (e.g., India vs Australia, England vs Pakistan, Chennai Super Kings vs Mumbai Indians).

2. **Live Match Hero Panel**:
   - Displays current teams, scores, overs, run rates, target requirements, and the last 12 deliveries in the over reel.

3. **Active Players Display**:
   - Shows the batter on strike with an active strike marker, balls faced, boundary counts, and strike rate.
   - Shows current bowler figures, economy rate, and active partnership data.

4. **Dashboard Views**:
   - **Ball-by-Ball Feed**: Chronological stream of ball events and commentary.
   - **Full Scorecard**: Complete batting and bowling statistics, extras, and fall of wickets.
   - **Match Summary**: Post-match analysis, Player of the Match, top performers, and match highlights.
   - **Scoring Console**: Manual run and extra inputs, single ball stepping, and automated simulation triggers.
