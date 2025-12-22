package com.bonechild.collision;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.bonechild.monsters.api.MobEntity;
import com.bonechild.playablecharacters.Pickup;
import com.bonechild.playablecharacters.Player;
import com.bonechild.playablecharacters.Projectile;

/**
 * Engine-level collision coordinator.
 *
 * Goal: keep collision rules out of entity implementations so modules stay decoupled.
 *
 * Currently handles:
 * - Projectile -> MobEntity hits
 * - Pickup magnetic pull + auto-collect
 * - Loot drops on mob death
 */
public class CollisionSystem {
    
    /**
     * Callback interface for spawning pickups
     * This allows CollisionSystem to spawn loot without depending on WorldManager
     */
    public interface PickupSpawner {
        void spawnPickup(Pickup pickup);
    }
    
    private PickupSpawner pickupSpawner;

    /**
     * Set the pickup spawner callback (injected by BoneChildGame)
     */
    public void setPickupSpawner(PickupSpawner spawner) {
        this.pickupSpawner = spawner;
    }

    /**
     * Process all world collisions.
     *
     * Contract:
     * - Mutates mobs (damage), projectiles (deactivate), pickups (collect), player (gold/xp/heal)
     * - Safe to call every frame
     */
    public void process(float delta,
                        Player player,
                        Array<MobEntity> mobs,
                        Array<Projectile> projectiles,
                        Array<Pickup> pickups) {
        if (player == null) return;

        processPickups(delta, player, pickups);
        processProjectileHits(player, mobs, projectiles);
        processMobContactDamage(player, mobs);
    }

    private void processPickups(float delta, Player player, Array<Pickup> pickups) {
        if (pickups == null || pickups.size == 0) return;

        for (int i = pickups.size - 1; i >= 0; i--) {
            Pickup pickup = pickups.get(i);
            if (pickup == null || pickup.isCollected()) continue;

            // Magnetic pull is a player-centric mechanic.
            pickup.applyMagneticPull(player, delta);

            if (pickup.shouldCollect(player)) {
                pickup.collect();

                switch (pickup.getType()) {
                    case GOLD_COIN:
                        player.addGold((int) pickup.getValue());
                        break;
                    case XP_ORB:
                        player.addExperience(pickup.getValue());
                        break;
                    case HEALTH_ORB:
                        player.heal(pickup.getValue());
                        break;
                }
            }
        }
    }

    private void processProjectileHits(Player player, Array<MobEntity> mobs, Array<Projectile> projectiles) {
        if (mobs == null || mobs.size == 0) return;
        if (projectiles == null || projectiles.size == 0) return;

        for (int p = projectiles.size - 1; p >= 0; p--) {
            Projectile projectile = projectiles.get(p);
            if (projectile == null || !projectile.isActive()) continue;

            // Find first mob hit (simple rule for now)
            for (int m = mobs.size - 1; m >= 0; m--) {
                MobEntity mob = mobs.get(m);
                if (mob == null || mob.isDead() || !mob.isActive()) continue;

                if (intersects(projectile, mob)) {
                    boolean wasAlive = !mob.isDead();
                    applyDamage(mob, projectile.getDamage());
                    projectile.deactivate();

                    // Basic log (effects/particles handled elsewhere)
                    Gdx.app.log("CollisionSystem", "Projectile hit mob: " + mob.getTypeId() + " dmg=" + projectile.getDamage());

                    // If mob died, give player streak AND spawn loot
                    if (wasAlive && mob.isDead()) {
                        player.incrementKillStreak();
                        spawnLootForMob(mob, player);
                    }

                    break; // projectile consumed
                }
            }
        }
    }

    /**
     * Spawn loot pickups when a mob dies
     */
    private void spawnLootForMob(MobEntity mob, Player player) {
        if (pickupSpawner == null) return;

        // Calculate mob center for spawn position
        float mobCenterX = mob.getX() + mob.getWidth() / 2f;
        float mobCenterY = mob.getY() + mob.getHeight() / 2f;

        // Always drop XP (scaled by player's kill streak multiplier)
        float xpAmount = 10f * player.getKillStreakMultiplier();
        Pickup xpOrb = new Pickup(mobCenterX, mobCenterY, Pickup.PickupType.XP_ORB, xpAmount);
        pickupSpawner.spawnPickup(xpOrb);

        // 50% chance to drop gold (also scaled by streak)
        if (Math.random() < 0.5f) {
            int goldAmount = (int)(5f * player.getKillStreakMultiplier());
            Pickup goldCoin = new Pickup(mobCenterX + 10f, mobCenterY, Pickup.PickupType.GOLD_COIN, goldAmount);
            pickupSpawner.spawnPickup(goldCoin);
        }

        // 10% chance to drop health orb
        if (Math.random() < 0.1f) {
            Pickup healthOrb = new Pickup(mobCenterX - 10f, mobCenterY, Pickup.PickupType.HEALTH_ORB, 20f);
            pickupSpawner.spawnPickup(healthOrb);
        }

        Gdx.app.log("CollisionSystem", "Spawned loot for " + mob.getTypeId());
    }

    private void processMobContactDamage(Player player, Array<MobEntity> mobs) {
        if (mobs == null || mobs.size == 0) return;
        if (player.isDead()) return;

        for (int i = 0; i < mobs.size; i++) {
            MobEntity mob = mobs.get(i);
            if (mob == null || mob.isDead() || !mob.isActive()) continue;

            if (intersectsPlayerHitbox(player, mob)) {
                float dmg = mob.getDamage();
                if (dmg > 0f) {
                    player.takeDamage(dmg);
                }
            }
        }
    }

    private boolean intersectsPlayerHitbox(Player player, MobEntity mob) {
        // Player hitbox rect
        float pX = player.getPosition().x + player.getHitboxOffsetX();
        float pY = player.getPosition().y + player.getHitboxOffsetY();
        float pW = player.getHitboxWidth();
        float pH = player.getHitboxHeight();

        // Mob hitbox rect
        float mX = mob.getX() + mob.getHitboxOffsetX();
        float mY = mob.getY() + mob.getHitboxOffsetY();
        float mW = mob.getHitboxWidth();
        float mH = mob.getHitboxHeight();

        return pX < mX + mW && pX + pW > mX && pY < mY + mH && pY + pH > mY;
    }

    private void applyDamage(MobEntity mob, float damage) {
        mob.takeDamage(damage);
    }

    /**
     * Projectile vs mob hitbox circle test.
     */
    private boolean intersects(Projectile projectile, MobEntity mob) {
        Vector2 pPos = projectile.getPosition();
        if (pPos == null) return false;

        float mobCenterX = mob.getX() + mob.getHitboxOffsetX() + mob.getHitboxWidth() / 2f;
        float mobCenterY = mob.getY() + mob.getHitboxOffsetY() + mob.getHitboxHeight() / 2f;

        float dx = mobCenterX - pPos.x;
        float dy = mobCenterY - pPos.y;
        float dist2 = dx * dx + dy * dy;

        float mobRadius = Math.max(mob.getHitboxWidth(), mob.getHitboxHeight()) / 2f;
        float r = projectile.getRadius() + mobRadius;

        return dist2 <= r * r;
    }
}
