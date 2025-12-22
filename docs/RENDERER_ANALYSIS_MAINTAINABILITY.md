# Renderer Analysis & Maintainability Plan

## What Renderer Currently Does

Renderer is the **central rendering system** that draws everything on screen.

### Renders:
1. ✅ **Background** - Tile map
2. ✅ **Player** - Animations, ghost trails, health bar, invincibility flash
3. ✅ **Mobs** - Calls `mob.render()`, draws health bars
4. ✅ **Pickups** - Gold coins, XP orbs, health orbs
5. ✅ **Projectiles** - Fireballs with rotation
6. ❌ **Explosions** - REMOVED (dead code, Explosion class deleted)
7. ✅ **Effects** - Particles, damage numbers, screen effects
8. ✅ **Camera** - Shake, updates
9. ✅ **Debug** - Hitboxes

---

## Problems Identified

### 1. ❌ Inconsistent Architecture
**Player:** Renderer manages animations
```java
private Animation playerIdleAnimation;
private Animation playerWalkAnimation;
// Renderer controls player animation state
```

**Mobs:** Mobs manage their own animations
```java
// Goblin.render(batch) - Goblin handles its own animations
```

**Problem:** Inconsistent! Either ALL entities manage animations OR Renderer does.

### 2. ❌ renderExplosions() is Dead Code
- References `com.bonechild.world.Explosion` which was deleted
- Should use ParticleSystem instead
- **FIXED** ✅

### 3. ❌ Renderer Controls Batch Lifecycle
```java
public void renderMobs(Array<MobEntity> mobs) {
    batch.begin(); // Renderer controls this
    mob.render(batch); // But mob needs the batch
    batch.end(); // Renderer controls this
}
```

**Issue:** Tight coupling. Mobs can't render without Renderer coordinating.

### 4. ❌ Projectiles Render in Renderer
Should projectiles render themselves like mobs do?

---

## Recommended Architecture for Maintainability

### Design Principle: **Entities Render Themselves**

Each entity type manages its own:
- Animations
- Sprite rendering
- Visual state

Renderer just:
- Coordinates batch lifecycle
- Provides batch to entities
- Handles shared effects (health bars, etc.)

### Example:

```java
// GOOD: Mob renders itself
public class Goblin extends Mob {
    private Animation walkAnimation;
    private Animation deathAnimation;
    
    public void render(SpriteBatch batch) {
        // Goblin decides what to draw
        Animation current = isDead() ? deathAnimation : walkAnimation;
        current.update(deltaTime);
        batch.draw(current.getCurrentFrame(), x, y, width, height);
    }
}

// GOOD: Renderer just coordinates
public void renderMobs(Array<MobEntity> mobs) {
    batch.begin();
    for (MobEntity mob : mobs) {
        if (mob instanceof Goblin) {
            ((Goblin) mob).render(batch);
        } else if (mob instanceof Boss08B) {
            ((Boss08B) mob).render(batch);
        }
    }
    batch.end();
}
```

---

## What Needs to Change

### Phase 1: Move Player Animations to Player ✅ HIGH PRIORITY

**Currently:**
```java
// Renderer.java
private Animation playerIdleAnimation;
private Animation playerWalkAnimation;
```

**Should be:**
```java
// Player.java (in playable-characters module)
private Animation idleAnimation;
private Animation walkAnimation;

public void render(SpriteBatch batch) {
    // Player handles its own rendering
}
```

**Benefits:**
- Consistent with mobs
- Player module is self-contained
- Renderer doesn't need to know about player states

### Phase 2: Projectile Self-Rendering (Optional)

**Currently:**
```java
// Renderer.java
public void renderProjectiles(Array<Projectile> projectiles) {
    Animation fireballAnim = assets.getFireballAnimation();
    // ... Renderer handles all projectile rendering
}
```

**Could be:**
```java
// Projectile.java
public void render(SpriteBatch batch, Animation fireballAnim) {
    // Projectile renders itself
}
```

### Phase 3: Pickup Self-Rendering (Optional)

Same pattern as projectiles.

---

## For Multiple Stages & Waves

### Current Design Works! ✅

```java
// Stage spawns mobs via StageSpawner
stageSpawner.update(delta, mobs); // Adds mobs to list

// Renderer renders whatever mobs exist
renderer.renderMobs(mobs); // Generic - works for any mob!
```

**Why it works:**
- Renderer doesn't care what mobs exist
- Mobs render themselves
- Health bars use MobEntity interface (generic)
- Stages just populate the mobs array

**To add new mob type:**
1. Create mob class (e.g., `Skeleton.java`)
2. Add to MobFactory
3. Add one line to Renderer:
```java
} else if (mob instanceof Skeleton) {
    ((Skeleton) mob).render(batch);
}
```
4. Use in stage JSON

---

## For Mob Attacks & Animations

### Current Approach: ✅ Mobs Manage Own Animations

```java
// Boss08B.java
private Animation walkAnimation;
private Animation attack1Animation;
private Animation deathAnimation;

public void render(SpriteBatch batch) {
    Animation current = getCurrentAnimation(); // Boss decides
    batch.draw(current.getCurrentFrame(), x, y, width, height);
}
```

**This is GOOD!** Each mob:
- Has its own animations
- Decides which animation to show
- Renders itself
- Renderer doesn't need to know details

### For Mob Attacks:

**Option 1: Mob Spawns Projectiles (Recommended)**
```java
// Boss08B.update()
if (shouldAttack()) {
    Projectile fireball = createFireball();
    worldManager.addProjectile(fireball); // World manages it
}
```

**Option 2: Mob Renders Attack Directly**
```java
// Boss08B.render()
if (isAttacking()) {
    drawAttackEffect(batch); // Boss draws its own effect
}
```

Both work! Depends on if attack is a separate entity or visual effect.

---

## Summary: Current State

### ✅ What Works:
- Mobs render themselves (good!)
- Generic mob rendering via interface
- Stages/waves work with current design
- Health bars are generic

### ❌ What's Inconsistent:
- Player animations in Renderer (should be in Player)
- Projectiles rendered by Renderer (could self-render)
- Explosions dead code (FIXED)

### 🎯 Recommended Fixes:

**Priority 1 (Do Now):**
- ✅ Remove renderExplosions() - DONE
- ⚠️ Move player animations to Player class

**Priority 2 (Later):**
- Projectile self-rendering
- Pickup self-rendering

**Priority 3 (Nice to Have):**
- Better abstraction (render() in MobEntity interface?)
- Remove instanceof checks (use polymorphism)

---

## Final Recommendation

**Current design is 80% there!** The main issue is player animation management. Once you move that to Player, the architecture will be consistent:

```
✅ Mobs render themselves
✅ Player renders itself (after fix)
✅ Renderer just coordinates
✅ Easy to add new mobs/stages
```

**For your goals:**
- ✅ Multiple stages - Already works via StageSpawner
- ✅ Multiple waves - Already works via StageSpawner
- ✅ Mobs render own animations - Already done!
- ⚠️ Mobs render own attacks - Depends on approach (projectiles vs effects)

**The system is maintainable!** Just needs player animation consistency.

---

**Date:** December 22, 2025  
**Status:** Renderer analysis complete, explosions removed, player animation fix recommended

