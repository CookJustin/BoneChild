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
    
    // Orc sprite sheet for mobs
    private Texture orcWalkSheet;
    
    // Coin sprites for animation
    private Texture coin1;
    private Texture coin2;
    private Texture coin3;
    private Texture coin4;
    
    // Flask sprites for health orb animation
    private Texture flask1;
    private Texture flask2;
    private Texture flask3;
    private Texture flask4;
    
    // Fireball sprites for projectile animation (60 frames)
    private Texture[] fireballFrames;
    
    // Explosion sprites for explosion effect (82 frames)
    private Texture[] explosionFrames;
    
    // UI Bar sprites
    private Texture pixelBarOutline;
    private Texture[] pixelBarInners; // 6 different inner bar fills
    
    // Animations
    private Animation idleAnimation;
    private Animation walkAnimation;
    private Animation hurtAnimation;
    private Animation deathAnimation;
    private Animation coinAnimation;
    private Animation healthOrbAnimation;
    private Animation fireballAnimation;
    private Animation explosionAnimation;
    
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
            tilesetTexture = new Texture(Gdx.files.internal("assets/tiles/Dungeon_Tileset.png"));
            Gdx.app.log("Assets", "Loaded dungeon tileset texture");
            
            // Load skeleton sprite sheets from the new player folder
            skeletonIdleSheet = new Texture(Gdx.files.internal("assets/player/SkeletonIdle.png"));
            skeletonWalkSheet = new Texture(Gdx.files.internal("assets/player/SkeletonWalk.png"));
            skeletonHurtSheet = new Texture(Gdx.files.internal("assets/player/SkeletonHurt.png"));
            skeletonDieSheet = new Texture(Gdx.files.internal("assets/player/SkeletonDie.png"));
            Gdx.app.log("Assets", "Loaded skeleton sprite sheets");
            
            // Load orc sprite sheet for mobs from the enemies folder
            orcWalkSheet = new Texture(Gdx.files.internal("assets/enemies/Orc-Walk.png"));
            Gdx.app.log("Assets", "Loaded orc walk sprite sheet");
            
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
            coin1 = new Texture(Gdx.files.internal("assets/items/coin_1.png"));
            coin2 = new Texture(Gdx.files.internal("assets/items/coin_2.png"));
            coin3 = new Texture(Gdx.files.internal("assets/items/coin_3.png"));
            coin4 = new Texture(Gdx.files.internal("assets/items/coin_4.png"));
            
            // Create coin animation by combining the 4 coin textures
            coinAnimation = createCoinAnimation();
            
            Gdx.app.log("Assets", "Loaded coin sprites and created coin animation");
            
            // Load flask sprites for health orbs
            flask1 = new Texture(Gdx.files.internal("assets/items/flasks_1_1.png"));
            flask2 = new Texture(Gdx.files.internal("assets/items/flasks_1_2.png"));
            flask3 = new Texture(Gdx.files.internal("assets/items/flasks_1_3.png"));
            flask4 = new Texture(Gdx.files.internal("assets/items/flasks_1_4.png"));
            
            // Create health orb animation
            healthOrbAnimation = createHealthOrbAnimation();
            
            Gdx.app.log("Assets", "Loaded flask sprites and created health orb animation");
            
            // Try to load fireball sprites (60 frames: Fireball1.png to Fireball60.png) - optional
            try {
                fireballFrames = new Texture[60];
                for (int i = 0; i < 60; i++) {
                    fireballFrames[i] = new Texture(Gdx.files.internal("assets/projectiles/Fireball" + (i + 1) + ".png"));
                }
                
                // Create fireball animation - fast animation for dynamic effect
                fireballAnimation = new Animation(fireballFrames, 0.01f, true);
                Gdx.app.log("Assets", "Loaded 60 fireball frames and created fireball animation");
            } catch (Exception e) {
                Gdx.app.log("Assets", "Fireball sprites not found (optional) - projectiles will use fallback rendering");
                fireballFrames = null;
                fireballAnimation = null;
            }
            
            // Load PixelBarOutline and PixelBarInners for UI bars
            pixelBarOutline = new Texture(Gdx.files.internal("assets/ui/PixelBarOutline.png"));
            pixelBarOutline.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest); // Crisp pixel art
            
            pixelBarInners = new Texture[6];
            for (int i = 0; i < 6; i++) {
                pixelBarInners[i] = new Texture(Gdx.files.internal("assets/ui/PixelBarInner" + (i + 1) + ".png"));
                pixelBarInners[i].setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            }
            
            Gdx.app.log("Assets", "Loaded PixelBarOutline and 6 PixelBarInners");
            
            // Try to load explosion sprites (82 frames: explode0000.png to explode0081.png) - optional
            try {
                explosionFrames = new Texture[82];
                for (int i = 0; i < 82; i++) {
                    String frameNumber = String.format("%04d", i);
                    explosionFrames[i] = new Texture(Gdx.files.internal("assets/effects/explode" + frameNumber + ".png"));
                }
                
                // Create explosion animation - slowed down for better visibility (82 frames in ~0.82 seconds)
                explosionAnimation = new Animation(explosionFrames, 0.01f, false); // Non-looping, slowed from 0.006f
                Gdx.app.log("Assets", "Loaded 82 explosion frames and created explosion animation");
            } catch (Exception e) {
                Gdx.app.log("Assets", "Explosion sprites not found (optional): " + e.getMessage());
                explosionFrames = null;
                explosionAnimation = null;
            }
        } catch (Exception e) {
            Gdx.app.error("Assets", "Failed to load skeleton animations: " + e.getMessage());
        }
        
        // Load fonts - create a larger, crisper font
        font = new BitmapFont();
        font.getData().setScale(2.5f); // Larger scale for better readability
        font.setColor(Color.WHITE);
        font.getData().markupEnabled = true; // Enable colored text
        
        // Get the font texture and set it to use nearest neighbor filtering for crisp text
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        font.setUseIntegerPositions(true); // Snap to pixels for crisp rendering
        
        // Adjust line height for better spacing
        font.getData().lineHeight *= 1.1f;
        
        Gdx.app.log("Assets", "Font loaded with crisp rendering (scale 2.5x)");
        
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
            
            if (Gdx.files.internal("assets/audio/death-sound.mp3").exists()) {
                deathSound = Gdx.audio.newSound(Gdx.files.internal("assets/audio/death-sound.mp3"));
                Gdx.app.log("Assets", "Loaded death sound: death-sound.mp3");
            } else if (Gdx.files.internal("assets/audio/death.ogg").exists()) {
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
        
        if (orcWalkSheet != null) {
            orcWalkSheet.dispose();
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
        
        if (flask1 != null) {
            flask1.dispose();
        }
        
        if (flask2 != null) {
            flask2.dispose();
        }
        
        if (flask3 != null) {
            flask3.dispose();
        }
        
        if (flask4 != null) {
            flask4.dispose();
        }
        
        // Dispose fireball frames
        if (fireballFrames != null) {
            for (Texture frame : fireballFrames) {
                if (frame != null) {
                    frame.dispose();
                }
            }
        }
        
        // Dispose explosion frames
        if (explosionFrames != null) {
            for (Texture frame : explosionFrames) {
                if (frame != null) {
                    frame.dispose();
                }
            }
        }
        
        // Dispose PixelBarOutline and PixelBarInners
        if (pixelBarOutline != null) {
            pixelBarOutline.dispose();
        }
        
        if (pixelBarInners != null) {
            for (Texture inner : pixelBarInners) {
                if (inner != null) {
                    inner.dispose();
                }
            }
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
    public Animation getHealthOrbAnimation() { return healthOrbAnimation; }
    public Animation getFireballAnimation() { return fireballAnimation; }
    public Animation getExplosionAnimation() { return explosionAnimation; }
    public BitmapFont getFont() { return font; }
    public boolean isLoaded() { return loaded; }
    public Texture getPixelBarOutline() { return pixelBarOutline; }
    public Texture[] getPixelBarInners() { return pixelBarInners; }
    
    /**
     * Create a new independent walk animation instance (for mobs)
     */
    public Animation createWalkAnimation() {
        if (orcWalkSheet != null) {
            // Orc-Walk sprite sheet is 800x100, meaning 8 frames of 100x100 pixels
            return new Animation(orcWalkSheet, 8, 0, 100, 100, 0.1f, true);
        }
        return null;
    }
    
    /**
     * Create a new independent player idle animation instance
     */
    public Animation createPlayerIdleAnimation() {
        if (skeletonWalkSheet != null) {
            int[] idleFrame = {1};
            return new Animation(skeletonWalkSheet, idleFrame, 0, 32, 64, 1.0f, true);
        }
        return null;
    }
    
    /**
     * Create a new independent player walk animation instance
     */
    public Animation createPlayerWalkAnimation() {
        if (skeletonWalkSheet != null) {
            int[] goodWalkFrames = {1, 4, 7, 10, 13, 16, 19, 22, 25, 28};
            return new Animation(skeletonWalkSheet, goodWalkFrames, 0, 32, 64, 0.1f, true);
        }
        return null;
    }
    
    /**
     * Create a new independent player hurt animation instance
     */
    public Animation createPlayerHurtAnimation() {
        if (skeletonHurtSheet != null) {
            int[] goodHurtFrames = {1, 4, 7, 10, 13};
            return new Animation(skeletonHurtSheet, goodHurtFrames, 0, 32, 64, 0.05f, false);
        }
        return null;
    }
    
    /**
     * Create a new independent player death animation instance
     */
    public Animation createPlayerDeathAnimation() {
        if (skeletonDieSheet != null) {
            int[] goodDeathFrames = {1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34, 37};
            return new Animation(skeletonDieSheet, goodDeathFrames, 0, 32, 64, 0.1f, false);
        }
        return null;
    }
    
    /**
     * Create a coin animation using the loaded coin textures
     */
    private Animation createCoinAnimation() {
        Texture[] coinFrames = {coin1, coin2, coin3, coin4};
        return new Animation(coinFrames, 0.2f, true); // Slowed down from 0.1f to 0.2f
    }
    
    /**
     * Create a health orb animation using the loaded flask textures
     */
    private Animation createHealthOrbAnimation() {
        Texture[] flaskFrames = {flask1, flask2, flask3, flask4};
        return new Animation(flaskFrames, 0.2f, true); // Slowed down from 0.1f to 0.2f
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
