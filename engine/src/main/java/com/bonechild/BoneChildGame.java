package com.bonechild;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bonechild.playablecharacters.Player;
import com.bonechild.playablecharacters.PlayerInput;
import com.bonechild.collision.CollisionSystem;
import com.bonechild.rendering.Assets;
import com.bonechild.rendering.Renderer;
import com.bonechild.ui.GameUI;
import com.bonechild.ui.MenuScreen;
import com.bonechild.ui.SettingsScreen;
import com.bonechild.ui.PauseMenu;
import com.bonechild.ui.GameOverScreen;
import com.bonechild.ui.PowerUpScreen;
import com.bonechild.ui.BossWarningScreen; // NEW: Boss warning screen
import com.bonechild.world.WorldManager;
import com.bonechild.ui.CharacterStatsScreen;
import com.bonechild.ui.InventoryUI;

/**
 * Main game class for BoneChild Game
 * A top-down survival action game built with LibGDX
 */
public class BoneChildGame extends ApplicationAdapter implements MenuScreen.MenuCallback, SettingsScreen.SettingsCallback, PauseMenu.PauseCallback, GameOverScreen.GameOverCallback, PowerUpScreen.PowerUpCallback {

    private OrthographicCamera camera;
    private Viewport viewport;
    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    // Game systems
    private Assets assets;
    private WorldManager worldManager;
    private Renderer renderer;
    private PlayerInput playerInput;
    private CollisionSystem collisionSystem;
    
    // UI
    private MenuScreen menuScreen;
    private SettingsScreen settingsScreen;
    private PauseMenu pauseMenu;
    private GameOverScreen gameOverScreen;
    private PowerUpScreen powerUpScreen;
    private CharacterStatsScreen characterStatsScreen;
    private BossWarningScreen bossWarningScreen; // NEW: Boss warning screen
    private GameUI gameUI;
    private InventoryUI inventoryUI;

    // Game state
    private boolean gameStarted = false;
    private boolean gamePaused = false;
    private int lastBossWarningWave = -1;  // Track which wave showed boss warning
    private float deathTimer = 0f;
    private boolean deathScreenShown = false;
    private boolean deathSoundPlayed = false;
    private static final float DEATH_ANIMATION_DELAY = 2.0f; // Wait 2 seconds before showing death screen
    
    @Override
    public void create() {
        Gdx.app.log("BoneChild", "Initializing game...");
        
        // Setup camera with fixed viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.update();
        
        // Load assets
        assets = new Assets();
        assets.load();
        
        // Create menu screen (shown first)
        menuScreen = new MenuScreen(assets, this);
        
        // Initialize collision system
        collisionSystem = new CollisionSystem();
        
        Gdx.app.log("BoneChild", "Game initialized successfully!");
        Gdx.app.log("BoneChild", "Showing menu screen...");
    }
    
