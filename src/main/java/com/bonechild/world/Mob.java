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
        super(x, y, 48, 48, 50f, 100f);
        this.target = target;
        this.damage = 10f;
        this.attackCooldown = 1f; // Attack once per second
        this.timeSinceLastAttack = 0;
    }
    
    @Override
    public void update(float delta) {
        if (dead || target == null || target.isDead()) {
            return;
        }
        
        // Move towards player
        Vector2 targetPos = target.getPosition();
        Vector2 direction = new Vector2(targetPos.x - position.x, targetPos.y - position.y);
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
        // TODO: Drop experience
    }
    
    public float getDamage() { return damage; }
}
