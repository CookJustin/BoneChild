package com.bonechild.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Handles sprite sheet animations
 */
public class Animation {
    private TextureRegion[] frames;
    private float frameDuration;
    private float stateTime;
    private boolean looping;
    
    public Animation(Texture spriteSheet, int frameCount, int row, int frameWidth, int frameHeight, float frameDuration, boolean looping) {
        this.frames = new TextureRegion[frameCount];
        this.frameDuration = frameDuration;
        this.stateTime = 0;
        this.looping = looping;
        
        // Cut the sprite sheet into frames
        for (int i = 0; i < frameCount; i++) {
            int x = i * frameWidth;
            int y = row * frameHeight;
            frames[i] = new TextureRegion(spriteSheet, x, y, frameWidth, frameHeight);
        }
    }
    
    /**
     * Constructor that allows selecting specific frames from a sprite sheet
     */
    public Animation(Texture spriteSheet, int[] frameIndices, int row, int frameWidth, int frameHeight, float frameDuration, boolean looping) {
        this.frames = new TextureRegion[frameIndices.length];
        this.frameDuration = frameDuration;
        this.stateTime = 0;
        this.looping = looping;
        
        // Cut the sprite sheet into frames using specific indices
        for (int i = 0; i < frameIndices.length; i++) {
            int frameIndex = frameIndices[i];
            int x = frameIndex * frameWidth;
            int y = row * frameHeight;
            frames[i] = new TextureRegion(spriteSheet, x, y, frameWidth, frameHeight);
        }
    }
    
    /**
     * Constructor for animation from separate texture files (e.g., coin_1.png, coin_2.png, etc.)
     */
    public Animation(Texture[] textures, float frameDuration, boolean looping) {
        this.frames = new TextureRegion[textures.length];
        this.frameDuration = frameDuration;
        this.stateTime = 0;
        this.looping = looping;
        
        // Create texture regions from individual textures
        for (int i = 0; i < textures.length; i++) {
            frames[i] = new TextureRegion(textures[i]);
        }
    }
    
    /**
     * Constructor for animation from separate texture files with custom region size
     * Useful when the actual sprite is smaller than the PNG canvas
     */
    public Animation(Texture[] textures, int regionWidth, int regionHeight, float frameDuration, boolean looping) {
        this.frames = new TextureRegion[textures.length];
        this.frameDuration = frameDuration;
        this.stateTime = 0;
        this.looping = looping;
        
        // Create texture regions from individual textures, using only the specified region
        for (int i = 0; i < textures.length; i++) {
            // Extract the region from the top-left corner (0, 0) with specified dimensions
            frames[i] = new TextureRegion(textures[i], 0, 0, regionWidth, regionHeight);
        }
    }
    
    /**
     * Update animation state
     */
    public void update(float delta) {
        stateTime += delta;
        
        // Reset if not looping and finished
        if (!looping && stateTime > frameDuration * frames.length) {
            stateTime = frameDuration * frames.length;
        }
    }
    
    /**
     * Get current frame
     */
    public TextureRegion getCurrentFrame() {
        int frameIndex;
        
        if (looping) {
            // Loop the animation
            frameIndex = (int) (stateTime / frameDuration) % frames.length;
        } else {
            // Play once
            frameIndex = Math.min((int) (stateTime / frameDuration), frames.length - 1);
        }
        
        return frames[frameIndex];
    }
    
    /**
     * Reset animation to start
     */
    public void reset() {
        stateTime = 0;
    }
    
    /**
     * Check if animation has finished (for non-looping animations)
     */
    public boolean isFinished() {
        if (looping) return false;
        return stateTime >= frameDuration * frames.length;
    }
    
    public float getStateTime() {
        return stateTime;
    }
    
    /**
     * Get all frames (for cloning animations)
     */
    public TextureRegion[] getFrames() {
        return frames;
    }
    
    /**
     * Create animation from sprite sheet - single row
     */
    public static Animation fromSpriteSheet(Texture sheet, int row, int frameCount, int frameWidth, int frameHeight, float frameDuration, boolean looping) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        
        for (int i = 0; i < frameCount; i++) {
            int x = i * frameWidth;
            int y = row * frameHeight;
            frames[i] = new TextureRegion(sheet, x, y, frameWidth, frameHeight);
        }
        
        return new Animation(frames, frameDuration, looping);
    }
    
    /**
     * Create animation from sprite sheet - multiple rows
     * Used for animations that span multiple rows (like Boss08_B death animation)
     */
    public static Animation fromSpriteSheetMultiRow(Texture sheet, int startRow, int rowCount, int framesPerRow, int frameWidth, int frameHeight, float frameDuration, boolean looping) {
        int totalFrames = rowCount * framesPerRow;
        TextureRegion[] frames = new TextureRegion[totalFrames];
        
        int frameIndex = 0;
        for (int row = startRow; row < startRow + rowCount; row++) {
            for (int col = 0; col < framesPerRow; col++) {
                int x = col * frameWidth;
                int y = row * frameHeight;
                frames[frameIndex] = new TextureRegion(sheet, x, y, frameWidth, frameHeight);
                frameIndex++;
            }
        }
        
        return new Animation(frames, frameDuration, looping);
    }
    
    /**
     * Constructor for pre-made frames (used by static factory methods)
     */
    private Animation(TextureRegion[] frames, float frameDuration, boolean looping) {
        this.frames = frames;
        this.frameDuration = frameDuration;
        this.stateTime = 0;
        this.looping = looping;
    }
}
