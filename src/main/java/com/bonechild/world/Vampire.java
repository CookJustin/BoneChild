package com.bonechild.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bonechild.rendering.Assets;
import com.badlogic.gdx.math.Vector2;

/**
 * Vampire enemy with separate body/head sprites and basic movement/attack
 */
public class Vampire extends Mob {
    @SuppressWarnings("unchecked")
    private Animation<TextureRegion>[] bodyAnimations = (Animation<TextureRegion>[]) new Animation[4]; // 0:down, 1:up, 2:left, 3:right
    @SuppressWarnings("unchecked")
    private Animation<TextureRegion>[] headAnimations = (Animation<TextureRegion>[]) new Animation[4];
    private float stateTime = 0f;
    private int currentDirection = 0; // 0:down, 1:up, 2:left, 3:right
    private Assets assets;

    public Vampire(float x, float y, Player target, Assets assets) {
        super(x, y, target);
        this.width = 120f;
        this.height = 120f;
        // Center a 60x60 hitbox in the 120x120 sprite
        float hitboxW = 60f;
        float hitboxH = 60f;
        float offsetX = (this.width - hitboxW) / 2f;
        float offsetY = (this.height - hitboxH) / 2f;
        setHitbox(hitboxW, hitboxH, offsetX, offsetY);
        this.assets = assets;
        // Split body and head sprite sheets (4 rows, 12 columns, 64x64 each)
        TextureRegion[][] bodyFrames2D = TextureRegion.split(assets.getVampireBodyAttack(), 64, 64);
        TextureRegion[][] headFrames2D = TextureRegion.split(assets.getVampireHeadAttack(), 64, 64);
        for (int row = 0; row < 4; row++) {
            TextureRegion[] bodyFrames = new TextureRegion[12];
            TextureRegion[] headFrames = new TextureRegion[12];
            for (int col = 0; col < 12; col++) {
                bodyFrames[col] = bodyFrames2D[row][col];
                headFrames[col] = headFrames2D[row][col];
            }
            bodyAnimations[row] = new Animation<>(0.1f, bodyFrames);
            headAnimations[row] = new Animation<>(0.1f, headFrames);
        }

        // Set custom health and damage
        this.maxHealth = 150f;      // 150 max health
        this.currentHealth = 150f;  // Start at full health
        setDamage(25f); // Set custom damage for Vampire
    }

    public void render(SpriteBatch batch, float delta) {
        stateTime += delta;
        // Determine direction based on velocity
        Vector2 vel = this.velocity;
        if (Math.abs(vel.x) > Math.abs(vel.y)) {
            if (vel.x > 0) currentDirection = 3; // right
            else if (vel.x < 0) currentDirection = 2; // left
        } else {
            if (vel.y > 0) currentDirection = 1; // up
            else if (vel.y < 0) currentDirection = 0; // down
        }
        Animation<TextureRegion> bodyAnim = bodyAnimations[currentDirection];
        Animation<TextureRegion> headAnim = headAnimations[currentDirection];
        TextureRegion bodyFrame = bodyAnim.getKeyFrame(stateTime, true);
        TextureRegion headFrame = headAnim.getKeyFrame(stateTime, true);
        // Draw sprite at 120x120 (matching hitbox)
        batch.draw(bodyFrame, position.x, position.y, width, height);
        batch.draw(headFrame, position.x, position.y, width, height);
    }
}
