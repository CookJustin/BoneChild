package com.bonechild.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * EPIC floating damage number with bounce and scale animations!
 */
public class DamageNumber {
    private Vector2 position;
    private Vector2 velocity;
    private String text;
    private float lifetime;
    private float maxLifetime;
    private float alpha;
    private boolean active;
    private boolean isCritical;
    
    // Animation variables for JUICE
    private float scale;
    private float targetScale;
    private float rotation;
    private float rotationSpeed;
    private float bounceTimer;
    
    public DamageNumber(float x, float y, float damage, boolean isCritical) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(
            MathUtils.random(-20f, 20f), 
            isCritical ? 120f : 80f  // Crits pop up faster!
        );
        this.text = String.format("%.0f", damage);
        this.lifetime = isCritical ? 2f : 1.5f;
        this.maxLifetime = this.lifetime;
        this.alpha = 1f;
        this.active = true;
        this.isCritical = isCritical;
        
        // Epic animation values
        this.scale = 0.5f;
        this.targetScale = isCritical ? 1.3f : 1.0f;
        this.rotation = MathUtils.random(-15f, 15f);
        this.rotationSpeed = MathUtils.random(-180f, 180f);
        this.bounceTimer = 0f;
    }
    
    public void update(float delta) {
        if (!active) return;
        
        // Epic scale animation - pop in with overshoot!
        if (scale < targetScale) {
            scale += delta * 8f;
            if (scale > targetScale) {
                scale = targetScale + 0.2f; // Overshoot
            }
        } else if (scale > targetScale) {
            scale -= delta * 4f;
            if (scale < targetScale) {
                scale = targetScale;
            }
        }
        
        // Physics simulation with gravity
        velocity.y -= 280f * delta; // Gravity
        velocity.x *= 0.95f; // Air resistance
        
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        
        // Bouncy rotation
        rotation += rotationSpeed * delta;
        bounceTimer += delta;
        
        // Fade out over lifetime
        lifetime -= delta;
        if (lifetime < 0.5f) {
            alpha = lifetime / 0.5f; // Fade in last 0.5 seconds
        }
        
        if (lifetime <= 0) {
            active = false;
        }
    }
    
    public void render(SpriteBatch batch, BitmapFont font) {
        if (!active) return;
        
        // Save original state
        float originalScaleX = font.getData().scaleX;
        float originalScaleY = font.getData().scaleY;
        
        // Bouncy scale with sine wave
        float bounce = 1f + MathUtils.sin(bounceTimer * 8f) * 0.1f;
        float finalScale = scale * bounce * 0.7f;
        font.getData().setScale(finalScale);
        
        // Calculate text dimensions for centering
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(font, text);
        float textWidth = layout.width;
        float textHeight = layout.height;
        
        float centerX = position.x;
        float centerY = position.y;
        
        // Draw shadow layers for depth (offset more for crits)
        if (isCritical) {
            // Triple layer shadow for crits
            font.setColor(0f, 0f, 0f, alpha * 0.6f);
            font.draw(batch, text, centerX - textWidth/2 + 3, centerY + textHeight/2 - 3);
            font.setColor(0f, 0f, 0f, alpha * 0.4f);
            font.draw(batch, text, centerX - textWidth/2 + 2, centerY + textHeight/2 - 2);
        }
        
        // Standard shadow
        font.setColor(0f, 0f, 0f, alpha * 0.8f);
        font.draw(batch, text, centerX - textWidth/2 + 1.5f, centerY + textHeight/2 - 1.5f);
        
        // Outline glow for crits
        if (isCritical) {
            font.setColor(1f, 0.5f, 0f, alpha * 0.5f);
            font.draw(batch, text, centerX - textWidth/2 - 1, centerY + textHeight/2 + 1);
            font.draw(batch, text, centerX - textWidth/2 + 1, centerY + textHeight/2 + 1);
            font.draw(batch, text, centerX - textWidth/2 - 1, centerY + textHeight/2 - 1);
            font.draw(batch, text, centerX - textWidth/2 + 1, centerY + textHeight/2 - 1);
        }
        
        // Main text with epic colors
        if (isCritical) {
            // Animated gold-to-orange gradient for crits
            float colorPulse = MathUtils.sin(bounceTimer * 6f) * 0.5f + 0.5f;
            font.setColor(1f, 0.85f - colorPulse * 0.2f, colorPulse * 0.3f, alpha);
        } else {
            // White with slight pulse
            float pulse = 0.9f + MathUtils.sin(bounceTimer * 4f) * 0.1f;
            font.setColor(pulse, pulse, pulse, alpha);
        }
        
        font.draw(batch, text, centerX - textWidth/2, centerY + textHeight/2);
        
        // Restore original scale
        font.getData().setScale(originalScaleX, originalScaleY);
        font.setColor(Color.WHITE);
    }
    
    public boolean isActive() { return active; }
}
