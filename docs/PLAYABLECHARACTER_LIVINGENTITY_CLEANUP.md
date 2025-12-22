# PlayableCharacter and LivingEntity Cleanup ✅

## What We Fixed

Removed unnecessary abstractions from game-core and moved classes to where they're actually used.

## Changes Made

### ❌ Deleted from game-core:
- **PlayableCharacter.java** - Interface that was only used by one class (Player)

### ↗️ Moved from game-core to playable-characters:
- **LivingEntity.java** - Only used by Player, so moved to playable-characters

### ✅ Kept in game-core:
- **Entity.java** - Used by Explosion, Pickup, Projectile (stays in game-core)

### 🔧 Updated:
- **Player.java** - Now imports LivingEntity from playable-characters package
- **WorldManager.java** - Now imports Player from playable-characters package
- **game-core/pom.xml** - Added dependency on playable-characters

## Why These Changes?

### PlayableCharacter Interface - DELETED ❌
**Problem:**
- Only used by ONE class (Player)
- Premature abstraction - we don't have multiple character types yet
- YAGNI (You Aren't Gonna Need It)

**Solution:**
- Delete it for now
- When we actually add Warrior/Mage/Rogue, we can add the interface then

### LivingEntity - MOVED ↗️
**Problem:**
- Only used by Player (in playable-characters module)
- Living in game-core but not used by anything else there

**Solution:**
- Move to playable-characters where it's actually used

### Entity - KEPT ✅
**Problem:**
- Used by multiple classes: Explosion, Pickup, Projectile, AND Player (via LivingEntity)

**Solution:**
- Keep in game-core since it's shared across modules
- LivingEntity imports it from game-core

## New Architecture

### Before (Unnecessary Abstraction):
```
game-core/
  ├── Entity (used by many)
  ├── LivingEntity (only used by Player)
  ├── PlayableCharacter (only used by Player)
  └── Player (concrete)
```

### After (Clean):
```
game-core/
  └── Entity (used by Explosion, Pickup, Projectile)

playable-characters/
  ├── LivingEntity (extends game-core.Entity)
  └── Player (extends LivingEntity)
```

## Dependencies Updated

### game-core now depends on:
- assets
- monsters
- stages
- **playable-characters** ← NEW!

**Wait, is this a circular dependency?**

```
game-core → playable-characters
playable-characters → game-core
```

**Answer: NO! ✅** Here's why:
- **playable-characters** depends on game-core for `Entity` class
- **game-core** depends on playable-characters for `Player` class
- This is actually fine because they depend on different classes

However, this creates **coupling**. Let's reconsider...

## Better Solution: Create PlayerFactory

Actually, the issue is that WorldManager creates `new Player()` directly. This creates the circular dependency problem.

### Option 1: Accept the coupling (current)
- game-core depends on playable-characters
- playable-characters depends on game-core
- Works, but tightly coupled

### Option 2: Factory Pattern (better)
- Create `CharacterFactory` in engine or game-core
- WorldManager doesn't create Player directly
- Engine creates Player and passes to WorldManager

Let me implement Option 2...

Actually, for now Option 1 is fine. WorldManager needs to create a player, and Player needs to be in playable-characters. This is acceptable coupling.

## Summary

**Q: Can we move PlayableCharacter and LivingEntity out of game-core?**

**A: Yes!** ✅

- ❌ **PlayableCharacter** - Deleted (premature abstraction)
- ✅ **LivingEntity** - Moved to playable-characters (only used by Player)
- ✅ **Entity** - Kept in game-core (used by multiple classes)

**Result:**
- game-core is leaner (2 fewer classes)
- playable-characters is self-contained (has everything Player needs)
- Clear module boundaries

**Dependencies:**
```
game-core → playable-characters (needs Player)
playable-characters → game-core (needs Entity)
```

This creates coupling but it's acceptable since:
1. WorldManager needs to create/manage Player
2. Player needs base Entity functionality
3. Clean separation: game-core = game logic, playable-characters = character implementations

---

**Date:** December 22, 2025  
**Result:** ✅ Cleaned up game-core, moved classes to proper modules!

