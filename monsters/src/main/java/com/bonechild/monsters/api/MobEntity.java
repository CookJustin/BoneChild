package com.bonechild.monsters.api;

/**
 * Minimal interface for mobs and bosses exposed from the monster module.
 * Engine code and stage system should depend on this rather than concrete classes.
 */
public interface MobEntity extends Damageable {
    String getTypeId();
    float getX();
    float getY();
    float getWidth();
    float getHeight();
    boolean isActive();
    boolean isDead();
    float getHealthPercentage();
    boolean isBoss();

    /**
     * Contact / attack damage for simple collision-based hits.
     */
    float getDamage();

    // Hitbox methods for collision and targeting
    float getHitboxOffsetX();
    float getHitboxOffsetY();
    float getHitboxWidth();
    float getHitboxHeight();
}
