package com.bonechild.world;

import com.badlogic.gdx.math.Vector2;
import com.bonechild.rendering.Animation;

/**
 * Explosion effect that plays an animation and deals AOE damage
 */
public class Explosion extends Entity {
    private Animation animation;
    private float animationTimer;
    private float damage;
    private float radius;
    private boolean hasDealtDamage;
    
    public Explosion(float x, float y, float damage, float radius, Animation animation) {
        super(x, y, 100, 100); // Size of explosion sprite
        this.damage = damage;
        this.radius = radius;
        this.animation = animation;
        this.animationTimer = 0f;
        this.hasDealtDamage = false;
        this.active = true;
    }
    
    @Override
    public void update(float delta) {
        if (!active) return;
        
        animationTimer += delta;
        
        // Update animation
        if (animation != null) {
            animation.update(delta);
            
            // Deactivate when animation finishes
            if (animation.isFinished()) {
                active = false;
            }
        } else {
            // If no animation, deactivate after a short time
            if (animationTimer > 0.5f) {
                active = false;
            }
        }
    }
    
    /**
     * Get the current animation frame
     */
    public Animation getAnimation() {
        return animation;
    }
    
    /**
     * Check if this explosion should damage a mob (within radius)
     */
    public boolean shouldDamageMob(Mob mob) {
        if (hasDealtDamage || mob == null || mob.isDead()) {
            return false;
        }
        
        Vector2 explosionCenter = new Vector2(position.x + width / 2f, position.y + height / 2f);
        Vector2 mobCenter = new Vector2(mob.getPosition().x + mob.getWidth() / 2f, 
                                       mob.getPosition().y + mob.getHeight() / 2f);
        
        float distance = explosionCenter.dst(mobCenter);
        return distance <= radius;
    }
    
    /**
     * Get the damage this explosion deals
     */
    public float getDamage() {
        return damage;
    }
    
    /**
     * Get the explosion radius
     */
    public float getRadius() {
        return radius;
    }
    
    /**
     * Mark that this explosion has dealt damage (so it only damages once)
     */
    public void markDamageDealt() {
        hasDealtDamage = true;
    }
    
    /**
     * Check if damage has been dealt
     */
    public boolean hasDealtDamage() {
        return hasDealtDamage;
    }
}