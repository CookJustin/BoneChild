# Game Core Module
- [ ] Network serialization for multiplayer
- [ ] Add save/load system
- [ ] Separate data models from logic (ECS pattern)
- [ ] Add event system for decoupled communication
- [ ] Extract interfaces for Player, WorldManager
## Future Enhancements

✅ **Reusable** - Could be used for server, replay system, etc.
✅ **Testable** - Can test game logic without graphics  
✅ **No UI dependencies** - UI depends on us, not vice versa  
✅ **No rendering code** - Just data and logic  

## Design Principles

```
└────┘    └────┘   └────────┘
│    │    │der │   │        │
│ ui │    │ren-│   │ engine │
┌─▼──┐    ┌──▼─┐   ┌───▼────┐
  │          │         │
  ┌────┴─────┬─────────┐
       │
   Used by
       │
└──────┬───────┘
│  game-core   │  ← Pure domain logic
┌──────────────┐
```

This module is **pure game logic** - no rendering, no UI, no I/O.

## Architecture

- Kill streaks and combos
- Power-up system
- XP and leveling
- Wave system
### Game Progression

- Entity removal when dead/collected
- Collision detection
- Updating entity state each frame
- Spawning entities (mobs, pickups, projectiles)
### Entity Lifecycle

```
world.spawnMob(x, y);
world.update(delta);
world.setAssets(assets);
WorldManager world = new WorldManager();
```java
### World State

```
player.heal(50);
player.addExperience(100);
player.update(delta);
Player player = new Player(x, y);
```java
### Player Management

## Key Responsibilities

- `engine` - For game loop orchestration and rendering
- `ui` - For displaying game state (health, wave info, stats)
## Used By

- LibGDX Core - For math and graphics types
- `monsters` - For mob entities
- `assets` - For textures and animations
## Dependencies

- **PlayerInput** - Input handling for player controls
### Input (`com.bonechild.input`)

- **ComboSystem** - Kill streak tracking
- **Explosion** - AOE explosion effects
- **Projectile** - Player projectiles (fireballs)
- **Pickup** - Collectibles (gold, XP, health orbs)
- **LivingEntity** - Entities with health (Player, mobs)
- **Entity** - Base entity class (position, velocity)
- **WorldManager** - Entity lifecycle, wave spawning, collision detection
- **Player** - Player character with stats, abilities, progression
### World (`com.bonechild.world`)

## What's in this module

Core game logic and domain models for BoneChild. Contains the fundamental game mechanics, entities, and state management that are independent of rendering or UI.
## Purpose


