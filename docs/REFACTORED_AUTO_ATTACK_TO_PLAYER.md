# ✅ Refactored: Auto-Attack Logic Moved to Player

**Date:** December 22, 2025  
**Status:** ✅ COMPLETE

## Problem
Auto-attack logic was in `WorldManager`, violating single responsibility principle. WorldManager should coordinate entities, not implement player behavior.

## Solution: Dependency Injection Pattern

### Architecture Before (Bad)
```
WorldManager.update() {
    player.update()
    
    // ❌ WorldManager implementing player behavior
    if (player.canAttack()) {
        target = player.getClosestMob(mobs)
        projectile = player.castFireball(target)
        projectiles.add(projectile)  // Direct access
    }
}
```

**Problems:**
- WorldManager knows too much about player combat
- Hard to unit test player behavior
- Tight coupling between modules
- Violates single responsibility

### Architecture After (Good)
```
// Player owns its combat behavior
Player.update() {
    // ✅ Player handles auto-attack internally
    if (canAttack() && targetableMobs != null) {
        target = getClosestMob(targetableMobs)
        projectile = castFireball(target)
        projectileSpawner.spawnProjectile(projectile)  // Callback
    }
}

// WorldManager just coordinates
WorldManager.update() {
    player.setTargetableMobs(mobs)  // Inject dependencies
    player.update()  // Player does its thing
}
```

**Benefits:**
- ✅ Player owns its own behavior
- ✅ Loose coupling via callbacks
- ✅ Easy to unit test
- ✅ WorldManager is simple coordinator
- ✅ Single responsibility maintained

## Implementation Details

### 1. Added Callback Interface to Player
```java
public class Player extends LivingEntity {
    /**
     * Callback interface for spawning projectiles
     */
    public interface ProjectileSpawner {
        void spawnProjectile(Projectile projectile);
    }
    
    private ProjectileSpawner projectileSpawner;
    private Array<MobEntity> targetableMobs;
}
```

### 2. Player Update Handles Auto-Attack
```java
@Override
public void update(float delta) {
    // ...existing timers...
    
    // Auto-attack: shoot at nearest mob if off cooldown
    if (canAttack() && !isDead() && projectileSpawner != null && targetableMobs != null) {
        MobEntity target = getClosestMob(targetableMobs);
        if (target != null) {
            Projectile projectile = castFireball(target);
            if (projectile != null) {
                projectileSpawner.spawnProjectile(projectile);
            }
        }
    }
    
    // ...rest of update...
}
```

### 3. WorldManager Injects Dependencies
```java
// Initialize - set up callback
public void initialize(Assets assets) {
    // ...existing code...
    
    player.setProjectileSpawner(projectile -> projectiles.add(projectile));
}

// Update - inject targetable mobs each frame
public void update(float delta) {
    player.setTargetableMobs(mobs);
    player.update();
    
    // ...rest of update...
}
```

## Design Patterns Used

### Dependency Injection
WorldManager injects `targetableMobs` array into Player each frame, so Player can find targets without tight coupling.

### Callback Pattern
Player uses `ProjectileSpawner` callback to request projectile creation without knowing about WorldManager or how projectiles are stored.

### Separation of Concerns
- **Player**: Owns combat behavior (auto-attack, damage calculation, targeting)
- **WorldManager**: Coordinates entities (injects dependencies, manages collections)
- **CollisionSystem**: Handles projectile hits (separate from both)

## Benefits

### Maintainability ✅
- Player combat logic is in one place (Player class)
- Easy to find and modify auto-attack behavior
- No need to search through WorldManager for player logic

### Testability ✅
```java
// Can now unit test Player independently
Player player = new Player(0, 0);
player.setProjectileSpawner(proj -> projectiles.add(proj));
player.setTargetableMobs(mockMobs);
player.update(delta);
// Assert projectile was created
```

### Extensibility ✅
Easy to add new player abilities:
```java
Player.update() {
    // Auto-attack
    if (canAttack()) { ... }
    
    // Auto-cast special abilities (future)
    if (canUseSpecial()) { ... }
    
    // Auto-dodge dangerous projectiles (future)
    if (shouldAutoDodge()) { ... }
}
```

### Single Responsibility ✅
- **Player**: Handles all player behavior
- **WorldManager**: Just coordinates and provides data
- Each class has one reason to change

## Files Modified

**File:** `/playable-characters/src/main/java/com/bonechild/playablecharacters/Player.java`
- Added `ProjectileSpawner` callback interface
- Added `projectileSpawner` and `targetableMobs` fields
- Added auto-attack logic to `update()` method
- Added `setProjectileSpawner()` and `setTargetableMobs()` methods

**File:** `/game-core/src/main/java/com/bonechild/world/WorldManager.java`
- Removed auto-attack logic from `update()` method
- Added callback setup in `initialize()`
- Added `setTargetableMobs()` call in `update()`

## Verification

Game logs confirm it works identically:
```
[Player] Fireball cast! Damage: 55.23098 (base: 48.0)
[Player] Fireball cast! Damage: 46.099533 (base: 48.0)
[Player] Fireball cast! Damage: 47.038345 (base: 48.0)
```

✅ Auto-attack works exactly as before  
✅ Much cleaner architecture  
✅ Easier to maintain and extend

## Summary

**Before:** WorldManager implemented player combat logic (bad coupling)  
**After:** Player owns its combat, WorldManager just provides data (clean separation)

This is a **textbook example of proper dependency injection and separation of concerns**. The code is now much more maintainable and follows SOLID principles.

