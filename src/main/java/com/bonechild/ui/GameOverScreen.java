package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.bonechild.rendering.Assets;

/**
 * Game Over screen shown when player dies
 */
public class GameOverScreen {
    // Virtual resolution for UI (same as game world for consistency)
    private static final float VIRTUAL_WIDTH = 1280f;
    private static final float VIRTUAL_HEIGHT = 720f;

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final GlyphLayout glyphLayout;

    // UI camera for proper scaling
    private final com.badlogic.gdx.graphics.OrthographicCamera uiCamera;

    // Buttons
    private Rectangle playAgainButton;
    private Rectangle exitButton;
    private float buttonWidth = 250f;
    private float buttonHeight = 60f;

    private boolean isVisible;
    private GameOverCallback callback;

    // Game stats to display
    private int finalWave;
    private int finalGold;
    private int finalLevel;

    public interface GameOverCallback {
        void onPlayAgain();
        void onExitToMenu();
    }

    public GameOverScreen(Assets assets, GameOverCallback callback) {
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = assets.getFont();
        this.glyphLayout = new GlyphLayout();
        this.callback = callback;
        this.isVisible = false;

        // Create UI camera with fixed virtual resolution
        this.uiCamera = new com.badlogic.gdx.graphics.OrthographicCamera();
        this.uiCamera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // Create title font
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(4.0f);
        this.titleFont.setColor(Color.WHITE);
        this.titleFont.setUseIntegerPositions(false);
        this.titleFont.getRegion().getTexture().setFilter(
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
        );

        setupUI();
    }

    private void setupUI() {
        float centerX = VIRTUAL_WIDTH / 2f - buttonWidth / 2f;

        // Play Again button
        float startY = VIRTUAL_HEIGHT / 2f - 50f;
        playAgainButton = new Rectangle(centerX, startY, buttonWidth, buttonHeight);

        // Exit button
        float exitY = startY - buttonHeight - 20f;
        exitButton = new Rectangle(centerX, exitY, buttonWidth, buttonHeight);
    }

    /**
     * Handle game over screen input
     */
    public void update(float delta) {
        if (!isVisible) return;

        // Check mouse clicks - convert screen coordinates to virtual coordinates
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

            // Convert screen coordinates to virtual coordinates
            float virtualX = (mouseX / Gdx.graphics.getWidth()) * VIRTUAL_WIDTH;
            float virtualY = (mouseY / Gdx.graphics.getHeight()) * VIRTUAL_HEIGHT;

            if (playAgainButton.contains(virtualX, virtualY)) {
                if (callback != null) {
                    callback.onPlayAgain();
                }
                isVisible = false;
            } else if (exitButton.contains(virtualX, virtualY)) {
                if (callback != null) {
                    callback.onExitToMenu();
                }
                isVisible = false;
            }
        }

