package com.bonechild.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import java.util.Random;

/**
 * Camera shake effect for impactful visual feedback
 */
public class CameraShake {
    private static boolean enabled = true; // Global enable/disable flag
    
    private float intensity;
    private float duration;
    private float timer;
    private Random random;
    
    private float originalX;
    private float originalY;
    
    public CameraShake() {
        this.random = new Random();
        this.intensity = 0;
        this.duration = 0;
        this.timer = 0;
    }
    
    /**
     * Set whether camera shake is enabled globally
     */
    public static void setEnabled(boolean enabled) {
        CameraShake.enabled = enabled;
    }
    
    /**
     * Check if camera shake is enabled globally
     */
    public static boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Trigger a camera shake
     * @param intensity How strong the shake is (in pixels)
     * @param duration How long the shake lasts (in seconds)
     */
    public void shake(float intensity, float duration) {
        this.intensity = Math.max(this.intensity, intensity); // Take the strongest shake
        this.duration = Math.max(this.duration, duration);
        this.timer = 0;
    }
    
    /**
     * Update the camera shake and apply to camera
     */
    public void update(OrthographicCamera camera, float delta) {
        if (!enabled) {
            // If disabled, restore original position and reset
            if (timer > 0) {
                camera.position.x = originalX;
                camera.position.y = originalY;
                timer = 0;
                intensity = 0;
                duration = 0;
            }
            return;
        }
        
        if (timer < duration) {
            // Save original position on first frame
            if (timer == 0) {
                originalX = camera.position.x;
                originalY = camera.position.y;
            }
            
            // Calculate fade-out factor (shake gets weaker over time)
            float progress = timer / duration;
            float fadeOut = 1.0f - progress;
            
            // Apply random offset
            float currentIntensity = intensity * fadeOut;
            float offsetX = (random.nextFloat() - 0.5f) * 2 * currentIntensity;
            float offsetY = (random.nextFloat() - 0.5f) * 2 * currentIntensity;
            
            camera.position.x = originalX + offsetX;
            camera.position.y = originalY + offsetY;
            
            timer += delta;
        } else if (timer > 0) {
            // Restore original position
            camera.position.x = originalX;
            camera.position.y = originalY;
            timer = 0;
            intensity = 0;
            duration = 0;
        }
    }
    
    /**
     * Check if currently shaking
     */
    public boolean isShaking() {
        return timer < duration;
    }
}
