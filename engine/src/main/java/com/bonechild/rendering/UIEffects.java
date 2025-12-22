package com.bonechild.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Handles fancy UI and screen effects like vignette, flashes, and pulses
 */
public class UIEffects {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera uiCamera;
    
    // Screen flash effect
    private float flashAlpha = 0f;
    private Color flashColor = new Color(1f, 1f, 1f, 1f);
    private float flashDecaySpeed = 3f;
    
    // Vignette intensity
    private float vignetteIntensity = 0.3f;
    private float targetVignetteIntensity = 0.3f;
    private float vignetteTransitionSpeed = 2f;
    
    // Pulse effect timer
    private float pulseTimer = 0f;
    
    // Virtual resolution
    private static final float VIRTUAL_WIDTH = 1280f;
    private static final float VIRTUAL_HEIGHT = 720f;
    
    public UIEffects() {
        this.shapeRenderer = new ShapeRenderer();
        this.uiCamera = new OrthographicCamera();
        this.uiCamera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
    }
    
    public void update(float delta) {
        // Update flash effect
        if (flashAlpha > 0) {
            flashAlpha -= flashDecaySpeed * delta;
            if (flashAlpha < 0) flashAlpha = 0;
        }
        
        // Update vignette transition
        if (vignetteIntensity != targetVignetteIntensity) {
            float diff = targetVignetteIntensity - vignetteIntensity;
            vignetteIntensity += diff * vignetteTransitionSpeed * delta;
            
            if (Math.abs(diff) < 0.01f) {
                vignetteIntensity = targetVignetteIntensity;
            }
        }
        
        // Update pulse timer
        pulseTimer += delta;
    }
    
    /**
     * Render screen vignette effect for intensity
     */
    public void renderVignette() {
        if (vignetteIntensity <= 0) return;
        
        uiCamera.update();
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        float edgeWidth = 300f;
        float alpha = vignetteIntensity * 0.7f;
        
        // Top vignette
        shapeRenderer.rect(0, VIRTUAL_HEIGHT - edgeWidth, VIRTUAL_WIDTH, edgeWidth,
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, alpha),
            new Color(0, 0, 0, alpha));
        
        // Bottom vignette
        shapeRenderer.rect(0, 0, VIRTUAL_WIDTH, edgeWidth,
            new Color(0, 0, 0, alpha),
            new Color(0, 0, 0, alpha),
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 0));
        
        // Left vignette
        shapeRenderer.rect(0, 0, edgeWidth, VIRTUAL_HEIGHT,
            new Color(0, 0, 0, alpha),
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, alpha));
        
        // Right vignette
        shapeRenderer.rect(VIRTUAL_WIDTH - edgeWidth, 0, edgeWidth, VIRTUAL_HEIGHT,
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, alpha),
            new Color(0, 0, 0, alpha),
            new Color(0, 0, 0, 0));
        
        shapeRenderer.end();
    }
    
    /**
     * Render screen flash effect
     */
    public void renderFlash() {
        if (flashAlpha <= 0) return;
        
        uiCamera.update();
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(flashColor.r, flashColor.g, flashColor.b, flashAlpha);
        shapeRenderer.rect(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        shapeRenderer.end();
    }
    
    /**
     * Trigger a screen flash with custom color
     */
    public void flash(Color color, float intensity) {
        flashColor.set(color);
        flashAlpha = MathUtils.clamp(intensity, 0f, 1f);
    }
    
    /**
     * Trigger a white flash (damage taken)
     */
    public void flashWhite(float intensity) {
        flash(Color.WHITE, intensity);
    }
    
    /**
     * Trigger a red flash (damage taken)
     */
    public void flashRed(float intensity) {
        flash(new Color(1f, 0.2f, 0.2f, 1f), intensity);
    }
    
    /**
     * Trigger a green flash (healing)
     */
    public void flashGreen(float intensity) {
        flash(new Color(0.2f, 1f, 0.2f, 1f), intensity);
    }
    
    /**
     * Set vignette intensity (0-1, default 0.3)
     */
    public void setVignetteIntensity(float intensity) {
        targetVignetteIntensity = MathUtils.clamp(intensity, 0f, 1f);
    }
    
    /**
     * Get pulsing value for UI animations (0-1)
     */
    public float getPulse(float speed) {
        return (MathUtils.sin(pulseTimer * speed) + 1f) * 0.5f;
    }
    
    /**
     * Get strong pulse value (more pronounced)
     */
    public float getStrongPulse(float speed) {
        float pulse = getPulse(speed);
        return 0.7f + pulse * 0.3f; // Range: 0.7 to 1.0
    }
    
    /**
     * Get subtle pulse value (less pronounced)
     */
    public float getSubtlePulse(float speed) {
        float pulse = getPulse(speed);
        return 0.9f + pulse * 0.1f; // Range: 0.9 to 1.0
    }
    
    public void dispose() {
        shapeRenderer.dispose();
    }
}
