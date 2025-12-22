package com.bonechild.monsters.api;

/**
 * Something that can take damage.
 *
 * Kept intentionally small so engine systems can operate on interfaces
 * without depending on concrete monster implementations.
 */
public interface Damageable {
    void takeDamage(float damage);
}

