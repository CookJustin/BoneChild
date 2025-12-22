package com.bonechild.monsters.impl;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.bonechild.rendering.Animation;
import com.bonechild.rendering.Assets;

/**
 * Boss08_B - Simple boss with basic movement and attacks
 */
public class Boss08B extends Mob {
    private Animation walkAnimation;
    private Animation deathAnimation;

    public Boss08B(float x, float y, Vector2 playerPosition, Assets assets) {
        super(x, y, playerPosition);

        // Boss stats
        this.maxHealth = 500f;
        this.currentHealth = 500f;
        this.speed = 50f;
        setDamage(25f);

        // Boss size
        this.width = 120f;
        this.height = 120f;

        // Hitbox
        this.hitboxWidth = 80f;
        this.hitboxHeight = 80f;
        this.hitboxOffsetX = 20f;
        this.hitboxOffsetY = 20f;

        // Load animations
        if (assets != null) {
            var registry = assets.getRegistry();
            this.walkAnimation = registry.getAnimation("boss08b_walk");
            this.deathAnimation = registry.getAnimation("boss08b_death");
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

        // Use parent Mob's update for movement toward target
        super.update(delta);
    }

    public void render(SpriteBatch batch) {
        Animation currentAnimation = isDead() ? deathAnimation : walkAnimation;
        if (currentAnimation == null) return;

        TextureRegion frame = currentAnimation.getCurrentFrame();
        if (frame == null) return;

        // Draw the boss
        batch.draw(frame, position.x, position.y, width, height);
    }

    @Override
    public String getTypeId() {
        return "boss08b";
    }

    @Override
    public boolean isBoss() {
        return true;
    }
}

