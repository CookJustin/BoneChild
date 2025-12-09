package com.bonechild.world;

import com.badlogic.gdx.Gdx;

/**
 * Player character
 */
public class Player extends LivingEntity {
    private int level;
    private float experience;
    private float experienceToNextLevel;
    
    // Animation state
    public enum AnimationState {
        IDLE, WALKING, ATTACKING
    }
    
    private AnimationState currentState;
    private AnimationState previousState;
    private boolean facingRight;
    
    // Attack properties
    private float attackDamage;
    private float attackRange;
    private float attackCooldown;
    private float timeSinceLastAttack;
    private boolean isAttacking; // Track if actively attacking
    
    public Player(float x, float y) {
        super(x, y, 64, 64, 100f, 200f); // Changed size to 64x64 for sprite
        this.level = 1;
        this.experience = 0;
        this.experienceToNextLevel = 100;
        this.currentState = AnimationState.IDLE;
        this.previousState = AnimationState.IDLE;
        this.facingRight = true;
        
        // Attack setup
        this.attackDamage = 25f;
        this.attackRange = 80f; // Attack range in pixels
        this.attackCooldown = 0.5f; // Can attack twice per second
        this.timeSinceLastAttack = 0;
        this.isAttacking = false;
    }
    
    @Override
    public void update(float delta) {
        // Store previous state
        previousState = currentState;
        
        // Update attack cooldown
        timeSinceLastAttack += delta;
        
        // Update facing direction based on movement (when moving)
        if (velocity.len() > 0) {
            if (velocity.x > 0) {
                facingRight = true;
            } else if (velocity.x < 0) {
                facingRight = false;
            }
        }
        
        // Determine animation state based on velocity and attack status
        if (isAttacking) {
            // Stay in attacking state while actively attacking
            currentState = AnimationState.ATTACKING;
        } else if (velocity.len() > 0) {
            // Walking state takes priority when not attacking
            currentState = AnimationState.WALKING;
        } else {
            // Only idle when not moving and not attacking
            currentState = AnimationState.IDLE;
        }
        
        // Apply velocity
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        
        // Keep player on screen
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        position.x = Math.max(0, Math.min(position.x, screenWidth - width));
        position.y = Math.max(0, Math.min(position.y, screenHeight - height));
    }
    
    /**
     * Trigger attack animation and damage nearby enemies
     */
    public void attack() {
        // Check cooldown
        if (timeSinceLastAttack < attackCooldown) {
            return;
        }
        
        isAttacking = true;
        timeSinceLastAttack = 0;
        
        Gdx.app.log("Player", "Player attacked! Damage: " + attackDamage + " Range: " + attackRange);
    }
    
    /**
     * Stop attacking
     */
    public void stopAttack() {
        isAttacking = false;
    }
    
    /**
     * Deal damage to a mob if in range
     */
    public boolean attackMob(Mob mob) {
        if (mob == null || mob.isDead()) {
            return false;
        }
        
        // Calculate distance to mob
        float dx = mob.getPosition().x - position.x;
        float dy = mob.getPosition().y - position.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // Check if in attack range
        if (distance <= attackRange) {
            mob.takeDamage(attackDamage);
            Gdx.app.log("Player", "Hit mob! Distance: " + distance + " Damage: " + attackDamage);
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if player can attack
     */
    public boolean canAttack() {
        return timeSinceLastAttack >= attackCooldown;
    }
    
    /**
     * Check if currently attacking
     */
    public boolean isCurrentlyAttacking() {
        return isAttacking;
    }
    
    /**
     * Check if state changed (for animation reset)
     */
    public boolean stateChanged() {
        return currentState != previousState;
    }
    
    /**
     * Add experience and check for level up
     */
    public void addExperience(float amount) {
        experience += amount;
        while (experience >= experienceToNextLevel) {
            levelUp();
        }
    }
    
    /**
     * Level up the player
     */
    private void levelUp() {
        level++;
        experience -= experienceToNextLevel;
        experienceToNextLevel *= 1.5f; // Increase exp needed for next level
        
        // Heal player on level up
        heal(maxHealth * 0.2f);
        
        Gdx.app.log("Player", "Level up! Now level " + level);
    }
    
    @Override
    protected void onDeath() {
        Gdx.app.log("Player", "Player died!");
        // TODO: Game over logic
    }
    
    // Getters
    public int getLevel() { return level; }
    public float getExperience() { return experience; }
    public float getExperienceToNextLevel() { return experienceToNextLevel; }
    public float getExperiencePercentage() { return experience / experienceToNextLevel; }
    
    // Animation getters
    public AnimationState getCurrentState() { return currentState; }
    public boolean isFacingRight() { return facingRight; }
}
