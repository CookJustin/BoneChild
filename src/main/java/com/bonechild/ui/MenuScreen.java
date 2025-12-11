package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
    // Virtual resolution (matches the game's viewport)
    private static final float VIRTUAL_WIDTH = 1280f;
    private static final float VIRTUAL_HEIGHT = 720f;

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final GlyphLayout glyphLayout;
    
    // Scrolling background
    private Texture backgroundTexture;
    private float backgroundX1;  // Position of first background image
    private float backgroundX2;  // Position of second background image
    private float backgroundX3;  // Position of third background image (extra coverage)
    private float scrollSpeed = 30f;  // Pixels per second (reduced from 50 for slower scrolling)
    private float scaledBackgroundWidth;  // Cached scaled width
    
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
        
        // Load background texture
        try {
            this.backgroundTexture = new Texture(Gdx.files.internal("assets/backgrounds/TitleScreen.png"));
            this.backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            System.out.println("[MenuScreen] Background texture loaded: " + backgroundTexture.getWidth() + "x" + backgroundTexture.getHeight());
            
            // Calculate scaled width based on VIRTUAL height
            float bgHeight = backgroundTexture.getHeight();
            float scale = VIRTUAL_HEIGHT / bgHeight;
            this.scaledBackgroundWidth = backgroundTexture.getWidth() * scale;
            
            // Initialize background positions - images overlap to create seamless scrolling
            this.backgroundX1 = 0;
            this.backgroundX2 = -this.scaledBackgroundWidth;
            this.backgroundX3 = -this.scaledBackgroundWidth * 2;
            
            System.out.println("[MenuScreen] Scaled background width: " + scaledBackgroundWidth);
            System.out.println("[MenuScreen] Initial positions: x1=" + backgroundX1 + ", x2=" + backgroundX2 + ", x3=" + backgroundX3);
        } catch (Exception e) {
            System.out.println("[MenuScreen] Background texture not found: " + e.getMessage());
            this.backgroundTexture = null;
        }
        
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
        // Use VIRTUAL resolution instead of physical screen size
        float centerX = VIRTUAL_WIDTH / 2f - buttonWidth / 2f;
        
        // Start Game button (centered vertically with some offset)
        float startY = VIRTUAL_HEIGHT / 2f + 30f;
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
        
        // Update scrolling background
        if (backgroundTexture != null) {
            backgroundX1 += scrollSpeed * delta;
            backgroundX2 += scrollSpeed * delta;
            backgroundX3 += scrollSpeed * delta;
            
            // When an image scrolls completely off the right edge of the VIRTUAL screen,
            // reposition it to the left side (negative position) to create seamless loop
            if (backgroundX1 > VIRTUAL_WIDTH) {
                float leftmost = Math.min(Math.min(backgroundX2, backgroundX3), backgroundX1);
                backgroundX1 = leftmost - scaledBackgroundWidth;
            }
            if (backgroundX2 > VIRTUAL_WIDTH) {
                float leftmost = Math.min(Math.min(backgroundX1, backgroundX3), backgroundX2);
                backgroundX2 = leftmost - scaledBackgroundWidth;
            }
            if (backgroundX3 > VIRTUAL_WIDTH) {
                float leftmost = Math.min(Math.min(backgroundX1, backgroundX2), backgroundX3);
                backgroundX3 = leftmost - scaledBackgroundWidth;
            }
        }
        
        // Check mouse clicks (convert physical mouse coords to virtual coords)
        if (Gdx.input.justTouched()) {
            // Convert physical screen coordinates to virtual coordinates
            float mouseX = Gdx.input.getX() * (VIRTUAL_WIDTH / Gdx.graphics.getWidth());
            float mouseY = (Gdx.graphics.getHeight() - Gdx.input.getY()) * (VIRTUAL_HEIGHT / Gdx.graphics.getHeight());
            
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
        
        // Allow keyboard shortcut (ENTER only, no SPACE)
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
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
        
        // Clear the screen first
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);
        
        // Enable blending for proper texture rendering
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        batch.begin();
        
        // Draw scrolling background using VIRTUAL dimensions
        if (backgroundTexture != null) {
            // Set batch color to darker (0.7 = 70% brightness) to make text stand out
            batch.setColor(0.7f, 0.7f, 0.7f, 1f);
            
            // Draw all three background images using the cached scaled width
            batch.draw(backgroundTexture, backgroundX1, 0, scaledBackgroundWidth, VIRTUAL_HEIGHT);
            batch.draw(backgroundTexture, backgroundX2, 0, scaledBackgroundWidth, VIRTUAL_HEIGHT);
            batch.draw(backgroundTexture, backgroundX3, 0, scaledBackgroundWidth, VIRTUAL_HEIGHT);
            
            // Reset batch color to white for text rendering
            batch.setColor(1f, 1f, 1f, 1f);
        }
        
        // Draw title using VIRTUAL dimensions
        String title = "BoneChild";
        glyphLayout.setText(titleFont, title);
        float titleX = VIRTUAL_WIDTH / 2f - glyphLayout.width / 2f;
        float titleY = VIRTUAL_HEIGHT - 150f;
        
        // Title shadow
        titleFont.setColor(0, 0, 0, 0.8f);
        titleFont.draw(batch, title, titleX + 3, titleY - 3);
        
        // Title text
        titleFont.setColor(1f, 0.2f, 0.2f, 1f); // Blood red
        titleFont.draw(batch, title, titleX, titleY);
        
        // Draw subtitle
        String subtitle = "A Survival Action Game";
        glyphLayout.setText(font, subtitle);
        float subtitleX = VIRTUAL_WIDTH / 2f - glyphLayout.width / 2f;
        float subtitleY = VIRTUAL_HEIGHT - 200f;
        
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
        // Convert physical mouse coords to virtual coords for hover detection
        float mouseX = Gdx.input.getX() * (VIRTUAL_WIDTH / Gdx.graphics.getWidth());
        float mouseY = (Gdx.graphics.getHeight() - Gdx.input.getY()) * (VIRTUAL_HEIGHT / Gdx.graphics.getHeight());
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
        
        // Recalculate background scaling for VIRTUAL resolution
        if (backgroundTexture != null) {
            float bgHeight = backgroundTexture.getHeight();
            float scale = VIRTUAL_HEIGHT / bgHeight;
            this.scaledBackgroundWidth = backgroundTexture.getWidth() * scale;
            
            // Reposition backgrounds to maintain seamless scrolling
            this.backgroundX1 = 0;
            this.backgroundX2 = -this.scaledBackgroundWidth;
            this.backgroundX3 = -this.scaledBackgroundWidth * 2;
            
            Gdx.app.log("MenuScreen", "Resized - Background width: " + scaledBackgroundWidth + " for virtual resolution: 1280x720");
        }
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
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void show() {
        isVisible = true;
    }
}
