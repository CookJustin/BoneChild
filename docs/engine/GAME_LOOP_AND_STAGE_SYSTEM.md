# BoneChild Game Loop & Stage/Wave System

## Overview
This document explains how BoneChildGame orchestrates the entire game, how stage/wave progression works, and what information flows where.

---

## 🎮 The Complete Game Loop

### High-Level Flow
```
┌─────────────────────────────────────────┐
│       BoneChildGame.render()            │
│  (Called ~60 times/second by LibGDX)    │
└───────────────┬─────────────────────────┘
                │
                ├─> Menu State (if !gameStarted)
                │   └─> menuScreen.update/render
                │
                └─> Gameplay State (if gameStarted)
                    │
                    ├─> Death Check
                    │   └─> Show GameOverScreen after 2s delay
                    │
                    ├─> UI Screen Priority Check
                    │   (GameOver > CharStats > PowerUp > BossWarning > Pause)
                    │   └─> Render frozen world + active UI screen
                    │
                    ├─> Level Up Check
                    │   └─> Show PowerUpScreen, pause game
                    │
                    ├─> Active Gameplay
                    │   ├─> handleInput()        // Route input to PlayerInput or UI
                    │   ├─> update(delta)        // World update + collision
                    │   │   ├─> worldManager.update(delta)
                    │   │   └─> gameUI.update(delta)
                    │   │
                    │   └─> Render Pipeline
                    │       ├─> renderer.updateCamera()
                    │       ├─> renderer.renderBackground()
                    │       ├─> renderer.renderPlayer()
                    │       ├─> renderer.renderMobs()
                    │       ├─> renderer.renderProjectiles()
                    │       ├─> renderer.renderPickups()
                    │       ├─> renderer.renderHitboxes() [debug]
                    │       ├─> renderer.renderEffects()
                    │       └─> gameUI.render()
```

---

## 📊 Stage/Wave System Architecture

### Data Flow
```
BoneChildGame                    WorldManager                    StageSpawner
     │                                │                                │
     │ (onStartGame)                  │                                │
     ├─> creates WorldManager         │                                │
     │                                │                                │
     │ (worldManager.initialize)      │                                │
     ├───────────────────────────────>│                                │
     │                                │ (creates StageSpawner)         │
     │                                ├───────────────────────────────>│
     │                                │                                │
     │                                │ (loadStage)                    │
     │                                ├───────────────────────────────>│
     │                                │                                ├─> Loads stage-1.json
     │                                │                                │   {
     │                                │                                │     "stageId": "stage_1",
     │                                │                                │     "waves": [...]
     │                                │                                │   }
     │                                │                                │
     │ (worldManager.startWave)       │                                │
     ├───────────────────────────────>│ (stageSpawner.startWave)      │
     │                                ├───────────────────────────────>│
     │                                │                                ├─> Schedules spawns
     │                                │                                │
     │                                │                                │
     │ Every frame:                   │                                │
     │ render() {                     │                                │
     │   update(delta) {              │                                │
     │     worldManager.update        │                                │
     ├───────────────────────────────>│ stageSpawner.update(delta)    │
     │                                ├───────────────────────────────>│
     │                                │                                ├─> Spawns mobs
     │                                │<───────────────────────────────┤   at scheduled times
     │                                │                                │
     │                                │ checkWaveProgress()            │
     │                                ├─> if (mobs.size == 0           │
     │                                │     && !waveActive)            │
     │                                │   stageSpawner.nextWave()      │
     │                                ├───────────────────────────────>│
     │                                │                                ├─> Wave++, start next
     │   }                            │                                │
     │ }                              │                                │
```

---

## 🌊 How We Know What Stage/Wave We're On

### Information Source Hierarchy
```
StageSpawner (source of truth)
     │
     ├─> currentWaveIndex (0-based internal)
     ├─> currentStage.waves (loaded from JSON)
     └─> Methods:
         ├─> getCurrentWave() → returns (currentWaveIndex + 1)  // 1-based for UI
         ├─> getTotalWaves() → returns currentStage.waves.size
         ├─> getCurrentWaveDefinition() → returns WaveDefinition object
         │   └─> Contains: waveNumber, spawns, isBossWave
         ├─> getStageName() → returns currentStage.name
         └─> isStageComplete() → returns currentWaveIndex >= total waves
```

### Exposed via WorldManager
WorldManager wraps StageSpawner and exposes stage info to BoneChildGame:

