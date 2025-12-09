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
 * Title screen menu for BoneChild
 */
public class MenuScreen {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final GlyphLayout glyphLayout;
    
    // Button properties
    private Rectangle startButton;
    private Rectangle settingsButton;
    private Rectangle exitButton;
    private float buttonWidth;
    private float buttonHeight;
    private float padding;
    
    private boolean isVisible;
    private MenuCallback callback;
    
    public interface MenuCallback {
        void onStartGame();
        void onSettings();
        void onExit();
    }
    
    public MenuScreen(Assets assets, MenuCallback callback) {
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = assets.getFont();
        this.glyphLayout = new GlyphLayout();
        this.callback = callback;
        this.isVisible = true;
        
        // Create title font (larger)
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(4.0f);
        this.titleFont.setColor(Color.WHITE);
        this.titleFont.setUseIntegerPositions(false);
        this.titleFont.getRegion().getTexture().setFilter(
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
        );
        
        // Button setup
        this.buttonWidth = 300f;
        this.buttonHeight = 60f;
        this.padding = 20f;
        setupButtons();
    }
    
    private void setupButtons() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float centerX = screenWidth / 2f - buttonWidth / 2f;
        
        // Start Game button (centered vertically with some offset)
        float startY = screenHeight / 2f + 30f;
        startButton = new Rectangle(centerX, startY, buttonWidth, buttonHeight);
        
        // Settings button (below Start Game)
        float settingsY = startY - buttonHeight - padding;
        settingsButton = new Rectangle(centerX, settingsY, buttonWidth, buttonHeight);
        
        // Exit Game button (below Settings)
        float exitY = settingsY - buttonHeight - padding;
        exitButton = new Rectangle(centerX, exitY, buttonWidth, buttonHeight);
    }
    
    /**
     * Handle menu input
     */
    public void update(float delta) {
        if (!isVisible) return;
        
        // Check mouse clicks
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Flip Y coordinate
            
            if (startButton.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onStartGame();
                }
                isVisible = false;
            } else if (settingsButton.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onSettings();
                }
            } else if (exitButton.contains(mouseX, mouseY)) {
                if (callback != null) {
                    callback.onExit();
                }
            }
        }
        
        // Allow keyboard shortcuts
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            if (callback != null) {
                callback.onStartGame();
            }
            isVisible = false;
        }
        
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            if (callback != null) {
                callback.onExit();
            }
        }
    }
    
    /**
     * Render the menu screen
     */
    public void render() {
        if (!isVisible) return;
        
        // Draw semi-transparent background overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();
        
        batch.begin();
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Draw title
        String title = "BoneChild";
        glyphLayout.setText(titleFont, title);
        float titleX = screenWidth / 2f - glyphLayout.width / 2f;
        float titleY = screenHeight - 150f;
        
        // Title shadow
        titleFont.setColor(0, 0, 0, 0.8f);
        titleFont.draw(batch, title, titleX + 3, titleY - 3);
        
        // Title text
        titleFont.setColor(1f, 0.2f, 0.2f, 1f); // Blood red
        titleFont.draw(batch, title, titleX, titleY);
        
        // Draw subtitle
        String subtitle = "A Survival Action Game";
        glyphLayout.setText(font, subtitle);
        float subtitleX = screenWidth / 2f - glyphLayout.width / 2f;
        float subtitleY = screenHeight - 200f;
        
        font.setColor(0.8f, 0.8f, 0.8f, 1f);
        font.draw(batch, subtitle, subtitleX, subtitleY);
        
        batch.end();
        
        // Draw buttons
        drawButton(startButton, "START GAME", true);
        drawButton(settingsButton, "SETTINGS", false);
        drawButton(exitButton, "EXIT GAME", false);
    }
    
    /**
     * Draw a button with text
     */
    private void drawButton(Rectangle button, String text, boolean isHovered) {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hovered = button.contains(mouseX, mouseY);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Button background
        if (hovered) {
            shapeRenderer.setColor(1f, 0.3f, 0.3f, 0.9f); // Bright red on hover
        } else {
            shapeRenderer.setColor(0.4f, 0.1f, 0.1f, 0.8f); // Dark red
        }
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        
        // Button border (drawn as outline with line)
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f); // Blood red border
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
     * Resize menu
     */
    public void resize(int width, int height) {
        setupButtons();
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
}
