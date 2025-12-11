package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.bonechild.rendering.Assets;

/**
 * Pause menu for BoneChild gameplay - uses PNG assets
 */
public class PauseMenu {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final Assets assets;
    
    // Textures
    private Texture bgTexture;
    private Texture playButtonTexture;
    private Texture settingsButtonTexture;
    private Texture exitButtonTexture;
    
    // Button hit boxes
    private Rectangle playButtonHitbox;
    private Rectangle settingsButtonHitbox;
    private Rectangle exitButtonHitbox;
    
    private boolean isVisible;
    private PauseCallback callback;
    
    public interface PauseCallback {
        void onResume();
        void onPauseSettings();
        void onExitToMenu();
    }
    
    public PauseMenu(Assets assets, PauseCallback callback) {
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.assets = assets;
        this.callback = callback;
        this.isVisible = false;
        
        // Load textures from assets (may be null if assets not loaded yet)
        this.bgTexture = assets.getExitScreenMenuBg();
        this.playButtonTexture = assets.getPlayButton();
        this.settingsButtonTexture = assets.getSettingsButton();
        this.exitButtonTexture = assets.getExitButton();
        
        // Only setup UI if textures are loaded
        if (bgTexture != null && playButtonTexture != null && settingsButtonTexture != null && exitButtonTexture != null) {
            setupUI();
        }
    }
    
    private void setupUI() {
        // Check if textures are loaded
        if (bgTexture == null || playButtonTexture == null || settingsButtonTexture == null || exitButtonTexture == null) {
            return;
        }
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Calculate background size and position (centered, scaled to 50% for smaller menu)
        float bgScale = Math.min(screenWidth / bgTexture.getWidth(), screenHeight / bgTexture.getHeight()) * 0.5f;
        float bgWidth = bgTexture.getWidth() * bgScale;
        float bgHeight = bgTexture.getHeight() * bgScale;
        float bgX = (screenWidth - bgWidth) / 2f;
        float bgY = (screenHeight - bgHeight) / 2f;
        
        // Button dimensions (all buttons are the same size now, cropped without whitespace)
        float buttonScale = bgScale * 0.85f; // Scale buttons to fit nicely in the menu
        float buttonWidth = playButtonTexture.getWidth() * buttonScale;
        float buttonHeight = playButtonTexture.getHeight() * buttonScale;
        
        // Position buttons vertically - evenly spaced within the menu box
        float centerX = screenWidth / 2f;
        float centerY = bgY + (bgHeight / 2f); // Center of the background box
        
        // Spacing between buttons
        float buttonSpacing = buttonHeight * 0.3f; // 30% of button height as spacing
        
        // Total height of all buttons plus spacing
        float totalContentHeight = (buttonHeight * 3) + (buttonSpacing * 2);
        
        // Start Y position - top of the button group, centered vertically in the menu
        float startY = centerY + (totalContentHeight / 2f);
        
        // Play button (Resume) - top
        playButtonHitbox = new Rectangle(
            centerX - buttonWidth / 2f,
            startY - buttonHeight,
            buttonWidth,
            buttonHeight
        );
        
        // Settings button - middle
        settingsButtonHitbox = new Rectangle(
            centerX - buttonWidth / 2f,
            startY - buttonHeight - buttonSpacing - buttonHeight,
            buttonWidth,
            buttonHeight
        );
        
        // Exit button - bottom
        exitButtonHitbox = new Rectangle(
            centerX - buttonWidth / 2f,
            startY - buttonHeight - buttonSpacing - buttonHeight - buttonSpacing - buttonHeight,
            buttonWidth,
            buttonHeight
        );
    }
    
