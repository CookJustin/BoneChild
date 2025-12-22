package com.bonechild.monsters.api;

/**
 * Minimal interface for mobs and bosses exposed from the monster module.
 * Engine code and stage system should depend on this rather than concrete classes.
 */
public interface MobEntity {
    String getTypeId();
    float getX();
    float getY();
    float getWidth();
    float getHeight();
    boolean isActive();
    boolean isDead();
    float getHealthPercentage();
    boolean isBoss();

    // Hitbox methods for collision and targeting
    float getHitboxOffsetX();
    float getHitboxOffsetY();
    float getHitboxWidth();
    float getHitboxHeight();
}
