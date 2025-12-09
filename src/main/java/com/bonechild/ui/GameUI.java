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
        float barWidth = 400;
        float barHeight = 32;
        float x = Gdx.graphics.getWidth() / 2f - barWidth / 2f;
        float y = 30;
        float borderWidth = 3;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Outer border (dark)
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        shapeRenderer.rect(x - borderWidth, y - borderWidth, 
                          barWidth + borderWidth * 2, barHeight + borderWidth * 2);
        
        // Background
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.95f);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        
        // Health bar with gradient effect (simulated with layered rects)
        float healthPct = player.getHealthPercentage();
        float healthWidth = barWidth * healthPct;
        
        if (healthWidth > 0) {
            Color healthColor = healthPct > 0.6f ? new Color(0.2f, 0.8f, 0.2f, 1f) : 
                               healthPct > 0.3f ? new Color(0.9f, 0.9f, 0.2f, 1f) : 
                               new Color(0.9f, 0.2f, 0.2f, 1f);
            
            // Main health bar
            shapeRenderer.setColor(healthColor);
            shapeRenderer.rect(x, y, healthWidth, barHeight);
            
            // Lighter top gradient
            Color lightColor = healthColor.cpy().lerp(Color.WHITE, 0.3f);
            shapeRenderer.setColor(lightColor.r, lightColor.g, lightColor.b, 0.4f);
            shapeRenderer.rect(x, y + barHeight * 0.6f, healthWidth, barHeight * 0.4f);
            
            // Darker bottom edge
            Color darkColor = healthColor.cpy().lerp(Color.BLACK, 0.3f);
            shapeRenderer.setColor(darkColor.r, darkColor.g, darkColor.b, 0.6f);
            shapeRenderer.rect(x, y, healthWidth, barHeight * 0.15f);
        }
        
        shapeRenderer.end();
        
        // Draw health text inside bar
        batch.begin();
        String healthText = String.format("%.0f / %.0f HP", 
            player.getCurrentHealth(), player.getMaxHealth());
        glyphLayout.setText(font, healthText);
        float textX = x + barWidth / 2f - glyphLayout.width / 2f;
        float textY = y + barHeight / 2f + glyphLayout.height / 2f + 2;
        
        // Draw shadow for readability (double shadow for more depth)
        font.setColor(0, 0, 0, 0.9f);
        font.draw(batch, healthText, textX + 2, textY - 2);
        font.draw(batch, healthText, textX + 1, textY - 1);
        
        // Draw main text
        font.setColor(Color.WHITE);
        font.draw(batch, healthText, textX, textY);
        batch.end();
    }
    
    private void drawExpBar() {
        float barWidth = 400;
        float barHeight = 20;
        float x = Gdx.graphics.getWidth() / 2f - barWidth / 2f;
        float y = 70;
        float borderWidth = 2;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Outer border
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.9f);
        shapeRenderer.rect(x - borderWidth, y - borderWidth, 
                          barWidth + borderWidth * 2, barHeight + borderWidth * 2);
        
        // Background
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.95f);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        
        // Experience bar with gradient
        float expPct = player.getExperiencePercentage();
        float expWidth = barWidth * expPct;
        
        if (expWidth > 0) {
            // Main XP bar (gold/yellow)
            shapeRenderer.setColor(0.85f, 0.7f, 0.1f, 1f);
            shapeRenderer.rect(x, y, expWidth, barHeight);
            
            // Lighter top gradient
            shapeRenderer.setColor(1f, 0.9f, 0.4f, 0.5f);
            shapeRenderer.rect(x, y + barHeight * 0.6f, expWidth, barHeight * 0.4f);
            
            // Darker bottom
            shapeRenderer.setColor(0.6f, 0.5f, 0.05f, 0.6f);
            shapeRenderer.rect(x, y, expWidth, barHeight * 0.2f);
        }
        
        shapeRenderer.end();
        
        // Draw XP text inside bar
        batch.begin();
        String expText = String.format("%.0f / %.0f XP", 
            player.getExperience(), player.getExperienceToNextLevel());
        glyphLayout.setText(font, expText);
        float textX = x + barWidth / 2f - glyphLayout.width / 2f;
        float textY = y + barHeight / 2f + glyphLayout.height / 2f + 1;
        
        // Draw shadow for readability
        font.setColor(0, 0, 0, 0.9f);
        font.draw(batch, expText, textX + 2, textY - 2);
        font.draw(batch, expText, textX + 1, textY - 1);
        
        // Draw main text
        font.setColor(Color.WHITE);
        font.draw(batch, expText, textX, textY);
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
