package com.bonechild.monsters.impl;

import com.badlogic.gdx.math.Vector2;
import com.bonechild.monsters.api.MobEntity;

/**
 * Base mob class with health, movement, and collision
 */
public class Mob implements MobEntity {
    // Position and dimensions
    protected Vector2 position;
    protected Vector2 velocity;
    protected float width;
    protected float height;

    // Hitbox
    protected float hitboxWidth;
    protected float hitboxHeight;
    protected float hitboxOffsetX;
    protected float hitboxOffsetY;

    // Health
    protected float maxHealth;
    protected float currentHealth;
    protected boolean dead;

    // Movement
    protected float speed;
    protected Vector2 targetPosition;  // Target to chase (usually player position)

    // Combat
    protected float damage;

    public Mob(float x, float y, Vector2 targetPosition) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2();
        this.targetPosition = targetPosition;

        // Default mob stats
        this.width = 80f;
        this.height = 80f;
        this.maxHealth = 50f;
        this.currentHealth = 50f;
        this.speed = 100f;
        this.damage = 10f;
        this.dead = false;

        // Default hitbox matches sprite size
        this.hitboxWidth = width;
        this.hitboxHeight = height;
        this.hitboxOffsetX = 0;
        this.hitboxOffsetY = 0;
    }

    /**
     * Update mob - chase target position
     */
    public void update(float delta) {
        if (dead || targetPosition == null) {
            return;
        }

        // Move toward target
        Vector2 direction = new Vector2(targetPosition).sub(position).nor();
        velocity.set(direction.x * speed, direction.y * speed);
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }

    /**
     * Update target position (called by game-core with player position)
     */
    public void setTargetPosition(Vector2 target) {
        this.targetPosition = target;
    }

    /**
     * Set custom hitbox
     */
    protected void setHitbox(float width, float height, float offsetX, float offsetY) {
        this.hitboxWidth = width;
        this.hitboxHeight = height;
        this.hitboxOffsetX = offsetX;
        this.hitboxOffsetY = offsetY;
    }

    /**
     * Take damage
     */
    public void takeDamage(float damage) {
        if (dead) return;

        currentHealth -= damage;
        if (currentHealth <= 0) {
            currentHealth = 0;
            dead = true;
            onDeath();
        }
    }

    /**
     * Called when mob dies
     */
    protected void onDeath() {
        // Override in subclasses if needed
    }

    /**
     * Heal the mob
     */
    public void heal(float amount) {
        currentHealth = Math.min(currentHealth + amount, maxHealth);
    }

    // Getters
    public Vector2 getPosition() { return position; }
    public float getDamage() { return damage; }
    public float getCurrentHealth() { return currentHealth; }
    public float getMaxHealth() { return maxHealth; }
    public float getSpeed() { return speed; }

    // Setters
    public void setDamage(float damage) { this.damage = damage; }

    // MobEntity interface implementation
    @Override
    public String getTypeId() {
        return "mob";
    }

    @Override
    public float getX() {
        return position.x;
    }

    @Override
    public float getY() {
        return position.y;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public float getHitboxOffsetX() {
        return hitboxOffsetX;
    }

    @Override
    public float getHitboxOffsetY() {
        return hitboxOffsetY;
    }

    @Override
    public float getHitboxWidth() {
        return hitboxWidth;
    }

    @Override
    public float getHitboxHeight() {
        return hitboxHeight;
    }

    @Override
    public boolean isActive() {
        return !dead;
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    @Override
    public float getHealthPercentage() {
        return maxHealth <= 0f ? 0f : currentHealth / maxHealth;
    }

    @Override
    public boolean isBoss() {
        return false;
    }
}

