package com.bonechild.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.bonechild.rendering.Assets;
import com.bonechild.monsters.api.MobEntity;
import com.bonechild.monsters.api.MobFactory;
import com.bonechild.monsters.core.DefaultMobFactory;
import com.bonechild.stages.StageSpawner;
import com.bonechild.playablecharacters.Player;
import com.bonechild.playablecharacters.Pickup;
import com.bonechild.playablecharacters.Projectile;
import com.bonechild.saves.SaveState;
import com.bonechild.saves.SaveStateManager;
/**
 * Manages all entities in the game world
 *
 * Responsibilities:
 * - Initialize game entities (player is passed in from Engine)
 * - Update all entities each frame (just their internal state)
 * - Remove dead/inactive entities (cleanup)
 * - Coordinate wave spawning via StageSpawner
 * - Provide access to entities (for Engine to render and handle collisions)
 *
 * NOT responsible for:
 * - Creating player (Engine does this)
 * - Collision detection (Engine does this)
 * - Combat mechanics (Player/Engine does this)
 * - Pickup collection (Player does this)
 * - Loot drops (Mobs do this)
 */
public class WorldManager {
    private Player player;
    private Array<MobEntity> mobs;
    private Array<Pickup> pickups;
    private Array<Projectile> projectiles;
    private Assets assets;
    private MobFactory mobFactory;
    private StageSpawner stageSpawner;

    public WorldManager(Player player) {
        this.player = player;
        this.mobs = new Array<>();
        this.pickups = new Array<>();
        this.projectiles = new Array<>();

        Gdx.app.log("WorldManager", "World initialized with player");
    }

    /**
     * Initialize with assets and load first stage
     */
    public void initialize(Assets assets) {
        this.assets = assets;

        // Initialize mob factory with player position reference
        this.mobFactory = new DefaultMobFactory(player.getPosition(), assets);

        // Initialize stage spawner
        this.stageSpawner = new StageSpawner(mobFactory);
        this.stageSpawner.loadStage("stages/stage-1.json");
        this.stageSpawner.setSpawnBounds(100, 1820, 100, 980);

        // Set up player's projectile spawner callback
        player.setProjectileSpawner(projectile -> projectiles.add(projectile));

        Gdx.app.log("WorldManager", "Loaded stage: " + stageSpawner.getStageName());
    }

    /**
     * Start the current wave
     */
    public void startWave() {
        if (stageSpawner != null) {
            stageSpawner.startWave();
        }
    }

    /**
     * Skip to a specific wave (for loading saved games)
     */
    public void skipToWave(int waveNumber) {
        if (stageSpawner == null) return;

        // Clear any existing mobs
        mobs.clear();

        // Advance to the target wave
        int currentWave = stageSpawner.getCurrentWave();
        for (int i = currentWave; i < waveNumber; i++) {
            stageSpawner.nextWave();
        }

        Gdx.app.log("WorldManager", "Skipped to wave " + waveNumber);
    }

    /**
     * Update all entities
     */
    public void update(float delta) {
        // Inject targetable mobs into player for auto-targeting
        player.setTargetableMobs(mobs);
        
        // Update player (player handles its own auto-attack logic)
        player.update(delta);

        // Update stage spawner (spawns mobs at scheduled times)
        if (stageSpawner != null) {
            stageSpawner.update(delta, mobs);
        }

        // Update all mobs
        updateMobs(delta);

        // Check if wave complete and advance
        checkWaveProgress();

        // Update pickups
        updatePickups(delta);

        // Update projectiles
        updateProjectiles(delta);
    }

    /**
     * Update all mobs - set target, update state, remove dead
     */
    private void updateMobs(float delta) {
        for (int i = mobs.size - 1; i >= 0; i--) {
            MobEntity mob = mobs.get(i);

            // Set target position (mobs chase player)
            if (mob instanceof com.bonechild.monsters.impl.Mob) {
                com.bonechild.monsters.impl.Mob mobImpl = (com.bonechild.monsters.impl.Mob) mob;
                mobImpl.setTargetPosition(player.getPosition());
                mobImpl.update(delta);
            }

            // Remove dead mobs
            if (mob.isDead()) {
                mobs.removeIndex(i);
                Gdx.app.log("WorldManager", "Mob died. Remaining: " + mobs.size);
            }
        }
    }

