package com.bonechild.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bonechild.rendering.Animation;
import com.bonechild.rendering.Assets;

/**
 * Goblin enemy - faster and weaker than regular mobs
 * Uses goblin walk and death animations
 */
public class Goblin extends Mob {
    private Animation walkAnimation;
    private Animation deathAnimation;
    
    public Goblin(float x, float y, Player player, Assets assets) {
        super(x, y, player);
        
        // Goblin sprite size - similar to other enemies
        this.width = 120f;
        this.height = 120f;
        
        // Center a 60x60 hitbox in the 120x120 sprite
        float hitboxW = 60f;
        float hitboxH = 60f;
        float offsetX = (this.width - hitboxW) / 2f;
        float offsetY = (this.height - hitboxH) / 2f;
        setHitbox(hitboxW, hitboxH, offsetX, offsetY);
        
        // Goblin stats - faster but weaker
        this.maxHealth = 80f;       // Less health than regular mob (100)
        this.currentHealth = 80f;   // Start at full health
        this.speed = 90f;           // Faster than regular mob (50)
        setDamage(8f);              // Less damage than regular mob (10)
        
        // Initialize animations
        if (assets != null) {
            this.walkAnimation = assets.createGoblinWalkAnimation();
            this.deathAnimation = assets.createGoblinDeathAnimation();
        }
    }
    
    @Override
    public void update(float delta) {
        super.update(delta);
        
        // Update current animation
        if (isDead() && deathAnimation != null) {
            deathAnimation.update(delta);
        } else if (walkAnimation != null) {
            walkAnimation.update(delta);
        }
    }
    
    public void render(SpriteBatch batch, float delta) {
        Animation currentAnim = isDead() ? deathAnimation : walkAnimation;
        if (currentAnim != null) {
            batch.draw(currentAnim.getCurrentFrame(), position.x, position.y, width, height);
        }
    }
}
