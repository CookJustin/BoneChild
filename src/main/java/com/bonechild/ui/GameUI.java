package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.bonechild.rendering.Assets;
import com.bonechild.world.Player;
import com.bonechild.world.WorldManager;

/**
 * Game HUD displaying health, level, wave info, etc.
 */
public class GameUI {
    private final Stage stage;
    private final Skin skin;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    private final SpriteBatch batch;
    
    // UI Labels (only for non-bar UI)
    private Label waveLabel;
    private Label mobCountLabel;
    
    // References
    private final Player player;
    private final WorldManager worldManager;
    
    public GameUI(Assets assets, Player player, WorldManager worldManager) {
        this.player = player;
        this.worldManager = worldManager;
        this.stage = new Stage(new ScreenViewport());
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = assets.getFont();
        this.glyphLayout = new GlyphLayout();
        
        // Create skin for UI
        skin = new Skin();
        skin.add("default", assets.getFont());
        
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = assets.getFont();
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);
        
        setupUI();
    }
    
    private void setupUI() {
        Table table = new Table();
        table.setFillParent(false);
        table.top().left();
        table.setPosition(15, Gdx.graphics.getHeight() - 15);
        stage.addActor(table);
        
        // Wave info with enhanced styling
        waveLabel = new Label("Wave: 0", skin);
        waveLabel.setColor(new Color(1f, 0.6f, 0.1f, 1f)); // Bright orange
        table.add(waveLabel).left().padBottom(8);
        table.row();
        
        // Mob count with enhanced styling
        mobCountLabel = new Label("Enemies: 0", skin);
        mobCountLabel.setColor(new Color(1f, 0.3f, 0.3f, 1f)); // Bright red
        table.add(mobCountLabel).left();
    }
    
    /**
     * Update UI with current game state
     */
    public void update(float delta) {
        if (player == null || worldManager == null) return;
        
        // Update labels
        waveLabel.setText("Wave: " + worldManager.getCurrentWave());
        mobCountLabel.setText("Enemies: " + worldManager.getMobCount());
        
        stage.act(delta);
    }
    
    /**
     * Render the UI
     */
    public void render() {
        stage.draw();
        
        // Draw UI elements at bottom of screen
        drawLevelOrb();
        drawHealthBar();
        drawExpBar();
        drawGoldCounter();
    }
    
    private void drawLevelOrb() {
        float orbRadius = 50;
        float orbX = 80; // Left side of screen
        float orbY = 80; // Bottom of screen
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Outer glow ring
        shapeRenderer.setColor(0.1f, 0.5f, 0.8f, 0.3f);
        for (int i = 3; i > 0; i--) {
            shapeRenderer.circle(orbX, orbY, orbRadius + i * 4, 32);
        }
        
        // Outer border (dark)
        shapeRenderer.setColor(0.05f, 0.05f, 0.1f, 0.9f);
        shapeRenderer.circle(orbX, orbY, orbRadius + 3, 32);
        
        // Main orb background (gradient effect with circles)
        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 0.95f);
        shapeRenderer.circle(orbX, orbY, orbRadius, 32);
        
        // Inner gradient - lighter top
        shapeRenderer.setColor(0.2f, 0.4f, 0.6f, 0.4f);
        shapeRenderer.circle(orbX, orbY + orbRadius * 0.2f, orbRadius * 0.7f, 32);
        
        // Highlight shine
        shapeRenderer.setColor(0.4f, 0.7f, 1f, 0.3f);
        shapeRenderer.circle(orbX - orbRadius * 0.25f, orbY + orbRadius * 0.35f, orbRadius * 0.3f, 24);
        
        shapeRenderer.end();
        
        // Draw level text in the orb
        batch.begin();
        String levelText = String.valueOf(player.getLevel());
        
        // Scale font for level number
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 1.8f);
        
        glyphLayout.setText(font, levelText);
        float textX = orbX - glyphLayout.width / 2f;
        float textY = orbY + glyphLayout.height / 2f;
        
        // Shadow for depth
        font.setColor(0, 0, 0, 0.9f);
        font.draw(batch, levelText, textX + 2, textY - 2);
        
        // Main text in bright cyan
        font.setColor(0.3f, 0.9f, 1f, 1f);
        font.draw(batch, levelText, textX, textY);
        
        // Restore font scale
        font.getData().setScale(originalScale);
        
        // Draw "LEVEL" label below number
        font.getData().setScale(originalScale * 0.6f);
        String labelText = "LEVEL";
        glyphLayout.setText(font, labelText);
        float labelX = orbX - glyphLayout.width / 2f;
        float labelY = orbY - orbRadius * 0.4f;
        
        // Shadow
        font.setColor(0, 0, 0, 0.8f);
        font.draw(batch, labelText, labelX + 1, labelY - 1);
        
        // Label text
        font.setColor(0.7f, 0.7f, 0.8f, 1f);
        font.draw(batch, labelText, labelX, labelY);
        
        // Restore original scale
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawHealthBar() {
        float barWidth = 420;
        float barHeight = 36;
        float x = Gdx.graphics.getWidth() / 2f - barWidth / 2f;
        float y = 35;
        float borderWidth = 3;
        float innerPadding = 2;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Drop shadow for depth
        shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
        shapeRenderer.rect(x + 3, y - 3, barWidth, barHeight);
        
        // Outer metallic border (dark steel)
        shapeRenderer.setColor(0.15f, 0.15f, 0.18f, 1f);
        shapeRenderer.rect(x - borderWidth, y - borderWidth, 
                          barWidth + borderWidth * 2, barHeight + borderWidth * 2);
        
        // Inner border (lighter metallic edge for 3D effect)
        shapeRenderer.setColor(0.4f, 0.4f, 0.45f, 1f);
        shapeRenderer.rect(x - borderWidth + 1, y + barHeight + borderWidth - 2, 
                          barWidth + borderWidth * 2 - 2, 1);
        
        // Dark inner background with subtle vignette
        shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 1f);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        
        // Background texture pattern (vertical lines)
        shapeRenderer.setColor(0.12f, 0.12f, 0.14f, 0.5f);
        for (int i = 0; i < barWidth; i += 4) {
            shapeRenderer.rect(x + i, y, 1, barHeight);
        }
        
        // Health bar with advanced gradient and glow
        float healthPct = player.getHealthPercentage();
        float healthWidth = (barWidth - innerPadding * 2) * healthPct;
        
        if (healthWidth > 0) {
            // Determine color based on health percentage
            Color healthColor, glowColor;
            if (healthPct > 0.6f) {
                // High health - vibrant green
                healthColor = new Color(0.15f, 0.85f, 0.15f, 1f);
                glowColor = new Color(0.3f, 1f, 0.3f, 0.6f);
            } else if (healthPct > 0.3f) {
                // Medium health - bright yellow/orange
                healthColor = new Color(0.95f, 0.85f, 0.15f, 1f);
                glowColor = new Color(1f, 0.95f, 0.4f, 0.6f);
            } else {
                // Low health - intense red with pulse
                healthColor = new Color(0.95f, 0.15f, 0.15f, 1f);
                glowColor = new Color(1f, 0.3f, 0.3f, 0.7f);
            }
            
            // Glow underneath (slightly wider for effect)
            shapeRenderer.setColor(glowColor.r, glowColor.g, glowColor.b, 0.4f);
            shapeRenderer.rect(x + innerPadding - 2, y + innerPadding - 1, 
                              healthWidth + 4, barHeight - innerPadding * 2 + 2);
            
            // Main health bar - bottom section (darker)
            Color darkHealth = healthColor.cpy().lerp(Color.BLACK, 0.4f);
            shapeRenderer.setColor(darkHealth);
            shapeRenderer.rect(x + innerPadding, y + innerPadding, 
                              healthWidth, (barHeight - innerPadding * 2) * 0.5f);
            
            // Main health bar - top section (brighter)
            shapeRenderer.setColor(healthColor);
            shapeRenderer.rect(x + innerPadding, y + innerPadding + (barHeight - innerPadding * 2) * 0.5f, 
                              healthWidth, (barHeight - innerPadding * 2) * 0.5f);
            
            // Top highlight gradient for glass effect
            Color highlightColor = healthColor.cpy().lerp(Color.WHITE, 0.6f);
            shapeRenderer.setColor(highlightColor.r, highlightColor.g, highlightColor.b, 0.5f);
            shapeRenderer.rect(x + innerPadding, y + barHeight - innerPadding - 6, 
                              healthWidth, 4);
            
            // Bright top edge shine
            shapeRenderer.setColor(1f, 1f, 1f, 0.7f);
            shapeRenderer.rect(x + innerPadding, y + barHeight - innerPadding - 2, 
                              healthWidth, 1);
            
            // Vertical highlight strips for dynamic effect
            shapeRenderer.setColor(1f, 1f, 1f, 0.15f);
            for (int i = 0; i < healthWidth; i += 20) {
                shapeRenderer.rect(x + innerPadding + i, y + innerPadding, 
                                  2, barHeight - innerPadding * 2);
            }
        }
        
        // Frame overlay for glass panel effect
        shapeRenderer.setColor(0.3f, 0.3f, 0.35f, 0.3f);
        shapeRenderer.rect(x, y, barWidth, 1); // Bottom edge
        shapeRenderer.rect(x, y, 1, barHeight); // Left edge
        shapeRenderer.rect(x + barWidth - 1, y, 1, barHeight); // Right edge
        
        shapeRenderer.end();
        
        // Draw health text with enhanced styling
        batch.begin();
        String healthText = String.format("%.0f / %.0f", 
            player.getCurrentHealth(), player.getMaxHealth());
        
        // Scale font slightly for health bar
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.9f);
        
        glyphLayout.setText(font, healthText);
        float textX = x + barWidth / 2f - glyphLayout.width / 2f;
        float textY = y + barHeight / 2f + glyphLayout.height / 2f + 2;
        
        // Triple shadow for strong depth
        font.setColor(0, 0, 0, 0.9f);
        font.draw(batch, healthText, textX + 3, textY - 3);
        font.setColor(0, 0, 0, 0.7f);
        font.draw(batch, healthText, textX + 2, textY - 2);
        font.setColor(0, 0, 0, 0.5f);
        font.draw(batch, healthText, textX + 1, textY - 1);
        
        // Main text with subtle outline glow
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, healthText, textX, textY);
        
        // Restore font scale
        font.getData().setScale(originalScale);
        
        // HP label
        font.getData().setScale(originalScale * 0.6f);
        String hpLabel = "HP";
        glyphLayout.setText(font, hpLabel);
        float labelX = x + barWidth / 2f - glyphLayout.width / 2f;
        float labelY = y + barHeight + 18;
        
        // Shadow
        font.setColor(0, 0, 0, 0.8f);
        font.draw(batch, hpLabel, labelX + 1, labelY - 1);
        
        // Label
        font.setColor(0.85f, 0.85f, 0.9f, 1f);
        font.draw(batch, hpLabel, labelX, labelY);
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawExpBar() {
        float barWidth = 420;
        float barHeight = 24;
        float x = Gdx.graphics.getWidth() / 2f - barWidth / 2f;
        float y = 78;
        float borderWidth = 2;
        float innerPadding = 2;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Drop shadow
        shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
        shapeRenderer.rect(x + 2, y - 2, barWidth, barHeight);
        
        // Outer metallic border
        shapeRenderer.setColor(0.15f, 0.15f, 0.18f, 1f);
        shapeRenderer.rect(x - borderWidth, y - borderWidth, 
                          barWidth + borderWidth * 2, barHeight + borderWidth * 2);
        
        // Inner border highlight (top edge for 3D)
        shapeRenderer.setColor(0.4f, 0.4f, 0.45f, 1f);
        shapeRenderer.rect(x - borderWidth + 1, y + barHeight + borderWidth - 2, 
                          barWidth + borderWidth * 2 - 2, 1);
        
        // Dark background
        shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 1f);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        
        // Background pattern (diagonal lines for texture)
        shapeRenderer.setColor(0.12f, 0.12f, 0.14f, 0.4f);
        for (int i = 0; i < barWidth; i += 6) {
            shapeRenderer.rect(x + i, y, 2, barHeight);
        }
        
        // Experience bar with golden glow
        float expPct = player.getExperiencePercentage();
        float expWidth = (barWidth - innerPadding * 2) * expPct;
        
        if (expWidth > 0) {
            // Golden glow underneath
            shapeRenderer.setColor(1f, 0.9f, 0.3f, 0.4f);
            shapeRenderer.rect(x + innerPadding - 2, y + innerPadding - 1, 
                              expWidth + 4, barHeight - innerPadding * 2 + 2);
            
            // Bottom darker gold
            shapeRenderer.setColor(0.65f, 0.5f, 0.08f, 1f);
            shapeRenderer.rect(x + innerPadding, y + innerPadding, 
                              expWidth, (barHeight - innerPadding * 2) * 0.4f);
            
            // Middle bright gold
            shapeRenderer.setColor(0.95f, 0.8f, 0.15f, 1f);
            shapeRenderer.rect(x + innerPadding, y + innerPadding + (barHeight - innerPadding * 2) * 0.4f, 
                              expWidth, (barHeight - innerPadding * 2) * 0.4f);
            
            // Top highlight (bright yellow-white)
            shapeRenderer.setColor(1f, 0.95f, 0.5f, 0.7f);
            shapeRenderer.rect(x + innerPadding, y + barHeight - innerPadding - 4, 
                              expWidth, 3);
            
            // Top edge shine
            shapeRenderer.setColor(1f, 1f, 0.8f, 0.8f);
            shapeRenderer.rect(x + innerPadding, y + barHeight - innerPadding - 1, 
                              expWidth, 1);
            
            // Vertical light rays
            shapeRenderer.setColor(1f, 1f, 1f, 0.2f);
            for (int i = 0; i < expWidth; i += 15) {
                shapeRenderer.rect(x + innerPadding + i, y + innerPadding, 
                                  1, barHeight - innerPadding * 2);
            }
        }
        
        // Glass panel frame overlay
        shapeRenderer.setColor(0.3f, 0.3f, 0.35f, 0.25f);
        shapeRenderer.rect(x, y, barWidth, 1);
        shapeRenderer.rect(x, y, 1, barHeight);
        shapeRenderer.rect(x + barWidth - 1, y, 1, barHeight);
        
        shapeRenderer.end();
        
        // Draw XP text with enhanced styling
        batch.begin();
        String expText = String.format("%.0f / %.0f", 
            player.getExperience(), player.getExperienceToNextLevel());
        
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.75f);
        
        glyphLayout.setText(font, expText);
        float textX = x + barWidth / 2f - glyphLayout.width / 2f;
        float textY = y + barHeight / 2f + glyphLayout.height / 2f + 1;
        
        // Triple shadow for depth
        font.setColor(0, 0, 0, 0.9f);
        font.draw(batch, expText, textX + 2, textY - 2);
        font.setColor(0, 0, 0, 0.6f);
        font.draw(batch, expText, textX + 1, textY - 1);
        
        // Main text
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, expText, textX, textY);
        
        // XP label
        font.getData().setScale(originalScale * 0.55f);
        String xpLabel = "EXPERIENCE";
        glyphLayout.setText(font, xpLabel);
        float labelX = x + barWidth / 2f - glyphLayout.width / 2f;
        float labelY = y + barHeight + 14;
        
        // Shadow
        font.setColor(0, 0, 0, 0.8f);
        font.draw(batch, xpLabel, labelX + 1, labelY - 1);
        
        // Label in gold
        font.setColor(1f, 0.85f, 0.3f, 1f);
        font.draw(batch, xpLabel, labelX, labelY);
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawGoldCounter() {
        float x = Gdx.graphics.getWidth() - 200;
        float y = 30;
        float boxWidth = 180;
        float boxHeight = 50;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Box background
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.95f);
        shapeRenderer.rect(x, y, boxWidth, boxHeight);
        
        // Gold accent border (top)
        shapeRenderer.setColor(1f, 0.85f, 0f, 0.9f);
        shapeRenderer.rect(x, y + boxHeight - 3, boxWidth, 3);
        
        // Gold coin icon
        shapeRenderer.setColor(1f, 0.85f, 0f, 1f);
        shapeRenderer.circle(x + 25, y + boxHeight / 2, 12, 16);
        
        shapeRenderer.end();
        
        // Gold text
        batch.begin();
        String goldText = "Gold: " + player.getGold();
        glyphLayout.setText(font, goldText);
        float textX = x + 50;
        float textY = y + boxHeight / 2 + glyphLayout.height / 2 + 2;
        
        // Shadow
        font.setColor(0, 0, 0, 0.9f);
        font.draw(batch, goldText, textX + 1, textY - 1);
        
        // Main text
        font.setColor(1f, 0.85f, 0f, 1f);
        font.draw(batch, goldText, textX, textY);
        batch.end();
    }
    
    /**
     * Resize the UI
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    /**
     * Dispose UI resources
     */
    public void dispose() {
        stage.dispose();
        skin.dispose();
        shapeRenderer.dispose();
        batch.dispose();
    }
    
    public Stage getStage() { return stage; }
}
