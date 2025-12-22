# Stages Module - Complete! ✅

## What Was Created

A brand new **stages** module, completely separate from assets, game-core, and engine!

## Module Structure

```
BoneChild/
├── assets/          # Textures, animations, audio
├── monsters/        # Monster entities (Mob, Goblin, Boss08B)
├── stages/          # ⭐ NEW! Stage & wave management
│   ├── src/main/java/com/bonechild/stages/
│   │   └── StageSpawner.java
│   ├── src/main/resources/stages/
│   │   └── stage-1.json
│   └── docs/
│       └── README.md
├── game-core/       # Game logic (Player, WorldManager)
├── ui/              # UI screens
└── engine/          # Orchestration
```

## What StageSpawner Does

### Loads Stage Definitions (JSON)
```json
{
  "stageId": "stage_1",
  "name": "The Beginning",
  "waves": [
    {
      "waveNumber": 1,
      "spawns": [
        { "mobType": "goblin", "count": 5, "spawnDelay": 1.0 }
      ]
    },
    {
      "waveNumber": 5,
      "spawns": [
        { "mobType": "boss08b", "count": 1, "spawnDelay": 0 }
      ],
      "isBossWave": true
    }
  ]
}
```

### Manages Wave Progression
- Schedules mob spawns at specific times
- Spawns mobs around map edges
- Tracks wave completion
- Handles boss wave detection

### Simple API
```java
// Setup
StageSpawner spawner = new StageSpawner(mobFactory);
spawner.loadStage("stages/stage-1.json");
spawner.startWave();

// Game loop
spawner.update(delta, mobs);

// Next wave
if (allMobsDead()) {
    spawner.nextWave();
}
```

## Stage 1 Definition

**5 Waves:**
1. 5 Goblins (warmup)
2. 8 Goblins (harder)
3. 10 Goblins (swarm!)
4. 6 Goblins + 3 regular Mobs (mixed)
5. 1 Boss08B (boss wave! 🚨)

## How to Add Monsters to Waves

### 1. Make sure monster is registered
```java
// In DefaultMobFactory
register("skeleton", ctx -> new Skeleton(...));
```

### 2. Add to stage JSON
```json
{
  "waveNumber": 3,
  "spawns": [
    { "mobType": "skeleton", "count": 10, "spawnDelay": 0.8 }
  ]
}
```

That's it! The system automatically:
- Loads the monster definition
- Spawns at scheduled times
- Places around map edges
- Tracks when wave is complete

## Adding New Stages

Just create a new JSON file:

```
stages/src/main/resources/stages/
├── stage-1.json  ✅
├── stage-2.json  ← Create this
└── stage-3.json  ← Or this
```

Load it: `spawner.loadStage("stages/stage-2.json");`

## Module Dependencies

```
stages → monsters (for spawning)
stages → LibGDX (for JSON)

game-core → stages (to use StageSpawner)
engine → stages (to display stage info)
```

**No circular dependencies!** ✅

## Benefits

✅ **Fully Modular** - Stages completely separate from everything else  
✅ **Data-Driven** - No code changes to add stages/waves  
✅ **Designer-Friendly** - Simple JSON format  
✅ **Flexible** - Mix any monsters in any pattern  
✅ **Scalable** - Easy to add stages 4, 5, 6, 100...  

## Next Steps

To integrate with game:

1. **game-core** needs to:
   - Add `stages` dependency to pom.xml
   - Import `StageSpawner` in WorldManager
   - Use it instead of hardcoded wave logic

2. **Create more stages:**
   - stage-2.json (harder waves)
   - stage-3.json (epic finale)

3. **Add stage transitions:**
   - Portal spawns when stage complete
   - Player enters portal → load next stage

---

**Date:** December 22, 2025  
**Status:** ✅ Stages module complete and compiling!  
**Files:** StageSpawner.java + stage-1.json  
**Ready to use!** 🎉

