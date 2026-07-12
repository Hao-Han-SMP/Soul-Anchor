# Soul Anchor — Build & Run Guide

## Requirements

| Tool | Version | Check |
|---------|---------|----------|
| Java JDK | **21+** | `java -version` |
| Maven | **3.8+** | `mvn -version` |

> No Maven? Download at https://maven.apache.org/download.cgi
> or use an IDE (IntelliJ IDEA includes Maven).

---

## Step 1: Build Plugin

```bash
# Open terminal in Soul-Anchor/ directory
mvn clean package -DskipTests
```

**Output:** `target/Soul-Anchor-1.0-SNAPSHOT.jar`

---

## Step 2: Set Up Purpur Test Server

### 2a. Download Purpur
Download Purpur 1.21.x from: https://purpurmc.org/downloads

### 2b. Create server directory
```
C:\MC_TestServer\
├── purpur-1.21.x-xxx.jar
├── start.bat          ← create this file (see below)
└── plugins\
```

### 2c. Create start.bat file
```bat
@echo off
java -Xms512M -Xmx2G -jar purpur-1.21.x-xxx.jar nogui
pause
```
*(Update JAR name as needed)*

### 2d. First run (accept EULA)
```
double-click start.bat
```
Server will stop and create `eula.txt`. Edit it and change:
```
eula=false  →  eula=true
```

---

## Step 3: Deploy Plugin

```
copy target\Soul-Anchor-1.0-SNAPSHOT.jar  C:\MC_TestServer\plugins\
```

Run `start.bat` again. Server log should show:

```
[Soul-Anchor] === Soul Anchor ===
[Soul-Anchor] Loaded waypoint data.
[Soul-Anchor] TeleportEngine started.
[Soul-Anchor] Plugin enabled. Ready to teleport!
```

---

## Step 4: Test In-Game

Join the server (offline mode OK with `server.properties` → `online-mode=false`):

```
/soulanchor info
/soulanchor debug
/soulanchor list
/soulanchor reload
```

---

## Step 5: Create Custom Waypoint

Create a new waypoint at your current location:

```
/soulanchor set my_waypoint "My custom waypoint location"
```

Then teleport to it:

```
/soulanchor tp my_waypoint
```

Waypoints are saved in `plugins/Soul-Anchor/waypoints.json` and persist across restarts.

---

## Troubleshooting

| Error | Cause | Solution |
|-----|------------|-----------|
| `BUILD FAILURE` during `mvn package` | Missing Java 21 or Maven | Check `java -version` and `mvn -version` |
| Plugin fails to load | JAR in wrong directory | Must be in `plugins/` folder |
| Command not recognized | Mismatched plugin name | Check server log during startup |
| Waypoint not saved | Permission issue | Ensure player has `haohansmp.soulanchor.set` permission |
