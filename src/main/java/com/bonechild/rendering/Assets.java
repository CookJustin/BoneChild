package com.bonechild.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Manages and loads all game assets (textures, fonts, sounds, etc.)
 */
public class Assets {
    // Textures
    private Texture skeletonSpriteSheet;
    
    // Animations (9 frames per row based on the sprite sheet)
    private Animation idleAnimation;
    private Animation walkAnimation;
    private Animation attackAnimation;
    
    // Fonts
    private BitmapFont font;
    
    private boolean loaded = false;
    
    // Sprite sheet configuration
    private static final int FRAME_WIDTH = 32;  // Width of each frame
    private static final int FRAME_HEIGHT = 32; // Height of each frame
    private static final int FRAMES_PER_ROW = 3; // 9 frames per animation
    
    /**
     * Load all assets
     */
    public void load() {
        if (loaded) {
            Gdx.app.log("Assets", "Assets already loaded");
            return;
        }
        
        Gdx.app.log("Assets", "Loading assets...");
        
        // Load textures
        try {
            skeletonSpriteSheet = new Texture(Gdx.files.internal("assets/SkeletonSpriteSheet.png"));
            Gdx.app.log("Assets", "Loaded skeleton sprite sheet");
            
            // Create animations
            // Row 0: Idle animation
            idleAnimation = new Animation(skeletonSpriteSheet, FRAMES_PER_ROW, 0, 
                                         FRAME_WIDTH, FRAME_HEIGHT, 0.1f, true);
            
            // Row 1: Walk animation
            walkAnimation = new Animation(skeletonSpriteSheet, FRAMES_PER_ROW, 1, 
                                         FRAME_WIDTH, FRAME_HEIGHT, 0.08f, true);
            
            // Row 2: Attack animation
            attackAnimation = new Animation(skeletonSpriteSheet, FRAMES_PER_ROW, 2, 
                                           FRAME_WIDTH, FRAME_HEIGHT, 0.06f, false);
            
            Gdx.app.log("Assets", "Created player animations (Idle, Walk, Attack)");
        } catch (Exception e) {
            Gdx.app.error("Assets", "Failed to load skeleton sprite sheet: " + e.getMessage());
        }
        
        // Load fonts
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        
        loaded = true;
        Gdx.app.log("Assets", "All assets loaded successfully");
    }
    
    /**
     * Dispose of all assets
     */
    public void dispose() {
        if (!loaded) return;
        
        Gdx.app.log("Assets", "Disposing assets...");
        
        if (skeletonSpriteSheet != null) {
            skeletonSpriteSheet.dispose();
        }
        
        if (font != null) {
            font.dispose();
        }
        
        loaded = false;
    }
    
    // Getters
    public Texture getSkeletonSpriteSheet() { return skeletonSpriteSheet; }
    public Animation getIdleAnimation() { return idleAnimation; }
    public Animation getWalkAnimation() { return walkAnimation; }
    public Animation getAttackAnimation() { return attackAnimation; }
    public BitmapFont getFont() { return font; }
    public boolean isLoaded() { return loaded; }
}
