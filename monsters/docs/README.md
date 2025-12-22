# Monsters Module

## Purpose
Provides monster/boss entities with behavior, animations, and spawning logic for BoneChild.

## What's in this module

### API (`com.bonechild.monsters.api`)
- **MobEntity** - Interface for all mobs (position, health, boss flag, etc.)
- **MobFactory** - Factory interface for creating mobs by type ID
- **SpawnContext** - Spawn position and parameters

### Core (`com.bonechild.monsters.core`)
- **DefaultMobFactory** - Concrete factory implementation that registers all mob types

### Implementations (`com.bonechild.monsters.impl`)
- **Mob** - Base mob class with AI, movement, and attack logic
- **Boss08B** - Epic boss with multiple attack animations
- **Orc** - Medium strength melee fighter
- **Glob** - Fast, weak blob enemy
- **Enemy17B** - Red demon with high HP
- **Vampire** - Multi-layered sprite enemy
- **ChristmasJad** - Holiday boss enemy
- **Goblin** - Small fast enemy

## Dependencies
- `assets` module - For sprites and animations
- LibGDX Core - For math and rendering types

## Used By
- `engine` - WorldManager spawns mobs via MobFactory

## Architecture

### Type-based Spawning
Mobs are spawned by string ID through the factory:

```java
MobFactory factory = new DefaultMobFactory(player, assets);
MobEntity mob = factory.create("boss08b", new SpawnContext(x, y));
```

### Each mob has a unique type ID:
- `"mob"` - Generic base mob
- `"glob"` - Glob enemy
- `"orc"` - Orc enemy
- `"boss08b"` - Boss08B
- `"enemy17b"` - Enemy17B (Red Demon)
- `"vampire"` - Vampire
- `"christmas_jad"` - Christmas Jad
- `"goblin"` - Goblin

## Adding a New Monster

### 1. Create the monster class
```java
package com.bonechild.monsters.impl;

public class NewMonster extends Mob {
    @Override
    public String getTypeId() { return "new_monster"; }
    
    public NewMonster(float x, float y, Player player, Assets assets) {
        super(x, y, player);
        // Set stats, hitbox, load animations
    }
}
```

### 2. Register in DefaultMobFactory
```java
register("new_monster", ctx -> 
    new NewMonster(ctx.getPosition().x, ctx.getPosition().y, player, assets)
);
```

### 3. Add sprites to monsters-assets.json
```json
{
  "animations": {
    "new_monster_walk": {
      "type": "frame_sequence",
      "pattern": "assets/monsters/NewMonster{1-6}.png",
      "frameTime": 0.1,
      "loop": true
    }
  }
}
```

### 4. Spawn it
```java
MobEntity mob = mobFactory.create("new_monster", new SpawnContext(x, y));
```

## Future Enhancements
- [ ] Data-driven mob definitions (stats, AI in JSON)
- [ ] Separate AI behaviors from mob classes
- [ ] Monster ability/skill system
- [ ] Loot tables per mob type

