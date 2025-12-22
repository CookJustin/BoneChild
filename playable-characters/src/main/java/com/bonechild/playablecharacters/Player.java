package com.bonechild.playablecharacters;

import com.badlogic.gdx.Gdx;
import com.bonechild.monsters.api.MobEntity;

/**
 * Player character
 */
public class Player extends LivingEntity {
    
    /**
     * Callback interface for spawning projectiles
     * This allows Player to create projectiles without depending on WorldManager
     */
    public interface ProjectileSpawner {
        void spawnProjectile(Projectile projectile);
    }
    
    // Callback for spawning projectiles
    private ProjectileSpawner projectileSpawner;
    
    // Reference to mobs for auto-targeting (injected by WorldManager)
    private com.badlogic.gdx.utils.Array<MobEntity> targetableMobs;
    
    // Player stats
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
    private int chainLightningLevel = 0;
    private int lifestealLevel = 0;
    private boolean leveledUpThisFrame = false;
    
    // Animation state
    public enum AnimationState {
        IDLE, WALKING, ATTACKING, HURT, DEAD
    }
    
    private AnimationState currentState;
    private AnimationState previousState;
    private boolean facingRight;
    
    // Hurt animation
    private static final float HURT_ANIMATION_DURATION = 0.25f;
    private float hurtAnimationTimer = 0f;
    private boolean isPlayingHurtAnimation = false;
    
    // Invincibility frames
    private static final float INVINCIBILITY_DURATION = 0.375f;
    private float invincibilityTimer = 0f;
    private boolean isInvincible = false;
    
    // Dodge mechanic
    private static final int MAX_DODGE_CHARGES = 3;
    private static final float DODGE_CHARGE_COOLDOWN = 2.0f;
    private static final float DODGE_DURATION = 0.25f;
    private static final float DODGE_DISTANCE = 150f;
    private static final float GHOST_SPAWN_INTERVAL = 0.04f; // Spawn ghost every 0.04s during dodge
    private int dodgeCharges = MAX_DODGE_CHARGES;
    private float dodgeRechargeTimer = 0f;
    private boolean isDodging = false;
    private float dodgeTimer = 0f;
    private float dodgeDirectionX = 0f;
    private float dodgeDirectionY = 0f;
    private float ghostSpawnTimer = 0f;
    private java.util.ArrayList<GhostSprite> ghostTrail = new java.util.ArrayList<>();

    // Kill streak system
    private static final float KILL_STREAK_TIMEOUT = 5.0f;
    private int killStreak = 0;
    private float killStreakTimer = 0f;
    private float killStreakMultiplier = 1.0f;
    
    // Combat stats
    private static final float BASE_CRIT_CHANCE = 0.15f;
    private static final float CRIT_MULTIPLIER = 2.0f;
    private static final float DAMAGE_VARIANCE = 0.2f;
    private java.util.Random critRandom = new java.util.Random();
    
    // Temporary grab boost on level up
    private boolean tempGrabBoostActive = false;
    private float tempGrabBoostTimer = 0f;
    private static final float TEMP_GRAB_BOOST_DURATION = 5.0f; // 5 seconds of max grab on level up
    private static final int TEMP_GRAB_BOOST_LEVEL = 10; // Temporarily max out grab to level 10
    
    // Attack properties
    private float attackDamage;
    private float attackRange;
    private float attackCooldown;
    private float timeSinceLastAttack;
    
