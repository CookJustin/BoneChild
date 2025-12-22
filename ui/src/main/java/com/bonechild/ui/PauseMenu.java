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
    // Virtual resolution for UI (same as game world for consistency)
    private static final float VIRTUAL_WIDTH = 1280f;
    private static final float VIRTUAL_HEIGHT = 720f;

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final Assets assets;

    // Screen projection matrix for UI
    private final com.badlogic.gdx.graphics.OrthographicCamera uiCamera;

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

        // Create UI camera with fixed virtual resolution
        this.uiCamera = new com.badlogic.gdx.graphics.OrthographicCamera();
        this.uiCamera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

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

        // Use virtual resolution instead of screen dimensions
        float screenWidth = VIRTUAL_WIDTH;
        float screenHeight = VIRTUAL_HEIGHT;

        // Calculate background size and position (centered, scaled to 50% for smaller menu)
        float bgScale = Math.min(screenWidth / bgTexture.getWidth(), screenHeight / bgTexture.getHeight()) * 0.5f;
        float bgWidth = bgTexture.getWidth() * bgScale;
        float bgHeight = bgTexture.getHeight() * bgScale;
        float bgX = (screenWidth - bgWidth) / 2f;
        float bgY = (screenHeight - bgHeight) / 2f;

        // Button dimensions (all buttons are the same size now, cropped without whitespace)
        float buttonScale = bgScale * 0.85f;
        float buttonWidth = playButtonTexture.getWidth() * buttonScale;
        float buttonHeight = playButtonTexture.getHeight() * buttonScale;

        // Position buttons vertically - evenly spaced within the menu box
        float centerX = screenWidth / 2f;
        float centerY = bgY + (bgHeight / 2f);

        // Spacing between buttons
        float buttonSpacing = buttonHeight * 0.3f;

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

        // Check mouse clicks - convert screen coordinates to virtual coordinates
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

            // Convert to virtual coordinates
            float virtualX = (mouseX / Gdx.graphics.getWidth()) * VIRTUAL_WIDTH;
            float virtualY = (mouseY / Gdx.graphics.getHeight()) * VIRTUAL_HEIGHT;

            if (playButtonHitbox.contains(virtualX, virtualY)) {
                if (callback != null) {
                    callback.onResume();
                }
                isVisible = false;
            } else if (settingsButtonHitbox.contains(virtualX, virtualY)) {
                if (callback != null) {
                    callback.onPauseSettings();
                }
            } else if (exitButtonHitbox.contains(virtualX, virtualY)) {
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

        // Load textures if not already loaded
        if (bgTexture == null || playButtonTexture == null || settingsButtonTexture == null || exitButtonTexture == null) {
            bgTexture = assets.getExitScreenMenuBg();
            playButtonTexture = assets.getPlayButton();
            settingsButtonTexture = assets.getSettingsButton();
            exitButtonTexture = assets.getExitButton();

            // Setup UI once textures are loaded
            if (bgTexture != null && playButtonTexture != null && settingsButtonTexture != null && exitButtonTexture != null) {
                setupUI();
            }
        }

        // If textures still not loaded, skip rendering (assets not ready yet)
        if (bgTexture == null || playButtonTexture == null || settingsButtonTexture == null || exitButtonTexture == null) {
            return;
        }

        // If hitboxes not set up, skip rendering
        if (playButtonHitbox == null || settingsButtonHitbox == null || exitButtonHitbox == null) {
            return;
        }

        // Use virtual resolution
        float screenWidth = VIRTUAL_WIDTH;
        float screenHeight = VIRTUAL_HEIGHT;

        // Update camera and set projection matrices
        uiCamera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.setProjectionMatrix(uiCamera.combined);

        // Enable blending for proper transparency
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Draw semi-transparent overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        // Draw UI textures
        batch.begin();

        // Calculate background size and position (centered, scaled appropriately)
        float bgScale = Math.min(screenWidth / bgTexture.getWidth(), screenHeight / bgTexture.getHeight()) * 0.5f;
        float bgWidth = bgTexture.getWidth() * bgScale;
        float bgHeight = bgTexture.getHeight() * bgScale;
        float bgX = (screenWidth - bgWidth) / 2f;
        float bgY = (screenHeight - bgHeight) / 2f;

        batch.draw(bgTexture, bgX, bgY, bgWidth, bgHeight);

        // Check which button is hovered
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        float virtualX = (mouseX / Gdx.graphics.getWidth()) * VIRTUAL_WIDTH;
        float virtualY = (mouseY / Gdx.graphics.getHeight()) * VIRTUAL_HEIGHT;
        boolean playHovered = playButtonHitbox.contains(virtualX, virtualY);
        boolean settingsHovered = settingsButtonHitbox.contains(virtualX, virtualY);
        boolean exitHovered = exitButtonHitbox.contains(virtualX, virtualY);

        // Draw buttons with hover effect
        if (playHovered) {
            batch.setColor(0.8f, 0.8f, 0.8f, 1f);
        }
        batch.draw(playButtonTexture, playButtonHitbox.x, playButtonHitbox.y, playButtonHitbox.width, playButtonHitbox.height);
        batch.setColor(1f, 1f, 1f, 1f);

        if (settingsHovered) {
            batch.setColor(0.8f, 0.8f, 0.8f, 1f);
        }
        batch.draw(settingsButtonTexture, settingsButtonHitbox.x, settingsButtonHitbox.y, settingsButtonHitbox.width, settingsButtonHitbox.height);
        batch.setColor(1f, 1f, 1f, 1f);

        if (exitHovered) {
            batch.setColor(0.8f, 0.8f, 0.8f, 1f);
        }
        batch.draw(exitButtonTexture, exitButtonHitbox.x, exitButtonHitbox.y, exitButtonHitbox.width, exitButtonHitbox.height);
        batch.setColor(1f, 1f, 1f, 1f);

        batch.end();

        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    }

    /**
     * Resize pause menu
     */
    public void resize(int width, int height) {
        // Keep using fixed virtual resolution
        uiCamera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
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
