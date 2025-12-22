package com.bonechild.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

    // Legacy cached font (optional)
    private BitmapFont font;

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

        if (font != null) {
            font.dispose();
            font = null;
        }
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

    // ---------------------------------------------------------------------
    // Legacy API (UI / old engine code expects these)
    // ---------------------------------------------------------------------

    /**
     * Legacy UI font.
     * NOTE: Prefer a dedicated UI asset id later.
     */
    @Deprecated
    public BitmapFont getFont() {
        if (font == null) {
            font = new BitmapFont();
        }
        return font;
    }

    /**
     * Legacy background music handle.
     * In the new system, music should be loaded/managed via an Audio module.
     */
    @Deprecated
    public Music getBackgroundMusic() {
        return null;
    }

    // Legacy UI textures (best-effort ID mapping; returns null if not present)
    @Deprecated public Texture getExitScreenMenuBg() { return tryTexture("ui_menu_bg", "ui_exit_screen_menu_bg", "exit_screen_menu_bg", "menu_bg"); }
    @Deprecated public Texture getPlayButton() { return tryTexture("ui_play_button", "play_button"); }
    @Deprecated public Texture getSettingsButton() { return tryTexture("ui_settings_button", "settings_button"); }
    @Deprecated public Texture getExitButton() { return tryTexture("ui_exit_button", "exit_button"); }

    // Legacy pickup animations
    @Deprecated public Animation getCoinAnimation() { return tryAnimation("coin", "coin_spin", "pickup_coin"); }
    @Deprecated public Animation getHealthOrbAnimation() { return tryAnimation("health_orb", "health_flask", "pickup_health"); }

    private Texture tryTexture(String... ids) {
        for (String id : ids) {
            if (id != null && registry.hasTexture(id)) {
                return registry.getTexture(id);
            }
        }
        return null;
    }

    private Animation tryAnimation(String... ids) {
        for (String id : ids) {
            if (id != null) {
                Animation anim = registry.getAnimation(id);
                if (anim != null) return anim;
            }
        }
        return null;
    }
}
