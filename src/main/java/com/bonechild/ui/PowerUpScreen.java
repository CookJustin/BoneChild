package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.bonechild.rendering.Assets;
import com.bonechild.world.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Power-up selection screen shown when player levels up
 */
public class PowerUpScreen {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final GlyphLayout glyphLayout;
    
    // Screen projection matrix for UI
    private final com.badlogic.gdx.graphics.OrthographicCamera uiCamera;
    
    // Buttons for each power-up (vertical layout)
    private Rectangle[] powerUpButtons = new Rectangle[3];
    private Rectangle rerollButton;
    private float buttonWidth = 350f;
    private float buttonHeight = 70f;
    private float buttonSpacing = 20f;
    
    private boolean isVisible;
    private PowerUpCallback callback;
    private Player player;
    private Random random;
    
    // Current power-up selections
    private PowerUp[] currentPowerUps = new PowerUp[3];
    private static final int REROLL_COST = 10;
    
    public enum PowerUp {
        SPEED("SPEED", "Speed", "Increase movement speed", 0.3f, 0.9f, 1f),
        STRENGTH("STRENGTH", "Strength", "Increase attack damage", 1f, 0.3f, 0.3f),
        GRAB("GRAB", "Grab", "Increase pickup range", 1f, 0.85f, 0.2f),
        ATTACK_SPEED("ATTACK_SPEED", "Attack Speed", "Attack faster", 1f, 0.6f, 0.2f),
        MAX_HP("MAX_HP", "Max HP", "Increase max health", 0.2f, 1f, 0.3f),
        XP_BOOST("XP_BOOST", "XP Boost", "Gain 10% more XP", 0.8f, 0.3f, 1f),
        EXPLOSION_CHANCE("EXPLOSION_CHANCE", "Explosion", "5% chance to explode on kill", 1f, 0.5f, 0f),
        CHAIN_LIGHTNING("CHAIN_LIGHTNING", "Chain Lightning", "20% chance to chain to nearby enemies", 0.4f, 0.6f, 1f),
        LIFESTEAL("LIFESTEAL", "Lifesteal", "Heal 15% of damage on kill", 0.2f, 1f, 0.5f);
        
        public final String id;
        public final String displayName;
        public final String description;
        public final float r, g, b;
        
        PowerUp(String id, String displayName, String description, float r, float g, float b) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.r = r;
            this.g = g;
            this.b = b;
        }
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
        this.random = new Random();
        
        // Create UI camera for proper screen-space rendering
        this.uiCamera = new com.badlogic.gdx.graphics.OrthographicCamera();
        this.uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
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
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    private void setupUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Center buttons vertically
        float totalHeight = (buttonHeight * 3) + (buttonSpacing * 2);
        float centerX = screenWidth / 2f - buttonWidth / 2f;
        float startY = screenHeight / 2f - totalHeight / 2f - 30f;
        
        // Create 3 power-up buttons
        for (int i = 0; i < 3; i++) {
            powerUpButtons[i] = new Rectangle(
                centerX, 
                startY + (buttonHeight + buttonSpacing) * i, 
                buttonWidth, 
                buttonHeight
            );
        }
        
