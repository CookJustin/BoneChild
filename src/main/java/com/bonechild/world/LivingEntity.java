package com.bonechild.world;

/**
 * Living entity with health and damage capabilities
 */
public abstract class LivingEntity extends Entity {
    protected float maxHealth;
    protected float currentHealth;
    protected float speed;
    protected boolean dead;
    
    public LivingEntity(float x, float y, float width, float height, float maxHealth, float speed) {
        super(x, y, width, height);
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.speed = speed;
        this.dead = false;
    }
    
    /**
     * Take damage and check if dead
     */
    public void takeDamage(float damage) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            currentHealth = 0;
            dead = true;
            onDeath();
        }
    }
    
    /**
     * Heal the entity
     */
    public void heal(float amount) {
        currentHealth = Math.min(currentHealth + amount, maxHealth);
    }
    
    /**
     * Called when entity dies
     */
    protected abstract void onDeath();
    
    // Getters
    public float getMaxHealth() { return maxHealth; }
    public float getCurrentHealth() { return currentHealth; }
    public float getSpeed() { return speed; }
    public boolean isDead() { return dead; }
    
    public float getHealthPercentage() {
        return currentHealth / maxHealth;
    }
}
