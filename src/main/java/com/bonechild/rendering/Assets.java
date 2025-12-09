package com.bonechild.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Manages and loads all game assets (textures, fonts, sounds, etc.)
 */
public class Assets {
    // Textures
    private Texture skeletonSpriteSheet;
    private Texture tilesetTexture;
    
    // Animations (9 frames per row based on the sprite sheet)
    private Animation idleAnimation;
    private Animation walkAnimation;
    private Animation attackAnimation;
    
    // Fonts
    private BitmapFont font;
    
    // Audio
    private Music backgroundMusic;
    private Sound attackSound;
    private Sound hitSound;
    private Sound levelUpSound;
    private Sound deathSound;
    
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
            tilesetTexture = new Texture(Gdx.files.internal("assets/SkeletonGraveyardTileset.png"));
            Gdx.app.log("Assets", "Loaded tileset texture");
            
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
        
        // Load fonts - use built-in with improved settings
        font = new BitmapFont();
        font.getData().setScale(2.0f);
        font.setColor(Color.WHITE);
        font.setUseIntegerPositions(false); // Smoother rendering
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        
        // Load audio (optional - will use placeholders if files don't exist)
        loadAudio();
        
        loaded = true;
        Gdx.app.log("Assets", "All assets loaded successfully");
    }
    
    /**
     * Load audio files (background music and sound effects)
     */
    private void loadAudio() {
        try {
            // Try to load background music - "7th realm.mp3"
            if (Gdx.files.internal("assets/audio/7th realm.mp3").exists()) {
                backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/audio/7th realm.mp3"));
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.5f);
                Gdx.app.log("Assets", "Loaded background music: 7th realm.mp3");
            } else if (Gdx.files.internal("assets/audio/background.ogg").exists()) {
                backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/audio/background.ogg"));
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.5f);
                Gdx.app.log("Assets", "Loaded background music: background.ogg");
            } else if (Gdx.files.internal("assets/audio/background.mp3").exists()) {
                backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/audio/background.mp3"));
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.5f);
                Gdx.app.log("Assets", "Loaded background music: background.mp3");
            } else {
                Gdx.app.log("Assets", "No background music found (place '7th realm.mp3' in assets/audio/)");
            }
            
            // Try to load sound effects
            if (Gdx.files.internal("assets/audio/attack.ogg").exists()) {
                attackSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/attack.ogg"));
                Gdx.app.log("Assets", "Loaded attack sound");
            } else if (Gdx.files.internal("assets/audio/attack.wav").exists()) {
                attackSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/attack.wav"));
                Gdx.app.log("Assets", "Loaded attack sound");
            }
            
            if (Gdx.files.internal("assets/audio/hit.ogg").exists()) {
                hitSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/hit.ogg"));
                Gdx.app.log("Assets", "Loaded hit sound");
            } else if (Gdx.files.internal("assets/audio/hit.wav").exists()) {
                hitSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/hit.wav"));
                Gdx.app.log("Assets", "Loaded hit sound");
            }
            
            if (Gdx.files.internal("assets/audio/levelup.ogg").exists()) {
                levelUpSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/levelup.ogg"));
                Gdx.app.log("Assets", "Loaded level up sound");
            } else if (Gdx.files.internal("assets/audio/levelup.wav").exists()) {
                levelUpSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/levelup.wav"));
                Gdx.app.log("Assets", "Loaded level up sound");
            }
            
            if (Gdx.files.internal("assets/audio/death.ogg").exists()) {
                deathSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/death.ogg"));
                Gdx.app.log("Assets", "Loaded death sound");
            } else if (Gdx.files.internal("assets/audio/death.wav").exists()) {
                deathSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/death.wav"));
                Gdx.app.log("Assets", "Loaded death sound");
            }
            
        } catch (Exception e) {
            Gdx.app.error("Assets", "Error loading audio: " + e.getMessage());
        }
    }
    
    /**
     * Dispose of all assets
     */
    public void dispose() {
        if (!loaded) return;
        
        Gdx.app.log("Assets", "Disposing assets...");
        
        if (tilesetTexture != null) {
            tilesetTexture.dispose();
        }
        
        if (skeletonSpriteSheet != null) {
            skeletonSpriteSheet.dispose();
        }
        
        if (font != null) {
            font.dispose();
        }
        
        // Dispose audio
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
        
        if (attackSound != null) {
            attackSound.dispose();
        }
        
        if (hitSound != null) {
            hitSound.dispose();
        }
        
        if (levelUpSound != null) {
            levelUpSound.dispose();
        }
        
        if (deathSound != null) {
            deathSound.dispose();
        }
        
        loaded = false;
    }
    
    // Getters
    public Texture getTilesetTexture() { return tilesetTexture; }
    public Texture getSkeletonSpriteSheet() { return skeletonSpriteSheet; }
    public Animation getIdleAnimation() { return idleAnimation; }
    public Animation getWalkAnimation() { return walkAnimation; }
    public Animation getAttackAnimation() { return attackAnimation; }
    public BitmapFont getFont() { return font; }
    public boolean isLoaded() { return loaded; }
    
    // Audio getters
    public Music getBackgroundMusic() { return backgroundMusic; }
    public Sound getAttackSound() { return attackSound; }
    public Sound getHitSound() { return hitSound; }
    public Sound getLevelUpSound() { return levelUpSound; }
    public Sound getDeathSound() { return deathSound; }
    
    /**
     * Play a sound effect if it's loaded
     */
    public void playSound(Sound sound) {
        if (sound != null) {
            sound.play(0.6f); // 60% volume
        }
    }
    
    /**
     * Play a sound effect with custom volume
     */
    public void playSound(Sound sound, float volume) {
        if (sound != null) {
            sound.play(volume);
        }
    }
}
