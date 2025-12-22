package com.bonechild.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.bonechild.assets.AssetLoader;
import com.bonechild.assets.AssetRegistry;

/**
 * Compatibility wrapper around AssetRegistry.
 * Maintains the old Assets API while using the new data-driven system internally.
 *
 * MIGRATION: Gradually replace direct calls to this class with AssetRegistry.getTexture(id) / .getAnimation(id)
 */
public class Assets {
    private final AssetRegistry registry;
    private boolean loaded = false;

    public Assets() {
        this.registry = new AssetRegistry();
    }

    public AssetRegistry getRegistry() {
        return registry;
    }

    /**
     * Load all assets from modular JSON files
     */
    public void load() {
        if (loaded) {
            Gdx.app.log("Assets", "Assets already loaded");
            return;
        }

        Gdx.app.log("Assets", "Loading assets from modular manifests...");
        AssetLoader loader = new AssetLoader(registry);

        // Load from module-specific asset files
        loader.loadFromModules();

        loaded = true;
        Gdx.app.log("Assets", registry.getStats());
    }

    /**
     * Dispose all assets
     */
    public void dispose() {
        if (!loaded) return;
        registry.dispose();
        loaded = false;
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Get a texture by ID
     * @deprecated Use getRegistry().getTexture(id) directly instead
     */
    @Deprecated
    public Texture getTexture(String id) {
        return registry.getTexture(id);
    }

    /**
     * Get an animation by ID (returns independent copy)
     * @deprecated Use getRegistry().getAnimation(id) directly instead
     */
    @Deprecated
    public Animation getAnimation(String id) {
        return registry.getAnimation(id);
    }
}