        // Reroll button at bottom
        float rerollWidth = 200f;
        float rerollHeight = 45f;
        rerollButton = new Rectangle(
            screenWidth / 2f - rerollWidth / 2f,
            startY - rerollHeight - 30f,
            rerollWidth,
            rerollHeight
        );
    }
    
    /**
     * Randomize power-up selections
     */
    private void randomizePowerUps() {
        // Get all available power-ups
        List<PowerUp> allPowerUps = new ArrayList<>();
        for (PowerUp powerUp : PowerUp.values()) {
            // Skip Explosion Chance if player already has level 20 (100% chance)
            if (powerUp == PowerUp.EXPLOSION_CHANCE && player != null && player.getExplosionChanceLevel() >= 20) {
                continue; // Don't add to pool
            }
            allPowerUps.add(powerUp);
        }
        
        // If we have less than 3 power-ups available (all maxed out), allow duplicates
        if (allPowerUps.size() < 3) {
            // This shouldn't happen unless all power-ups are maxed, but just in case
            while (allPowerUps.size() < 3) {
                allPowerUps.add(PowerUp.SPEED); // Default fallback
            }
        }
        
        // Shuffle and pick 3
        Collections.shuffle(allPowerUps, random);
        for (int i = 0; i < 3; i++) {
            currentPowerUps[i] = allPowerUps.get(i);
        }
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
            
            // Check power-up button clicks
            for (int i = 0; i < 3; i++) {
                if (powerUpButtons[i].contains(mouseX, mouseY)) {
                    if (callback != null && currentPowerUps[i] != null) {
                        callback.onPowerUpSelected(currentPowerUps[i]);
                    }
                    isVisible = false;
                    return;
                }
            }
            
            // Check reroll button
            if (rerollButton.contains(mouseX, mouseY)) {
                if (player != null && player.spendGold(REROLL_COST)) {
                    randomizePowerUps();
                    Gdx.app.log("PowerUpScreen", "Rerolled power-ups for " + REROLL_COST + " gold");
                } else {
                    Gdx.app.log("PowerUpScreen", "Not enough gold to reroll (need " + REROLL_COST + ")");
                }
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
        
        // Update camera and set projection matrices
        uiCamera.setToOrtho(false, screenWidth, screenHeight);
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        // Enable blending for proper transparency
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        // Draw semi-transparent overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.35f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        // Draw black box background for menu
        float boxWidth = 480f;
        float boxHeight = 480f;
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
        
        // Draw power-up buttons
        for (int i = 0; i < 3; i++) {
            if (currentPowerUps[i] != null) {
                drawPowerUpButton(
                    powerUpButtons[i], 
                    currentPowerUps[i].displayName, 
                    currentPowerUps[i].description,
                    currentPowerUps[i].r,
                    currentPowerUps[i].g,
                    currentPowerUps[i].b
                );
            }
        }
        
        // Draw reroll button
        drawRerollButton();
    }
    
    /**
     * Draw a power-up button with description
     */
    private void drawPowerUpButton(Rectangle button, String title, String description, float r, float g, float b) {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hovered = button.contains(mouseX, mouseY);
        
        // Draw button background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
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
        
        float originalScale = font.getData().scaleX;
        
        // Power-up name
        glyphLayout.setText(font, title);
        float textX = button.x + button.width / 2f - glyphLayout.width / 2f;
        float textY = button.y + button.height - 15f;
        
        if (hovered) {
            font.setColor(Color.WHITE);
        } else {
            font.setColor(0.9f, 0.9f, 0.9f, 1f);
        }
        font.draw(batch, title, textX, textY);
        
        // Description
        font.getData().setScale(originalScale * 0.5f);
        glyphLayout.setText(font, description);
        float descX = button.x + button.width / 2f - glyphLayout.width / 2f;
        float descY = button.y + 25f;
        
        font.setColor(0.7f, 0.7f, 0.7f, 1f);
        font.draw(batch, description, descX, descY);
        
        font.getData().setScale(originalScale);
        
        batch.end();
    }
    
    /**
     * Draw reroll button
     */
    private void drawRerollButton() {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hovered = rerollButton.contains(mouseX, mouseY);
        boolean canAfford = player != null && player.getGold() >= REROLL_COST;
        
        // Draw button background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        if (!canAfford) {
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.7f);
        } else if (hovered) {
            shapeRenderer.setColor(1f, 0.85f, 0f, 0.9f);
        } else {
            shapeRenderer.setColor(0.6f, 0.5f, 0f, 0.75f);
        }
        shapeRenderer.rect(rerollButton.x, rerollButton.y, rerollButton.width, rerollButton.height);
        
        shapeRenderer.end();
        
        // Button border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (canAfford) {
            shapeRenderer.setColor(1f, 0.85f, 0f, 1f);
        } else {
            shapeRenderer.setColor(0.4f, 0.4f, 0.4f, 1f);
        }
        shapeRenderer.rect(rerollButton.x, rerollButton.y, rerollButton.width, rerollButton.height);
        shapeRenderer.end();
        
        // Draw button text
        batch.begin();
        
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.7f);
        
        String text = "Reroll (" + REROLL_COST + " gold)";
        glyphLayout.setText(font, text);
        float textX = rerollButton.x + rerollButton.width / 2f - glyphLayout.width / 2f;
        float textY = rerollButton.y + rerollButton.height / 2f + glyphLayout.height / 2f;
        
        if (canAfford) {
            if (hovered) {
                font.setColor(Color.WHITE);
            } else {
                font.setColor(1f, 0.9f, 0.6f, 1f);
            }
        } else {
            font.setColor(0.5f, 0.5f, 0.5f, 1f);
        }
        font.draw(batch, text, textX, textY);
        
        font.getData().setScale(originalScale);
        
        batch.end();
    }
    
    /**
     * Resize screen
     */
    public void resize(int width, int height) {
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
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
        randomizePowerUps(); // Randomize when showing
        isVisible = true;
    }
    
    public void hide() {
        isVisible = false;
    }
}
