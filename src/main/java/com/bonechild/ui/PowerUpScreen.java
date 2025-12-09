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
 * Power-up selection screen shown when player levels up
 */
public class PowerUpScreen {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final GlyphLayout glyphLayout;
    
    // Buttons for each power-up (vertical layout)
    private Rectangle speedButton;
    private Rectangle strengthButton;
    private Rectangle grabButton;
    private float buttonWidth = 300f;
    private float buttonHeight = 70f;
    private float buttonSpacing = 20f;
    
    private boolean isVisible;
    private PowerUpCallback callback;
    
    public enum PowerUp {
        SPEED, STRENGTH, GRAB
    }
    
    public interface PowerUpCallback {
        void onPowerUpSelected(PowerUp powerUp);
    }
    
    public PowerUpScreen(Assets assets, PowerUpCallback callback) {
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = assets.getFont();
        this.glyphLayout = new GlyphLayout();
        this.callback = callback;
        this.isVisible = false;
        
        // Create title font
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(2.5f);
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
        
        // Center buttons vertically
        float totalHeight = (buttonHeight * 3) + (buttonSpacing * 2);
        float centerX = screenWidth / 2f - buttonWidth / 2f;
        // Position buttons lower to avoid overlapping with title
        float startY = screenHeight / 2f - totalHeight / 2f - 30f;
        
        // Speed button (top)
        speedButton = new Rectangle(centerX, startY, buttonWidth, buttonHeight);
        
        // Strength button (middle)
        strengthButton = new Rectangle(centerX, startY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight);
        
        // Grab button (bottom)
        grabButton = new Rectangle(centerX, startY + (buttonHeight * 2) + (buttonSpacing * 2), buttonWidth, buttonHeight);
    }
    
    /**
     * Handle power-up screen input
     */
    public void update(float delta) {
        if (!isVisible) return;
        
        // Check mouse clicks only
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
            
            if (speedButton.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onPowerUpSelected(PowerUp.SPEED);
                }
                isVisible = false;
            } else if (strengthButton.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onPowerUpSelected(PowerUp.STRENGTH);
                }
                isVisible = false;
            } else if (grabButton.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onPowerUpSelected(PowerUp.GRAB);
                }
                isVisible = false;
            }
        }
    }
    
    /**
     * Render the power-up selection screen
     */
    public void render() {
        if (!isVisible) return;
        
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
        
        // Draw black box background for menu (centered)
        float boxWidth = 450f;
        float boxHeight = 400f;
        float boxX = screenWidth / 2f - boxWidth / 2f;
        float boxY = screenHeight / 2f - boxHeight / 2f;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.95f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();
        
        // Draw box border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.85f, 0f, 1f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();
        
        batch.begin();
        
        // Draw title
        String title = "CHOOSE A POWER-UP";
        glyphLayout.setText(titleFont, title);
        float titleX = screenWidth / 2f - glyphLayout.width / 2f;
        float titleY = boxY + boxHeight - 40f;
        
        titleFont.setColor(1f, 0.85f, 0f, 1f);
        titleFont.draw(batch, title, titleX, titleY);
        
        batch.end();
        
        // Draw buttons
        drawPowerUpButton(speedButton, "SPEED", "Increase movement speed", 0.3f, 0.9f, 1f);
        drawPowerUpButton(strengthButton, "STRENGTH", "Increase attack damage", 1f, 0.3f, 0.3f);
        drawPowerUpButton(grabButton, "GRAB", "Increase pickup range", 1f, 0.85f, 0.2f);
    }
    
    /**
     * Draw a power-up button with description
     */
    private void drawPowerUpButton(Rectangle button, String title, String description, float r, float g, float b) {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hovered = button.contains(mouseX, mouseY);
        
        // Draw button background with box
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Button background
        if (hovered) {
            shapeRenderer.setColor(r, g, b, 0.9f);
        } else {
            shapeRenderer.setColor(r * 0.4f, g * 0.4f, b * 0.4f, 0.75f);
        }
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        
        shapeRenderer.end();
        
        // Button border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(r, g, b, 1f);
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        shapeRenderer.end();
        
        // Draw button text
        batch.begin();
        
        // Store original scale
        float originalScale = font.getData().scaleX;
        
        // Power-up name (large)
        glyphLayout.setText(font, title);
        float textX = button.x + button.width / 2f - glyphLayout.width / 2f;
        float textY = button.y + button.height - 15f;
        
        if (hovered) {
            font.setColor(Color.WHITE);
        } else {
            font.setColor(0.9f, 0.9f, 0.9f, 1f);
        }
        font.draw(batch, title, textX, textY);
        
        // Description (smaller)
        font.getData().setScale(originalScale * 0.5f);
        glyphLayout.setText(font, description);
        float descX = button.x + button.width / 2f - glyphLayout.width / 2f;
        float descY = button.y + 25f;
        
        font.setColor(0.7f, 0.7f, 0.7f, 1f);
        font.draw(batch, description, descX, descY);
        
        // Restore original font scale
        font.getData().setScale(originalScale);
        
        batch.end();
    }
    
    /**
     * Resize screen
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
