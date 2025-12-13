package com.bonechild.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Floating damage number that appears above enemies
 */
public class DamageNumber {
    private Vector2 position;
    private String text;
    private float lifetime;
    private float maxLifetime;
    private float alpha;
    private boolean active;
    private boolean isCritical;
    
    public DamageNumber(float x, float y, float damage, boolean isCritical) {
        this.position = new Vector2(x, y);
        this.text = String.format("%.0f", damage);
        this.lifetime = 1.5f;
        this.maxLifetime = 1.5f;
        this.alpha = 1f;
        this.active = true;
        this.isCritical = isCritical;
    }
    
    public void update(float delta) {
        if (!active) return;
        
        // Float upward slower for better visibility
        position.y += 30f * delta;
        
        // Fade out over lifetime
        lifetime -= delta;
        alpha = Math.max(0, lifetime / maxLifetime);
        
        if (lifetime <= 0) {
            active = false;
        }
    }
    
    public void render(SpriteBatch batch, BitmapFont font) {
        if (!active) return;
        
        // Set color based on crit
        if (isCritical) {
            font.setColor(1f, 0.85f, 0f, alpha); // Gold for crits
        } else {
            font.setColor(1f, 1f, 1f, alpha); // White for normal damage
        }
        
        // Save original scale
        float originalScaleX = font.getData().scaleX;
        float originalScaleY = font.getData().scaleY;
        
        // Apply fixed scale for world coordinates
        float worldScale = isCritical ? 0.9f : 0.6f; // Fixed absolute values
        font.getData().setScale(worldScale);
        
        // Draw text centered
        float textWidth = font.draw(batch, text, 0, 0).width;
        font.draw(batch, text, position.x - textWidth / 2, position.y);
        
        // Restore original scale
        font.getData().setScale(originalScaleX, originalScaleY);
        
        // Reset color
        font.setColor(Color.WHITE);
    }
    
    public boolean isActive() { return active; }
}
