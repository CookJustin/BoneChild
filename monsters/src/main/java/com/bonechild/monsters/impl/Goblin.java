package com.bonechild.monsters.impl;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.bonechild.rendering.Animation;
import com.bonechild.rendering.Assets;

/**
 * Goblin enemy - faster and weaker than regular mobs
 */
public class Goblin extends Mob {
    private Animation walkAnimation;
    private Animation deathAnimation;

    public Goblin(float x, float y, Vector2 playerPosition, Assets assets) {
        super(x, y, playerPosition);

        // Goblin size
        this.width = 80f;
        this.height = 80f;

        // Hitbox
        this.hitboxWidth = 50f;
        this.hitboxHeight = 50f;
        this.hitboxOffsetX = 15f;
        this.hitboxOffsetY = 15f;

        // Goblin stats - fast but weak
        this.maxHealth = 30f;
        this.currentHealth = 30f;
        this.speed = 120f;  // Fast!
        setDamage(5f);

        // Load animations
        if (assets != null) {
            var registry = assets.getRegistry();
            this.walkAnimation = registry.getAnimation("goblin_walk");
            this.deathAnimation = registry.getAnimation("goblin_death");
        }
    }

    @Override
    public void update(float delta) {
        // If dead, just update death animation
        if (isDead()) {
            if (deathAnimation != null) {
                deathAnimation.update(delta);
            }
            return;
        }

        // Update walk animation
        if (walkAnimation != null) {
            walkAnimation.update(delta);
        }

        // Use parent Mob's update for movement
        super.update(delta);
    }

    public void render(SpriteBatch batch) {
        Animation currentAnimation = isDead() ? deathAnimation : walkAnimation;
        if (currentAnimation == null) return;

        TextureRegion frame = currentAnimation.getCurrentFrame();
        if (frame == null) return;

        // Draw the goblin
        batch.draw(frame, position.x, position.y, width, height);
    }

    @Override
    public String getTypeId() {
        return "goblin";
    }

    @Override
    public boolean isBoss() {
        return false;
    }
}

