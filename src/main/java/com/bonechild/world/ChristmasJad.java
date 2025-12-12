package com.bonechild.world;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bonechild.rendering.Assets;

/**
 * Christmas Jad - A festive holiday boss-type enemy
 * Stronger and tankier than regular mobs
 * Uses a single static PNG image (not animated)
 */
public class ChristmasJad extends Mob {
    private Texture texture;
    private Assets assets;

    public ChristmasJad(float x, float y, Player target, Assets assets) {
        super(x, y, target);
        
        // Reasonable sprite size (similar to Vampire at 120x120)
        this.width = 120f;
        this.height = 120f;
        
        // Center a 60x60 hitbox in the 120x120 sprite
        float hitboxW = 60f;
        float hitboxH = 60f;
        float offsetX = (this.width - hitboxW) / 2f;
        float offsetY = (this.height - hitboxH) / 2f;
        setHitbox(hitboxW, hitboxH, offsetX, offsetY);
        
        this.assets = assets;
        
        // Load the single Christmas Jad image (not a spritesheet)
        this.texture = assets.getChristmasJadSheet();

        // Boss-type stats: Higher health, more damage, slower speed
        this.maxHealth = 300f;      // 2x Vampire health (Vampire has 150)
        this.currentHealth = 300f;  // Start at full health
        this.speed = 80f;           // Slower than regular mobs (regular is 100f)
        setDamage(35f);             // High damage (Vampire does 25f)
    }

    public void render(SpriteBatch batch, float delta) {
        if (texture != null) {
            // Draw the static Christmas Jad sprite scaled down to 120x120
            batch.draw(texture, position.x, position.y, width, height);
        }
    }
}
