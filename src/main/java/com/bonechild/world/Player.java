package com.bonechild.world;

import com.badlogic.gdx.Gdx;

/**
 * Player character
 */
public class Player extends LivingEntity {
    private int level;
    private float experience;
    private float experienceToNextLevel;
    private int gold;
    
    // Power-up levels
    private int speedLevel = 0;
    private int strengthLevel = 0;
    private int grabLevel = 0;
    private int attackSpeedLevel = 0;
    private int maxHpLevel = 0;
    private int xpBoostLevel = 0;
    private int explosionChanceLevel = 0;
    private boolean leveledUpThisFrame = false;
    
    // Animation state
    public enum AnimationState {
        IDLE, WALKING, ATTACKING, HURT, DEAD
    }
    
    private AnimationState currentState;
    private AnimationState previousState;
    private boolean facingRight;
    private float hurtAnimationTimer = 0f;
    private boolean isPlayingHurtAnimation = false;
    private static final float HURT_ANIMATION_DURATION = 0.25f; // 5 frames * 0.05s
    
    // Invincibility frames
    private float invincibilityTimer = 0f;
    private boolean isInvincible = false;
    private static final float INVINCIBILITY_DURATION = 1.0f; // 1 second of invincibility after getting hit
    
    // Attack properties
    private float attackDamage;
    private float attackRange;
    private float attackCooldown;
    private float timeSinceLastAttack;
    
    public Player(float x, float y) {
        super(x, y, 64, 64, 100f, 200f); // Changed size to 64x64 for sprite
        this.level = 1;
        this.experience = 0;
        this.experienceToNextLevel = 100;
        this.gold = 0;
        this.currentState = AnimationState.IDLE;
        this.previousState = AnimationState.IDLE;
        this.facingRight = true;
        
        // Attack setup
        this.attackDamage = 40f;
        this.attackRange = 500f; // Attack range in pixels (projectile range)
        this.attackCooldown = 0.5f; // Can attack twice per second
        this.timeSinceLastAttack = 0;
    }
    
    @Override
    public void update(float delta) {
        // Store previous state
        previousState = currentState;
        
        // Update attack cooldown
        timeSinceLastAttack += delta;
        
        // Update invincibility timer
        if (isInvincible) {
            invincibilityTimer += delta;
            if (invincibilityTimer >= INVINCIBILITY_DURATION) {
                invincibilityTimer = 0f;
                isInvincible = false;
            }
        }
        
        // If dead, stay in dead state and stop all movement
        if (isDead()) {
            if (currentState != AnimationState.DEAD) {
                currentState = AnimationState.DEAD;
                // Stop all movement when dead
                velocity.x = 0;
                velocity.y = 0;
            }
            return;
        }
        
        // Update hurt animation timer (if playing)
        if (isPlayingHurtAnimation) {
            hurtAnimationTimer += delta;
            if (hurtAnimationTimer >= HURT_ANIMATION_DURATION) {
                hurtAnimationTimer = 0f;
                isPlayingHurtAnimation = false;
            }
        }
        
        // Update facing direction based on movement (when moving)
        if (velocity.len() > 0) {
            if (velocity.x > 0) {
                facingRight = true;
            } else if (velocity.x < 0) {
                facingRight = false;
            }
        }
        
        // Determine animation state based on hurt status and velocity
        if (isPlayingHurtAnimation) {
            // Show hurt animation but allow movement to continue
            currentState = AnimationState.HURT;
        } else if (velocity.len() > 0) {
            // Walking state when moving
            currentState = AnimationState.WALKING;
        } else {
            // Idle when not moving
            currentState = AnimationState.IDLE;
        }
        
        // Apply velocity (movement continues during hurt animation)
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        
        // Keep player on screen
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        position.x = Math.max(0, Math.min(position.x, screenWidth - width));
        position.y = Math.max(0, Math.min(position.y, screenHeight - height));
    }
    
    @Override
    public void takeDamage(float damage) {
        if (isInvincible) {
            return;
        }
        
        super.takeDamage(damage);
        // Trigger hurt animation when taking damage (if not dead)
        if (!isDead()) {
            currentState = AnimationState.HURT;
            hurtAnimationTimer = 0f;
            isPlayingHurtAnimation = true;
            isInvincible = true;
        }
    }
    
    /**
     * Get the closest mob within attack range
     */
    public Mob getClosestMob(com.badlogic.gdx.utils.Array<Mob> mobs) {
        Mob closest = null;
        float closestDistance = attackRange;
        
        for (Mob mob : mobs) {
            if (mob == null || mob.isDead()) {
                continue;
            }
            
            // Calculate distance to mob
            float dx = mob.getPosition().x - position.x;
            float dy = mob.getPosition().y - position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            // Check if in range and closer than previous closest
            if (distance <= attackRange && distance < closestDistance) {
                closest = mob;
                closestDistance = distance;
            }
        }
        
        return closest;
    }
    
    /**
     * Create a fireball projectile at the closest mob
     */
    public Projectile castFireball(Mob targetMob) {
        // Check cooldown
        if (timeSinceLastAttack < attackCooldown || targetMob == null) {
            return null;
        }
        
        // Reset cooldown
        timeSinceLastAttack = 0;
        
        // Create projectile toward target
        Projectile projectile = new Projectile(
            position.x + width / 2f,
            position.y + height / 2f,
            targetMob.getPosition().x + targetMob.getWidth() / 2f,
            targetMob.getPosition().y + targetMob.getHeight() / 2f,
            attackDamage
        );
        
        Gdx.app.log("Player", "Fireball cast! Damage: " + attackDamage);
        return projectile;
    }
    
