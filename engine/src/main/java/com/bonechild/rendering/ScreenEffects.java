package com.bonechild.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Epic screen effects for maximum juice!
 */
public class ScreenEffects {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    
    // Flash effects
    private float whiteFlashIntensity = 0f;
    private float redFlashIntensity = 0f;
    private float goldFlashIntensity = 0f;
    
    // Vignette
    private float vignetteIntensity = 0f;
    private float targetVignetteIntensity = 0.3f;
    
    // Screen shake
    private float screenShakeIntensity = 0f;
    private float screenShakeDuration = 0f;
    
    // Hit stop (freeze frame)
    private float hitStopDuration = 0f;
    
    public ScreenEffects(OrthographicCamera camera) {
        this.camera = camera;
        this.shapeRenderer = new ShapeRenderer();
    }
    
    public void update(float delta) {
        // Smooth fade out effects
        if (whiteFlashIntensity > 0f) {
            whiteFlashIntensity = Math.max(0f, whiteFlashIntensity - delta * 4f);
        }
        if (redFlashIntensity > 0f) {
            redFlashIntensity = Math.max(0f, redFlashIntensity - delta * 3f);
        }
        if (goldFlashIntensity > 0f) {
            goldFlashIntensity = Math.max(0f, goldFlashIntensity - delta * 2f);
        }
        
        // Smooth vignette
        vignetteIntensity += (targetVignetteIntensity - vignetteIntensity) * delta * 5f;
        
        // Screen shake countdown
        if (screenShakeDuration > 0f) {
            screenShakeDuration -= delta;
            if (screenShakeDuration <= 0f) {
                screenShakeIntensity = 0f;
            }
        }
        
        // Hit stop countdown
        if (hitStopDuration > 0f) {
            hitStopDuration -= delta;
        }
    }
    
    public void render() {
        // Get screen dimensions
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        // Flash effects (fullscreen)
        if (whiteFlashIntensity > 0f) {
            shapeRenderer.setColor(1f, 1f, 1f, whiteFlashIntensity);
            shapeRenderer.rect(camera.position.x - screenWidth / 2, camera.position.y - screenHeight / 2, 
                             screenWidth, screenHeight);
        }
        
        if (redFlashIntensity > 0f) {
            shapeRenderer.setColor(1f, 0.2f, 0.2f, redFlashIntensity);
            shapeRenderer.rect(camera.position.x - screenWidth / 2, camera.position.y - screenHeight / 2, 
                             screenWidth, screenHeight);
        }
        
        if (goldFlashIntensity > 0f) {
            shapeRenderer.setColor(1f, 0.9f, 0.3f, goldFlashIntensity);
            shapeRenderer.rect(camera.position.x - screenWidth / 2, camera.position.y - screenHeight / 2, 
                             screenWidth, screenHeight);
        }
        
        // Vignette effect (darkened edges)
        if (vignetteIntensity > 0f) {
            float vigRadius = Math.min(screenWidth, screenHeight) * 0.6f;
            float centerX = camera.position.x;
            float centerY = camera.position.y;
            
            // Create gradient rings for vignette
            for (int i = 10; i >= 0; i--) {
                float alpha = (i / 10f) * vignetteIntensity;
                float radius = vigRadius + (i * 30f);
                shapeRenderer.setColor(0f, 0f, 0f, alpha * 0.15f);
                shapeRenderer.circle(centerX, centerY, radius, 32);
            }
        }
        
        shapeRenderer.end();
    }
    
    // === TRIGGER METHODS ===
    
    /**
     * White flash for critical hits
     */
    public void flashWhite(float intensity) {
        whiteFlashIntensity = Math.min(1f, Math.max(whiteFlashIntensity, intensity));
    }
    
    /**
     * Red flash for taking damage
     */
    public void flashRed(float intensity) {
        redFlashIntensity = Math.min(1f, Math.max(redFlashIntensity, intensity));
    }
    
    /**
     * Gold flash for level up
     */
    public void flashGold(float intensity) {
        goldFlashIntensity = Math.min(1f, Math.max(goldFlashIntensity, intensity));
    }
    
    /**
     * Set vignette intensity (0 = none, 1 = maximum darkness)
     */
    public void setVignette(float intensity) {
        targetVignetteIntensity = Math.max(0f, Math.min(1f, intensity));
    }
    
    /**
     * Trigger screen shake
     */
    public void shake(float intensity, float duration) {
        screenShakeIntensity = intensity;
        screenShakeDuration = duration;
    }
    
    /**
     * Trigger hit stop (freeze frame effect)
     */
    public void hitStop(float duration) {
        hitStopDuration = duration;
    }
    
    /**
     * Check if game should be frozen (hit stop active)
     */
    public boolean isHitStopActive() {
        return hitStopDuration > 0f;
    }
    
    /**
     * Get delta multiplier (0 during hit stop, 1 normally)
     */
    public float getDeltaMultiplier() {
        return hitStopDuration > 0f ? 0f : 1f;
    }
    
    public void dispose() {
        shapeRenderer.dispose();
    }
}
