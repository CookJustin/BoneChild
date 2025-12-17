package com.bonechild;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bonechild.input.PlayerInput;
import com.bonechild.rendering.Assets;
import com.bonechild.rendering.Renderer;
import com.bonechild.ui.GameUI;
import com.bonechild.ui.InventoryUI;
import com.bonechild.ui.MenuScreen;
import com.bonechild.ui.SettingsScreen;
import com.bonechild.ui.PauseMenu;
import com.bonechild.ui.GameOverScreen;
import com.bonechild.ui.PowerUpScreen;
import com.bonechild.ui.BossWarningScreen; // NEW: Boss warning screen
import com.bonechild.world.WorldManager;
import com.bonechild.ui.CharacterStatsScreen;

/**
 * Main game class for BoneChild Game
 * A top-down survival action game built with LibGDX
 */
public class BoneChildGame extends ApplicationAdapter implements MenuScreen.MenuCallback, SettingsScreen.SettingsCallback, PauseMenu.PauseCallback, GameOverScreen.GameOverCallback, PowerUpScreen.PowerUpCallback {
    // Virtual resolution for the game world (fixed size that scales to screen)
    private static final float VIRTUAL_WIDTH = 1280f;
    private static final float VIRTUAL_HEIGHT = 720f;
    
    private OrthographicCamera camera;
    private Viewport viewport;
    
    // Game systems
    private Assets assets;
    private WorldManager worldManager;
    private Renderer renderer;
    private PlayerInput playerInput;
    
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
    private float deathTimer = 0f;
    private boolean deathScreenShown = false;
    private boolean deathSoundPlayed = false;
    private static final float DEATH_ANIMATION_DELAY = 1.5f; // Delay before showing game over screen
    
    @Override
    public void create() {
        Gdx.app.log("BoneChild", "Initializing game...");
        
        // Setup camera with fixed virtual resolution
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        camera.update();
        
        // Load assets
        assets = new Assets();
        assets.load();
        
        // Create menu screen (shown first)
        menuScreen = new MenuScreen(assets, this);
        
        Gdx.app.log("BoneChild", "Game initialized successfully!");
        Gdx.app.log("BoneChild", "Showing menu screen...");
    }
    
