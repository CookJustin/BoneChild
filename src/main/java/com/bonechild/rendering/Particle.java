package com.bonechild.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A single particle for visual effects
 */
public class Particle {
    private Vector2 position;
    private Vector2 velocity;
    private Color color;
    private float size;
    private float lifetime;
    private float maxLifetime;
    private boolean active;
    private float gravity;
    private float fadeSpeed;
    private ParticleShape shape;
    
    public enum ParticleShape {
        CIRCLE, SQUARE, SPARK
    }
    
    public Particle() {
        this.position = new Vector2();
        this.velocity = new Vector2();
        this.color = new Color(1, 1, 1, 1);
        this.active = false;
        this.shape = ParticleShape.CIRCLE;
    }
    
    public void spawn(float x, float y, float vx, float vy, Color color, float size, float lifetime, float gravity, ParticleShape shape) {
        this.position.set(x, y);
        this.velocity.set(vx, vy);
        this.color.set(color);
        this.size = size;
        this.lifetime = lifetime;
        this.maxLifetime = lifetime;
        this.active = true;
        this.gravity = gravity;
        this.fadeSpeed = 1f / lifetime;
        this.shape = shape;
    }
    
    public void update(float delta) {
        if (!active) return;
        
        // Update velocity with gravity
        velocity.y += gravity * delta;
        
        // Update position
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        
        // Update lifetime
        lifetime -= delta;
        if (lifetime <= 0) {
            active = false;
            return;
        }
        
        // Fade out
        float alpha = lifetime / maxLifetime;
        color.a = alpha;
    }
    
    public void render(ShapeRenderer shapeRenderer) {
        if (!active) return;
        
        shapeRenderer.setColor(color);
        
        switch (shape) {
            case CIRCLE:
                shapeRenderer.circle(position.x, position.y, size, 8);
                break;
            case SQUARE:
                shapeRenderer.rect(position.x - size/2, position.y - size/2, size, size);
                break;
            case SPARK:
                // Draw a line for spark effect
                float endX = position.x - velocity.x * 0.1f;
                float endY = position.y - velocity.y * 0.1f;
                shapeRenderer.rectLine(position.x, position.y, endX, endY, size * 0.5f);
                break;
        }
    }
    
    public boolean isActive() {
        return active;
    }
}
