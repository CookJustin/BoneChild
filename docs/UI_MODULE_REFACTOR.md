# UI Module Refactoring Plan

## Current State
UI classes are in `engine/src/main/java/com/bonechild/ui/` and tightly coupled to engine.

## Problem: Circular Dependency

```
┌─────────┐      uses UI screens      ┌────────┐
│ Engine  │ ─────────────────────────> │   UI   │
│         │                            │        │
│         │ <───────────────────────── │        │
└─────────┘   needs Player, WorldMgr   └────────┘
```

**UI needs:**
- `Player` (to display health, XP, stats)
- `WorldManager` (to display wave info)
- `PlayerInput` (for settings)

**Engine needs:**
- UI screens (GameUI, PauseMenu, PowerUpScreen, etc.)

This creates a circular dependency that Maven cannot resolve!

---

## Solution: Extract Core Domain Layer

### Target Architecture

```
┌──────────────┐
│    Assets    │  ← Textures, animations
└──────────────┘
       ▲
       │
┌──────────────┐
│  Game-Core   │  ← Player, WorldManager, GameState (interfaces + impl)
│  (domain)    │
└──────────────┘
       ▲
       │
   ┌───┴───┬──────────┐
   │       │          │
┌──▼───┐ ┌─▼──┐  ┌───▼────┐
│ UI   │ │Mon-│  │ Engine │  ← Orchestration, main game loop
│      │ │ster│  │        │
└──────┘ └────┘  └────────┘
```

### Steps to Refactor

#### 1. Create `game-core` Module
```xml
<module>game-core</module>
```

Move to `game-core`:
- `Player.java`
- `WorldManager.java`
- `Entity.java`, `LivingEntity.java`
- `Pickup.java`, `Projectile.java`, `Explosion.java`
- `ComboSystem.java`
- Game state interfaces

#### 2. Update Dependencies

**game-core:**
```xml
<dependencies>
  <dependency>assets</dependency>
  <dependency>monsters</dependency>
</dependencies>
```

**ui:**
```xml
<dependencies>
  <dependency>game-core</dependency>  ← Can access Player, WorldManager
  <dependency>assets</dependency>
</dependencies>
```

**engine:**
```xml
<dependencies>
  <dependency>game-core</dependency>
  <dependency>ui</dependency>         ← Can use UI screens
  <dependency>monsters</dependency>
</dependencies>
```

#### 3. Move UI Classes

Move from `engine/ui/` to `ui/src/main/java/com/bonechild/ui/`:
- `GameUI.java`
- `PauseMenu.java`
- `PowerUpScreen.java`
- `GameOverScreen.java`
- `BossWarningScreen.java`
- `CharacterStatsScreen.java`
- `InventoryUI.java`
- `MenuScreen.java`
- `SettingsScreen.java`
- `UIEffectsManager.java`

#### 4. Keep in Engine

Engine-specific (orchestration):
- `BoneChildGame.java` - Main game class
- `Main.java` - Entry point
- `Renderer.java` - Game renderer
- `TileMap.java` - Map rendering
- Rendering effects (CameraShake, ParticleSystem, etc.)

---

## Alternative: Keep UI in Engine (Current State)

If extracting game-core is too much work right now, we can:

1. **Keep UI in engine** but organize better:
   ```
   engine/
   ├── src/main/java/com/bonechild/
   │   ├── game/           ← Game logic (Player, WorldManager)
   │   ├── ui/             ← UI screens
   │   ├── rendering/      ← Rendering
   │   └── BoneChildGame.java
   ```

2. **Document dependencies clearly** in each package README

3. **Plan for future extraction** when we have time

---

## Benefits of Proper Module Split

✅ **Clear separation of concerns**
- UI only handles presentation
- Game-core handles logic
- Engine orchestrates everything

✅ **Better testability**
- Can test game logic without UI
- Can test UI with mock game state

✅ **Reusability**
- Could build different frontends (desktop, mobile, web)
- Could reuse game-core in server for multiplayer

✅ **Easier maintenance**
- Changes to UI don't affect game logic
- Changes to game logic don't affect UI rendering

---

## Current Decision

**For now: Keep UI in engine** to avoid blocking progress.

**Next refactor: Extract game-core module** when we have:
- Time for larger refactor
- Clear interfaces defined
- Good test coverage

---

## Module Structure (Future)

```
BoneChild/
├── assets/           # Textures, animations (done ✅)
├── monsters/         # Monster entities (done ✅)
├── game-core/        # Player, WorldManager, game logic (TODO)
├── ui/               # UI screens, HUD (TODO - depends on game-core)
└── engine/           # Main game loop, rendering (depends on ui + game-core)
```

This is the target architecture for a fully modular, maintainable game!