    public Player(float x, float y) {
        super(x, y, 48, 48, 100f, 200f); // Entity size matches PNG: 48x48
        
        // The PNG is 48x48, with visible character art in the center
        // The character sprite is roughly 20x20 pixels centered in the 48x48 PNG
        // Set hitbox to 24x24 centered BOTH horizontally and vertically in the 48x48 entity
        setHitbox(24, 24, 12, 12); // 24x24 hitbox, centered at (12, 12) offset
        
        this.level = 1;
        this.experience = 0;
        this.experienceToNextLevel = 100;
        this.gold = 0;
        this.currentState = AnimationState.IDLE;
        this.previousState = AnimationState.IDLE;
        this.facingRight = true;
        
        // Attack setup
        this.attackDamage = 48f; // Increased by 20% from 40f
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
        
        // Auto-attack: shoot at nearest mob if off cooldown
        if (canAttack() && !isDead() && projectileSpawner != null && targetableMobs != null) {
            MobEntity target = getClosestMob(targetableMobs);
            if (target != null) {
                Projectile projectile = castFireball(target);
                if (projectile != null) {
                    projectileSpawner.spawnProjectile(projectile);
                }
            }
        } else {
            // Debug: Log why auto-attack isn't firing
            if (!canAttack()) {
                // Cooldown active - don't log every frame
            } else if (isDead()) {
                Gdx.app.log("Player", "Can't attack - player is dead");
            } else if (projectileSpawner == null) {
                Gdx.app.log("Player", "Can't attack - projectileSpawner is null!");
            } else if (targetableMobs == null) {
                Gdx.app.log("Player", "Can't attack - targetableMobs is null!");
            }
        }
        
        // Update kill streak timer
        if (killStreak > 0) {
            killStreakTimer += delta;
            if (killStreakTimer >= KILL_STREAK_TIMEOUT) {
                // Reset streak
                killStreak = 0;
                killStreakTimer = 0f;
                updateKillStreakMultiplier();
                Gdx.app.log("Player", "Kill streak lost!");
            }
        }
        
        // Update dodge recharge timer
        if (dodgeCharges < MAX_DODGE_CHARGES) {
            dodgeRechargeTimer += delta;
            if (dodgeRechargeTimer >= DODGE_CHARGE_COOLDOWN) {
                dodgeCharges++;
                dodgeRechargeTimer = 0f;
                Gdx.app.log("Player", "Dodge charge restored! Charges: " + dodgeCharges);
            }
        }
        
        // Update dodge state
        if (isDodging) {
            dodgeTimer += delta;
            
            // Spawn ghost trail during dodge
            ghostSpawnTimer += delta;
            if (ghostSpawnTimer >= GHOST_SPAWN_INTERVAL) {
                spawnGhost();
                ghostSpawnTimer = 0f;
            }

            // Apply dodge movement
            float dodgeSpeed = DODGE_DISTANCE / DODGE_DURATION;
            velocity.x = dodgeDirectionX * dodgeSpeed;
            velocity.y = dodgeDirectionY * dodgeSpeed;
            
            // End dodge after duration
            if (dodgeTimer >= DODGE_DURATION) {
                isDodging = false;
                dodgeTimer = 0f;
                isInvincible = false;
                velocity.x = 0;
                velocity.y = 0;

                // Spawn final ghost
                spawnGhost();
            }
        }
        
        // Update ghost trail (fade out ghosts)
        for (int i = ghostTrail.size() - 1; i >= 0; i--) {
            GhostSprite ghost = ghostTrail.get(i);
            ghost.update(delta);
            if (ghost.isExpired()) {
                ghostTrail.remove(i);
            }
        }

        // Update invincibility timer (for taking damage, not dodge)
        if (isInvincible && !isDodging) {
            invincibilityTimer += delta;
            if (invincibilityTimer >= INVINCIBILITY_DURATION) {
                invincibilityTimer = 0f;
                isInvincible = false;
            }
        }
        
        // Update temporary grab boost timer
        if (tempGrabBoostActive) {
            tempGrabBoostTimer += delta;
            if (tempGrabBoostTimer >= TEMP_GRAB_BOOST_DURATION) {
                tempGrabBoostActive = false;
                tempGrabBoostTimer = 0f;
                Gdx.app.log("Player", "Temporary grab boost expired!");
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
    
    /**
     * Perform a dodge/dash
     */
    public boolean dodge(float dirX, float dirY) {
        if (dodgeCharges <= 0 || isDodging || isDead()) {
            return false;
        }
        
        // Use a charge
        dodgeCharges--;
        
        // If no direction provided, dodge in facing direction
        if (dirX == 0 && dirY == 0) {
            dirX = facingRight ? 1 : -1;
        } else {
            // Normalize direction
            float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            if (length > 0) {
                dirX /= length;
                dirY /= length;
            }
        }
        
        // Start dodge
        isDodging = true;
        dodgeTimer = 0f;
        ghostSpawnTimer = 0f; // Reset ghost spawn timer
        dodgeDirectionX = dirX;
        dodgeDirectionY = dirY;
        isInvincible = true; // Grant i-frames during dodge
        
        Gdx.app.log("Player", "Dodge! Charges remaining: " + dodgeCharges);
        return true;
    }
    
    /**
     * Spawn a ghost sprite at current position
     */
    private void spawnGhost() {
        ghostTrail.add(new GhostSprite(position.x, position.y, width, height, facingRight));
    }

    /**
     * Get ghost trail for rendering
     */
    public java.util.ArrayList<GhostSprite> getGhostTrail() {
        return ghostTrail;
    }

    @Override
    public void takeDamage(float damage) {
        if (isInvincible) {
            return;
        }
        
        super.takeDamage(damage);
        
        // Reset kill streak when taking damage
        resetKillStreak();
        
        // Trigger hurt animation when taking damage (if not dead)
        if (!isDead()) {
            currentState = AnimationState.HURT;
            hurtAnimationTimer = 0f;
            isPlayingHurtAnimation = true;
        }
    }
    
    /**
     * Get the closest mob within attack range
     */
    public MobEntity getClosestMob(com.badlogic.gdx.utils.Array<MobEntity> mobs) {
        MobEntity closest = null;
        float closestDistance = attackRange;
        
        for (MobEntity mob : mobs) {
            if (mob == null || mob.isDead()) {
                continue;
            }
            
            // Calculate distance to mob
            float dx = mob.getX() - position.x;
            float dy = mob.getY() - position.y;
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
    public Projectile castFireball(MobEntity targetMob) {
        // Check cooldown
        if (timeSinceLastAttack < attackCooldown || targetMob == null) {
            return null;
        }
        
        // Reset cooldown
        timeSinceLastAttack = 0;
        
        // Apply damage variance (80-120% of base damage)
        float damageVariance = 1.0f + (critRandom.nextFloat() * 2.0f - 1.0f) * DAMAGE_VARIANCE;
        float variedDamage = attackDamage * damageVariance;
        
        // Roll for critical hit (applies to the varied damage)
        boolean isCrit = critRandom.nextFloat() < BASE_CRIT_CHANCE;
        float finalDamage = isCrit ? variedDamage * CRIT_MULTIPLIER : variedDamage;
        
        // Create projectile toward target's hitbox center (not sprite center)
        float targetHitboxCenterX = targetMob.getX() + targetMob.getHitboxOffsetX() + targetMob.getHitboxWidth() / 2f;
        float targetHitboxCenterY = targetMob.getY() + targetMob.getHitboxOffsetY() + targetMob.getHitboxHeight() / 2f;

        // Spawn from the player's hitbox center (where the character's body actually is)
        float visualCenterX = position.x + getHitboxOffsetX() + getHitboxWidth() / 2f;
        float visualCenterY = position.y + getHitboxOffsetY() + getHitboxHeight() / 2f;
        
        Projectile projectile = new Projectile(
            visualCenterX,
            visualCenterY,
            targetHitboxCenterX,
            targetHitboxCenterY,
            finalDamage,
            isCrit,
            "fireball"
        );
        
        if (isCrit) {
            Gdx.app.log("Player", "💥 CRITICAL HIT! Damage: " + finalDamage + " (base: " + attackDamage + ")");
        } else {
            Gdx.app.log("Player", "Fireball cast! Damage: " + finalDamage + " (base: " + attackDamage + ")");
        }
        return projectile;
    }
    
    /**
     * Set the projectile spawner callback
     */
    public void setProjectileSpawner(ProjectileSpawner spawner) {
        this.projectileSpawner = spawner;
    }

    /**
     * Set the targetable mobs array (injected by WorldManager each frame)
     */
    public void setTargetableMobs(com.badlogic.gdx.utils.Array<MobEntity> mobs) {
        this.targetableMobs = mobs;
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
        
        // Activate temporary grab boost
        tempGrabBoostActive = true;
        tempGrabBoostTimer = 0f;
        Gdx.app.log("Player", "🌟 GRAB BOOST ACTIVATED! Max pickup range for " + TEMP_GRAB_BOOST_DURATION + " seconds!");
        
        leveledUpThisFrame = true;
        Gdx.app.log("Player", "🎉 Level up! Now level " + level + ", Next level needs: " + experienceToNextLevel + " XP");
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
            case "CHAIN_LIGHTNING":
                chainLightningLevel++;
                // Each level gives 20% chance for chain lightning (maxes at 100% at level 5)
                Gdx.app.log("Player", "Chain Lightning upgraded! Level: " + chainLightningLevel + ", Chance: " + Math.min(100, chainLightningLevel * 20) + "%");
                break;
            case "LIFESTEAL":
                lifestealLevel++;
                // Each level gives 15% lifesteal on kill
                Gdx.app.log("Player", "Lifesteal upgraded! Level: " + lifestealLevel + ", Heal: " + (lifestealLevel * 15) + "% damage on kill");
                break;
        }
    }
    
    /**
     * Get chain lightning chance (20% per level, max 100%)
     */
    public float getChainLightningChance() {
        return Math.min(1.0f, chainLightningLevel * 0.20f);
    }
    
    /**
     * Get lifesteal percentage (15% per level)
     */
    public float getLifestealPercent() {
        return lifestealLevel * 0.15f;
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
    public boolean hasLeveledUpThisFrame() {
        boolean result = leveledUpThisFrame;
        leveledUpThisFrame = false; // Reset flag after checking
        return result;
    }
    
    /**
     * Clear the level up flag (called after handling level up)
     */
    public void clearLevelUpFlag() {
        leveledUpThisFrame = false;
    }
    
    @Override
    protected void onDeath() {
        Gdx.app.log("Player", "Player died!");
        // TODO: Game over logic
    }
    
    /**
     * Increment kill streak (called when player kills an enemy)
     */
    public void incrementKillStreak() {
        killStreak++;
        killStreakTimer = 0f; // Reset timer
        updateKillStreakMultiplier();
        
        // Log milestone streaks
        if (killStreak == 5 || killStreak == 10 || killStreak == 25 || killStreak == 50 || killStreak == 100) {
            Gdx.app.log("Player", "🔥 KILL STREAK: " + killStreak + "! Multiplier: " + killStreakMultiplier + "x");
        }
    }
    
    /**
     * Reset kill streak (called when player takes damage)
     */
    public void resetKillStreak() {
        if (killStreak > 0) {
            Gdx.app.log("Player", "Kill streak broken at " + killStreak);
            killStreak = 0;
            killStreakTimer = 0f;
            updateKillStreakMultiplier();
        }
    }
    
    /**
     * Update the gold multiplier based on streak
     */
    private void updateKillStreakMultiplier() {
        if (killStreak >= 50) {
            killStreakMultiplier = 3.0f;
        } else if (killStreak >= 25) {
            killStreakMultiplier = 2.5f;
        } else if (killStreak >= 10) {
            killStreakMultiplier = 2.0f;
        } else if (killStreak >= 5) {
            killStreakMultiplier = 1.5f;
        } else {
            killStreakMultiplier = 1.0f;
        }
    }
    
    // Getters
    public int getLevel() { return level; }
    public float getExperience() { return experience; }
    public float getExperienceToNextLevel() { return experienceToNextLevel; }
    public float getExperiencePercentage() { return experience / experienceToNextLevel; }
    public int getGold() { return gold; }
    public int getSpeedLevel() { return speedLevel; }
    public int getStrengthLevel() { return strengthLevel; }
    public int getGrabLevel() { 
        // Return boosted level if temporary boost is active
        return tempGrabBoostActive ? TEMP_GRAB_BOOST_LEVEL : grabLevel; 
    }
    public int getAttackSpeedLevel() { return attackSpeedLevel; }
    public int getMaxHpLevel() { return maxHpLevel; }
    public int getXpBoostLevel() { return xpBoostLevel; }
    public int getExplosionChanceLevel() { return explosionChanceLevel; }
    public int getChainLightningLevel() { return chainLightningLevel; }
    public int getLifestealLevel() { return lifestealLevel; }
    public float getAttackDamage() { return attackDamage; }
    public int getDodgeCharges() { return dodgeCharges; }
    public int getMaxDodgeCharges() { return MAX_DODGE_CHARGES; }
    public float getDodgeRechargeProgress() { 
        return dodgeCharges < MAX_DODGE_CHARGES ? dodgeRechargeTimer / DODGE_CHARGE_COOLDOWN : 0f; 
    }
    public boolean isDodging() { return isDodging; }
    public int getKillStreak() { return killStreak; }
    public float getKillStreakMultiplier() { return killStreakMultiplier; }
    public boolean isTempGrabBoostActive() { return tempGrabBoostActive; }
    public float getTempGrabBoostTimeRemaining() { 
        return tempGrabBoostActive ? (TEMP_GRAB_BOOST_DURATION - tempGrabBoostTimer) : 0f; 
    }

    // Animation getters
    public AnimationState getCurrentState() { return currentState; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isInvincible() { return isInvincible; }
    public float getInvincibilityTimer() { return invincibilityTimer; }

    /**
     * Backwards-compatible alias for hasLeveledUpThisFrame used by BoneChildGame
     */
    public boolean didLevelUpThisFrame() {
        return hasLeveledUpThisFrame();
    }

    // Setters for save state loading

    public void setLevel(int level) {
        this.level = level;
    }

    public void setExperience(float experience) {
        this.experience = experience;
    }

    public void setExperienceToNextLevel(float experienceToNextLevel) {
        this.experienceToNextLevel = experienceToNextLevel;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void setCurrentHealth(float health) {
        this.currentHealth = health;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }
}
