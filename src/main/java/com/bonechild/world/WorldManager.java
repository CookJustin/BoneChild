package com.bonechild.world;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Manages all entities in the game world
 * Handles spawning, updating, and entity lifecycle
 */
public class WorldManager {
    private Player player;
    private Array<Mob> mobs;
    private Array<Pickup> pickups;
    private Random random;
    
    // Wave management
    private float waveTimer;
    private float waveInterval;
    private int currentWave;
    private int mobsPerWave;
    
    public WorldManager() {
        this.mobs = new Array<>();
        this.pickups = new Array<>();
        this.random = new Random();
        
        // Initialize wave system
        this.waveTimer = 0;
        this.waveInterval = 3f; // Spawn first wave after 3 seconds (faster than 10)
        this.currentWave = 0;
        this.mobsPerWave = 3;
        
        // Create player at center of screen
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;
        this.player = new Player(centerX, centerY);
        
        Gdx.app.log("WorldManager", "World initialized with player at (" + centerX + ", " + centerY + ")");
    }
    
    /**
     * Update all entities
     */
    public void update(float delta) {
        // Update player
        player.update(delta);
        
        // Update mobs
        for (int i = mobs.size - 1; i >= 0; i--) {
            Mob mob = mobs.get(i);
            mob.update(delta);
            
            // Remove dead mobs and spawn pickups
            if (mob.isDead()) {
                spawnPickupsFromMob(mob);
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
        
        // Process player attacks when attacking
        if (player.getCurrentState() == Player.AnimationState.ATTACKING) {
            // Attack all mobs in range
            for (Mob mob : mobs) {
                player.attackMob(mob);
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
        
        // 70% chance to drop gold coins (1-3 coins)
        if (random.nextFloat() < 0.7f) {
            int coinCount = 1 + random.nextInt(3); // 1-3 coins
            for (int i = 0; i < coinCount; i++) {
                float offsetX = (random.nextFloat() - 0.5f) * 40f;
                float offsetY = (random.nextFloat() - 0.5f) * 40f;
                
                Pickup coin = new Pickup(
                    mobX + offsetX,
                    mobY + offsetY,
                    Pickup.PickupType.GOLD_COIN,
                    random.nextInt(5) + 5f // 5-10 gold per coin
                );
                pickups.add(coin);
            }
        }
        
        // 60% chance to drop XP orb
        if (random.nextFloat() < 0.6f) {
            Pickup xpOrb = new Pickup(
                mobX,
                mobY,
                Pickup.PickupType.XP_ORB,
                25f // 25 XP per orb
            );
            pickups.add(xpOrb);
        }
        
        Gdx.app.log("WorldManager", "Spawned pickups at (" + mobX + ", " + mobY + ")");
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
        }
        
        pickup.collect();
    }
    
    /**
     * Spawn a wave of enemies
     */
    private void spawnWave() {
        currentWave++;
        int mobCount = mobsPerWave + (currentWave - 1); // Increase mobs each wave
        
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
    }
    
    /**
     * Spawn a single mob at a specific position
     */
    public void spawnMob(float x, float y) {
        mobs.add(new Mob(x, y, player));
    }
    
    // Getters
    public Player getPlayer() { return player; }
    public Array<Mob> getMobs() { return mobs; }
    public Array<Pickup> getPickups() { return pickups; }
    public int getCurrentWave() { return currentWave; }
    public int getMobCount() { return mobs.size; }
}
