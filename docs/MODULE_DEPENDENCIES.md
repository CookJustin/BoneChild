# Module Dependency Architecture

## Answer: game-core uses stages module

```
stages → monsters (for MobFactory)
   ↓
game-core → stages (WorldManager uses StageSpawner)
   ↓
ui → game-core (UI reads Player, WorldManager state)
   ↓
engine → ui (Engine displays UI screens)
```

## Full Dependency Chain

**From bottom to top:**
1. **assets** - Foundation (textures, animations)
2. **monsters** - Uses assets for sprite loading
3. **stages** - Uses monsters for spawning
4. **game-core** - Uses assets, monsters, stages for game logic
5. **ui** - Uses game-core (reads Player health, wave info) and assets (fonts, textures)
6. **engine** - Uses ui (displays screens), game-core (game state), monsters, assets

**One-way flow, no circles!** ✅

## Why game-core, not engine?

### game-core is LOGIC
- `WorldManager` manages game state (mobs, waves, progression)
- `StageSpawner` provides wave spawning logic
- **This is game behavior**, not rendering or orchestration

### engine is ORCHESTRATION
- Coordinates modules
- Handles rendering pipeline
- Manages screens and UI
- **Doesn't need to know about stage internals**

## Updated Dependency Graph

```
         ┌─────────┐
         │ assets  │
         └────┬────┘
              │
        ┌─────┴─────┬─────────┐
        │           │         │
   ┌────▼────┐ ┌───▼────┐    │
   │monsters │ │game-   │    │
   │         │ │core    │◄───┘
   └────┬────┘ └───┬────┘
        │          │
    ┌───▼────┐ ┌───┴────┐
    │stages  │ │   ui   │
    └───┬────┘ └───┬────┘
        │          │
        └──────┬───┴───┐
               │       │
           ┌───▼───────▼──┐
           │    engine    │
           └──────────────┘
```

## Module Purposes

| Module | Purpose | Depends On |
|--------|---------|------------|
| **assets** | Textures, animations, audio | LibGDX only |
| **monsters** | Monster entities | assets |
| **stages** | Wave definitions & spawning logic | monsters |
| **game-core** | Game state & logic (Player, WorldManager) | assets, monsters, stages |
| **ui** | User interface screens & HUD | game-core, assets |
| **engine** | Orchestration & rendering | ui, game-core, monsters, assets |

## Usage in game-core

### WorldManager should use StageSpawner like this:

```java
public class WorldManager {
    private StageSpawner stageSpawner;
    private Array<MobEntity> mobs;
    private MobFactory mobFactory;
    
    public void initialize(MobFactory factory) {
        this.mobFactory = factory;
        this.mobs = new Array<>();
        
        // Create and load stage
        stageSpawner = new StageSpawner(mobFactory);
        stageSpawner.loadStage("stages/stage-1.json");
        stageSpawner.setSpawnBounds(100, 1820, 100, 980);
        stageSpawner.startWave();
    }
    
    public void update(float delta) {
        // Update stage spawner (spawns mobs at scheduled times)
        stageSpawner.update(delta, mobs);
        
        // Update all mobs
        for (MobEntity mob : mobs) {
            mob.update(delta);
        }
        
        // Check if wave complete
        if (allMobsDead() && !stageSpawner.isWaveActive()) {
            if (!stageSpawner.isStageComplete()) {
                stageSpawner.nextWave();
            } else {
                // Stage complete!
                onStageComplete();
            }
        }
    }
    
    private boolean allMobsDead() {
        for (MobEntity mob : mobs) {
            if (!mob.isDead()) return false;
        }
        return true;
    }
}
```

## Engine doesn't need stages directly

### The Dependency Chain: engine → ui → game-core → stages

**Engine uses UI:**
```java
// In engine/BoneChildGame.java
private GameUI gameUI;
private PauseMenu pauseMenu;

public void create() {
    // Engine creates UI screens and passes game-core objects
    gameUI = new GameUI(assets, player, worldManager);
    pauseMenu = new PauseMenu(assets, callback);
}

public void render() {
    // Engine renders UI
    gameUI.render();
}
```

**UI uses game-core:**
```java
// In ui/GameUI.java
public class GameUI {
    private Player player;
    private WorldManager worldManager;
    
    public GameUI(Assets assets, Player player, WorldManager worldManager) {
        this.player = player;
        this.worldManager = worldManager;
    }
    
    public void render() {
        // UI reads game state from game-core
        int health = player.getCurrentHealth();
        int wave = worldManager.getCurrentWave();
        int totalWaves = worldManager.getTotalWaves();
        
        // Display it
        drawHealthBar(health);
        drawWaveCounter(wave, totalWaves);
    }
}
```

**game-core uses stages:**
```java
// In game-core/WorldManager.java
public class WorldManager {
    private StageSpawner stageSpawner;
    
    public void initialize(MobFactory factory) {
        stageSpawner = new StageSpawner(factory);
        stageSpawner.loadStage("stages/stage-1.json");
    }
    
    public int getCurrentWave() {
        return stageSpawner.getCurrentWave();
    }
}
```

### Why This Works

✅ **Engine** → Creates UI screens, passes game-core references  
✅ **UI** → Reads game state from game-core (Player, WorldManager)  
✅ **game-core** → Uses StageSpawner internally for wave logic  
✅ **stages** → Self-contained wave spawning system  

**No circular dependencies!** Each layer only knows about the layer below it.

---

## Alternative: Engine could use game-core directly

Engine can also bypass UI and talk to game-core directly:

```java
// In engine/BoneChildGame.java
public void render() {
    // Engine asks WorldManager, not StageSpawner directly
    int currentWave = worldManager.getCurrentWave();
    int totalWaves = worldManager.getTotalWaves();
    
    // Display on UI
    gameUI.setWaveInfo(currentWave, totalWaves);
}
```

WorldManager exposes this info:

```java
public class WorldManager {
    public int getCurrentWave() {
        return stageSpawner.getCurrentWave();
    }
    
    public int getTotalWaves() {
        return stageSpawner.getTotalWaves();
    }
}
```

## Benefits of This Architecture

✅ **Separation of concerns**
- game-core = game logic
- stages = wave definitions
- engine = orchestration

✅ **game-core owns gameplay**
- Spawning is game logic
- Stage progression is game logic
- Engine doesn't need to know these details

✅ **Clean dependencies**
- Engine depends on game-core (high level)
- game-core depends on stages (game logic detail)
- No circular dependencies!

## Summary

**Q: What takes in stages as a module?**

**A: game-core**

- game-core depends on stages in pom.xml ✅
- WorldManager uses StageSpawner for wave spawning
- Engine uses game-core (doesn't need stages directly)

This keeps the architecture clean and modular!

