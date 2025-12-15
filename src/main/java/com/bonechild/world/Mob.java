package com.bonechild.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

/**
 * Enemy mob that chases the player
 */
public class Mob extends LivingEntity {
    private Player target;
    private float damage;
    private float attackCooldown;
    private float timeSinceLastAttack;
    
    public Mob(float x, float y, Player target) {
        super(x, y, 120, 120, 50f, 100f); // Reduced from 240x240 to 120x120 (50% smaller, 2.5x the 48px sprite)
        this.target = target;
        this.damage = 10f;
        this.attackCooldown = 1f; // Attack once per second
        this.timeSinceLastAttack = 0;
        
        // Set hitbox (24x24) positioned at the chest/body area - larger for easier hits
        // X: Centered horizontally (120 - 24) / 2 = 48
        // Y: Positioned at body area (lower portion of sprite)
        // 24x24 is about 20% of the 120px sprite, making it easier to hit
        setHitbox(24, 24, 48, 15);
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
    
    public void setDamage(float damage) { 
        this.damage = damage; 
    }
}
