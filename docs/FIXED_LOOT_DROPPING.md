# ✅ FIXED: Mobs Not Dropping Loot

**Date:** December 22, 2025  
**Status:** ✅ RESOLVED

## Problem
Mobs weren't dropping any loot (gold, XP, health) when they died during the refactoring.

## Root Cause
The loot dropping logic was completely removed from the collision system. When we refactored to use CollisionSystem for handling projectile hits, we forgot to add the loot spawning code.

**Old system:** Mob death was handled in WorldManager with loot spawning  
**After refactoring:** CollisionSystem handled mob death but didn't spawn any loot

## Solution
Added loot dropping functionality to CollisionSystem using the same callback pattern we used for player auto-attack.

### Implementation

#### 1. Added PickupSpawner Callback to CollisionSystem
```java
public interface PickupSpawner {
    void spawnPickup(Pickup pickup);
}

private PickupSpawner pickupSpawner;

public void setPickupSpawner(PickupSpawner spawner) {
    this.pickupSpawner = spawner;
}
```

#### 2. Added Loot Spawning on Mob Death
```java
private void spawnLootForMob(MobEntity mob, Player player) {
    if (pickupSpawner == null) return;
    
    float mobCenterX = mob.getX() + mob.getWidth() / 2f;
    float mobCenterY = mob.getY() + mob.getHeight() / 2f;
    
    // Always drop XP (scaled by kill streak)
    float xpAmount = 10f * player.getKillStreakMultiplier();
    pickupSpawner.spawnPickup(new Pickup(mobCenterX, mobCenterY, Pickup.PickupType.XP_ORB, xpAmount));
    
    // 50% chance for gold (scaled by streak)
    if (Math.random() < 0.5f) {
        int goldAmount = (int)(5f * player.getKillStreakMultiplier());
        pickupSpawner.spawnPickup(new Pickup(mobCenterX + 10f, mobCenterY, Pickup.PickupType.GOLD_COIN, goldAmount));
    }
    
    // 10% chance for health orb
    if (Math.random() < 0.1f) {
        pickupSpawner.spawnPickup(new Pickup(mobCenterX - 10f, mobCenterY, Pickup.PickupType.HEALTH_ORB, 20f));
    }
}
```

#### 3. Wired Callback in BoneChildGame
```java
// Wire up collision system to spawn loot
collisionSystem.setPickupSpawner(worldManager.getPickupAdder()::accept);
```

#### 4. Added Getter in WorldManager
```java
public java.util.function.Consumer<Pickup> getPickupAdder() {
    return pickup -> pickups.add(pickup);
}
```

## Loot Drop Rates

### Always Drops
- **XP Orb:** 10 XP × kill streak multiplier

### Chance-Based Drops
- **Gold Coin (50%):** 5 gold × kill streak multiplier
- **Health Orb (10%):** 20 HP restore

### Kill Streak Multipliers
- 1-4 kills: 1.0x
- 5-9 kills: 1.5x
- 10-24 kills: 2.0x
- 25-49 kills: 2.5x
- 50+ kills: 3.0x

## Verification

Game logs confirm loot is spawning:
```
[CollisionSystem] Projectile hit mob: goblin dmg=50.09936
[CollisionSystem] Spawned loot for goblin
[WorldManager] Mob died. Remaining: 3

[CollisionSystem] Projectile hit mob: goblin dmg=55.92974
[CollisionSystem] Spawned loot for goblin
[WorldManager] Mob died. Remaining: 3
```

## Architecture Benefits

✅ **Consistent Pattern** - Same callback approach as player auto-attack  
✅ **Loose Coupling** - CollisionSystem doesn't depend on WorldManager  
✅ **Single Responsibility** - CollisionSystem handles collision outcomes  
✅ **Maintainable** - Loot logic is in one place  
✅ **Testable** - Can inject mock pickup spawner  

## Files Modified

1. **`/engine/src/main/java/com/bonechild/collision/CollisionSystem.java`**
   - Added `PickupSpawner` callback interface
   - Added `spawnLootForMob()` method
   - Updated `processProjectileHits()` to spawn loot on mob death

2. **`/game-core/src/main/java/com/bonechild/world/WorldManager.java`**
   - Added `getPickupAdder()` method to expose pickup collection callback

3. **`/engine/src/main/java/com/bonechild/BoneChildGame.java`**
   - Wired up `collisionSystem.setPickupSpawner()` on game start

## How It Works Now

```
Projectile hits mob
  ↓
CollisionSystem.processProjectileHits()
  ↓
mob.takeDamage()
  ↓
if (mob.isDead())
  ↓
player.incrementKillStreak()
  ↓
spawnLootForMob()
  ↓
pickupSpawner.spawnPickup() (callback)
  ↓
WorldManager adds pickup to array
  ↓
Pickup applies magnetic pull
  ↓
Player collects when close enough
```

## Status: ✅ Working

Loot is now spawning correctly with:
- XP for progression
- Gold for future shop/upgrades
- Health orbs for survival
- Kill streak multipliers for skilled play

The game economy is restored! 💰✨

