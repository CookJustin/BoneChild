package com.bonechild.world;

import com.badlogic.gdx.math.Vector2;

/**
 * Projectile fired by the player
 */
public class Projectile {
    private Vector2 position;
    private Vector2 velocity;
    private float radius;
    private float damage;
    private float maxDistance;
    private float distanceTraveled;
    private boolean active;
    private boolean isCritical;
    
    public Projectile(float startX, float startY, float targetX, float targetY, float damage, boolean isCritical) {
        this.position = new Vector2(startX, startY);
        this.radius = 5f; // Smaller fireball size for better visuals
        this.damage = damage;
        this.maxDistance = 1000f; // Max distance before disappearing
        this.distanceTraveled = 0;
        this.active = true;
        this.isCritical = isCritical;
        
        // Calculate direction to target
        float dx = targetX - startX;
        float dy = targetY - startY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // Normalize and apply speed
        float speed = 300f; // Projectile speed in pixels/second
        if (distance > 0) {
            this.velocity = new Vector2(dx / distance * speed, dy / distance * speed);
        } else {
            this.velocity = new Vector2(0, 0);
        }
    }
    
    /**
     * Update projectile position
     */
    public void update(float delta) {
        if (!active) return;
        
        // Move projectile
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        
        // Track distance traveled
        float movementDistance = velocity.len() * delta;
        distanceTraveled += movementDistance;
        
        // Deactivate if traveled too far
        if (distanceTraveled >= maxDistance) {
            active = false;
        }
    }
    
    /**
     * Check if projectile hits a mob
     */
    public boolean collidesWith(Mob mob) {
        if (!active || mob == null || mob.isDead()) {
            return false;
        }
        
        // Use the mob's actual hitbox center position (dynamic offset)
        float mobHitboxCenterX = mob.getPosition().x + mob.getHitboxOffsetX() + mob.getHitboxWidth() / 2f;
        float mobHitboxCenterY = mob.getPosition().y + mob.getHitboxOffsetY() + mob.getHitboxHeight() / 2f;
        
        // Calculate distance from projectile center to mob hitbox center
        float dx = mobHitboxCenterX - position.x;
        float dy = mobHitboxCenterY - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // Collision hitbox: projectile radius + mob's actual hitbox radius
        float mobHitboxRadius = mob.getHitboxWidth() / 2f;
        
        return distance < (radius + mobHitboxRadius);
    }
    
    /**
     * Deactivate projectile (e.g., on hit)
     */
    public void deactivate() {
        active = false;
    }
    
    // Getters
    public Vector2 getPosition() { return position; }
    public Vector2 getVelocity() { return velocity; }
    public float getRadius() { return radius; }
    public float getDamage() { return damage; }
    public boolean isActive() { return active; }
    public boolean isCritical() { return isCritical; }
}