    /**
     * Called when player clicks "Start Game" in menu
     */
    @Override
    public void onStartGame() {
        if (!gameStarted) {
            Gdx.app.log("BoneChild", "Starting game...");
            
            // Create settings screen if it doesn't exist (for keybinds)
            if (settingsScreen == null) {
                settingsScreen = new SettingsScreen(assets, this, null);
            }
            
            // Create world manager (creates player)
            worldManager = new WorldManager();
            worldManager.setAssets(assets); // Pass assets for explosion animations
            
            // Create renderer
            renderer = new Renderer(camera, assets);
            
            // Pass renderer to world manager for camera shake effects
            worldManager.setRenderer(renderer);
            
            // Create input handler
            playerInput = new PlayerInput(worldManager.getPlayer());
            
            // Apply saved keybinds from settings screen
            playerInput.setKeybinds(settingsScreen.getKeybinds());
            Gdx.app.log("BoneChild", "Applied keybinds");
            
            // Create UI
            gameUI = new GameUI(assets, worldManager.getPlayer(), worldManager);
            inventoryUI = new InventoryUI(assets);
            pauseMenu = new PauseMenu(assets, this);
            gameOverScreen = new GameOverScreen(assets, this);
            powerUpScreen = new PowerUpScreen(assets, this);
            powerUpScreen.setPlayer(worldManager.getPlayer()); // Pass player reference for reroll
            characterStatsScreen = new CharacterStatsScreen(assets, worldManager.getPlayer());
            bossWarningScreen = new BossWarningScreen(); // NEW: Initialize boss warning screen
            
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
        onStartGame();
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
        
        // Show menu if game hasn't started
        if (!gameStarted) {
            // MenuScreen handles its own screen clearing
            
            // Only update menu if settings screen is NOT visible
            if (settingsScreen == null || !settingsScreen.isVisible()) {
                menuScreen.update(delta);
            }
            
            menuScreen.render();
            
            // If settings screen is visible, update and render it on top
            if (settingsScreen != null && settingsScreen.isVisible()) {
                settingsScreen.update(delta);
                settingsScreen.render();
            }
            return;
        }
        
        // Clear screen (only when game is running)
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Check if player is dead
        boolean playerIsDead = worldManager.getPlayer().isDead();
        
        if (playerIsDead) {
            // Play death sound once when player first dies
            if (!deathSoundPlayed) {
                // Stop background music
                if (assets.getBackgroundMusic() != null && assets.getBackgroundMusic().isPlaying()) {
                    assets.getBackgroundMusic().stop();
                    Gdx.app.log("BoneChild", "Stopped background music");
                }
                
                // Play death sound
                assets.playSound(assets.getDeathSound());
                deathSoundPlayed = true;
                Gdx.app.log("BoneChild", "Playing death sound");
            }
            
            // Increment death timer
            deathTimer += delta;
            
            // Continue updating player to allow death animation to play
            worldManager.getPlayer().update(delta);
            
            // Set stats and show game over screen after delay
            if (deathTimer >= DEATH_ANIMATION_DELAY && !deathScreenShown) {
                if (gameOverScreen != null && !gameOverScreen.isVisible()) {
                    gameOverScreen.setStats(worldManager.getCurrentWave(), worldManager.getPlayer().getGold(), worldManager.getPlayer().getLevel());
                    gameOverScreen.show();
                    deathScreenShown = true;
                    Gdx.app.log("BoneChild", "Game over screen shown");
                }
            }
            
            // Render the game world in background (frozen, no updates except player animation)
            renderer.updateCamera();
            renderer.setDeltaTime(delta); // Keep animations playing during death
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            renderer.renderPickups(worldManager.getPickups());
            renderer.renderProjectiles(worldManager.getProjectiles());
            renderer.renderExplosions(worldManager.getExplosions());
            gameUI.render();
            
            // Render game over screen on top if shown
            if (gameOverScreen != null && gameOverScreen.isVisible()) {
                gameOverScreen.update(delta);
                gameOverScreen.render();
            }
            
            return; // Don't process any other game logic when dead
        } else {
            // Reset death timer and flags if player is not dead
            deathTimer = 0f;
            deathScreenShown = false;
            deathSoundPlayed = false;
        }
        
        // Check if player leveled up and show power-up screen
        if (worldManager.getPlayer().hasLeveledUpThisFrame()) {
            worldManager.getPlayer().clearLevelUpFlag();
            powerUpScreen.show();
            gamePaused = true; // Pause game when power-up screen appears
        }
        
        // NEW: Check if boss wave warning should be shown
        if (worldManager.shouldShowBossWarning() && !bossWarningScreen.isActive()) {
            bossWarningScreen.show("BOSS08_B");
            gamePaused = true; // Pause game for boss warning
            Gdx.app.log("BoneChild", "🚨 BOSS WARNING TRIGGERED!");
        }
        
        // NEW: Check if ORC boss wave warning should be shown
        if (worldManager.shouldShowOrcBossWarning() && !bossWarningScreen.isActive()) {
            bossWarningScreen.show("ORC_BOSS");
            gamePaused = true; // Pause game for orc boss warning
            Gdx.app.log("BoneChild", "🚨 ORC BOSS WARNING TRIGGERED!");
        }
        
        // ALWAYS handle input FIRST (before any screen checks) so menus can be closed
        handleInput();
        
        // Only update game world when not paused
        if (!gamePaused) {
            update(delta);
        }
        
        // Game is running - check character stats screen FIRST (takes priority)
        if (characterStatsScreen != null && characterStatsScreen.isVisible()) {
            // Render game in background (paused state)
            renderer.updateCamera();
            renderer.setDeltaTime(0); // No animation updates while paused
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            renderer.renderPickups(worldManager.getPickups());
            renderer.renderProjectiles(worldManager.getProjectiles());
            renderer.renderExplosions(worldManager.getExplosions());
            gameUI.render();
            
            // Update and render character stats screen on top
            characterStatsScreen.update(delta);
            characterStatsScreen.render();
            return; // Don't render anything else
        }
        
        // Game is running - check inventory screen (takes priority over pause)
        if (inventoryUI != null && inventoryUI.isVisible()) {
            // Render game in background (paused state)
            renderer.updateCamera();
            renderer.setDeltaTime(0); // No animation updates while paused
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            renderer.renderPickups(worldManager.getPickups());
            renderer.renderProjectiles(worldManager.getProjectiles());
            renderer.renderExplosions(worldManager.getExplosions());
            gameUI.render();
            
            // Update and render inventory screen on top
            inventoryUI.update(delta);
            inventoryUI.render();
            return; // Don't render anything else
        }
        
        // Game is running - check power-up screen FIRST (takes priority over pause)
        if (powerUpScreen != null && powerUpScreen.isVisible()) {
            // Render game in background (paused state)
            renderer.updateCamera();
            renderer.setDeltaTime(0); // No animation updates while paused
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            renderer.renderPickups(worldManager.getPickups());
            renderer.renderProjectiles(worldManager.getProjectiles());
            renderer.renderExplosions(worldManager.getExplosions());
            gameUI.render();
            
            // Update and render power-up screen on top
            powerUpScreen.update(delta);
            powerUpScreen.render();
            return; // Don't render anything else
        }
        
        // NEW: Boss warning screen (takes priority over pause menu)
        if (bossWarningScreen != null && bossWarningScreen.isActive()) {
            // Render game in background (paused state)
            renderer.updateCamera();
            renderer.setDeltaTime(0); // No animation updates while paused
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            renderer.renderPickups(worldManager.getPickups());
            renderer.renderProjectiles(worldManager.getProjectiles());
            renderer.renderExplosions(worldManager.getExplosions());
            gameUI.render();
            
            // Update and render boss warning screen on top
            bossWarningScreen.update(delta);
            renderer.getBatch().begin();
            bossWarningScreen.render(renderer.getBatch());
            renderer.getBatch().end();
            return; // Don't render anything else
        }
        
        // Game is running - handle pause menu
        if (gamePaused) {
            // Render game in background (paused state)
            renderer.updateCamera();
            renderer.setDeltaTime(0); // No animation updates while paused
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            renderer.renderProjectiles(worldManager.getProjectiles());
            renderer.renderExplosions(worldManager.getExplosions());
            renderer.renderPickups(worldManager.getPickups());
            gameUI.render();
            
            // Update and render pause menu
            if (settingsScreen == null || !settingsScreen.isVisible()) {
                pauseMenu.update(delta);
            }
            
            pauseMenu.render();
            
            // If settings screen is visible, update and render it on top
            if (settingsScreen != null && settingsScreen.isVisible()) {
                settingsScreen.update(delta);
                settingsScreen.render();
            }
            return;
        }
        
        // Update camera
        renderer.updateCamera();
        
        // Set delta time for animations
        renderer.setDeltaTime(delta);
        
        // Render background first
        renderer.renderBackground();
        
        // Render game entities
        renderer.renderPlayer(worldManager.getPlayer());
        renderer.renderMobs(worldManager.getMobs());
        renderer.renderProjectiles(worldManager.getProjectiles());
        renderer.renderExplosions(worldManager.getExplosions());
        renderer.renderPickups(worldManager.getPickups());
        
        // DEBUG: Render hitboxes to see what's happening
        renderer.renderHitboxes(worldManager.getPlayer(), worldManager.getMobs());
        
        // Render particle effects and damage numbers on top of everything
        renderer.renderEffects();
        
        // Render UI
        gameUI.render();
        inventoryUI.render();
    }
    
    private void handleInput() {
        // DEBUG: Log when ESC is pressed
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            Gdx.app.log("BoneChild", "🔑 ESC key pressed! gamePaused=" + gamePaused + 
                       ", charStats=" + (characterStatsScreen != null && characterStatsScreen.isVisible()) +
                       ", inventory=" + (inventoryUI != null && inventoryUI.isVisible()) +
                       ", pauseMenu=" + (pauseMenu != null && pauseMenu.isVisible()) +
                       ", powerUp=" + (powerUpScreen != null && powerUpScreen.isVisible()));
        }
        
        // Check character stats screen (C key) - handle both opening and closing
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.C)) {
            if (characterStatsScreen != null) {
                if (characterStatsScreen.isVisible()) {
                    // Close character stats screen
                    characterStatsScreen.hide();
                    gamePaused = false;
                    Gdx.app.log("BoneChild", "Character stats closed, game resumed");
                } else {
                    // Open character stats screen
                    characterStatsScreen.show();
                    gamePaused = true;
                    Gdx.app.log("BoneChild", "Character stats opened, game paused");
                }
            }
            return;
        }
        
        // If character stats screen is open, check for ESC to close it
        if (characterStatsScreen != null && characterStatsScreen.isVisible()) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
                characterStatsScreen.hide();
                gamePaused = false;
                Gdx.app.log("BoneChild", "Character stats closed with ESC, game resumed");
                return;
            }
            // Don't process other inputs while character stats is open
            return;
        }
        
        // If inventory is open, check for ESC or I to close it
        if (inventoryUI != null && inventoryUI.isVisible()) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE) || 
                Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.I)) {
                inventoryUI.hide();
                Gdx.app.log("BoneChild", "Inventory closed");
                return;
            }
            // Don't process other inputs while inventory is open
            return;
        }
        
        // If power-up screen is open, don't allow pause menu
        if (powerUpScreen != null && powerUpScreen.isVisible()) {
            Gdx.app.log("BoneChild", "Power-up screen is open, ignoring other inputs");
            return;
        }
        
        // NEW: If boss warning screen is active, check for SPACE to dismiss
        if (bossWarningScreen != null && bossWarningScreen.isActive()) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
                String bossType = bossWarningScreen.getBossType();
                bossWarningScreen.dismiss();
                
                // Check which boss to spawn based on the warning type
                if ("ORC_BOSS".equals(bossType)) {
                    worldManager.acknowledgeOrcBossWarning();
                    spawnOrcBossAtCenter();
                    Gdx.app.log("BoneChild", "🎮 Orc Boss warning dismissed! Orc Boss spawning!");
                } else {
                    worldManager.acknowledgeBossWarning();
                    spawnBossAtCenter();
                    Gdx.app.log("BoneChild", "🎮 Boss warning dismissed! Boss spawning!");
                }
                
                gamePaused = false; // Resume game after dismissing warning
            }
            return; // Don't process other inputs while boss warning is active
        }
        
        // If settings screen is open, don't process ESC for pause menu (let settings handle it)
        if (settingsScreen != null && settingsScreen.isVisible()) {
            return;
        }
        
        // Handle ESC key for pause menu (toggle pause)
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            if (gamePaused && pauseMenu != null && pauseMenu.isVisible()) {
                // Unpause
                gamePaused = false;
                pauseMenu.hide();
                Gdx.app.log("BoneChild", "✅ Game resumed via ESC");
            } else if (!gamePaused) {
                // Pause
                gamePaused = true;
                if (pauseMenu != null) {
                    pauseMenu.show();
                }
                Gdx.app.log("BoneChild", "✅ Game paused via ESC");
            }
            return;
        }
        
        // Toggle inventory with I
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.I)) {
            inventoryUI.toggle();
            return;
        }
        
        // Only update player input if no menus are open
        if (!gamePaused) {
            playerInput.update();
        }
    }
    
    private void update(float delta) {
        // Update world (player, enemies, spawning)
        worldManager.update(delta);
        
        // Update UI
        gameUI.update(delta);
        inventoryUI.update(delta);
    }
    
    /**
     * Spawn the boss at the center top of the VIRTUAL world (not physical screen)
     */
    private void spawnBossAtCenter() {
        // Use VIRTUAL coordinates (1280x720), not physical screen size
        float bossX = VIRTUAL_WIDTH / 2f - 100f; // Center the 200px wide boss
        float bossY = VIRTUAL_HEIGHT + 100f; // Spawn above the virtual screen
        
        // Manually add the boss to the world
        com.bonechild.world.Boss08B boss = new com.bonechild.world.Boss08B(
            bossX, 
            bossY, 
            worldManager.getPlayer(), 
            assets
        );
        
        worldManager.getMobs().add(boss);
        
        Gdx.app.log("BoneChild", "👹 BOSS SPAWNED at virtual center-top (" + bossX + ", " + bossY + ")");
    }
    
    /**
     * Spawn the Orc boss at the center top of the VIRTUAL world (not physical screen)
     */
    private void spawnOrcBossAtCenter() {
        // Use VIRTUAL coordinates (1280x720), not physical screen size
        // Orc sprite is 157x195 at 80% scale = ~125x156
        float bossX = VIRTUAL_WIDTH / 2f - 62.5f; // Center the ~125px wide orc (half of 125)
        float bossY = VIRTUAL_HEIGHT + 100f; // Spawn above the virtual screen
        
        // Manually call the WorldManager's spawn method which creates the boss Orc
        worldManager.spawnOrcBossWave();
        
        Gdx.app.log("BoneChild", "⚔️👹 ORC BOSS SPAWNED at virtual center-top!");
    }
    
    @Override
    public void resize(int width, int height) {
        Gdx.app.log("BoneChild", "Resize called: " + width + "x" + height);
        
        // Update camera viewport
        viewport.update(width, height);
        camera.update();
        
        // Update menu screen (for title screen background scrolling)
        if (menuScreen != null) {
            menuScreen.resize(width, height);
        }
        
        // Update all UI screens if game has started
        if (gameStarted) {
            if (gameUI != null) {
                gameUI.resize(width, height);
            }
            if (inventoryUI != null) {
                inventoryUI.resize(width, height);
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
            if (settingsScreen != null) {
                settingsScreen.resize(width, height);
            }
        } else {
            // Even if game hasn't started, settings screen might be open
            if (settingsScreen != null) {
                settingsScreen.resize(width, height);
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
            if (inventoryUI != null) {
                inventoryUI.dispose();
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
