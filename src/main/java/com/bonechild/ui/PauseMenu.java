package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.bonechild.rendering.Assets;

/**
 * Pause menu for BoneChild gameplay
 */
public class PauseMenu {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final GlyphLayout glyphLayout;
    
    // Buttons
    private Rectangle resumeButton;
    private Rectangle settingsButton;
    private Rectangle exitButton;
    private float buttonWidth = 250f;
    private float buttonHeight = 50f;
    
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
        this.font = assets.getFont();
        this.glyphLayout = new GlyphLayout();
        this.callback = callback;
        this.isVisible = false;
        
        // Create title font
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(2.0f);
        this.titleFont.setColor(Color.WHITE);
        this.titleFont.setUseIntegerPositions(false);
        this.titleFont.getRegion().getTexture().setFilter(
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
        );
        
        setupUI();
    }
    
    private void setupUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float centerX = screenWidth / 2f - buttonWidth / 2f;
        
        float startY = screenHeight / 2f + 50f;
        resumeButton = new Rectangle(centerX, startY, buttonWidth, buttonHeight);
        settingsButton = new Rectangle(centerX, startY - buttonHeight - 20f, buttonWidth, buttonHeight);
        exitButton = new Rectangle(centerX, startY - (buttonHeight * 2) - 40f, buttonWidth, buttonHeight);
    }
    
    /**
     * Handle pause menu input
     */
    public void update(float delta) {
        if (!isVisible) return;
        
        // Check mouse clicks
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
            
            if (resumeButton.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onResume();
                }
                isVisible = false;
            } else if (settingsButton.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onPauseSettings();
                }
            } else if (exitButton.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onExitToMenu();
                }
            }
        }
        
        // Resume with ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (callback != null) {
                callback.onResume();
            }
            isVisible = false;
        }
    }
    
    /**
     * Render the pause menu
     */
    public void render() {
        if (!isVisible) return;
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Enable blending for proper transparency
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        // Draw semi-transparent overlay (low opacity - lets game show through)
        shapeRenderer.setProjectionMatrix(shapeRenderer.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.35f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        // Draw black box background for menu (centered)
        float boxWidth = 400f;
        float boxHeight = 350f;
        float boxX = screenWidth / 2f - boxWidth / 2f;
        float boxY = screenHeight / 2f - boxHeight / 2f;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.95f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();
        
        // Draw black box border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();
        
        batch.begin();
        
        // Draw title (moved higher)
        String title = "PAUSED";
        glyphLayout.setText(titleFont, title);
        float titleX = screenWidth / 2f - glyphLayout.width / 2f;
        float titleY = boxY + boxHeight - 30f;
        
        titleFont.setColor(1f, 0.2f, 0.2f, 1f);
        titleFont.draw(batch, title, titleX, titleY);
        
        batch.end();
        
        // Draw buttons
        drawButton(resumeButton, "RESUME");
        drawButton(settingsButton, "SETTINGS");
        drawButton(exitButton, "EXIT TO MENU");
    }
    
    /**
     * Draw a button with text
     */
    private void drawButton(Rectangle button, String text) {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hovered = button.contains(mouseX, mouseY);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Button background
        if (hovered) {
            shapeRenderer.setColor(1f, 0.3f, 0.3f, 0.9f);
        } else {
            shapeRenderer.setColor(0.4f, 0.1f, 0.1f, 0.8f);
        }
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        
        // Button border
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        shapeRenderer.end();
        
        // Draw button text
        batch.begin();
        glyphLayout.setText(font, text);
        float textX = button.x + button.width / 2f - glyphLayout.width / 2f;
        float textY = button.y + button.height / 2f + glyphLayout.height / 2f;
        
        if (hovered) {
            font.setColor(Color.WHITE);
        } else {
            font.setColor(0.9f, 0.9f, 0.9f, 1f);
        }
        font.draw(batch, text, textX, textY);
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
