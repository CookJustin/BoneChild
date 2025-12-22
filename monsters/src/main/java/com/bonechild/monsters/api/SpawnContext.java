package com.bonechild.monsters.api;

import com.badlogic.gdx.math.Vector2;

/**
 * Context passed to mob factory containing spawn position and any
 * additional references needed for AI and rendering.
 */
public final class SpawnContext {
    private final Vector2 position;

    public SpawnContext(float x, float y) {
        this.position = new Vector2(x, y);
    }

    public Vector2 getPosition() {
        return position;
    }
}