    /**
     * Check if save file exists (for menu to show Continue button)
     */
    @Override
    public boolean hasSaveFile() {
        // Directly check if the save file exists using LibGDX
        try {
            com.badlogic.gdx.files.FileHandle saveFile = Gdx.files.local("bonechild_save.json");
            boolean exists = saveFile.exists();
            Gdx.app.log("BoneChild", "Save file check: " + exists);
            return exists;
        } catch (Exception e) {
            Gdx.app.error("BoneChild", "Error checking save file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Called when player clicks "Continue" in menu
     */
    @Override
    public void onContinueGame() {
        Gdx.app.log("BoneChild", "Continue game selected");
        startGameWithSave(true);
    }
    
    /**
     * Called when player clicks "New Game" in menu
     */
    @Override
    public void onNewGame() {
        Gdx.app.log("BoneChild", "New game selected");
        startGameWithSave(false);
    }
    
    /**
     * Start game with or without loading save
     */
    private void startGameWithSave(boolean loadSave) {
        if (!gameStarted) {
            Gdx.app.log("BoneChild", "Starting game...");
            
            // Create settings screen if it doesn't exist (for keybinds)
            if (settingsScreen == null) {
                settingsScreen = new SettingsScreen(assets, this, null);
            }
            
            // Create player at center of world
            Player player = new Player(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f);
            
            // Create world manager with player and initialize stage system
            worldManager = new WorldManager(player);
            worldManager.initialize(assets);
            
            // Check if we should load save file
            if (loadSave && worldManager.hasSaveFile()) {
                Gdx.app.log("BoneChild", "📁 Loading saved game...");
                com.bonechild.saves.SaveState saveState = worldManager.loadGame();
                
                if (saveState != null) {
                    // Restore player stats
                    player.setLevel(saveState.level);
                    player.setExperience(saveState.experience);
                    player.setExperienceToNextLevel(saveState.experienceToNextLevel);
                    player.setGold(saveState.gold);
                    player.setCurrentHealth(saveState.currentHealth);
                    player.setMaxHealth(saveState.maxHealth);
                    
                    // Restore power-up levels
                    for (int i = 0; i < saveState.speedLevel; i++) {
                        player.applyPowerUp("SPEED");
                    }
                    for (int i = 0; i < saveState.strengthLevel; i++) {
                        player.applyPowerUp("STRENGTH");
                    }
                    for (int i = 0; i < saveState.grabLevel; i++) {
                        player.applyPowerUp("GRAB");
                    }
                    for (int i = 0; i < saveState.attackSpeedLevel; i++) {
                        player.applyPowerUp("ATTACK_SPEED");
                    }
                    for (int i = 0; i < saveState.maxHpLevel; i++) {
                        player.applyPowerUp("MAX_HP");
                    }
                    for (int i = 0; i < saveState.xpBoostLevel; i++) {
                        player.applyPowerUp("XP_BOOST");
                    }
                    for (int i = 0; i < saveState.explosionChanceLevel; i++) {
                        player.applyPowerUp("EXPLOSION_CHANCE");
                    }
                    for (int i = 0; i < saveState.chainLightningLevel; i++) {
                        player.applyPowerUp("CHAIN_LIGHTNING");
                    }
                    for (int i = 0; i < saveState.lifestealLevel; i++) {
                        player.applyPowerUp("LIFESTEAL");
                    }
                    
                    // Skip to saved wave
                    if (saveState.currentWave > 1) {
                        Gdx.app.log("BoneChild", "⏩ Skipping to wave " + saveState.currentWave);
                        worldManager.skipToWave(saveState.currentWave);
                    }
                    
                    Gdx.app.log("BoneChild", "✅ Save loaded! Level " + saveState.level + ", Wave " + saveState.currentWave);
                } else {
                    Gdx.app.log("BoneChild", "⚠️ Failed to load save - starting new game");
                }
            } else {
                Gdx.app.log("BoneChild", "No save file found - starting new game");
            }
            
            worldManager.startWave();
            
            // Wire up collision system to spawn loot
            // Wire up collision system to spawn loot
            collisionSystem.setPickupSpawner(worldManager.getPickupAdder()::accept);
            
            // Create renderer
            renderer = new Renderer(camera, assets);
            
            // Create input handler
            playerInput = new PlayerInput(worldManager.getPlayer());
            
            // Apply saved keybinds from settings screen
            playerInput.setKeybinds(settingsScreen.getKeybinds());
            Gdx.app.log("BoneChild", "Applied keybinds");
            
            // Create UI
            gameUI = new GameUI(assets, worldManager.getPlayer(), worldManager);
            pauseMenu = new PauseMenu(assets, this);
            gameOverScreen = new GameOverScreen(assets, this);
            powerUpScreen = new PowerUpScreen(assets, this);
            powerUpScreen.setPlayer(worldManager.getPlayer()); // Pass player reference for reroll
            characterStatsScreen = new CharacterStatsScreen(assets, worldManager.getPlayer());
            bossWarningScreen = new BossWarningScreen(); // NEW: Initialize boss warning screen
            inventoryUI = new InventoryUI(assets, worldManager.getPlayer());

            // Start background music
            if (assets.getBackgroundMusic() != null) {
                assets.getBackgroundMusic().play();
                Gdx.app.log("BoneChild", "Background music started");
            }
            
            gameStarted = true;
            Gdx.app.log("BoneChild", "Controls: WASD/Arrow Keys to move, SPACE to attack, ESC to exit, I for inventory");
        }
    }
    
    /**
     * Called when player clicks "Settings" in menu
     */
    @Override
    public void onSettings() {
        if (settingsScreen == null) {
            settingsScreen = new SettingsScreen(assets, this, null);
        }
        settingsScreen.show();
    }
    
    /**
     * Called when player returns from settings
     */
    @Override
    public void onBack() {
        if (settingsScreen != null && settingsScreen.isVisible()) {
            settingsScreen.hide();
            // Apply the new keybinds to player input if game has started
            if (gameStarted && playerInput != null) {
                playerInput.setKeybinds(settingsScreen.getKeybinds());
                Gdx.app.log("BoneChild", "Keybinds updated");
            } else if (!gameStarted) {
                // If game hasn't started, keybinds will be applied when game starts
                Gdx.app.log("BoneChild", "Keybinds saved (will be applied when game starts)");
            }
        }
    }
    
    /**
     * Called when player clicks "Exit Game" in menu
     */
    @Override
    public void onExit() {
        Gdx.app.exit();
    }
    
    /**
     * Called when player clicks "Resume" in pause menu
     */
    public void onResume() {
        gamePaused = false;
        Gdx.app.log("BoneChild", "Game resumed");
    }
    
    /**
     * Called when player clicks "Settings" in pause menu
     */
    public void onPauseSettings() {
        if (settingsScreen == null) {
            settingsScreen = new SettingsScreen(assets, this, playerInput);
        }
        settingsScreen.show();
    }
    
    /**
     * Called when player clicks "Exit to Menu" in pause menu or game over screen
     */
    @Override
    public void onExitToMenu() {
        Gdx.app.log("BoneChild", "Exiting to main menu...");
        
        // Hide pause menu if visible
        if (pauseMenu != null) {
            pauseMenu.hide();
        }
        
        // Hide game over screen if visible
        if (gameOverScreen != null) {
            gameOverScreen.hide();
        }
        
        gamePaused = false;
        gameStarted = false;
        
        // Hide settings if open
        if (settingsScreen != null && settingsScreen.isVisible()) {
            settingsScreen.hide();
        }
        
        // Cleanup game resources
        if (assets.getBackgroundMusic() != null) {
            assets.getBackgroundMusic().stop();
        }
        
        // Make sure menu is visible
        if (menuScreen != null) {
            menuScreen.show();
        }
    }
    
    /**
     * Called when player clicks "Play Again" on game over screen
     */
    @Override
    public void onPlayAgain() {
        Gdx.app.log("BoneChild", "Restarting game...");
        gameStarted = false;
        
        // Hide game over screen
        if (gameOverScreen != null) {
            gameOverScreen.hide();
        }
        
        // Cleanup game resources
        if (assets.getBackgroundMusic() != null) {
            assets.getBackgroundMusic().stop();
        }
        
        // Restart game
        onContinueGame();
    }
    
    /**
     * Called when player selects a power-up during level up
     */
    @Override
    public void onPowerUpSelected(PowerUpScreen.PowerUp powerUp) {
        Gdx.app.log("BoneChild", "Power-up selected: " + powerUp.name());
        
        String powerUpType = powerUp.name();
        worldManager.getPlayer().applyPowerUp(powerUpType);
        
        // Hide power-up screen and resume game
        if (powerUpScreen != null) {
            powerUpScreen.hide();
        }
        gamePaused = false;
    }
    
    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Menu flow when game hasn't started
        if (!gameStarted) {
            if (settingsScreen == null || !settingsScreen.isVisible()) {
                menuScreen.update(delta);
            }
            menuScreen.render();
            if (settingsScreen != null && settingsScreen.isVisible()) {
                settingsScreen.update(delta);
                settingsScreen.render();
            }
            return;
        }
        
        // Check if player is dead and show game over screen after delay
        if (worldManager.getPlayer().isDead()) {
            deathTimer += delta;
            if (deathTimer >= DEATH_ANIMATION_DELAY && !deathScreenShown) {
                if (gameOverScreen != null && !gameOverScreen.isVisible()) {
                    gameOverScreen.setStats(worldManager.getCurrentWave(), worldManager.getPlayer().getGold(), worldManager.getPlayer().getLevel());
                    gameOverScreen.show();
                    deathScreenShown = true;
                }
            }
        } else {
            deathTimer = 0f;
            deathScreenShown = false;
            deathSoundPlayed = false;
        }
        
        // If game over screen is visible, render paused game in background and the overlay, then return
        if (gameOverScreen != null && gameOverScreen.isVisible()) {
            renderer.updateCamera();
            renderer.setDeltaTime(0);
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            renderer.renderPickups(worldManager.getPickups());
            gameUI.render();
            
            gameOverScreen.update(delta);
            gameOverScreen.render();
            return; // Don't process any other game logic when dead
        }
        
        // Character stats screen has highest priority while visible
        if (characterStatsScreen != null && characterStatsScreen.isVisible()) {
            renderer.updateCamera();
            renderer.setDeltaTime(0);
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            renderer.renderPickups(worldManager.getPickups());
            gameUI.render();
            
            characterStatsScreen.update(delta);
            characterStatsScreen.render();
            
            if (!characterStatsScreen.isVisible()) {
                gamePaused = false;
            }
            return;
        }
        
        // Power-up screen priority over pause
        if (powerUpScreen != null && powerUpScreen.isVisible()) {
            renderer.updateCamera();
            renderer.setDeltaTime(0);
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            renderer.renderPickups(worldManager.getPickups());
            gameUI.render();
            
            powerUpScreen.update(delta);
            powerUpScreen.render();
            return;
        }
        
        // Boss warning screen priority over pause
        if (bossWarningScreen != null && bossWarningScreen.isActive()) {
            renderer.updateCamera();
            renderer.setDeltaTime(0);
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            renderer.renderPickups(worldManager.getPickups());
            renderer.renderProjectiles(worldManager.getProjectiles());
            gameUI.render();
            
            bossWarningScreen.update(delta);
            // BossWarningScreen manages its own batch state (ends/restarts for shapes)
            com.badlogic.gdx.graphics.g2d.SpriteBatch batch = renderer.getBatch();
            bossWarningScreen.render(batch);
            return; // Don't render anything else while warning active
        }
        
        // Pause menu handling
        if (gamePaused) {
            renderer.updateCamera();
            renderer.setDeltaTime(0);
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            gameUI.render();
            
            if (settingsScreen == null || !settingsScreen.isVisible()) {
                pauseMenu.update(delta);
            }
            pauseMenu.render();
            if (settingsScreen != null && settingsScreen.isVisible()) {
                settingsScreen.update(delta);
                settingsScreen.render();
            }
            return;
        }
        
        // Check level up for power-up screen (unpaused flow)
        if (worldManager.getPlayer().hasLeveledUpThisFrame()) {
            worldManager.getPlayer().clearLevelUpFlag();
            powerUpScreen.show();
            gamePaused = true;
        }
        
        // Check if current wave is a boss wave and show banner (only once per wave)
        int currentWave = worldManager.getCurrentWave();
        if (worldManager.isCurrentWaveBossWave() &&
            !bossWarningScreen.isActive() &&
            worldManager.getMobCount() > 0 &&
            lastBossWarningWave != currentWave) {

            String bossWaveName = "WAVE " + currentWave + " - BOSS FIGHT";
            bossWarningScreen.show(bossWaveName);
            gamePaused = true;
            lastBossWarningWave = currentWave;  // Mark this wave as shown
            Gdx.app.log("BoneChild", "🚨 BOSS WAVE! " + bossWaveName);
        }
        
                // Game running: handle input, update world, then render
        handleInput();
        if (!gamePaused) {
            update(delta);
        }

        renderer.updateCamera();
        renderer.setDeltaTime(delta);
        renderer.renderBackground();
        renderer.renderPlayer(worldManager.getPlayer());
        renderer.renderMobs(worldManager.getMobs());
        renderer.renderProjectiles(worldManager.getProjectiles());
        renderer.renderPickups(worldManager.getPickups());
        renderer.renderHitboxes(worldManager.getPlayer(), worldManager.getMobs());
        renderer.renderEffects();
        gameUI.render();
    }
    
    private void handleInput() {
        // Character stats toggle (C)
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.C)) {
            if (characterStatsScreen != null) {
                if (characterStatsScreen.isVisible()) {
                    characterStatsScreen.hide();
                    gamePaused = false;
                    Gdx.app.log("BoneChild", "Character stats closed, game resumed");
                } else {
                    characterStatsScreen.show();
                    gamePaused = true;
                    Gdx.app.log("BoneChild", "Character stats opened, game paused");
                }
            }
            return;
        }
        
        // While character stats screen is open, allow ESC to close but ignore other inputs
        if (characterStatsScreen != null && characterStatsScreen.isVisible()) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
                characterStatsScreen.hide();
                gamePaused = false;
                Gdx.app.log("BoneChild", "Character stats closed with ESC, game resumed");
            }
            return;
        }
        
        // Inventory toggle (I / ESC while open)
        if (inventoryUI != null && inventoryUI.isVisible()) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE) ||
                Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.I)) {
                inventoryUI.hide();
                Gdx.app.log("BoneChild", "Inventory closed");
            }
            return; // Don't process other inputs while inventory is open
        }
        
        // Power-up screen open: ignore all inputs here
        if (powerUpScreen != null && powerUpScreen.isVisible()) {
            Gdx.app.log("BoneChild", "Power-up screen is open, ignoring other inputs");
            return;
        }
        
        // Boss warning screen active: SPACE dismisses it
        if (bossWarningScreen != null && bossWarningScreen.isActive()) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
                bossWarningScreen.dismiss();
                gamePaused = false;
                Gdx.app.log("BoneChild", "Boss warning dismissed!");
            }
            return;
        }
        
        // If settings screen is open, let it consume input (e.g., ESC) and ignore pause toggling here
        if (settingsScreen != null && settingsScreen.isVisible()) {
            return;
        }
        
        // ESC toggles pause menu when game is running and no higher-priority UI is active
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            if (gamePaused && pauseMenu != null && pauseMenu.isVisible()) {
                gamePaused = false;
                pauseMenu.hide();
                Gdx.app.log("BoneChild", "Game resumed via ESC");
            } else if (!gamePaused) {
                gamePaused = true;
                pauseMenu.show();
                Gdx.app.log("BoneChild", "Game paused");
            }
            return;
        }
        
        // Finally, update player controls (only if game is not paused)
        if (!gamePaused) {
            playerInput.update();
        }
    }
    
    private void update(float delta) {
        // Update world (player, enemies, spawning)
        worldManager.update(delta);
        
        // Process collisions (projectile hits, mob contact damage, pickup collection)
        collisionSystem.process(
            delta,
            worldManager.getPlayer(),
            worldManager.getMobs(),
            worldManager.getProjectiles(),
            worldManager.getPickups()
        );
        
        // Update UI
        gameUI.update(delta);
    }
    
    @Override
    public void resize(int width, int height) {
        // Update viewport - this maintains consistent world view
        viewport.update(width, height, true);
        
        if (menuScreen != null) {
            menuScreen.resize(width, height);
        }
        
        if (settingsScreen != null) {
            settingsScreen.resize(width, height);
        }
        
        if (gameStarted) {
            if (renderer != null) {
                renderer.resize(width, height);
            }
            if (gameUI != null) {
                gameUI.resize(width, height);
            }
            if (pauseMenu != null) {
                pauseMenu.resize(width, height);
            }
            if (gameOverScreen != null) {
                gameOverScreen.resize(width, height);
            }
            if (powerUpScreen != null) {
                powerUpScreen.resize(width, height);
            }
            if (characterStatsScreen != null) {
                characterStatsScreen.resize(width, height);
            }
            // BossWarningScreen does not currently need explicit resize
            if (inventoryUI != null) {
                inventoryUI.resize(width, height);
            }
        }
    }
    
    @Override
    public void dispose() {
        Gdx.app.log("BoneChild", "Disposing game resources...");
        
        if (menuScreen != null) {
            menuScreen.dispose();
        }
        
        if (gameStarted) {
            if (renderer != null) {
                renderer.dispose();
            }
            if (gameUI != null) {
                gameUI.dispose();
            }
            if (pauseMenu != null) {
                pauseMenu.dispose();
            }
            if (gameOverScreen != null) {
                gameOverScreen.dispose();
            }
            if (bossWarningScreen != null) {
                bossWarningScreen.dispose();
            }
            if (inventoryUI != null) {
                inventoryUI.dispose();
            }
        }
        
        if (settingsScreen != null) {
            settingsScreen.dispose();
        }
        
        if (assets != null) {
            assets.dispose();
        }
        
        Gdx.app.log("BoneChild", "Game disposed successfully!");
    }
}
