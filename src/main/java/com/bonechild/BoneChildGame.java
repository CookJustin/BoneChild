package com.bonechild;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.bonechild.input.PlayerInput;
import com.bonechild.rendering.Assets;
import com.bonechild.rendering.Renderer;
import com.bonechild.ui.GameUI;
import com.bonechild.ui.InventoryUI;
import com.bonechild.world.WorldManager;

/**
 * Main game class for BoneChild
 * This is a Vampire Survivors-style game built with LibGDX
 */
public class BoneChildGame extends ApplicationAdapter {
    private OrthographicCamera camera;
    
    // Game systems
    private Assets assets;
    private WorldManager worldManager;
    private Renderer renderer;
    private PlayerInput playerInput;
    
    // UI
    private GameUI gameUI;
    private InventoryUI inventoryUI;
    
    @Override
    public void create() {
        Gdx.app.log("BoneChild", "Initializing game...");
        
        // Setup camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Load assets
        assets = new Assets();
        assets.load();
        
        // Create world manager (creates player)
        worldManager = new WorldManager();
        
        // Create renderer
        renderer = new Renderer(camera, assets);
        
        // Create input handler
        playerInput = new PlayerInput(worldManager.getPlayer());
        
        // Create UI
        gameUI = new GameUI(assets, worldManager.getPlayer(), worldManager);
        inventoryUI = new InventoryUI(assets);
        
        Gdx.app.log("BoneChild", "Game initialized successfully!");
        Gdx.app.log("BoneChild", "Controls: WASD/Arrow Keys to move, SPACE to attack, ESC to exit, I for inventory");
    }
    
    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Handle input
        handleInput();
        
        // Update game logic
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
        
        // Render UI
        gameUI.render();
        inventoryUI.render();
    }
    
    private void handleInput() {
        // Exit on ESC
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        
        // Toggle inventory with I
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.I)) {
            inventoryUI.toggle();
        }
        
        // Update player input
        playerInput.update();
    }
    
    private void update(float delta) {
        // Update world (player, enemies, spawning)
        worldManager.update(delta);
        
        // Update UI
        gameUI.update(delta);
        inventoryUI.update(delta);
    }
    
    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        gameUI.resize(width, height);
        inventoryUI.resize(width, height);
    }
    
    @Override
    public void dispose() {
        Gdx.app.log("BoneChild", "Disposing game resources...");
        
        if (renderer != null) {
            renderer.dispose();
        }
        if (assets != null) {
            assets.dispose();
        }
        if (gameUI != null) {
            gameUI.dispose();
        }
        if (inventoryUI != null) {
            inventoryUI.dispose();
        }
        
        Gdx.app.log("BoneChild", "Game disposed successfully!");
    }
}
