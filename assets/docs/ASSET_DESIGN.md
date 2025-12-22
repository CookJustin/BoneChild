# Asset System Design - Final Architecture

## Overview
BoneChild uses a **fully modular, per-entity asset system**. Each monster, player, and game system can define its own assets independently.

## Core Principle
> **Each entity owns its asset definition.** No shared structure or monolithic files.

---

## Architecture Diagram

```
┌──────────────────────────────────────────────────────┐
│                  AssetRegistry                       │
│  ┌────────────────────────────────────────────────┐ │
│  │  Map<String, Texture>   textures               │ │
│  │  Map<String, Animation> animations             │ │
│  └────────────────────────────────────────────────┘ │
│                                                      │
│  • getTexture(id) → Texture                         │
│  • getAnimation(id) → Animation (independent copy)  │
│  • dispose()                                        │
└──────────────────────────────────────────────────────┘
                         ▲
                         │ populates
                         │
┌──────────────────────────────────────────────────────┐
│                   AssetLoader                        │
│  • loadFromModules()                                 │
│    ├─ player-assets.json                            │
│    ├─ effects-assets.json                           │
│    └─ monsters/*.json (auto-scans directory)        │
│                                                      │
│  • loadFromJson(path)                               │
│  • loadFromDirectory(path)                          │
└──────────────────────────────────────────────────────┘
                         ▲
                         │ uses
                         │
┌──────────────────────────────────────────────────────┐
│                    Assets                            │
│  Static Singleton Facade                             │
│                                                      │
│  • Assets.load()         → loads all                │
│  • Assets.animation(id)  → get animation            │
│  • Assets.texture(id)    → get texture              │
│  • Assets.dispose()      → cleanup                  │
│  • Assets.getRegistry()  → advanced access          │
└──────────────────────────────────────────────────────┘
```

---

## File Structure

```
assets/src/main/resources/
├── player-assets.json              # Player-specific assets
├── effects-assets.json             # Shared effects (explosions, projectiles)
├── ui-assets.json                  # UI elements
├── stages-assets.json              # Stage backgrounds
│
└── monsters/                       # Per-monster asset files
    ├── boss08b-assets.json        # Boss08B: idle, walk, 3 attacks, death, damage
    ├── orc-assets.json            # Orc: walk, death
    ├── glob-assets.json           # Glob: walk, death
    ├── enemy17b-assets.json       # Enemy17B: walk, death
    ├── vampire-assets.json        # Vampire: body/head/magic textures
    ├── goblin-assets.json         # Goblin: walk, death
    └── christmas-jad-assets.json  # ChristmasJad: single texture
```

---

## Key Design Decisions

### 1. Per-Monster Asset Files
**Why?** Each monster has different capabilities.

```json
// flying-dragon-assets.json
{
  "animations": {
    "dragon_fly": { ... },
    "dragon_hover": { ... },
    "dragon_dive": { ... }
  }
}

// swimming-fish-assets.json  
{
  "animations": {
    "fish_swim": { ... },
    "fish_dive": { ... }
  }
}
```

**No shared structure enforcement.** Each monster defines what it needs!

### 2. Auto-Discovery from Directory
`AssetLoader` scans `monsters/` and loads all `*.json` files automatically.

**Adding a new monster:**
1. Drop `new-monster-assets.json` in `monsters/` folder
2. Done! No code changes needed.

### 3. Static Singleton Assets Class
```java
// Simple, clean API
Animation walk = Assets.animation("player_walk");
Texture tex = Assets.texture("tileset");
```

**Why static?**
- Assets are inherently global in games
- Avoids passing objects through 10 constructors
- Industry standard (Unity, Unreal, Godot)

### 4. Registry Returns Independent Copies
```java
Animation anim1 = registry.getAnimation("player_walk");
Animation anim2 = registry.getAnimation("player_walk");
// anim1 and anim2 have independent state!
```

Each animation instance tracks its own frame timing.

---

## Usage Examples

### Game Initialization
```java
public void create() {
    Assets.load(); // Loads all JSON manifests
}
```

### In Monster Code
```java
public class FlyingDragon extends Mob {
    private Animation flyAnimation;
    private Animation hoverAnimation;
    
    public FlyingDragon(float x, float y, Player player, Assets assets) {
        super(x, y, player);
        
        var registry = assets.getRegistry();
        this.flyAnimation = registry.getAnimation("dragon_fly");
        this.hoverAnimation = registry.getAnimation("dragon_hover");
    }
}
```

### In Renderer
```java
Texture tileset = Assets.texture("tileset");
Animation explosion = Assets.animation("explosion");
```

---

## Adding New Monsters

**Step 1:** Create asset file
```
assets/src/main/resources/monsters/sea-serpent-assets.json
```

```json
{
  "animations": {
    "serpent_swim": {
      "type": "frame_sequence",
      "pattern": "assets/monsters/SeaSerpent_Swim_{1-8}.png",
      "frameTime": 0.12,
      "loop": true
    },
    "serpent_surface": {
      "type": "frame_sequence",
      "pattern": "assets/monsters/SeaSerpent_Surface_{1-4}.png",
      "frameTime": 0.15,
      "loop": false
    }
  }
}
```

**Step 2:** Use in code
```java
public class SeaSerpent extends Mob {
    public SeaSerpent(float x, float y, Player player, Assets assets) {
        super(x, y, player);
        
        var registry = assets.getRegistry();
        this.swimAnimation = registry.getAnimation("serpent_swim");
        this.surfaceAnimation = registry.getAnimation("serpent_surface");
    }
}
```

**That's it!** No changes to AssetLoader, no recompilation.

---

## Benefits

✅ **Fully Modular** - Each entity owns its assets  
✅ **Flexible** - Different monsters have different animations  
✅ **Data-Driven** - No Java code changes to add assets  
✅ **Auto-Discovery** - Drop JSON in folder, done  
✅ **Clean API** - `Assets.animation("id")`  
✅ **Maintainable** - Small focused files  
✅ **Moddable** - Easy for external mods  

---

## JSON Format Reference

### Frame Sequence (Individual PNGs)
```json
{
  "animations": {
    "my_anim": {
      "type": "frame_sequence",
      "pattern": "path/Frame{1-6}.png",
      "frameTime": 0.1,
      "loop": true
    }
  }
}
```

### Sprite Sheet (Single Row)
```json
{
  "textures": {
    "my_sheet": "path/sheet.png"
  },
  "animations": {
    "my_anim": {
      "type": "sprite_sheet",
      "texture": "my_sheet",
      "row": 0,
      "frames": 6,
      "frameWidth": 64,
      "frameHeight": 64,
      "frameTime": 0.1,
      "loop": true
    }
  }
}
```

### Sprite Sheet (Multi-Row)
```json
{
  "animations": {
    "my_anim": {
      "type": "sprite_sheet_multi_row",
      "texture": "my_sheet",
      "startRow": 0,
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

---

## Migration from Old System

**Before:**
```java
Animation anim = assets.createPlayerWalkAnimation();
Texture tex = assets.getTilesetTexture();
```

**After:**
```java
Animation anim = Assets.animation("player_walk");
Texture tex = Assets.texture("tileset");
```

Cleaner, more consistent, fully data-driven! 🎉