```java
// In WorldManager.java:
public int getCurrentWave() { 
    return stageSpawner != null ? stageSpawner.getCurrentWave() : 0; 
}

public int getTotalWaves() { 
    return stageSpawner != null ? stageSpawner.getTotalWaves() : 0; 
}

public String getStageName() { 
    return stageSpawner != null ? stageSpawner.getStageName() : ""; 
}

public boolean isBossWave() {
    if (stageSpawner == null) return false;
    WaveDefinition wave = stageSpawner.getCurrentWaveDefinition();
    return wave != null && wave.isBossWave;
}
```

### Used By BoneChildGame
BoneChildGame queries WorldManager for display/state:

```java
// In BoneChildGame.java:
// When showing game over screen:
gameOverScreen.setStats(
    worldManager.getCurrentWave(),    // "You survived 5 waves!"
    worldManager.getPlayer().getGold(),
    worldManager.getPlayer().getLevel()
);

// UI can display: "Wave 3/10" via:
worldManager.getCurrentWave()  // 3
worldManager.getTotalWaves()   // 10
worldManager.getStageName()    // "The Dungeon"
```

---

## 🎯 Boss Wave Detection

### Current State
Boss wave information flows like this:

```
stage-1.json
    ↓
{
  "waves": [
    { "waveNumber": 5, "isBossWave": true, ... }
  ]
}
    ↓
StageSpawner.loadStage()
    ↓
currentStage.waves[4].isBossWave = true
    ↓
WorldManager.isBossWave() queries stageSpawner.getCurrentWaveDefinition().isBossWave
    ↓
[Currently NOT used by BoneChildGame]
```

### Legacy vs. New System

**Old (Deprecated):**
```java
// BoneChildGame used to poll deprecated flags:
if (worldManager.shouldShowBossWarning()) {
    bossWarningScreen.show("BOSS08_B");  // ❌ Hardcoded
}
```

**New (Modular):**
```java
// Stage JSON defines boss waves:
{
  "waveNumber": 5,
  "isBossWave": true,
  "spawns": [
    { "mobType": "boss08b", "count": 1, "spawnDelay": 0.0 }
  ]
}

// BoneChildGame should detect boss wave like this:
if (worldManager.isBossWave() && worldManager.getMobCount() > 0 && !bossWarningShown) {
    bossWarningScreen.show(worldManager.getStageName() + " Boss");
    bossWarningShown = true;
}
```

---

## 🔄 Wave Progression Logic

### Where It Happens
Wave progression is driven by **WorldManager.checkWaveProgress()**:

```java
// In WorldManager.java:
private void checkWaveProgress() {
    if (stageSpawner == null) return;

    // All mobs dead AND spawning complete?
    if (mobs.size == 0 && !stageSpawner.isWaveActive()) {
        if (!stageSpawner.isStageComplete()) {
            Gdx.app.log("WorldManager", "✅ Wave cleared! Advancing...");
            stageSpawner.nextWave();  // ← Advances wave counter, starts next
        } else {
            Gdx.app.log("WorldManager", "🎉 STAGE COMPLETE!");
            // TODO: Trigger stage completion event
        }
    }
}
```

### Trigger Conditions
A wave advances when **BOTH** are true:
1. `mobs.size == 0` (all enemies dead)
2. `!stageSpawner.isWaveActive()` (no more scheduled spawns)

This prevents advancing mid-wave if mobs haven't spawned yet.

---

## 🏗️ BoneChildGame Update Pipeline (Detailed)

### update(delta) Method
```java
private void update(float delta) {
    // 1. Update world (player, enemies, spawning)
    worldManager.update(delta);
        ├─> player.update(delta)              // Movement, cooldowns, state
        ├─> stageSpawner.update(delta, mobs)  // Spawn mobs at scheduled times
        ├─> updateMobs(delta)                 // AI, movement, remove dead
        ├─> checkWaveProgress()               // Advance wave if cleared
        ├─> updatePickups(delta)              // Fade, animate
        └─> updateProjectiles(delta)          // Movement, remove inactive
    
    // 2. Collisions (handled by engine CollisionSystem - NOT shown here yet)
    //    This should happen AFTER worldManager.update but BEFORE UI
    //    collisionSystem.process(delta, player, mobs, projectiles, pickups)
    
    // 3. Update UI
    gameUI.update(delta);
        └─> Updates health bars, XP bars, wave counter, etc.
}
```

### Current Missing Piece ⚠️
**BoneChildGame does NOT call `collisionSystem.process()`!**

The collision system was created but never wired into the update loop. This needs to be added:

```java
private void update(float delta) {
    worldManager.update(delta);
    
    // ADD THIS:
    collisionSystem.process(
        delta,
        worldManager.getPlayer(),
        worldManager.getMobs(),
        worldManager.getProjectiles(),
        worldManager.getPickups()
    );
    
    gameUI.update(delta);
}
```

---

## 📋 What BoneChildGame Actually Does

