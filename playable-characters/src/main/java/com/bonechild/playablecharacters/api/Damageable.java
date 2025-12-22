package com.bonechild.playablecharacters.api;

/**
 * Something that can take damage.
 *
 * This is the playable-characters-side contract (player avatars, summons, etc.).
 * It intentionally does NOT depend on the monsters module.
 */
public interface Damageable {
    void takeDamage(float damage);
}
