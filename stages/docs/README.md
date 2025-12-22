# Stages Module

## Purpose
Manages stage progression, wave spawning, and difficulty scaling for BoneChild. Completely separate from assets and game-core.

## What's in this module

### Core Classes
- **StageSpawner** - Loads stage JSON, manages wave progression, spawns mobs at scheduled times

### Resources
- **stages/*.json** - Stage definitions with wave configurations

## Module Structure

```
stages/
├── src/main/java/com/bonechild/stages/
│   └── StageSpawner.java
├── src/main/resources/stages/
│   ├── stage-1.json
│   ├── stage-2.json
│   └── stage-3.json
└── docs/
    └── README.md
```

## Dependencies
- `monsters` - For MobFactory and spawning
- LibGDX Core - For JSON parsing

## Used By
- `game-core` - WorldManager uses StageSpawner
- `engine` - For displaying stage/wave info

---

## How to Use

### 1. Create a Stage JSON

Create `stages/src/main/resources/stages/stage-1.json`:

```json
{
  "stageId": "stage_1",
  "name": "The Beginning",
  "description": "Your first challenge",
  "waves": [
    {
      "waveNumber": 1,
      "spawns": [
        {
          "mobType": "goblin",
          "count": 5,
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

### 2. Use StageSpawner in Game

```java
// In WorldManager initialization
StageSpawner stageSpawner = new StageSpawner(mobFactory);
stageSpawner.loadStage("stages/stage-1.json");
stageSpawner.setSpawnBounds(100, 1820, 100, 980);
stageSpawner.startWave();

// In update loop
stageSpawner.update(delta, mobs);

// When all mobs are dead
if (mobs.size == 0 && !stageSpawner.isWaveActive()) {
    if (!stageSpawner.isStageComplete()) {
        stageSpawner.nextWave();
    } else {
        // Stage complete! Show portal to next stage
    }
}

// Display wave info
int currentWave = stageSpawner.getCurrentWave();
int totalWaves = stageSpawner.getTotalWaves();
String stageName = stageSpawner.getStageName();
```

### 3. Check if Boss Wave

```java
WaveDefinition wave = stageSpawner.getCurrentWaveDefinition();
if (wave != null && wave.isBossWave) {
    // Show boss warning screen
    showBossWarning();
}
```

---

## Stage Definition Format

### Stage Properties
- `stageId` - Unique identifier
- `name` - Display name
- `description` - Flavor text
- `waves` - Array of wave definitions

### Wave Properties
- `waveNumber` - Wave number (1-5, etc.)
- `spawns` - Array of spawn patterns
- `isBossWave` - Boolean, shows boss warning if true

### Spawn Pattern Properties
- `mobType` - Monster type ID (must be registered in MobFactory)
- `count` - Number of this mob type to spawn
- `spawnDelay` - Seconds between each spawn

---

## Adding New Stages

### Stage 2 Example

Create `stages/stage-2.json`:

```json
{
  "stageId": "stage_2",
  "name": "The Depths",
  "description": "Darkness closes in...",
  "waves": [
    {
      "waveNumber": 1,
      "spawns": [
        {
          "mobType": "mob",
          "count": 8,
          "spawnDelay": 0.8
        }
      ]
    },
    {
      "waveNumber": 2,
      "spawns": [
        {
          "mobType": "mob",
          "count": 5,
          "spawnDelay": 1.0
        },
        {
          "mobType": "goblin",
          "count": 10,
          "spawnDelay": 0.5
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
        },
        {
          "mobType": "goblin",
          "count": 5,
          "spawnDelay": 2.0
        }
      ],
      "isBossWave": true
    }
  ]
}
```

Load it: `stageSpawner.loadStage("stages/stage-2.json");`

---

## Design Benefits

✅ **Fully Data-Driven** - No code changes to add stages  
✅ **Modular** - Stages separate from game logic and assets  
✅ **Designer-Friendly** - JSON is easy to edit  
✅ **Flexible** - Mix any mobs in any wave pattern  
✅ **Testable** - Easy to test individual waves  

---

## Future Enhancements

- [ ] Stage transitions (portals)
- [ ] Difficulty scaling (health/damage multipliers)
- [ ] Environmental hazards per stage
- [ ] Special wave modifiers (speed boost, health regen, etc.)
- [ ] Dynamic spawn positions (specific zones)
- [ ] Wave time limits
- [ ] Endless mode (procedurally generated waves)

