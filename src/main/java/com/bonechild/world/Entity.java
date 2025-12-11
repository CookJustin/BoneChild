package com.bonechild.world;

import com.badlogic.gdx.math.Vector2;

/**
 * Base entity class for all game objects
 */
public abstract class Entity {
    protected Vector2 position;
    protected Vector2 velocity;
    protected float width;
    protected float height;
    protected float hitboxWidth;
    protected float hitboxHeight;
    protected float hitboxOffsetX;
    protected float hitboxOffsetY;
    protected boolean active;
    
    public Entity(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.width = width;
        this.height = height;
        // Default hitbox is same as visual size
        this.hitboxWidth = width;
        this.hitboxHeight = height;
        this.hitboxOffsetX = 0;
        this.hitboxOffsetY = 0;
        this.active = true;
    }
    
    /**
     * Update entity logic
     */
    public abstract void update(float delta);
    
    /**
     * Set custom hitbox size (for when visual size differs from collision size)
     */
    public void setHitbox(float hitboxWidth, float hitboxHeight, float offsetX, float offsetY) {
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.hitboxOffsetX = offsetX;
        this.hitboxOffsetY = offsetY;
    }
    
    /**
     * Check if this entity collides with another using hitboxes
     */
    public boolean collidesWith(Entity other) {
        float thisX = position.x + hitboxOffsetX;
        float thisY = position.y + hitboxOffsetY;
        float otherX = other.position.x + other.hitboxOffsetX;
        float otherY = other.position.y + other.hitboxOffsetY;
        
        return thisX < otherX + other.hitboxWidth &&
               thisX + hitboxWidth > otherX &&
               thisY < otherY + other.hitboxHeight &&
               thisY + hitboxHeight > otherY;
    }
    
    // Getters and Setters
    public Vector2 getPosition() { return position; }
    public void setPosition(float x, float y) { position.set(x, y); }
    
    public Vector2 getVelocity() { return velocity; }
    public void setVelocity(float x, float y) { velocity.set(x, y); }
    
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    
    public float getHitboxWidth() { return hitboxWidth; }
    public float getHitboxHeight() { return hitboxHeight; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