    /**
     * Handle pause menu input
     */
    public void update(float delta) {
        if (!isVisible) return;
        
        // Check if hitboxes are initialized
        if (playButtonHitbox == null || settingsButtonHitbox == null || exitButtonHitbox == null) {
            // Try to setup UI if textures are now loaded
            if (bgTexture != null && playButtonTexture != null && settingsButtonTexture != null && exitButtonTexture != null) {
                setupUI();
            } else {
                // Still can't setup, try loading textures
                bgTexture = assets.getExitScreenMenuBg();
                playButtonTexture = assets.getPlayButton();
                settingsButtonTexture = assets.getSettingsButton();
                exitButtonTexture = assets.getExitButton();
                
                if (bgTexture != null && playButtonTexture != null && settingsButtonTexture != null && exitButtonTexture != null) {
                    setupUI();
                }
            }
            
            // If still null, can't process input yet
            if (playButtonHitbox == null || settingsButtonHitbox == null || exitButtonHitbox == null) {
                return;
            }
        }
        
        // Check mouse clicks
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
            
            if (playButtonHitbox.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onResume();
                }
                isVisible = false;
            } else if (settingsButtonHitbox.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onPauseSettings();
                }
            } else if (exitButtonHitbox.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onExitToMenu();
                }
            }
        }
        
        // ESC key handling removed - now handled centrally in BoneChildGame.handleInput()
    }
    
    /**
     * Render the pause menu
     */
    public void render() {
        if (!isVisible) return;
        
        // Check if textures are loaded, if not, try to load them
        if (bgTexture == null || playButtonTexture == null || settingsButtonTexture == null || exitButtonTexture == null) {
            bgTexture = assets.getExitScreenMenuBg();
            playButtonTexture = assets.getPlayButton();
            settingsButtonTexture = assets.getSettingsButton();
            exitButtonTexture = assets.getExitButton();
            
            // If still null, can't render yet
            if (bgTexture == null || playButtonTexture == null || settingsButtonTexture == null || exitButtonTexture == null) {
                Gdx.app.log("PauseMenu", "Textures not loaded yet - cannot render");
                return;
            }
            
            // Setup UI now that textures are loaded
            Gdx.app.log("PauseMenu", "Textures loaded, setting up UI");
            setupUI();
        }
        
        // Check if hitboxes are set up
        if (playButtonHitbox == null || settingsButtonHitbox == null || exitButtonHitbox == null) {
            Gdx.app.log("PauseMenu", "Hitboxes not initialized - cannot render");
            return;
        }
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Enable blending for proper transparency
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        // Draw semi-transparent overlay (low opacity - lets game show through)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.35f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        // Draw background image
        batch.begin();
        
        // Calculate background size and position (centered, scaled to 50% for smaller menu)
        float bgScale = Math.min(screenWidth / bgTexture.getWidth(), screenHeight / bgTexture.getHeight()) * 0.5f;
        float bgWidth = bgTexture.getWidth() * bgScale;
        float bgHeight = bgTexture.getHeight() * bgScale;
        float bgX = (screenWidth - bgWidth) / 2f;
        float bgY = (screenHeight - bgHeight) / 2f;
        
        batch.draw(bgTexture, bgX, bgY, bgWidth, bgHeight);
        
        // Draw buttons
        batch.draw(playButtonTexture, playButtonHitbox.x, playButtonHitbox.y, playButtonHitbox.width, playButtonHitbox.height);
        batch.draw(settingsButtonTexture, settingsButtonHitbox.x, settingsButtonHitbox.y, settingsButtonHitbox.width, settingsButtonHitbox.height);
        batch.draw(exitButtonTexture, exitButtonHitbox.x, exitButtonHitbox.y, exitButtonHitbox.width, exitButtonHitbox.height);
        
        batch.end();
    }
    
    /**
     * Resize pause menu
     */
    public void resize(int width, int height) {
        setupUI();
    }
    
    /**
     * Dispose resources
     */
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        // Don't dispose textures - they're managed by Assets class
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void show() {
        isVisible = true;
    }
    
    public void hide() {
        isVisible = false;
    }
}
