# Modular Asset System

## Overview
BoneChild uses a **data-driven, modular asset system** based on JSON manifests. Each module defines its own assets independently.

## Architecture

### Core Components
- **AssetRegistry** - Simple string-based lookup for textures and animations
- **AssetLoader** - Reads JSON manifests and populates the registry
- **Assets** (compatibility wrapper) - Maintains old API while using registry internally

### Asset Manifests (JSON files)
Each module/entity provides its own asset definitions:

```
assets/src/main/resources/
├── player-assets.json          # Player sprites and animations
├── effects-assets.json         # Explosions, projectiles, pickups
├── ui-assets.json              # UI elements (future)
├── stages-assets.json          # Stage backgrounds/tilesets (future)
└── monsters/                   # Per-monster asset files
    ├── boss08b-assets.json    # Boss08B specific assets
    ├── orc-assets.json        # Orc specific assets
    ├── glob-assets.json       # Glob specific assets
    ├── enemy17b-assets.json   # Enemy17B specific assets
    ├── vampire-assets.json    # Vampire specific assets
    ├── goblin-assets.json     # Goblin specific assets
    └── christmas-jad-assets.json
```

**Per-Monster Assets**: Each monster can define whatever assets it needs. A swimming monster could have `swim` animations, a flying monster could have `fly` animations - no shared structure required!

## JSON Format

### Textures (single images)
```json
{
  "textures": {
    "texture_id": "path/to/image.png"
  }
}
```

### Animations

**Frame Sequence** (Player1.png, Player2.png, etc.):
```json
{
  "animations": {
    "player_walk": {
      "type": "frame_sequence",
      "pattern": "assets/player/Player{1-6}.png",
      "frameTime": 0.1,
      "loop": true
    }
  }
}
```

**Sprite Sheet** (single row):
```json
{
  "animations": {
    "boss_idle": {
      "type": "sprite_sheet",
      "texture": "boss_sheet",
      "row": 0,
      "frames": 6,
      "frameWidth": 64,
      "frameHeight": 64,
      "frameTime": 0.15,
      "loop": true
    }
  }
}
```

**Sprite Sheet Multi-Row** (animation spans multiple rows):
```json
{
  "animations": {
    "boss_death": {
      "type": "sprite_sheet_multi_row",
      "texture": "boss_sheet",
      "startRow": 6,
      "rows": 2,
      "framesPerRow": 6,
      "frameWidth": 64,
      "frameHeight": 64,
      "frameTime": 0.1,
      "loop": false
    }
  }
}
```

## Usage

### In Code (via AssetRegistry)
```java
// Get the registry
AssetRegistry registry = assets.getRegistry();

// Get a texture
Texture texture = registry.getTexture("boss08b_sheet");

// Get an animation (returns independent copy)
Animation anim = registry.getAnimation("player_walk");
```

### Old API (compatibility layer)
```java
// Still works during migration
Animation anim = assets.createPlayerWalkAnimation();
Texture tex = assets.getVampireBodyAttack();
```

## Adding New Assets

### Adding a New Monster

**1. Create monster asset file**
Create `assets/src/main/resources/monsters/my-monster-assets.json`:
```json
{
  "animations": {
    "my_monster_walk": {
      "type": "frame_sequence",
      "pattern": "assets/monsters/MyMonster_Walk_{1-6}.png",
      "frameTime": 0.1,
      "loop": true
    },
    "my_monster_swim": {
      "type": "frame_sequence",
      "pattern": "assets/monsters/MyMonster_Swim_{1-4}.png",
      "frameTime": 0.15,
      "loop": true
    },
    "my_monster_death": {
      "type": "frame_sequence",
      "pattern": "assets/monsters/MyMonster_Death_{1-6}.png",
      "frameTime": 0.1,
      "loop": false
    }
  }
}
```

**2. Place sprite files**
```
assets/src/main/resources/assets/monsters/
├── MyMonster_Walk_1.png
├── MyMonster_Walk_2.png
├── ...
├── MyMonster_Swim_1.png
├── ...
```

**3. Use in monster code**
```java
public class MyMonster extends Mob {
    public MyMonster(float x, float y, Player player, Assets assets) {
        super(x, y, player);
        
        var registry = assets.getRegistry();
        this.walkAnimation = registry.getAnimation("my_monster_walk");
        this.swimAnimation = registry.getAnimation("my_monster_swim"); // Custom!
        this.deathAnimation = registry.getAnimation("my_monster_death");
    }
}
```

**No code changes needed in AssetLoader** - it automatically scans the `monsters/` directory!

### Adding Player/Effect Assets

Edit the appropriate JSON file (`player-assets.json`, `effects-assets.json`, etc.)

## Benefits

✅ **Fully Modular** - Each monster has its own asset file  
✅ **Flexible Structure** - Monsters can have different animations (walk, swim, fly, etc.)  
✅ **Data-driven** - Add monsters without touching Java code  
✅ **No recompilation** - Asset changes don't require rebuilds  
✅ **Auto-discovery** - Drop a JSON file in `monsters/` and it's automatically loaded  
✅ **Easy maintenance** - Small, focused files instead of one giant manifest  
✅ **Supports modding** - External mods can add their own monster asset files  

## Migration Path

The old `Assets` class is now a thin wrapper around `AssetRegistry`. Over time:
1. Replace `assets.createXXX()` calls with `registry.getAnimation("xxx")`
2. Replace `assets.getXXX()` calls with `registry.getTexture("xxx")`
3. Eventually remove the compatibility layer entirely

