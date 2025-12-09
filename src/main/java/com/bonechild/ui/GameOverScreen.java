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
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final GlyphLayout glyphLayout;
    
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
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float centerX = screenWidth / 2f - buttonWidth / 2f;
        
        // Play Again button
        float startY = screenHeight / 2f - 50f;
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
        
        // Check mouse clicks
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
            
            if (playAgainButton.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onPlayAgain();
                }
                isVisible = false;
            } else if (exitButton.contains(mouseX, mouseY)) {
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
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Draw dark overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        batch.begin();
        
        // Draw "GAME OVER" title
        String title = "GAME OVER";
        glyphLayout.setText(titleFont, title);
        float titleX = screenWidth / 2f - glyphLayout.width / 2f;
        float titleY = screenHeight - 150f;
        
        // Title shadow
        titleFont.setColor(0, 0, 0, 0.9f);
        titleFont.draw(batch, title, titleX + 3, titleY - 3);
        
        // Title text in red
        titleFont.setColor(1f, 0.2f, 0.2f, 1f);
        titleFont.draw(batch, title, titleX, titleY);
        
        batch.end();
        
        // Draw stats box
        drawStatsBox(screenWidth, screenHeight);
        
        // Draw buttons
        drawButton(playAgainButton, "PLAY AGAIN");
        drawButton(exitButton, "EXIT TO MENU");
    }
    
    /**
     * Draw the stats display box
     */
    private void drawStatsBox(float screenWidth, float screenHeight) {
        float boxWidth = 350f;
        float boxHeight = 180f;
        float boxX = screenWidth / 2f - boxWidth / 2f;
        float boxY = screenHeight / 2f + 100f;
        
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