    /**
     * Check wave progression
     */
    private void checkWaveProgress() {
        if (stageSpawner == null) return;

        // All mobs dead and spawning complete? Advance wave
        if (mobs.size == 0 && !stageSpawner.isWaveActive()) {
            if (!stageSpawner.isStageComplete()) {
                Gdx.app.log("WorldManager", "✅ Wave cleared! Advancing...");
                stageSpawner.nextWave();
            } else {
                Gdx.app.log("WorldManager", "🎉 STAGE COMPLETE!");
            }
        }
    }

    /**
     * Check if current wave is a boss wave (for UI banner)
     */
    public boolean isCurrentWaveBossWave() {
        if (stageSpawner == null) return false;
        StageSpawner.WaveDefinition wave = stageSpawner.getCurrentWaveDefinition();
        return wave != null && wave.isBossWave;
    }

    /**
     * Get pickup adder callback for CollisionSystem
     */
    public java.util.function.Consumer<Pickup> getPickupAdder() {
        return pickup -> pickups.add(pickup);
    }

    /**
     * Update pickups
     */
    private void updatePickups(float delta) {
        for (int i = pickups.size - 1; i >= 0; i--) {
            Pickup pickup = pickups.get(i);
            pickup.update(delta);

            // Remove collected pickups
            if (pickup.isCollected()) {
                pickups.removeIndex(i);
            }
        }
    }

    /**
     * Update projectiles
     */
    private void updateProjectiles(float delta) {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(delta);

            // Remove inactive projectiles
            if (!projectile.isActive()) {
                projectiles.removeIndex(i);
            }
        }
    }

    /**
     * Add a pickup to the world
     */
    public void addPickup(Pickup pickup) {
        pickups.add(pickup);
    }

    /**
     * Add a projectile to the world
     */
    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }


    /**
     * Check if current wave is a boss wave
     */
    public boolean isBossWave() {
        if (stageSpawner == null) return false;
        StageSpawner.WaveDefinition wave = stageSpawner.getCurrentWaveDefinition();
        return wave != null && wave.isBossWave;
    }

    // Getters - provide access to entities
    public Player getPlayer() { return player; }
    public Array<MobEntity> getMobs() { return mobs; }
    public Array<Pickup> getPickups() { return pickups; }
    public Array<Projectile> getProjectiles() { return projectiles; }
    public Assets getAssets() { return assets; }

    // Stage/Wave info
    public int getCurrentWave() { return stageSpawner != null ? stageSpawner.getCurrentWave() : 0; }
    public int getTotalWaves() { return stageSpawner != null ? stageSpawner.getTotalWaves() : 0; }
    public int getMobCount() { return mobs.size; }
    public String getStageName() { return stageSpawner != null ? stageSpawner.getStageName() : ""; }
    // Save System
    private SaveStateManager saveStateManager = new SaveStateManager();
    public void saveGame() {
        SaveState state = new SaveState();
        state.level = player.getLevel();
        state.experience = player.getExperience();
        state.experienceToNextLevel = player.getExperienceToNextLevel();
        state.gold = player.getGold();
        state.currentHealth = player.getCurrentHealth();
        state.maxHealth = player.getMaxHealth();
        state.speedLevel = player.getSpeedLevel();
        state.strengthLevel = player.getStrengthLevel();
        state.grabLevel = player.getGrabLevel();
        state.attackSpeedLevel = player.getAttackSpeedLevel();
        state.maxHpLevel = player.getMaxHpLevel();
        state.xpBoostLevel = player.getXpBoostLevel();
        state.explosionChanceLevel = player.getExplosionChanceLevel();
        state.chainLightningLevel = player.getChainLightningLevel();
        state.lifestealLevel = player.getLifestealLevel();
        state.currentStageId = "stage_1";
        state.currentWave = getCurrentWave();
        state.saveTime = System.currentTimeMillis();
        saveStateManager.saveGame(state);
    }

    public SaveState loadGame() {
        return saveStateManager.loadGame();
    }

    public boolean hasSaveFile() {
        return saveStateManager.hasSaveFile();
    }
}
