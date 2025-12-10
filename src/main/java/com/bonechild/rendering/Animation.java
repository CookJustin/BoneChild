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
}
