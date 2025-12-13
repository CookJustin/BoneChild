package com.bonechild.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bonechild.rendering.Animation;
import com.bonechild.rendering.Assets;

/**
 * Glob enemy - faster and weaker than regular mobs
 * Uses glob walk and death animations
 */
public class Glob extends Mob {
    private Animation walkAnimation;
    private Animation deathAnimation;
    private float deathTimer = 0f;
    private static final float DEATH_DURATION = 0.8f; // Duration of death animation
    
    public Glob(float x, float y, Player player, Assets assets) {
        super(x, y, player);
        
        // Glob sprite size - scaled up for better visibility
        this.width = 160f;
        this.height = 160f;
        
        // The glob sprite is 48x48 but the actual blob body is only about 16x16
        // positioned in the center-bottom of the sprite.
        // Create a SMALL hitbox centered on the blob body.
        
        float hitboxW = 15f;  // Small hitbox matching the blob size
        float hitboxH = 15f;  // Square hitbox
        
        // Center the hitbox horizontally: (160 - 15) / 2 = 72.5
        // Position it at the bottom where the blob actually sits (not 60px up!)
        float offsetX = (this.width - hitboxW) / 2f;  // Center horizontally
        float offsetY = 5f;  // Position at the very bottom where the blob body actually is
        
        setHitbox(hitboxW, hitboxH, offsetX, offsetY);
        
        // Glob stats - faster but weaker
        this.maxHealth = 80f;       // Less health than regular mob (100)
        this.currentHealth = 80f;   // Start at full health
        this.speed = 90f;           // Faster than regular mob (50)
        setDamage(8f);              // Less damage than regular mob (10)
        
        // Initialize animations
        if (assets != null) {
            this.walkAnimation = assets.createGlobWalkAnimation();
            this.deathAnimation = assets.createGlobDeathAnimation();
        }
    }
    
    @Override
    protected void onDeath() {
        // Reset death animation when glob dies
        if (deathAnimation != null) {
            deathAnimation.reset();
        }
        deathTimer = 0f;
    }
    
    @Override
    public void update(float delta) {
        // If dead, only update death animation timer
        if (isDead()) {
            deathTimer += delta;
            if (deathAnimation != null) {
                deathAnimation.update(delta);
            }
            return; // Don't do movement/attack updates when dead
        }
        
        super.update(delta);
        
        // Update walk animation
        if (walkAnimation != null) {
            walkAnimation.update(delta);
        }
    }
    
    /**
     * Check if the death animation has finished playing
     */
    public boolean isDeathAnimationComplete() {
        return isDead() && deathTimer >= DEATH_DURATION;
    }
    
    public void render(SpriteBatch batch, float delta) {
        Animation currentAnim = isDead() ? deathAnimation : walkAnimation;
        if (currentAnim != null) {
            batch.draw(currentAnim.getCurrentFrame(), position.x, position.y, width, height);
        }
    }
}
