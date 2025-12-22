# Stage System Design

## Overview
The stage system manages waves of enemies, progression between stages, and difficulty scaling.

## Architecture

```
┌─────────────┐
│   Stage     │  ← Stage definition (which monsters, how many waves)
└──────┬──────┘
       │
       │ contains
       │
┌──────▼──────┐
│    Wave     │  ← Wave definition (spawn patterns, timing)
└──────┬──────┘
       │
       │ spawns
       │
┌──────▼──────┐
│ MobEntity   │  ← Actual monsters (from monsters module)
└─────────────┘
```

## Stage Definition (JSON)

Each stage is defined in JSON with multiple waves:

```json
{
  "stageId": "stage_1",
  "name": "The Beginning",
  "waves": [
    {
      "waveNumber": 1,
      "spawns": [
        {
          "mobType": "goblin",
          "count": 5,
          "spawnDelay": 0.5
        }
      ]
    },
    {
      "waveNumber": 2,
      "spawns": [
        {
          "mobType": "goblin",
          "count": 8,
          "spawnDelay": 0.4
        }
      ]
    },
    {
      "waveNumber": 3,
      "spawns": [
        {
          "mobType": "goblin",
          "count": 10,
          "spawnDelay": 0.3
        }
      ]
    },
    {
      "waveNumber": 4,
      "spawns": [
        {
          "mobType": "goblin",
          "count": 6,
          "spawnDelay": 0.4
        },
        {
          "mobType": "mob",
          "count": 3,
          "spawnDelay": 1.0
        }
      ]
    },
    {
      "waveNumber": 5,
      "spawns": [
        {
          "mobType": "boss08b",
          "count": 1,
          "spawnDelay": 0
        }
      ],
      "isBossWave": true
    }
  ]
}
```

## Usage in Game

### 1. Loading a Stage
```java
// In WorldManager or StageManager
StageDefinition stage = StageLoader.load("stages/stage_1.json");
currentStage = stage;
currentWave = 0;
```

### 2. Starting a Wave
```java
public void startWave(int waveNumber) {
    WaveDefinition wave = currentStage.getWave(waveNumber);
    
    for (SpawnPattern spawn : wave.getSpawns()) {
        // spawn.mobType = "goblin", "mob", "boss08b"
        // spawn.count = how many
        // spawn.spawnDelay = seconds between each spawn
        
        scheduleSpawns(spawn);
    }
}
```

### 3. Spawning Monsters
```java
private void scheduleSpawns(SpawnPattern pattern) {
    for (int i = 0; i < pattern.count; i++) {
        float delay = i * pattern.spawnDelay;
        
        // Schedule spawn after delay
        queueSpawn(pattern.mobType, delay);
    }
}

private void queueSpawn(String mobType, float delay) {
    // After delay seconds, spawn the mob
    SpawnContext ctx = new SpawnContext(getRandomSpawnPosition());
    MobEntity mob = mobFactory.create(mobType, ctx);
    mobs.add(mob);
}
```

## Example Stage Progression

### Stage 1: The Beginning
- **Wave 1-3**: Goblins only (easy warmup)
- **Wave 4**: Mix of Goblins + regular Mobs
- **Wave 5**: Boss08B (boss wave)

### Stage 2: The Depths
- **Wave 1-2**: Regular Mobs + Goblins (harder)
- **Wave 3**: Swarm of Goblins (15+)
- **Wave 4**: Multiple regular Mobs
- **Wave 5**: Boss08B + Goblin minions

### Stage 3: The Abyss
- **Wave 1-4**: Mixed waves, increasing difficulty
- **Wave 5**: 2x Boss08B (epic final battle!)

## Adding New Monsters

To add a new monster type:

1. **Create Monster Class** (in monsters module)
```java
public class Skeleton extends Mob {
    public Skeleton(float x, float y, Vector2 playerPosition, Assets assets) {
        super(x, y, playerPosition);
        this.maxHealth = 100f;
        this.speed = 80f;
        // ... load animations, set stats
    }
    
    @Override
    public String getTypeId() {
        return "skeleton";
    }
}
```

2. **Register in DefaultMobFactory**
```java
register("skeleton", ctx -> new Skeleton(
    ctx.getPosition().x, 
    ctx.getPosition().y, 
    playerPosition, 
    assets
));
```

3. **Use in Stage JSON**
```json
{
  "mobType": "skeleton",
  "count": 5,
  "spawnDelay": 0.5
}
```

That's it! The system automatically handles spawning.

## Benefits

✅ **Data-Driven** - Stages defined in JSON, no code changes needed
✅ **Flexible** - Mix any monsters in any wave
✅ **Scalable** - Easy to add stages 4, 5, 6...
✅ **Designer-Friendly** - Non-programmers can create stages
✅ **Easy Testing** - Change JSON, reload game

---

Next: Implementing the StageManager and WaveManager classes!

