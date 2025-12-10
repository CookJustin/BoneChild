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
    private Texture tilesetTexture;
    private Texture skeletonIdleSheet;
    private Texture skeletonWalkSheet;
    private Texture skeletonHurtSheet;
    private Texture skeletonDieSheet;
    
    // Coin sprites for animation
    private Texture coin1;
    private Texture coin2;
    private Texture coin3;
    private Texture coin4;
    
    // Animations
    private Animation idleAnimation;
    private Animation walkAnimation;
    private Animation hurtAnimation;
    private Animation deathAnimation;
    private Animation coinAnimation;
    
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
            
            // Load skeleton sprite sheets
            skeletonIdleSheet = new Texture(Gdx.files.internal("assets/SkeletonIdle.png"));
            skeletonWalkSheet = new Texture(Gdx.files.internal("assets/SkeletonWalk.png"));
            skeletonHurtSheet = new Texture(Gdx.files.internal("assets/SkeletonHurt.png"));
            skeletonDieSheet = new Texture(Gdx.files.internal("assets/SkeletonDie.png"));
            Gdx.app.log("Assets", "Loaded skeleton sprite sheets");
            
            // Create animations from sprite sheets
            // Use only the frames that have actual skeleton content (not empty frames)
            // Analysis showed only these frames have substantial content at 32px width
            int[] goodWalkFrames = {1, 4, 7, 10, 13, 16, 19, 22, 25, 28};
            int[] goodHurtFrames = {1, 4, 7, 10, 13};
            int[] goodDeathFrames = {1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34, 37};
            
            // Idle: use first good frame as static pose
            int[] idleFrame = {1};
            idleAnimation = new Animation(skeletonWalkSheet, idleFrame, 0, 32, 64, 1.0f, true);
            
            // Walk: use only the 10 good frames
            walkAnimation = new Animation(skeletonWalkSheet, goodWalkFrames, 0, 32, 64, 0.1f, true);
            
            // SkeletonHurt: use only the 5 good frames - faster to avoid stun lock
            hurtAnimation = new Animation(skeletonHurtSheet, goodHurtFrames, 0, 32, 64, 0.05f, false);
            
            // SkeletonDie: use only the 13 good frames
            deathAnimation = new Animation(skeletonDieSheet, goodDeathFrames, 0, 32, 64, 0.1f, false);
            
            Gdx.app.log("Assets", "Created player animations with 32x64 frames (Idle: 1f, Walk: 10f, Hurt: 5f, Death: 13f)");
            
            // Load coin sprites
            coin1 = new Texture(Gdx.files.internal("assets/coin_1.png"));
            coin2 = new Texture(Gdx.files.internal("assets/coin_2.png"));
            coin3 = new Texture(Gdx.files.internal("assets/coin_3.png"));
            coin4 = new Texture(Gdx.files.internal("assets/coin_4.png"));
            
            // Create coin animation by combining the 4 coin textures
            // We'll use a helper method to create this animation
            coinAnimation = createCoinAnimation();
            
            Gdx.app.log("Assets", "Loaded coin sprites and created coin animation");
        } catch (Exception e) {
            Gdx.app.error("Assets", "Failed to load skeleton animations: " + e.getMessage());
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
        
        if (skeletonIdleSheet != null) {
            skeletonIdleSheet.dispose();
        }
        
        if (skeletonWalkSheet != null) {
            skeletonWalkSheet.dispose();
        }
        
        if (skeletonHurtSheet != null) {
            skeletonHurtSheet.dispose();
        }
        
        if (skeletonDieSheet != null) {
            skeletonDieSheet.dispose();
        }
        
        if (coin1 != null) {
            coin1.dispose();
        }
        
        if (coin2 != null) {
            coin2.dispose();
        }
        
        if (coin3 != null) {
            coin3.dispose();
        }
        
        if (coin4 != null) {
            coin4.dispose();
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
    public Animation getIdleAnimation() { return idleAnimation; }
    public Animation getWalkAnimation() { return walkAnimation; }
    public Animation getHurtAnimation() { return hurtAnimation; }
    public Animation getDeathAnimation() { return deathAnimation; }
    public Animation getCoinAnimation() { return coinAnimation; }
    public BitmapFont getFont() { return font; }
    public boolean isLoaded() { return loaded; }
    
    /**
     * Create a new independent walk animation instance (for mobs)
     */
    public Animation createWalkAnimation() {
        if (skeletonWalkSheet != null) {
            int[] goodWalkFrames = {1, 4, 7, 10, 13, 16, 19, 22, 25, 28};
            return new Animation(skeletonWalkSheet, goodWalkFrames, 0, 32, 64, 0.15f, true);
        }
        return null;
    }
    
    /**
     * Create a coin animation using the loaded coin textures
     */
    private Animation createCoinAnimation() {
        Texture[] coinFrames = {coin1, coin2, coin3, coin4};
        return new Animation(coinFrames, 0.1f, true);
    }
    
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
