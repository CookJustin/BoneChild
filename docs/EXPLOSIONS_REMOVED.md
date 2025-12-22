# Explosions Removed from WorldManager ✅

## Your Question: "Why do we still have the updateExplosions method?"

**Answer: You're right - we shouldn't!** 

I've now removed it completely.

---

## What Was Removed

### ❌ Explosions array
```java
// REMOVED
private Array<Explosion> explosions;
```

### ❌ updateExplosions() method
```java
// REMOVED - 13 lines
private void updateExplosions(float delta) {
    for (int i = explosions.size - 1; i >= 0; i--) {
        explosion.update(delta);
        if (!explosion.isActive()) {
            explosions.removeIndex(i);
        }
    }
}
```

### ❌ addExplosion() method
```java
// REMOVED
public void addExplosion(Explosion explosion) {
    explosions.add(explosion);
}
```

### ❌ getExplosions() getter
```java
// REMOVED
public Array<Explosion> getExplosions() { return explosions; }
```

---

## Why Remove It?

### Explosions are NOT game state - they're visual effects!

**Game state (belongs in game-core):**
- ✅ Player health/position
- ✅ Mob health/position
- ✅ Pickup existence
- ✅ Projectile position

**Visual effects (belongs in Engine):**
- ❌ Explosions
- ❌ Particle effects
- ❌ Screen shake
- ❌ Damage numbers

### Explosions should be in the Engine's ParticleSystem or EffectsManager

When a mob dies with "Explosion on Kill" power-up:
1. **game-core**: Mob.isDead() = true
2. **Engine** checks: Should this trigger explosion? (player has power-up)
3. **Engine** spawns: Visual explosion effect (animation)
4. **Engine** checks: Which mobs are in radius?
5. **Engine** applies: Damage to those mobs

**WorldManager doesn't need to know about explosions at all.**

---

## Current WorldManager State

### What it manages now:
✅ **Player** - Game state  
✅ **Mobs** - Game state  
✅ **Pickups** - Game state  
✅ **Projectiles** - Game state  

### What it does NOT manage:
❌ **Explosions** - Visual effect (Engine)  
❌ **Collisions** - Detection logic (Engine)  
❌ **Combat** - Damage application (Engine/Player)  
❌ **Loot** - Drop logic (Mob/LootSystem)  

---

## Final WorldManager Responsibilities

```java
/**
 * Manages all entities in the game world
 *
 * Responsibilities:
 * - Initialize game entities (player, mobs, pickups, projectiles)
 * - Update entity internal state (position, animation)
 * - Remove dead/inactive entities (cleanup)
 * - Coordinate wave spawning via StageSpawner
 * - Provide access to entities (for Engine)
 *
 * NOT responsible for:
 * - Collision detection (Engine)
 * - Combat mechanics (Engine/Player)
 * - Visual effects (Engine)
 * - Pickup collection (Player)
 * - Loot drops (Mob)
 */
```

---

## Where Explosion Logic Should Go

### Option 1: Engine's ParticleSystem
```java
// In Engine
public class ParticleSystem {
    private Array<ExplosionEffect> explosions;
    
    public void spawnExplosion(float x, float y, float damage, float radius) {
        ExplosionEffect explosion = new ExplosionEffect(x, y, animation);
        explosions.add(explosion);
        
        // Check which mobs are in radius
        for (MobEntity mob : worldManager.getMobs()) {
            if (inRadius(mob, x, y, radius)) {
                mob.takeDamage(damage);
            }
        }
    }
    
    public void update(float delta) {
        // Update and render explosions
    }
}
```

### Option 2: Engine's CombatSystem
```java
// In Engine
public class CombatSystem {
    public void onMobDeath(MobEntity mob) {
        // Check explosion chance
        if (player.hasExplosionPowerUp() && shouldExplode()) {
            triggerExplosion(mob.getX(), mob.getY());
        }
    }
    
    private void triggerExplosion(float x, float y) {
        // Spawn visual effect
        particleSystem.spawnExplosion(x, y);
        
        // Deal AOE damage
        float radius = 100f;
        float damage = player.getAttackDamage() * 0.25f;
        for (MobEntity mob : worldManager.getMobs()) {
            if (inRadius(mob, x, y, radius)) {
                mob.takeDamage(damage);
            }
        }
    }
}
```

---

## Summary

**Q: Why do we still have updateExplosions?**  
**A: We don't anymore!** ✅

Explosions are **visual effects**, not game state. They belong in:
- Engine's ParticleSystem
- Engine's EffectsManager  
- Engine's CombatSystem

WorldManager is now **pure game state management**:
- Player ✅
- Mobs ✅
- Pickups ✅
- Projectiles ✅

**No visual effects. No combat logic. Just entity lifecycle.** 🎯

---

**Result:** WorldManager is now **truly minimal** and focused solely on managing entity lifecycle!

