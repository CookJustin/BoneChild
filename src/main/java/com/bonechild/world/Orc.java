package com.bonechild.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bonechild.rendering.Animation;
import com.bonechild.rendering.Assets;

/**
 * Orc enemy - medium strength melee fighter
 * Uses Orc for Nate sprites (18 frames total)
 */
public class Orc extends Mob {
    private Animation walkAnimation;
    private Animation deathAnimation;
    private boolean deathAnimationComplete = false;
    
    public Orc(float x, float y, Player player, Assets assets) {
        super(x, y, player);
        
        // Orc sprite size - larger and more imposing than goblins
        this.width = 180f;
        this.height = 180f;
        
        // Center a 60x60 hitbox in the 180x180 sprite
        float hitboxW = 60f;
        float hitboxH = 60f;
        float offsetX = (this.width - hitboxW) / 2f;
        float offsetY = (this.height - hitboxH) / 2f;
        setHitbox(hitboxW, hitboxH, offsetX, offsetY);
        
        // Orc stats - stronger and tougher than goblins
        this.maxHealth = 120f;      // More health than goblin (80) but less than enemy (100)
        this.currentHealth = 120f;  // Start at full health
        this.speed = 70f;           // Moderate speed (between goblin 90 and slower enemies)
        setDamage(15f);             // Higher damage than goblin (8) and regular mob (10)
        
        // Initialize animations
        if (assets != null) {
            this.walkAnimation = assets.createOrcWalkAnimation();
            this.deathAnimation = assets.createOrcDeathAnimation();
        }
    }
    
    @Override
    public void update(float delta) {
        super.update(delta);
        
        // Update current animation
        if (isDead() && deathAnimation != null) {
            deathAnimation.update(delta);
            // Mark death animation as complete when it finishes
            if (deathAnimation.isFinished()) {
                deathAnimationComplete = true;
            }
        } else if (walkAnimation != null) {
            walkAnimation.update(delta);
        }
    }
    
    /**
     * Check if death animation has finished playing
     */
    public boolean isDeathAnimationComplete() {
        return deathAnimationComplete;
    }
    
    /**
     * Set the Orc's health (for boss version)
     */
    public void setHealth(float health) {
        this.currentHealth = health;
    }
    
    /**
     * Set the Orc's max health (for boss version)
     */
    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }
    
    /**
     * Set the Orc's speed (for boss version)
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    
    public void render(SpriteBatch batch, float delta) {
        Animation currentAnim = isDead() ? deathAnimation : walkAnimation;
        if (currentAnim != null) {
            var frame = currentAnim.getCurrentFrame();
            
            // The Orc PNGs are 157x195 pixels - render at proper scale
            // Scale down slightly to match other enemies in size
            float scale = 0.8f;  // Scale down to fit better with other enemies
            float spriteWidth = 157f * scale;   // ~125 pixels wide
            float spriteHeight = 195f * scale;  // ~156 pixels tall
            
            // Center the sprite within the 180x180 entity bounds
            float spriteX = position.x + (width - spriteWidth) / 2f;
            float spriteY = position.y + (height - spriteHeight) / 2f;
            
            batch.draw(frame, spriteX, spriteY, spriteWidth, spriteHeight);
        }
    }
}
