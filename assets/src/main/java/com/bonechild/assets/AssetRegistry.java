package com.bonechild.assets;

import com.badlogic.gdx.graphics.Texture;
import com.bonechild.rendering.Animation;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple registry for game assets accessed by string ID.
 * Replaces the hard-coded Assets class with a data-driven approach.
 */
public class AssetRegistry {
    private final Map<String, Texture> textures = new HashMap<>();
    private final Map<String, Animation> animations = new HashMap<>();

    /**
     * Register a texture with an ID
     */
    public void registerTexture(String id, Texture texture) {
        textures.put(id, texture);
    }

    /**
     * Register an animation with an ID
     */
    public void registerAnimation(String id, Animation animation) {
        animations.put(id, animation);
    }

    /**
     * Get a texture by ID
     */
    public Texture getTexture(String id) {
        Texture texture = textures.get(id);
        if (texture == null) {
            throw new IllegalArgumentException("Texture not found: " + id);
        }
        return texture;
    }

    /**
     * Get an animation by ID (creates a new instance for independent state)
     */
    public Animation getAnimation(String id) {
        Animation template = animations.get(id);
        if (template == null) {
            throw new IllegalArgumentException("Animation not found: " + id);
        }
        return template.copy(); // Return independent copy
    }

    /**
     * Check if a texture exists
     */
    public boolean hasTexture(String id) {
        return textures.containsKey(id);
    }

    /**
     * Check if an animation exists
     */
    public boolean hasAnimation(String id) {
        return animations.containsKey(id);
    }

    /**
     * Dispose all assets
     */
    public void dispose() {
        for (Texture texture : textures.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        textures.clear();
        animations.clear();
    }

    /**
     * Get stats for debugging
     */
    public String getStats() {
        return "AssetRegistry: " + textures.size() + " textures, " + animations.size() + " animations";
    }
}

