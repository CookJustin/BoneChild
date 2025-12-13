package com.bonechild.world;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.bonechild.rendering.Assets;
import com.bonechild.rendering.Renderer;

/**
 * Manages all entities in the game world
 * Handles spawning, updating, and entity lifecycle
 */
public class WorldManager {
    private Player player;
    private Array<Mob> mobs;
    private Array<Pickup> pickups;
    private Array<Projectile> projectiles;
    private Array<Explosion> explosions;
    private Random random;
    private Assets assets;
    private Renderer renderer;
    private ComboSystem comboSystem;
    
    // Wave management
    private float waveTimer;
    private float waveInterval;
    private int currentWave;
    private int mobsPerWave;
    
    public WorldManager() {
        this.mobs = new Array<>();
        this.pickups = new Array<>();
        this.projectiles = new Array<>();
        this.explosions = new Array<>();
        this.random = new Random();
        this.comboSystem = new ComboSystem();
        
        // Initialize wave system
        this.waveTimer = 0;
        this.waveInterval = 0f; // Start at 0 so wave 1 spawns immediately
        this.currentWave = 0;
        this.mobsPerWave = 3;
        
        // Create player at center of screen
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;
        this.player = new Player(centerX, centerY);
        
        Gdx.app.log("WorldManager", "World initialized with player at (" + centerX + ", " + centerY + ")");
    }
    
    /**
     * Set assets (needed for explosion animation)
     */
    public void setAssets(Assets assets) {
        this.assets = assets;
    }
    
