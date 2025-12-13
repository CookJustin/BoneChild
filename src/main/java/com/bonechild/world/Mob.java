package com.bonechild.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

/**
 * Enemy mob that chases the player
 */
public class Mob extends LivingEntity {
    private Player target;
    private float damage;

    public void setDamage(float damage) {
        this.damage = damage;
    }



    private float attackCooldown;
    private float timeSinceLastAttack;
    
    public Mob(float x, float y, Player target) {
        super(x, y, 240, 240, 50f, 100f); // Increased from 48x48 to 240x240 (5x size)
        this.target = target;
        this.damage = 10f;
        this.attackCooldown = 1f; // Attack once per second
        this.timeSinceLastAttack = 0;
        
        // Set smaller hitbox (20x20) centered on the large sprite for better accuracy
        // Offset: (240 - 20) / 2 = 110 pixels from bottom-left to center the hitbox
        setHitbox(20, 20, 110, 110);
    }
    
    @Override
    public void update(float delta) {
        if (dead || target == null || target.isDead()) {
            return;
        }
        
        // Calculate center of this mob's hitbox
        float myHitboxCenterX = position.x + hitboxOffsetX + hitboxWidth / 2f;
        float myHitboxCenterY = position.y + hitboxOffsetY + hitboxHeight / 2f;
        
        // Calculate center of player's hitbox (player has default hitbox = full size)
        float targetHitboxCenterX = target.getPosition().x + target.getWidth() / 2f;
        float targetHitboxCenterY = target.getPosition().y + target.getHeight() / 2f;
        
        // Calculate direction from mob's hitbox center to player's hitbox center
        Vector2 direction = new Vector2(
            targetHitboxCenterX - myHitboxCenterX,
            targetHitboxCenterY - myHitboxCenterY
        );
        direction.nor(); // Normalize
        
        velocity.set(direction.x * speed, direction.y * speed);
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        
        // Attack player if close enough
        timeSinceLastAttack += delta;
        if (collidesWith(target) && timeSinceLastAttack >= attackCooldown) {
            attackPlayer();
            timeSinceLastAttack = 0;
        }
    }
    
    private void attackPlayer() {
        if (target != null && !target.isDead()) {
            target.takeDamage(damage);
            Gdx.app.log("Mob", "Attacked player for " + damage + " damage");
        }
    }
    
    @Override
    protected void onDeath() {
        Gdx.app.log("Mob", "Mob died!");
        // Pickups are spawned in WorldManager instead
    }
    
    public float getDamage() { return damage; }
}
