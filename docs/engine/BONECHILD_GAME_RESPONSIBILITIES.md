# BoneChildGame - Responsibilities & Architecture

**Location:** `engine/src/main/java/com/bonechild/BoneChildGame.java`

## Single Responsibility
`BoneChildGame` is the **application lifecycle coordinator** for the BoneChild game.

It orchestrates high-level game state and delegates specific responsibilities to specialized subsystems.

---

## What BoneChildGame IS Responsible For

### 1. LibGDX Application Lifecycle
- Implement `ApplicationAdapter` (create/render/resize/dispose)
- Initialize camera + viewport
- Load global assets on startup
- Clean up resources on exit

### 2. High-Level Game State Management
- Track game states: `!gameStarted` (menu), `gameStarted` (playing), `gamePaused`, `isDead`
- Coordinate state transitions (menu → game → death → restart)

### 3. UI Screen Orchestration
- Create and manage all UI screens:
  - `MenuScreen` (main menu)
  - `SettingsScreen` (keybinds/audio/display)
  - `PauseMenu`
  - `GameOverScreen`
  - `PowerUpScreen` (level-up)
  - `CharacterStatsScreen` (C key)
  - `InventoryUI` (I key)
  - `BossWarningScreen` (generic overlay, stage-driven)
  - `GameUI` (HUD)
- Implement callbacks for screen actions (MenuCallback, PauseCallback, etc.)
- Handle screen priority/layering (which screen blocks input)

### 4. High-Level Input Routing
- Determine which system should consume input based on active screen
- Route game input to `PlayerInput`
- Handle global hotkeys:
  - ESC → pause menu
  - C → character stats
  - I → inventory

### 5. Core Game Loop Coordination
```java
render() {
    if (!gameStarted) → menu flow
    if (player.isDead()) → death timer + game over screen
    if (any UI screen visible) → render paused game + UI overlay
    
    // Active gameplay:
    handleInput()       // route to PlayerInput or UI
    update(delta):
        worldManager.update()
        collisionSystem.process()
        gameUI.update()
    
    // Render pipeline:
    renderer.updateCamera()
    renderer.renderBackground()
    renderer.renderPlayer()
    renderer.renderMobs()
    renderer.renderProjectiles()
    renderer.renderPickups()
    renderer.renderEffects()
    gameUI.render()
}
```

### 6. Subsystem Initialization (on "Start Game")
- Create `Player`
- Create `WorldManager(player)` and initialize with assets
- Create `Renderer(camera, assets)`
- Create `PlayerInput(player)`
- Create `CollisionSystem`
- Apply saved settings (keybinds, audio)
- Start background music

---

## What BoneChildGame Is NOT Responsible For

### ❌ Gameplay Logic
- Spawning mobs/bosses → `WorldManager` + `StageSpawner`
- Wave progression → `WorldManager` + `StageSpawner`
- Damage calculation → `CollisionSystem` + `Damageable` entities
- Loot drops → mob `onDeath()` hooks
- Player stats/leveling → `Player` class
- Power-up effects → `Player.applyPowerUp()`

### ❌ Concrete Entity Knowledge
- Does NOT reference `Boss08B`, `Goblin`, `Orc`, or any specific mob types
- Does NOT know how mobs are created or configured
- Does NOT hardcode boss spawn positions or wave rules

### ❌ Rendering Details
- Does NOT manage SpriteBatch/ShapeRenderer directly
- Does NOT know about animations or asset loading specifics
- Delegates all rendering to `Renderer`

### ❌ Collision Detection
- Does NOT check if projectiles hit mobs
- Does NOT check if player touches pickups
- Delegates to `CollisionSystem`

### ❌ Asset Management
- Does NOT load textures/animations/sounds directly
- Creates `Assets` wrapper which handles loading
- Passes assets to subsystems that need them

---

## Key Design Decisions

### 1. No Mob-Specific Code
- **Before:** `spawnBossAtCenter()`, `spawnOrcBossAtCenter()`, hardcoded "BOSS08_B" strings
- **After:** Generic boss warning screen; stage system owns spawning

### 2. Screen Priority System
Screens are checked in strict order:
1. Game Over (highest)
2. Character Stats
3. Power-Up
4. Boss Warning
5. Pause Menu
6. Active Gameplay (lowest)

Each higher-priority screen blocks lower ones from updating.

### 3. Death Flow
- Player death detected → start 2-second timer
- After delay → show game over screen
- Game over screen pauses update but keeps rendering world (frozen background)

### 4. Settings Application
- Settings screen owned by BoneChildGame (shared between menu and pause)
- Keybinds applied to `PlayerInput` when changed
- Audio volume applied to `Assets.getBackgroundMusic()`

---

## Future Improvements

### 1. Event-Driven Boss Warnings
Instead of:
```java
if (worldManager.shouldShowBossWarning()) { ... }
```

Use:
```java
worldManager.onBossWaveStarting(bossId -> {
    bossWarningScreen.show(bossId);
    gamePaused = true;
});
```

### 2. State Machine
Replace boolean flags with proper state pattern:
```java
enum GameState { MENU, PLAYING, PAUSED, DEAD, TRANSITIONING }
```

### 3. Screen Manager
Abstract screen priority/layering into a `ScreenManager` so BoneChildGame doesn't need 30 lines of `if (screen.isVisible())` checks.

---

## Summary
`BoneChildGame` is now a **clean application coordinator** that:
- Sets up the game world
- Manages high-level state transitions
- Routes input to the right subsystem
- Coordinates the render loop
- **Does NOT know about specific mobs, bosses, or gameplay rules**

All gameplay logic lives in:
- `WorldManager` (world state, wave/stage progression)
- `StageSpawner` (when/where to spawn mobs)
- `CollisionSystem` (damage + pickup collection)
- `Player` / `MobEntity` implementations (stats, behavior)

