package com.bonechild.monsters.api;

/**
 * Factory interface for creating mobs/bosses by type id.
 * Concrete implementation will live in the monster module and be wired into the engine.
 */
public interface MobFactory {
    MobEntity create(String mobTypeId, SpawnContext context);
}

