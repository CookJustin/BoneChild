package com.bonechild.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents a pickup item (coin or XP orb) dropped by mobs
 */
public class Pickup extends Entity {
    public enum PickupType {
        GOLD_COIN, XP_ORB, HEALTH_ORB
    }
    
    private PickupType type;
    private float value;
    private float collectRadius;
    private float magneticPullDistance;
    private float magneticPullSpeed;
    private boolean collected;
    
    public Pickup(float x, float y, PickupType type, float value) {
        // Set size based on pickup type - XP orbs are much smaller
        super(x, y, 
            type == PickupType.XP_ORB ? 4 : 16,  // XP orbs are 4x4 (1/8th size of 16x16)
            type == PickupType.XP_ORB ? 4 : 16); 
        this.type = type;
        this.value = value;
        this.collectRadius = 20f; // How close to player to auto-collect
        this.magneticPullDistance = 80f; // How far away magnetic pull starts - reduced from 150
        this.magneticPullSpeed = 150f; // Speed of magnetic pull (pixels per second)
        this.collected = false;
    }
    
    @Override
    public void update(float delta) {
        if (collected) {
            return;
        }
        
        // Pickups don't actively move, but will be pulled by player's magnetic field
        // (movement happens in WorldManager when player is close)
    }
    
    /**
     * Apply magnetic pull toward the player
     */
    public void applyMagneticPull(Player player, float delta) {
        if (collected || player == null || player.isDead()) {
            return;
        }
        
        float dx = player.getPosition().x - position.x;
        float dy = player.getPosition().y - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // Adjust pull distance and speed based on grab level
        float adjustedPullDistance = magneticPullDistance + (player.getGrabLevel() * 30f); // +30 per grab level
        float adjustedPullSpeed = magneticPullSpeed + (player.getGrabLevel() * 50f); // +50 per grab level
        
        // Only pull if within magnetic distance and not yet collected
        if (distance < adjustedPullDistance && distance > collectRadius) {
            // Only pull if player is getting closer (moving toward the pickup)
            // Calculate player's velocity component toward this pickup
            float playerVelX = player.getVelocity().x;
            float playerVelY = player.getVelocity().y;
            
            // Dot product: if positive, player is moving toward pickup
            float dotProduct = playerVelX * dx + playerVelY * dy;
            
            // Only apply pull if player is moving toward the pickup OR standing still
            if (dotProduct >= 0) {
                // Normalize direction
                float dirX = dx / distance;
                float dirY = dy / distance;
                
                // Move toward player
                position.x += dirX * adjustedPullSpeed * delta;
                position.y += dirY * adjustedPullSpeed * delta;
            }
        }
    }
    
    /**
     * Check if this pickup collides with player and should be collected
     */
    public boolean shouldCollect(Player player) {
        if (collected || player == null || player.isDead()) {
            return false;
        }
        
        float dx = player.getPosition().x - position.x;
        float dy = player.getPosition().y - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        return distance <= collectRadius;
    }
    
    /**
     * Get the value to award to player
     */
    public float getValue() {
        return value;
    }
    
    /**
     * Mark this pickup as collected
     */
    public void collect() {
        collected = true;
        active = false;
    }
    
    public PickupType getType() { return type; }
    public boolean isCollected() { return collected; }
}
