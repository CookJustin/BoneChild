package com.bonechild.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.math.Vector2;
import com.bonechild.monsters.api.MobEntity;
import com.bonechild.monsters.api.MobFactory;
import com.bonechild.monsters.api.SpawnContext;

/**
 * Stage-based wave spawning system
 *
 * Usage:
 *
 * // Setup
 * StageSpawner spawner = new StageSpawner(mobFactory);
 * spawner.loadStage("stages/stage-1.json");
 *
 * // Game loop
 * spawner.update(delta, mobs);
 *
 * // When wave cleared
 * if (allMobsDead()) {
 *     spawner.nextWave();
 * }
 */
public class StageSpawner {
    private MobFactory mobFactory;
    private StageDefinition currentStage;
    private int currentWaveIndex = 0;
    private Array<ScheduledSpawn> scheduledSpawns = new Array<>();
    private float waveTimer = 0f;
    private boolean waveActive = false;

    // Spawn area bounds
    private float spawnMinX = 100f;
    private float spawnMaxX = 1820f;
    private float spawnMinY = 100f;
    private float spawnMaxY = 980f;

    public StageSpawner(MobFactory mobFactory) {
        this.mobFactory = mobFactory;
    }

    /**
     * Load stage definition from JSON
     */
    public void loadStage(String jsonPath) {
        FileHandle file = Gdx.files.internal(jsonPath);
        Json json = new Json();
        currentStage = json.fromJson(StageDefinition.class, file);
        currentWaveIndex = 0;
        Gdx.app.log("StageSpawner", "Loaded stage: " + currentStage.name + " with " + currentStage.waves.size + " waves");
    }

    /**
     * Start the current wave
     */
    public void startWave() {
        if (currentWaveIndex >= currentStage.waves.size) {
            Gdx.app.log("StageSpawner", "Stage complete!");
            return;
        }

        WaveDefinition wave = currentStage.waves.get(currentWaveIndex);
        Gdx.app.log("StageSpawner", "🌊 Starting wave " + wave.waveNumber);

        if (wave.isBossWave) {
            Gdx.app.log("StageSpawner", "🚨 BOSS WAVE!");
        }

        // Schedule all spawns for this wave
        scheduledSpawns.clear();
        float currentDelay = 0f;

        for (SpawnPattern pattern : wave.spawns) {
            for (int i = 0; i < pattern.count; i++) {
                ScheduledSpawn spawn = new ScheduledSpawn();
                spawn.mobType = pattern.mobType;
                spawn.spawnTime = currentDelay;
                scheduledSpawns.add(spawn);

                currentDelay += pattern.spawnDelay;
            }
        }

        waveTimer = 0f;
        waveActive = true;
    }

    /**
     * Move to next wave
     */
    public void nextWave() {
        currentWaveIndex++;
        if (currentWaveIndex < currentStage.waves.size) {
            startWave();
        } else {
            Gdx.app.log("StageSpawner", "🎉 Stage complete!");
        }
    }

    /**
     * Update and spawn mobs at scheduled times
     */
    public void update(float delta, Array<MobEntity> mobs) {
        if (!waveActive) return;

        waveTimer += delta;

        // Check for spawns that are ready
        for (int i = scheduledSpawns.size - 1; i >= 0; i--) {
            ScheduledSpawn spawn = scheduledSpawns.get(i);

            if (waveTimer >= spawn.spawnTime) {
                // Spawn the mob!
                Vector2 spawnPos = getRandomSpawnPosition();
                SpawnContext ctx = new SpawnContext(spawnPos);
                MobEntity mob = mobFactory.create(spawn.mobType, ctx);
                mobs.add(mob);

                Gdx.app.log("StageSpawner", "Spawned " + spawn.mobType + " at (" + (int)spawnPos.x + ", " + (int)spawnPos.y + ")");

                scheduledSpawns.removeIndex(i);
            }
        }

        // Check if all spawns completed
        if (scheduledSpawns.size == 0) {
            waveActive = false;
            Gdx.app.log("StageSpawner", "✅ Wave spawning complete");
        }
    }

    /**
     * Get random spawn position around the edges
     */
    private Vector2 getRandomSpawnPosition() {
        // Spawn randomly around the play area edges
        int side = (int)(Math.random() * 4);
        float x, y;

        switch (side) {
            case 0: // Top
                x = spawnMinX + (float)Math.random() * (spawnMaxX - spawnMinX);
                y = spawnMaxY;
                break;
            case 1: // Right
                x = spawnMaxX;
                y = spawnMinY + (float)Math.random() * (spawnMaxY - spawnMinY);
                break;
            case 2: // Bottom
                x = spawnMinX + (float)Math.random() * (spawnMaxX - spawnMinX);
                y = spawnMinY;
                break;
            default: // Left
                x = spawnMinX;
                y = spawnMinY + (float)Math.random() * (spawnMaxY - spawnMinY);
                break;
        }

        return new Vector2(x, y);
    }

    /**
     * Set spawn area bounds
     */
    public void setSpawnBounds(float minX, float maxX, float minY, float maxY) {
        this.spawnMinX = minX;
        this.spawnMaxX = maxX;
        this.spawnMinY = minY;
        this.spawnMaxY = maxY;
    }

    // Getters
    public int getCurrentWave() { return currentWaveIndex + 1; }
    public int getTotalWaves() { return currentStage != null ? currentStage.waves.size : 0; }
    public boolean isWaveActive() { return waveActive; }
    public boolean isStageComplete() { return currentWaveIndex >= currentStage.waves.size; }
    public WaveDefinition getCurrentWaveDefinition() {
        if (currentStage != null && currentWaveIndex < currentStage.waves.size) {
            return currentStage.waves.get(currentWaveIndex);
        }
        return null;
    }
    public String getStageName() { return currentStage != null ? currentStage.name : ""; }

    // Data classes for JSON parsing
    public static class StageDefinition {
        public String stageId;
        public String name;
        public String description;
        public Array<WaveDefinition> waves;
    }

    public static class WaveDefinition {
        public int waveNumber;
        public Array<SpawnPattern> spawns;
        public boolean isBossWave;
    }

    public static class SpawnPattern {
        public String mobType;
        public int count;
        public float spawnDelay;
    }

    private static class ScheduledSpawn {
        String mobType;
        float spawnTime;
    }
}

