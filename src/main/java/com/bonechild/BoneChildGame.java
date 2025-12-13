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
import com.bonechild.ui.MenuScreen;
import com.bonechild.ui.SettingsScreen;
import com.bonechild.ui.PauseMenu;
import com.bonechild.ui.GameOverScreen;
import com.bonechild.ui.PowerUpScreen;
import com.bonechild.world.WorldManager;
import com.bonechild.ui.CharacterStatsScreen;

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
    
    // UI
    private MenuScreen menuScreen;
    private SettingsScreen settingsScreen;
    private PauseMenu pauseMenu;
    private GameOverScreen gameOverScreen;
    private PowerUpScreen powerUpScreen;
    private CharacterStatsScreen characterStatsScreen;
    private GameUI gameUI;
    
    // Game state
    private boolean gameStarted = false;
    private boolean gamePaused = false;
    private float deathTimer = 0f;
    private boolean deathScreenShown = false;
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
            worldManager.setAssets(assets);
            
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
        
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Show menu if game hasn't started
        if (!gameStarted) {
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
        
        // Check if player is dead and show game over screen
        if (worldManager.getPlayer().isDead()) {
            // Increment death timer
            deathTimer += delta;
            
            // Set stats and show game over screen after delay
            if (deathTimer >= DEATH_ANIMATION_DELAY && !deathScreenShown) {
                if (gameOverScreen != null && !gameOverScreen.isVisible()) {
                    gameOverScreen.setStats(worldManager.getCurrentWave(), worldManager.getPlayer().getGold(), worldManager.getPlayer().getLevel());
                    gameOverScreen.show();
                    deathScreenShown = true;
                }
            }
        } else {
            // Reset death timer and flag if player is not dead
            deathTimer = 0f;
            deathScreenShown = false;
        }
        
        // Game is running - check game over screen
        if (gameOverScreen != null && gameOverScreen.isVisible()) {
            // Render game in background (paused state)
            renderer.updateCamera();
            renderer.setDeltaTime(0); // No animation updates while paused
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
            renderer.renderPickups(worldManager.getPickups());
            gameUI.render();
            
            // Update and render game over screen on top
            gameOverScreen.update(delta);
            gameOverScreen.render();
            return;
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
            gameUI.render();
            
            // Update and render character stats screen on top
            characterStatsScreen.update(delta);
            characterStatsScreen.render();
            
            // Handle closing stats screen
            if (!characterStatsScreen.isVisible()) {
                gamePaused = false;
            }
            return;
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
            gameUI.render();
            
            // Update and render power-up screen on top
            powerUpScreen.update(delta);
            powerUpScreen.render();
            return;
        }
        
        // Game is running - handle pause menu
        if (gamePaused) {
            // Render game in background (paused state)
            renderer.updateCamera();
            renderer.setDeltaTime(0); // No animation updates while paused
            renderer.renderBackground();
            renderer.renderPlayer(worldManager.getPlayer());
            renderer.renderMobs(worldManager.getMobs());
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
        
        // Check if player leveled up and show power-up screen
        if (worldManager.getPlayer().didLevelUpThisFrame()) {
            worldManager.getPlayer().clearLevelUpFlag();
            powerUpScreen.show();
            gamePaused = true; // Pause game when power-up screen appears
        }
        
        // Game is running - handle input and update
        handleInput();
        update(delta);
        
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
        renderer.renderPickups(worldManager.getPickups());
        
        // Render UI
        gameUI.render();
    }
    
    private void handleInput() {
        // Check character stats screen (C key)
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.C)) {
            if (characterStatsScreen != null) {
                characterStatsScreen.show();
                gamePaused = true;
            }
            return;
        }
        
        // Pause on ESC
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            gamePaused = true;
            pauseMenu.show();
            Gdx.app.log("BoneChild", "Game paused");
            return;
        }
        
        // Update player input
        playerInput.update();
    }
    
    private void update(float delta) {
        // Update world (player, enemies, spawning)
        worldManager.update(delta);
        
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
