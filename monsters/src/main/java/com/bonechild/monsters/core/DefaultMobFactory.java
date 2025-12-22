package com.bonechild.monsters.core;

import com.badlogic.gdx.math.Vector2;
import com.bonechild.monsters.api.MobEntity;
import com.bonechild.monsters.api.MobFactory;
import com.bonechild.monsters.api.SpawnContext;
import com.bonechild.monsters.impl.Mob;
import com.bonechild.monsters.impl.Goblin;
import com.bonechild.monsters.impl.Boss08B;
import com.bonechild.rendering.Assets;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Default implementation of MobFactory that maps mobTypeId strings
 * to concrete mob constructors.
 */
public class DefaultMobFactory implements MobFactory {

    /**
     * Creator function: (SpawnContext, Dependencies) -> MobEntity
     * For now we keep it simple and capture Player position / Assets in this factory.
     */
    private final Map<String, java.util.function.Function<SpawnContext, MobEntity>> registry = new HashMap<>();
    private final Vector2 playerPosition; // Reference to player position for mob AI

    public DefaultMobFactory(Vector2 playerPosition, Assets assets) {
        this.playerPosition = playerPosition;
        var assetRegistry = assets.getRegistry();

        // Register built-in mob types
        register("mob", ctx -> new Mob(ctx.getPosition().x, ctx.getPosition().y, playerPosition));
        register("goblin", ctx -> new Goblin(ctx.getPosition().x, ctx.getPosition().y, playerPosition, assets));
        register("boss08b", ctx -> new Boss08B(ctx.getPosition().x, ctx.getPosition().y, playerPosition, assets));
    }

    public DefaultMobFactory register(String typeId, java.util.function.Function<SpawnContext, MobEntity> creator) {
        registry.put(typeId, creator);
        return this;
    }

    @Override
    public MobEntity create(String mobTypeId, SpawnContext context) {
        var creator = registry.get(mobTypeId);
        if (creator == null) {
            throw new IllegalArgumentException("Unknown mob type id: " + mobTypeId);
        }
        return creator.apply(context);
    }
}