    /**
     * Set renderer (needed for camera shake effects)
     */
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }
    
    /**
     * Get the combo system
     */
    public ComboSystem getComboSystem() {
        return comboSystem;
    }
    
    /**
     * Update all entities
     */
    public void update(float delta) {
        // Update player
        player.update(delta);
        
        // 🎉 Check if player leveled up this frame and spawn celebration particles!
        if (player.hasLeveledUpThisFrame() && renderer != null) {
            float playerCenterX = player.getPosition().x + player.getWidth() / 2f;
            float playerCenterY = player.getPosition().y + player.getHeight() / 2f;
            renderer.spawnLevelUpParticles(playerCenterX, playerCenterY);
            Gdx.app.log("WorldManager", "🎉 LEVEL UP CELEBRATION PARTICLES!");
        }
        
        // Update mobs
        for (int i = mobs.size - 1; i >= 0; i--) {
            Mob mob = mobs.get(i);
            mob.update(delta);
            
            // Remove dead mobs and spawn pickups
            if (mob.isDead()) {
                float mobX = mob.getPosition().x;
                float mobY = mob.getPosition().y;
                
                // Increment kill streak when mob dies
                player.incrementKillStreak();
                
                spawnPickupsFromMob(mob);
                
                // Check for explosion chance
                float explosionChance = player.getExplosionChance();
                if (explosionChance > 0) {
                    float roll = random.nextFloat();
                    Gdx.app.log("WorldManager", "Explosion chance: " + (explosionChance * 100) + "%, rolled: " + (roll * 100) + "%");
                    if (roll < explosionChance) {
                        spawnExplosion(mobX, mobY);
                        Gdx.app.log("WorldManager", "✨ EXPLOSION TRIGGERED!");
                    }
                } else {
                    Gdx.app.log("WorldManager", "No explosion chance (need to select Explosion power-up)");
                }
                
                mobs.removeIndex(i);
                Gdx.app.log("WorldManager", "Mob removed. Remaining: " + mobs.size);
            }
        }
        
        // Update pickups
        for (int i = pickups.size - 1; i >= 0; i--) {
            Pickup pickup = pickups.get(i);
            pickup.update(delta);
            
            // Apply magnetic pull toward player
            pickup.applyMagneticPull(player, delta);
            
            // Check if player collected this pickup
            if (pickup.shouldCollect(player)) {
                collectPickup(pickup, i);
            }
            
            // Remove collected pickups
            if (pickup.isCollected()) {
                pickups.removeIndex(i);
            }
        }
        
        // Auto-cast fireballs at closest mob
        if (player.canAttack() && mobs.size > 0) {
            Mob closestMob = player.getClosestMob(mobs);
            if (closestMob != null) {
                Projectile fireball = player.castFireball(closestMob);
                if (fireball != null) {
                    projectiles.add(fireball);
                }
            }
        }
        
        // Update projectiles
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(delta);
            
            if (!projectile.isActive()) {
                projectiles.removeIndex(i);
                continue;
            }
            
            // Check collision with mobs
            for (Mob mob : mobs) {
                if (projectile.collidesWith(mob)) {
                    float damageDealt = projectile.getDamage();
                    boolean isCrit = projectile.isCritical();
                    
                    // Deal damage to mob
                    mob.takeDamage(damageDealt);
                    
                    // Check if mob died from this hit for lifesteal
                    boolean mobDied = mob.isDead();
                    
                    // LIFESTEAL: Heal player based on lifesteal power-up level
                    if (mobDied) {
                        int lifestealLevel = player.getLifestealLevel();
                        if (lifestealLevel > 0) {
                            float lifestealPercent = lifestealLevel * 0.10f; // 10% per level
                            float lifestealAmount = damageDealt * lifestealPercent;
                            player.heal(lifestealAmount);
                            Gdx.app.log("WorldManager", "💚 Lifesteal! Healed " + lifestealAmount + " HP (" + (lifestealPercent * 100) + "%)");
                        }
                    }
                    
                    // Spawn damage number above mob (closer to the mob, not way above it)
                    if (renderer != null) {
                        float mobCenterX = mob.getPosition().x + mob.getWidth() / 2f;
                        // Position damage number just slightly above center of mob instead of at the top
                        float mobCenterY = mob.getPosition().y + mob.getHeight() / 2f + 20f;
                        renderer.spawnDamageNumber(mobCenterX, mobCenterY, damageDealt, isCrit);
                        
                        // 🩸 BLOOD PARTICLES when mob is hit!
                        int particleCount = isCrit ? 15 : 8; // More blood on crits
                        renderer.spawnBloodParticles(mobCenterX, mobCenterY, particleCount);
                        
                        // ✨ HIT SPARKS on impact!
                        renderer.spawnHitSparks(mobCenterX, mobCenterY, isCrit ? 10 : 5);
                        
                        // SCREEN SHAKE on critical hits - bigger shake for bigger damage!
                        if (isCrit) {
                            float shakeIntensity = Math.min(damageDealt / 10f, 25f); // Scale with damage, max 25
                            float shakeDuration = 0.15f + (damageDealt / 500f); // Longer shake for bigger hits
                            renderer.shake(shakeIntensity, shakeDuration);
                            Gdx.app.log("WorldManager", "💥 SCREEN SHAKE! Intensity: " + shakeIntensity);
                        }
                    }
                    
                    // CHAIN LIGHTNING: Trigger based on player's chain lightning level
                    int chainLightningLevel = player.getChainLightningLevel();
                    if (chainLightningLevel > 0) {
                        float chainChance = chainLightningLevel * 0.15f; // 15% per level
                        if (random.nextFloat() < chainChance) {
                            int maxChains = chainLightningLevel; // More chains with higher level
                            chainLightning(mob, damageDealt, maxChains, 200f, 0.5f);
                            Gdx.app.log("WorldManager", "⚡ CHAIN LIGHTNING! Level " + chainLightningLevel + " (" + (chainChance * 100) + "% chance)");
                        }
                    }
                    
                    projectile.deactivate();
                    Gdx.app.log("WorldManager", "Projectile hit mob!");
                    break;
                }
            }
        }
        
        // Update explosions
        for (int i = explosions.size - 1; i >= 0; i--) {
            Explosion explosion = explosions.get(i);
            explosion.update(delta);
            
            // Deal AOE damage (only once per explosion)
            if (!explosion.hasDealtDamage()) {
                for (Mob mob : mobs) {
                    if (explosion.shouldDamageMob(mob)) {
                        mob.takeDamage(explosion.getDamage());
                        Gdx.app.log("WorldManager", "Explosion damaged mob for " + explosion.getDamage());
                    }
                }
                explosion.markDamageDealt();
            }
            
            // Remove finished explosions
            if (!explosion.isActive()) {
                explosions.removeIndex(i);
            }
        }
        
        // Wave spawning
        waveTimer += delta;
        if (waveTimer >= waveInterval) {
            spawnWave();
            waveTimer = 0;
        }
    }
    
    /**
     * Spawn pickups when a mob dies
     */
    private void spawnPickupsFromMob(Mob mob) {
        float mobX = mob.getPosition().x;
        float mobY = mob.getPosition().y;
        
        // Mob sprite is 240x240, so center is at mobX + 120, mobY + 120
        float mobCenterX = mobX + 120;
        float mobCenterY = mobY + 120;
        
        // Apply kill streak multiplier to gold drops
        float goldMultiplier = player.getKillStreakMultiplier();
        
        // 70% chance to drop gold coins (1-3 coins)
        if (random.nextFloat() < 0.7f) {
            int coinCount = 1 + random.nextInt(3); // 1-3 coins
            for (int i = 0; i < coinCount; i++) {
                float offsetX = (random.nextFloat() - 0.5f) * 40f;
                float offsetY = (random.nextFloat() - 0.5f) * 40f;
                
                // Base gold (2-4) multiplied by streak multiplier
                float baseGold = random.nextInt(3) + 2f;
                float bonusGold = baseGold * goldMultiplier;
                
                Pickup coin = new Pickup(
                    mobCenterX + offsetX,
                    mobCenterY + offsetY,
                    Pickup.PickupType.GOLD_COIN,
                    bonusGold
                );
                pickups.add(coin);
            }
        }
        
        // 60% chance to drop XP orb
        if (random.nextFloat() < 0.6f) {
            Pickup xpOrb = new Pickup(
                mobCenterX,
                mobCenterY,
                Pickup.PickupType.XP_ORB,
                25f // 25 XP per orb
            );
            pickups.add(xpOrb);
        }
        
        // 30% chance to drop health orb
        if (random.nextFloat() < 0.3f) {
            Pickup healthOrb = new Pickup(
                mobCenterX,
                mobCenterY,
                Pickup.PickupType.HEALTH_ORB,
                10f // Heal 10% of max health
            );
            pickups.add(healthOrb);
        }
        
        Gdx.app.log("WorldManager", "Spawned pickups at mob center (" + mobCenterX + ", " + mobCenterY + ")");
    }
    
    /**
     * Handle pickup collection by player
     */
    private void collectPickup(Pickup pickup, int index) {
        if (pickup.getType() == Pickup.PickupType.GOLD_COIN) {
            player.addGold((int) pickup.getValue());
            Gdx.app.log("WorldManager", "Collected gold coin: " + pickup.getValue());
        } else if (pickup.getType() == Pickup.PickupType.XP_ORB) {
            player.addExperience(pickup.getValue());
            Gdx.app.log("WorldManager", "Collected XP orb: " + pickup.getValue());
        } else if (pickup.getType() == Pickup.PickupType.HEALTH_ORB) {
            float healAmount = player.getMaxHealth() * (pickup.getValue() / 100f);
            player.heal(healAmount);
            Gdx.app.log("WorldManager", "Collected health orb: Healed " + healAmount + " HP");
        }
        
        // Add kill to combo system
        if (pickup.getType() == Pickup.PickupType.GOLD_COIN || pickup.getType() == Pickup.PickupType.XP_ORB) {
            comboSystem.addKill();
        }
        
        pickup.collect();
    }
    
    /**
     * Spawn a wave of enemies
     */
    private void spawnWave() {
        currentWave++;
        // Increase mobs each wave by only 1 instead of scaling with wave number
        int mobCount = mobsPerWave + Math.max(0, currentWave - 2);
        
        Gdx.app.log("WorldManager", "Spawning wave " + currentWave + " with " + mobCount + " mobs");
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        for (int i = 0; i < mobCount; i++) {
            // Spawn at random position at screen edges
            float x, y;
            if (random.nextBoolean()) {
                // Spawn on left or right edge
                x = random.nextBoolean() ? -50 : screenWidth + 50;
                y = random.nextFloat() * screenHeight;
            } else {
                // Spawn on top or bottom edge
                x = random.nextFloat() * screenWidth;
                y = random.nextBoolean() ? -50 : screenHeight + 50;
            }
            
            mobs.add(new Mob(x, y, player));
        }

        // Spawn one Vampire per wave for testing
        float vx, vy;
        if (random.nextBoolean()) {
            vx = random.nextBoolean() ? -50 : screenWidth + 50;
            vy = random.nextFloat() * screenHeight;
        } else {
            vx = random.nextFloat() * screenWidth;
            vy = random.nextBoolean() ? -50 : screenHeight + 50;
        }
        mobs.add(new Vampire(vx, vy, player, assets));
        
        // Spawn one Christmas Jad every 3 waves (on waves 3, 6, 9, etc.)
        if (currentWave % 3 == 0) {
            float cx, cy;
            if (random.nextBoolean()) {
                cx = random.nextBoolean() ? -50 : screenWidth + 50;
                cy = random.nextFloat() * screenHeight;
            } else {
                cx = random.nextFloat() * screenWidth;
                cy = random.nextBoolean() ? -50 : screenHeight + 50;
            }
            mobs.add(new ChristmasJad(cx, cy, player, assets));
            Gdx.app.log("WorldManager", "🎄 Spawned Christmas Jad boss on wave " + currentWave + "!");
        }
        
        // After wave 1 spawns, set interval to 7 seconds for all subsequent waves
        if (currentWave == 1) {
            waveInterval = 7f;
        }
    }
    
    /**
     * Spawn a single mob at a specific position
     */
    public void spawnMob(float x, float y) {
        mobs.add(new Mob(x, y, player));
    }
    
    /**
     * Spawn an explosion at the given position
     */
    private void spawnExplosion(float x, float y) {
        if (assets == null) {
            Gdx.app.log("WorldManager", "Cannot spawn explosion - assets not set");
            return;
        }
        
        // Calculate explosion damage (25% of player damage)
        float explosionDamage = player.getAttackDamage() * 0.25f;
        
        // Explosion radius
        float explosionRadius = 100f;
        
        // Create a NEW explosion animation instance (each explosion needs its own independent animation)
        com.bonechild.rendering.Animation explosionAnim = assets.createExplosionAnimation();
        
        if (explosionAnim == null) {
            Gdx.app.log("WorldManager", "Explosion animation not available - skipping explosion spawn");
            return;
        }
        
        // Mob sprite is 240x240, explosion sprite is 100x100
        // Center explosion on the mob: mob_x + (240/2) - (100/2) = mob_x + 120 - 50 = mob_x + 70
        Explosion explosion = new Explosion(
            x + 70, // Center horizontally on 240px mob sprite
            y + 70, // Center vertically on 240px mob sprite
            explosionDamage,
            explosionRadius,
            explosionAnim
        );
        
        explosions.add(explosion);
        
        // TRIGGER CAMERA SHAKE for explosion impact!
        if (renderer != null) {
            renderer.shake(15f, 0.3f); // Intense shake (15 pixels for 0.3 seconds)
        }
        
        Gdx.app.log("WorldManager", "✨ Spawned explosion at (" + (x + 70) + ", " + (y + 70) + ") with NEW animation instance");
    }
    
    /**
     * Chain lightning mechanic
     */
    private void chainLightning(Mob initialMob, float initialDamage, int maxChains, float chainRadius, float damageDecay) {
        Mob currentMob = initialMob;
        float currentDamage = initialDamage;
        int chains = 0;

        while (chains < maxChains) {
            Mob nextMob = null;
            float closestDistance = Float.MAX_VALUE;

            for (Mob mob : mobs) {
                if (mob == currentMob || mob.isDead()) continue;

                float distance = currentMob.getPosition().dst(mob.getPosition());
                if (distance < chainRadius && distance < closestDistance) {
                    closestDistance = distance;
                    nextMob = mob;
                }
            }

            if (nextMob == null) break;

            nextMob.takeDamage(currentDamage);
            Gdx.app.log("WorldManager", "⚡ Chain lightning hit mob for " + currentDamage + " damage!");

            if (renderer != null) {
                renderer.spawnDamageNumber(nextMob.getPosition().x + nextMob.getWidth() / 2f,
                                           nextMob.getPosition().y + nextMob.getHeight() / 2f + 20f,
                                           currentDamage, false);
            }

            currentDamage *= damageDecay;
            currentMob = nextMob;
            chains++;
        }
    }
    
    // Getters
    public Player getPlayer() { return player; }
    public Array<Mob> getMobs() { return mobs; }
    public Array<Pickup> getPickups() { return pickups; }
    public Array<Projectile> getProjectiles() { return projectiles; }
    public Array<Explosion> getExplosions() { return explosions; }
    public int getCurrentWave() { return currentWave; }
    public int getMobCount() { return mobs.size; }
}