        // Allow keyboard shortcuts
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            if (callback != null) {
                callback.onPlayAgain();
            }
            isVisible = false;
        }
    }

    /**
     * Render the game over screen
     */
    public void render() {
        if (!isVisible) return;

        // Update camera and set projection matrices for virtual coordinates
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.setProjectionMatrix(uiCamera.combined);

        // Also update button positions based on current screen size so they stay centered
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float centerX = screenWidth / 2f - buttonWidth / 2f;
        float startY = screenHeight / 2f - 50f;
        playAgainButton.set(centerX, startY, buttonWidth, buttonHeight);
        float exitY = startY - buttonHeight - 20f;
        exitButton.set(centerX, exitY, buttonWidth, buttonHeight);

        // Draw dark overlay using virtual resolution
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f);
        shapeRenderer.rect(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        shapeRenderer.end();

        batch.begin();

        // Draw "GAME OVER" title
        String title = "GAME OVER";
        glyphLayout.setText(titleFont, title);
        float titleX = VIRTUAL_WIDTH / 2f - glyphLayout.width / 2f;
        float titleY = VIRTUAL_HEIGHT - 150f;

        // Title shadow
        titleFont.setColor(0, 0, 0, 0.9f);
        titleFont.draw(batch, title, titleX + 3, titleY - 3);

        // Title text in red
        titleFont.setColor(1f, 0.2f, 0.2f, 1f);
        titleFont.draw(batch, title, titleX, titleY);

        batch.end();

        // Draw stats box and buttons
        drawStatsBox();
        drawButton(playAgainButton, "PLAY AGAIN");
        drawButton(exitButton, "EXIT TO MENU");
    }

    /**
     * Draw the stats display box
     */
    private void drawStatsBox() {
        float boxWidth = 350f;
        float boxHeight = 180f;
        float boxX = VIRTUAL_WIDTH / 2f - boxWidth / 2f;
        float boxY = VIRTUAL_HEIGHT / 2f + 100f;

        // Draw box background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);

        // Draw border
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();

        // Draw stats text
        batch.begin();
        float textX = boxX + 20f;
        float textY = boxY + boxHeight - 30f;

        font.setColor(1f, 0.85f, 0f, 1f);
        font.draw(batch, "Final Stats", textX, textY);

        textY -= 40f;
        font.setColor(0.9f, 0.9f, 0.9f, 1f);
        font.draw(batch, "Wave Reached: " + finalWave, textX, textY);

        textY -= 30f;
        font.draw(batch, "Level: " + finalLevel, textX, textY);

        textY -= 30f;
        font.draw(batch, "Gold Collected: " + finalGold, textX, textY);

        batch.end();
    }

    /**
     * Draw a button with text
     */
    private void drawButton(Rectangle button, String text) {
        // Convert screen mouse coordinates to virtual coordinates for hover detection
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        float virtualX = (mouseX / Gdx.graphics.getWidth()) * VIRTUAL_WIDTH;
        float virtualY = (mouseY / Gdx.graphics.getHeight()) * VIRTUAL_HEIGHT;
        boolean hovered = button.contains(virtualX, virtualY);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Button background
        if (hovered) {
            shapeRenderer.setColor(1f, 0.3f, 0.3f, 0.9f);
        } else {
            shapeRenderer.setColor(0.4f, 0.1f, 0.1f, 0.8f);
        }
        shapeRenderer.rect(button.x, button.y, button.width, button.height);

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        shapeRenderer.end();

        // Draw button text with smaller font for "EXIT TO MENU"
        batch.begin();

        float originalScale = font.getData().scaleX;
        // Scale down font for "EXIT TO MENU" button to fit better
        if (text.equals("EXIT TO MENU")) {
            font.getData().setScale(originalScale * 0.8f);
        }

        glyphLayout.setText(font, text);
        float textX = button.x + button.width / 2f - glyphLayout.width / 2f;
        float textY = button.y + button.height / 2f + glyphLayout.height / 2f;

        if (hovered) {
            font.setColor(Color.WHITE);
        } else {
            font.setColor(0.9f, 0.9f, 0.9f, 1f);
        }
        font.draw(batch, text, textX, textY);

        // Restore original font scale
        font.getData().setScale(originalScale);

        batch.end();
    }

    /**
     * Set the final stats to display
     */
    public void setStats(int wave, int gold, int level) {
        this.finalWave = wave;
        this.finalGold = gold;
        this.finalLevel = level;
    }

    /**
     * Resize screen
     */
    public void resize(int width, int height) {
        // Keep using fixed virtual resolution
        uiCamera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        setupUI();
        
        // Update projection matrices for batch and shape renderer
        com.badlogic.gdx.math.Matrix4 projectionMatrix = new com.badlogic.gdx.math.Matrix4();
        projectionMatrix.setToOrtho2D(0, 0, width, height);
        batch.setProjectionMatrix(projectionMatrix);
        shapeRenderer.setProjectionMatrix(projectionMatrix);
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
        if (titleFont != null) {
            titleFont.dispose();
        }
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