### ✅ Responsibilities
1. **Lifecycle Management**
   - `create()` → Initialize camera, assets, menu
   - `dispose()` → Cleanup resources

2. **State Orchestration**
   - Menu ↔ Game ↔ Death state transitions
   - Pause/unpause game
   - Show/hide UI screens in priority order

3. **Input Routing**
   - Check which screen should consume input
   - Route gameplay input to `PlayerInput`
   - Handle global hotkeys (ESC, C, I)

4. **Render Coordination**
   - Call world update when not paused
   - Coordinate renderer pipeline
   - Render UI overlays

5. **UI Callbacks**
   - Implement MenuCallback, PauseCallback, etc.
   - Handle "Start Game", "Exit to Menu", "Power-Up Selected"

### ❌ Does NOT Do
- Create mobs/bosses
- Manage waves/stages (WorldManager + StageSpawner do this)
- Apply damage (CollisionSystem does this)
- Know about specific mob types (no Boss08B references)
- Directly spawn entities

---

## 🔧 Current Issues & TODOs

### Issue 1: CollisionSystem Not Wired
**Problem:** `collisionSystem` is created but never called.

**Fix:**
```java
// In BoneChildGame.update():
collisionSystem.process(delta, player, mobs, projectiles, pickups);
```

### Issue 2: No Boss Warning Trigger
**Problem:** Old boss warning code was removed, but no new trigger added.

**Fix:** Add generic boss wave detection:
```java
// In BoneChildGame.render() after level-up check:
if (worldManager.isBossWave() && worldManager.getMobCount() > 0 && !bossWarningShown) {
    bossWarningScreen.show("Boss Wave");
    gamePaused = true;
    bossWarningShown = true;
}
```

### Issue 3: Stage Completion Not Handled
**Problem:** When `stageSpawner.isStageComplete()` is true, nothing happens.

**Fix:** Add stage transition:
```java
// In WorldManager or trigger an event to BoneChildGame:
if (stageSpawner.isStageComplete()) {
    // Show "Stage Complete" screen
    // Load next stage
    // OR show victory screen if final stage
}
```

---

## 📊 Information Flow Summary

```
User Action                 BoneChildGame           WorldManager        StageSpawner
─────────────────────────────────────────────────────────────────────────────────────
Click "Start Game"   ───>   onStartGame()
                                  │
                                  ├─> creates Player
                                  │
                                  ├─> new WorldManager(player)
                                  │               │
                                  │               └─> initialize(assets)
                                  │                           │
                                  │                           ├─> creates StageSpawner
                                  │                           │           │
                                  │                           │           └─> loadStage("stage-1.json")
                                  │                           │                   │
                                  │                           │                   └─> parses JSON
                                  │                           │
                                  │                           └─> startWave()
                                  │                                       │
                                  │                                       └─> schedules spawns
                                  │
                                  └─> creates UI screens

Each Frame (60 FPS)  ───>   render()
                                  │
                                  └─> update(delta)
                                              │
                                              └─> worldManager.update(delta)
                                                          │
                                                          ├─> stageSpawner.update(delta)
                                                          │           │
                                                          │           └─> spawns scheduled mobs
                                                          │
                                                          ├─> mobs.update()
                                                          │
                                                          └─> checkWaveProgress()
                                                                      │
                                                                      └─> if (wave cleared)
                                                                          stageSpawner.nextWave()

Query Wave Info      ───>   worldManager.getCurrentWave()
                                          │
                                          └─> stageSpawner.getCurrentWave()
                                                      │
                                                      └─> returns currentWaveIndex + 1
```

---

## 🎯 Summary

**How we know stage/wave:**
- `StageSpawner` is the source of truth (loaded from JSON)
- `WorldManager` wraps and exposes: `getCurrentWave()`, `getTotalWaves()`, `isBossWave()`, `getStageName()`
- `BoneChildGame` queries WorldManager when needed (game over screen, UI display)

**How BoneChildGame handles the game loop:**
1. **Menu state:** Render menu until "Start Game" clicked
2. **Gameplay state:**
   - Check for death → game over screen
   - Check for UI overlays (pause, stats, powerup) → pause and render
   - Normal gameplay:
     - `handleInput()` → route to PlayerInput or UI
     - `update(delta)` → world update + ~~collision~~ (NOT WIRED YET)
     - Render pipeline → background, entities, effects, UI
3. **State transitions:** Handle callbacks from UI screens

**BoneChildGame does NOT:**
- Know about Boss08B, Goblin, or any specific mobs
- Spawn entities directly
- Manage wave progression (WorldManager → StageSpawner does this)
- Apply damage (CollisionSystem does this... when wired)

