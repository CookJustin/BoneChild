package com.bonechild.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bonechild.rendering.Animation;

/**
 * Enemy_17_B mob with death animation
 */
public class Enemy17B extends Mob {
    // Death animation
    private Animation deathAnimation;
    private boolean deathAnimationComplete = false;
    private float deathAnimationTimer = 0f;
    private static final float DEATH_ANIMATION_DURATION = 0.6f; // 6 frames at 10 FPS
    
    // Walk animation
    private Animation walkAnimation;
    
    public Enemy17B(float x, float y, Player target, Animation walkAnimation, Animation deathAnimation) {
        super(x, y, target); // Call Mob constructor which sets up hitbox
        this.walkAnimation = walkAnimation;
        this.deathAnimation = deathAnimation;
        
        // Red Demon has higher HP than regular mobs (200 instead of 100)
        this.maxHealth = 200f;
        this.currentHealth = 200f;
    }
    
    @Override
    public void update(float delta) {
        // If dead, update death animation timer
        if (dead) {
            if (!deathAnimationComplete) {
                deathAnimationTimer += delta;
                if (deathAnimation != null) {
                    deathAnimation.update(delta);
                }
                if (deathAnimationTimer >= DEATH_ANIMATION_DURATION) {
                    deathAnimationComplete = true;
                }
            }
            return;
        }
        
        // Update walk animation
        if (walkAnimation != null) {
            walkAnimation.update(delta);
        }
        
        // Call parent update to handle movement and attacking
        super.update(delta);
    }
    
    @Override
    protected void onDeath() {
        Gdx.app.log("Enemy17B", "Enemy17B died!");
        // Reset death animation
        if (deathAnimation != null) {
            deathAnimation.reset();
        }
        deathAnimationTimer = 0f;
        deathAnimationComplete = false;
    }
    
    /**
     * Render the Enemy17B with proper animation
     */
    public void render(SpriteBatch batch, float delta) {
        if (!isActive()) return;
        
        Animation currentAnimation = dead ? deathAnimation : walkAnimation;
        
        if (currentAnimation != null) {
            var frame = currentAnimation.getCurrentFrame();
            
            // Draw at 120x120 size (2.5x scale of 48px sprite)
            batch.draw(
                frame,
                position.x,
                position.y,
                width,
                height
            );
        }
    }
    
    public boolean isDeathAnimationComplete() {
        return deathAnimationComplete;
    }
}