    /**
     * Check if player can attack
     */
    public boolean canAttack() {
        return timeSinceLastAttack >= attackCooldown;
    }
    
    /**
     * Check if state changed (for animation reset)
     */
    public boolean stateChanged() {
        return currentState != previousState;
    }
    
    /**
     * Add experience with XP boost multiplier
     */
    public void addExperience(float amount) {
        // Apply XP boost multiplier (10% per level)
        float multiplier = 1.0f + (xpBoostLevel * 0.1f);
        experience += amount * multiplier;
        while (experience >= experienceToNextLevel) {
            levelUp();
        }
    }
    
    /**
     * Add gold to player
     */
    public void addGold(int amount) {
        gold += amount;
        Gdx.app.log("Player", "Gold collected! Total: " + gold);
    }
    
    /**
     * Spend gold (returns true if successful)
     */
    public boolean spendGold(int amount) {
        if (gold >= amount) {
            gold -= amount;
            Gdx.app.log("Player", "Spent " + amount + " gold. Remaining: " + gold);
            return true;
        }
        return false;
    }
    
    /**
     * Level up the player
     */
    private void levelUp() {
        level++;
        experience -= experienceToNextLevel;
        
        // Increase exp needed for next level
        // First 3 levels: 1.5x multiplier (normal progression)
        // After level 3: 1.05x multiplier (scaled down by ~30% for easier leveling)
        if (level <= 3) {
            experienceToNextLevel *= 1.5f;
        } else {
            experienceToNextLevel *= 1.05f;
        }
        
        // Heal player on level up
        heal(maxHealth * 0.2f);
        
        leveledUpThisFrame = true;
        Gdx.app.log("Player", "Level up! Now level " + level + ", Next level needs: " + experienceToNextLevel + " XP");
    }
    
    /**
     * Apply a power-up upgrade
     */
    public void applyPowerUp(String powerUpType) {
        switch(powerUpType) {
            case "SPEED":
                speedLevel++;
                speed += 50f; // Increase speed by 50 each time
                Gdx.app.log("Player", "Speed upgraded! Level: " + speedLevel + ", Speed: " + speed);
                break;
            case "STRENGTH":
                strengthLevel++;
                attackDamage += 10f; // Increase damage by 10 each time
                Gdx.app.log("Player", "Strength upgraded! Level: " + strengthLevel + ", Damage: " + attackDamage);
                break;
            case "GRAB":
                grabLevel++;
                // Grab increases pickup pull distance and speed (handled in Pickup class)
                Gdx.app.log("Player", "Grab upgraded! Level: " + grabLevel);
                break;
            case "ATTACK_SPEED":
                attackSpeedLevel++;
                attackCooldown = Math.max(0.1f, attackCooldown - 0.05f); // Reduce cooldown by 0.05s, minimum 0.1s
                Gdx.app.log("Player", "Attack Speed upgraded! Level: " + attackSpeedLevel + ", Cooldown: " + attackCooldown);
                break;
            case "MAX_HP":
                maxHpLevel++;
                maxHealth += 20f; // Increase max health by 20
                currentHealth += 20f; // Also heal by 20 when upgrading
                Gdx.app.log("Player", "Max HP upgraded! Level: " + maxHpLevel + ", Max HP: " + maxHealth);
                break;
            case "XP_BOOST":
                xpBoostLevel++;
                // XP boost multiplier is calculated when collecting XP
                Gdx.app.log("Player", "XP Boost upgraded! Level: " + xpBoostLevel);
                break;
            case "EXPLOSION_CHANCE":
                explosionChanceLevel++;
                // Each level gives 5% chance for explosions
                Gdx.app.log("Player", "Explosion Chance upgraded! Level: " + explosionChanceLevel + ", Chance: " + (explosionChanceLevel * 5) + "%");
                break;
        }
    }
    
    /**
     * Get explosion chance (5% per level)
     */
    public float getExplosionChance() {
        return explosionChanceLevel * 0.05f; // 5% per level
    }
    
    /**
     * Check if player leveled up this frame
     */
    public boolean didLevelUpThisFrame() {
        return leveledUpThisFrame;
    }
    
    /**
     * Reset the level-up flag after checking
     */
    public void clearLevelUpFlag() {
        leveledUpThisFrame = false;
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
    public int getGold() { return gold; }
    public int getSpeedLevel() { return speedLevel; }
    public int getStrengthLevel() { return strengthLevel; }
    public int getGrabLevel() { return grabLevel; }
    public int getAttackSpeedLevel() { return attackSpeedLevel; }
    public int getMaxHpLevel() { return maxHpLevel; }
    public int getXpBoostLevel() { return xpBoostLevel; }
    public int getExplosionChanceLevel() { return explosionChanceLevel; }
    public float getAttackDamage() { return attackDamage; }
    
    // Animation getters
    public AnimationState getCurrentState() { return currentState; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isInvincible() { return isInvincible; }
    public float getInvincibilityTimer() { return invincibilityTimer; }
}
