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
    protected boolean active;
    
    public Entity(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.width = width;
        this.height = height;
        this.active = true;
    }
    
    /**
     * Update entity logic
     */
    public abstract void update(float delta);
    
    /**
     * Check if this entity collides with another
     */
    public boolean collidesWith(Entity other) {
        return position.x < other.position.x + other.width &&
               position.x + width > other.position.x &&
               position.y < other.position.y + other.height &&
               position.y + height > other.position.y;
    }
    
    // Getters and Setters
    public Vector2 getPosition() { return position; }
    public void setPosition(float x, float y) { position.set(x, y); }
    
    public Vector2 getVelocity() { return velocity; }
    public void setVelocity(float x, float y) { velocity.set(x, y); }
    
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
