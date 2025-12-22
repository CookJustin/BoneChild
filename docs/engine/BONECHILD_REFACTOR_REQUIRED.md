# BoneChildGame Refactoring - Removal of Hardcoded Boss Spawning

## Problem Found
**BoneChildGame was manually spawning Boss08B**, completely bypassing the StageSpawner system.

This violated the modular architecture where:
- Stage JSON defines which mobs/bosses spawn
- StageSpawner reads JSON and schedules spawns
- BoneChildGame should ONLY orchestrate high-level state

## Required Changes to BoneChildGame.java

### 1. Fix Imports
```java
// OLD:
import com.bonechild.input.PlayerInput;

// NEW:
import com.bonechild.playablecharacters.Player;
import com.bonechild.playablecharacters.PlayerInput;
import com.bonechild.collision.CollisionSystem;
```

### 2. Remove VIRTUAL_WIDTH/HEIGHT (only used for deleted boss spawning)
```java
// DELETE these lines:
private static final float VIRTUAL_WIDTH = 1280f;
private static final float VIRTUAL_HEIGHT = 720f;
```

### 3. Add CollisionSystem Field
```java
// Add after playerInput field:
private CollisionSystem collisionSystem;
```

### 4. Initialize CollisionSystem in create()
```java
// Add after menuScreen creation:
collisionSystem = new CollisionSystem();
```

### 5. Fix onStartGame() - Use New WorldManager API
```java
// OLD (deprecated):
worldManager = new WorldManager();
worldManager.setAssets(assets);
worldManager.setRenderer(renderer);

// NEW (modular):
Player player = new Player(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f);
worldManager = new WorldManager(player);
worldManager.initialize(assets);
worldManager.startWave();
```

### 6. Remove renderExplosions() Call
```java
// DELETE this line from boss warning rendering:
renderer.renderExplosions(worldManager.getExplosions());
```

### 7. Remove Hardcoded Boss Warning Triggers
```java
// DELETE these lines from render() method:
if (worldManager.shouldShowBossWarning() && !bossWarningScreen.isActive()) {
    bossWarningScreen.show("BOSS08_B");
    gamePaused = true;
    Gdx.app.log("BoneChild", "BOSS WARNING TRIGGERED!");
}
if (worldManager.shouldShowOrcBossWarning() && !bossWarningScreen.isActive()) {
    bossWarningScreen.show("ORC_BOSS");
    gamePaused = true;
    Gdx.app.log("BoneChild", "ORC BOSS WARNING TRIGGERED!");
}
```

### 8. Simplify Boss Warning Dismissal in handleInput()
```java
// OLD (hardcoded spawning):
if (bossWarningScreen != null && bossWarningScreen.isActive()) {
    if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
        String bossType = bossWarningScreen.getBossType();
        bossWarningScreen.dismiss();
        if ("ORC_BOSS".equals(bossType)) {
            worldManager.acknowledgeOrcBossWarning();
            spawnOrcBossAtCenter();
            ...
        } else {
            worldManager.acknowledgeBossWarning();
            spawnBossAtCenter();
            ...
        }
    }
}

// NEW (stage system handles spawning):
if (bossWarningScreen != null && bossWarningScreen.isActive()) {
    if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
        bossWarningScreen.dismiss();
        gamePaused = false;
        Gdx.app.log("BoneChild", "Boss warning dismissed - StageSpawner handles spawning");
    }
    return;
}
```

### 9. Wire CollisionSystem into update()
```java
private void update(float delta) {
    // Update world (player, enemies, spawning via StageSpawner)
    worldManager.update(delta);
    
    // ADD THIS - Process collisions:
    collisionSystem.process(
        delta,
        worldManager.getPlayer(),
        worldManager.getMobs(),
        worldManager.getProjectiles(),
        worldManager.getPickups()
    );
    
    // Update UI
    gameUI.update(delta);
}
```

### 10. DELETE Boss Spawn Methods Entirely
```java
// DELETE these entire methods:
private void spawnBossAtCenter() { ... }
private void spawnOrcBossAtCenter() { ... }
```

## How Boss Spawning Now Works

**Old (Hardcoded):**
```
BoneChildGame polls WorldManager.shouldShowBossWarning()
  → Shows hardcoded "BOSS08_B" warning
  → User presses SPACE
  → BoneChildGame.spawnBossAtCenter() creates new Boss08B(...)
  → Manually adds to worldManager.getMobs()
```

**New (Stage-Driven):**
```
stages/stage-1.json defines:
{
  "waveNumber": 5,
  "isBossWave": true,
  "spawns": [
    { "mobType": "boss08b", "count": 1, "spawnDelay": 0.0 }
  ]
}
  ↓
StageSpawner.startWave() schedules boss spawn
  ↓
StageSpawner.update() spawns boss at scheduled time
  ↓
MobFactory.create("boss08b", spawnContext) creates boss
  ↓
Boss added to mobs array automatically
```

## Benefits of This Change

✅ **Modular**: BoneChildGame knows nothing about Boss08B  
✅ **Data-Driven**: Add new bosses by editing JSON, not code  
✅ **Maintainable**: Stage designers control spawning, not engine devs  
✅ **Collision Works**: CollisionSystem now properly wired  
✅ **Clean Separation**: App lifecycle ≠ Gameplay rules

## Current State

- ✅ Documentation created
- ✅ Issues identified
- ⚠️ Manual changes required (sed/tool issues prevented automated fix)

**Next Step:** Apply the 10 changes listed above manually to BoneChildGame.java

## Summary

**You were absolutely right** - BoneChildGame was manually spawning Boss08B which completely bypassed the StageSpawner system. This violated the modular architecture we established where:

- **Stages define what spawns** (via JSON)
- **StageSpawner handles when/where** (schedules + coordinates)
- **MobFactory creates instances** (typeId → concrete mob)
- **BoneChildGame only orchestrates state** (NOT gameplay specifics)

The hardcoded `spawnBossAtCenter()` methods need to be deleted entirely, and boss spawning should flow through the stage JSON → StageSpawner → MobFactory pipeline instead.

