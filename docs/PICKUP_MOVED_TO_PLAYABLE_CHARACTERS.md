# Pickup Moved to Playable-Characters Module ✅

## What Changed

Moved `Pickup.java` from `game-core` to `playable-characters` module.

## Why This Change?

**Your insight was correct!** Pickup is something only players interact with:
- ✅ Players collect pickups (gold, XP, health)
- ✅ Pickup has magnetic pull toward player
- ✅ Pickup checks distance to player for collection
- ❌ Mobs don't interact with pickups
- ❌ Projectiles don't interact with pickups

**Pickup is player functionality, not core game logic!**

## Changes Made

### 1. Moved Pickup
```
game-core/src/main/java/com/bonechild/world/Pickup.java
    ↓
playable-characters/src/main/java/com/bonechild/playablecharacters/Pickup.java
```

### 2. Updated Pickup
- Changed package to `com.bonechild.playablecharacters`
- Made it standalone (no longer extends GameObject)
- Added own position/width/height fields
- Removed `@Override` annotations

### 3. Deleted GameObject
- Was only used by Pickup
- No longer needed in game-core

### 4. Updated WorldManager
- Imports Pickup from `playablecharacters` module
- game-core depends on playable-characters for Player AND Pickup

## New Module Structure

### playable-characters module now contains:
```
playable-characters/
├── Entity.java          # Base entity (position, size)
├── LivingEntity.java    # Entity with health
├── Player.java          # Playable character
└── Pickup.java          # Collectible items (NEW!)
```

### game-core module contains:
```
game-core/
├── WorldManager.java    # Manages all entities
├── Projectile.java      # Player attacks
└── (other game logic)
```

## Dependencies

```
game-core → playable-characters (needs Player and Pickup)
playable-characters → game-core (needs Projectile)
```

This creates a **bidirectional dependency**, which is acceptable because:
1. **game-core** manages Player and Pickups (entity lifecycle)
2. **playable-characters** creates Projectiles (Player attacks)
3. They depend on different classes, not circular on same class

## Benefits

✅ **Pickup is with Player** - Makes sense conceptually  
✅ **playable-characters is more complete** - Has everything related to player gameplay  
✅ **game-core is leaner** - Removed GameObject and Pickup  
✅ **Clear module boundaries** - Player stuff in playable-characters  

## What's in Each Module Now

| Module | Contains |
|--------|----------|
| **playable-characters** | Player, Pickup, Entity, LivingEntity |
| **game-core** | WorldManager, Projectile, game logic |
| **monsters** | Mob, Goblin, Boss08B |
| **stages** | StageSpawner, wave definitions |
| **engine** | Rendering, collision, orchestration |

---

**Result:** Pickup is now in playable-characters where it belongs! 🎉

**Date:** December 22, 2025  
**Status:** ✅ Complete - Pickup moved to playable-characters

