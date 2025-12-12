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
    private final Assets assets;
    
    // UI Labels (only for non-bar UI)
    private Label waveLabel;
    private Label mobCountLabel;
    
    // References
    private final Player player;
    private final WorldManager worldManager;
    
    public GameUI(Assets assets, Player player, WorldManager worldManager) {
        this.player = player;
        this.worldManager = worldManager;
        this.assets = assets;
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
        drawHotbar(); // New: Draw hotbar above bars
        drawHealthBar();
        drawExpBar();
        drawDodgeCharges(); // New: Draw dodge charges indicator
        drawGoldCounter();
    }
    
    private void drawLevelOrb() {
        float orbRadius = 25; // 50% smaller: was 50, now 25
        float orbX = 50; // Adjusted position closer to edge
        float orbY = 50; // Adjusted position closer to bottom
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Simplified and cleaner design
        // Outer border (clean, solid)
        shapeRenderer.setColor(0.3f, 0.85f, 1f, 1f);
        shapeRenderer.circle(orbX, orbY, orbRadius + 2, 32);
        
        // Main orb background (dark, clean)
        shapeRenderer.setColor(0.05f, 0.05f, 0.1f, 0.95f);
        shapeRenderer.circle(orbX, orbY, orbRadius, 32);
        
        // Simple inner highlight for depth
        shapeRenderer.setColor(0.15f, 0.25f, 0.35f, 0.6f);
        shapeRenderer.circle(orbX, orbY + orbRadius * 0.15f, orbRadius * 0.6f, 24);
        
        shapeRenderer.end();
        
        // Draw level text in the orb
        batch.begin();
        String levelText = String.valueOf(player.getLevel());
        
        // Scale font for level number (smaller since orb is smaller)
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.9f); // Reduced from 1.8f
        
        glyphLayout.setText(font, levelText);
        float textX = orbX - glyphLayout.width / 2f;
        float textY = orbY + glyphLayout.height / 2f;
        
        // Clean shadow
        font.setColor(0, 0, 0, 0.8f);
        font.draw(batch, levelText, textX + 1, textY - 1);
        
        // Main text in bright cyan
        font.setColor(0.3f, 0.9f, 1f, 1f);
        font.draw(batch, levelText, textX, textY);
        
        // Restore font scale
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawHealthBar() {
        float barWidth = 210; // 50% smaller: was 420, now 210
        float barHeight = 18; // 50% smaller: was 36, now 18
        float x = Gdx.graphics.getWidth() / 2f - barWidth / 2f;
        float y = 25; // Adjusted y position
        
        // Use the old fallback rendering
        drawHealthBarFallback(x, y, barWidth, barHeight);
        
        // Draw health text
        batch.begin();
        String healthText = String.format("%.0f / %.0f", 
            player.getCurrentHealth(), player.getMaxHealth());
        
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.4f); // Reduced from 0.65f to fit smaller bar
        
        glyphLayout.setText(font, healthText);
        float textX = x + barWidth / 2f - glyphLayout.width / 2f;
        float textY = y + barHeight / 2f + glyphLayout.height / 2f + 1;
        
        // Single subtle shadow for readability
        font.setColor(0, 0, 0, 0.8f);
        font.draw(batch, healthText, textX + 0.5f, textY - 0.5f);
        
        // Main text
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, healthText, textX, textY);
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawHealthBarFallback(float x, float y, float barWidth, float barHeight) {
        float borderWidth = 2; // Reduced from 3 for cleaner look
        float innerPadding = 2;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Drop shadow for depth
        shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
        shapeRenderer.rect(x + 2, y - 2, barWidth, barHeight);
        
        // Outer border (clean, simple)
        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 1f);
        shapeRenderer.rect(x - borderWidth, y - borderWidth, 
                          barWidth + borderWidth * 2, barHeight + borderWidth * 2);
        
        // Dark inner background (clean, no pattern)
        shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 1f);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        
        // Health bar with clean gradient
        float healthPct = player.getHealthPercentage();
        float healthWidth = (barWidth - innerPadding * 2) * healthPct;
        
        if (healthWidth > 0) {
            // Determine color based on health percentage
            Color healthColor;
            if (healthPct > 0.6f) {
                // High health - vibrant green
                healthColor = new Color(0.15f, 0.85f, 0.15f, 1f);
            } else if (healthPct > 0.3f) {
                // Medium health - bright yellow/orange
                healthColor = new Color(0.95f, 0.85f, 0.15f, 1f);
            } else {
                // Low health - intense red
                healthColor = new Color(0.95f, 0.15f, 0.15f, 1f);
            }
            
            // Simple gradient - bottom darker, top brighter
            Color darkHealth = healthColor.cpy().lerp(Color.BLACK, 0.3f);
            shapeRenderer.setColor(darkHealth);
            shapeRenderer.rect(x + innerPadding, y + innerPadding, 
                              healthWidth, (barHeight - innerPadding * 2) * 0.5f);
            
            shapeRenderer.setColor(healthColor);
            shapeRenderer.rect(x + innerPadding, y + innerPadding + (barHeight - innerPadding * 2) * 0.5f, 
                              healthWidth, (barHeight - innerPadding * 2) * 0.5f);
            
            // Subtle top highlight
            Color highlightColor = healthColor.cpy().lerp(Color.WHITE, 0.4f);
            shapeRenderer.setColor(highlightColor.r, highlightColor.g, highlightColor.b, 0.6f);
            shapeRenderer.rect(x + innerPadding, y + barHeight - innerPadding - 3, 
                              healthWidth, 2);
        }
        
        shapeRenderer.end();
    }
    
    private void drawExpBar() {
        float barWidth = 210; // 50% smaller: was 420, now 210
        float barHeight = 12; // 50% smaller: was 24, now 12
        float x = Gdx.graphics.getWidth() / 2f - barWidth / 2f;
        float y = 48; // Adjusted y position to be just above health bar
        float borderWidth = 2;
        float innerPadding = 2;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Drop shadow
        shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
        shapeRenderer.rect(x + 1, y - 1, barWidth, barHeight);
        
        // Outer border (clean, simple)
        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 1f);
        shapeRenderer.rect(x - borderWidth, y - borderWidth, 
                          barWidth + borderWidth * 2, barHeight + borderWidth * 2);
        
        // Dark inner background (clean, no pattern)
        shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 1f);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        
        // Experience bar with clean blue fill
        float expPct = player.getExperiencePercentage();
        float expWidth = (barWidth - innerPadding * 2) * expPct;
        
        if (expWidth > 0) {
            // Blue experience bar - simple gradient (darker bottom, brighter top)
            Color darkBlue = new Color(0.1f, 0.4f, 0.8f, 1f);
            Color brightBlue = new Color(0.3f, 0.6f, 1f, 1f);
            
            // Bottom darker blue
            shapeRenderer.setColor(darkBlue);
            shapeRenderer.rect(x + innerPadding, y + innerPadding, 
                              expWidth, (barHeight - innerPadding * 2) * 0.5f);
            
            // Top brighter blue
            shapeRenderer.setColor(brightBlue);
            shapeRenderer.rect(x + innerPadding, y + innerPadding + (barHeight - innerPadding * 2) * 0.5f, 
                              expWidth, (barHeight - innerPadding * 2) * 0.5f);
            
            // Subtle top highlight
            Color highlightColor = brightBlue.cpy().lerp(Color.WHITE, 0.4f);
            shapeRenderer.setColor(highlightColor.r, highlightColor.g, highlightColor.b, 0.6f);
            shapeRenderer.rect(x + innerPadding, y + barHeight - innerPadding - 2, 
                              expWidth, 1.5f);
        }
        
        shapeRenderer.end();
        
        // Draw XP text with enhanced styling
        batch.begin();
        String expText = String.format("%.0f / %.0f", 
            player.getExperience(), player.getExperienceToNextLevel());
        
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.35f); // Reduced from 0.75f to fit smaller bar
        
        glyphLayout.setText(font, expText);
        float textX = x + barWidth / 2f - glyphLayout.width / 2f;
        float textY = y + barHeight / 2f + glyphLayout.height / 2f + 0.5f;
        
        // Single shadow for smaller bar
        font.setColor(0, 0, 0, 0.9f);
        font.draw(batch, expText, textX + 0.5f, textY - 0.5f);
        
        // Main text
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, expText, textX, textY);
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawGoldCounter() {
        float x = Gdx.graphics.getWidth() - 120; // Adjusted position for larger size
        float y = 30;
        float boxWidth = 54; // 50% bigger: 36 * 1.5 = 54
        float boxHeight = 15; // 50% bigger: 10 * 1.5 = 15
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Box background
        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 0.95f);
        shapeRenderer.rect(x, y, boxWidth, boxHeight);
        
        // Gold accent border (top)
        shapeRenderer.setColor(1f, 0.85f, 0f, 0.9f);
        shapeRenderer.rect(x, y + boxHeight - 1.5f, boxWidth, 1.5f); // Slightly thicker border
        
        shapeRenderer.end();
        
        // Draw animated coin sprite and gold text
        batch.begin();
        
        // Draw animated coin icon (50% bigger)
        var coinAnim = assets.getCoinAnimation();
        if (coinAnim != null) {
            var coinFrame = coinAnim.getCurrentFrame();
            float coinSize = 7.5f; // 50% bigger: 5 * 1.5 = 7.5
            batch.draw(coinFrame, x + 3, y + boxHeight / 2 - coinSize / 2, coinSize, coinSize);
        }
        
        String goldText = "Gold: " + player.getGold();
        
        // Scale font to 30% (50% bigger than 20%)
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.3f);
        
        glyphLayout.setText(font, goldText);
        float textX = x + 13.5f; // Adjusted for larger size
        float textY = y + boxHeight / 2 + glyphLayout.height / 2;
        
        // Shadow
        font.setColor(0, 0, 0, 0.9f);
        font.draw(batch, goldText, textX + 0.75f, textY - 0.75f);
        
        // Main text
        font.setColor(1f, 0.85f, 0f, 1f);
        font.draw(batch, goldText, textX, textY);
        
        // Restore font scale
        font.getData().setScale(originalScale);
        
        batch.end();
    }
    
    private void drawHotbar() {
        float slotSize = 40f; // Size of each hotbar slot
        float slotSpacing = 8f; // Space between slots
        float totalWidth = (slotSize * 4) + (slotSpacing * 3); // 4 slots with 3 gaps
        float x = Gdx.graphics.getWidth() / 2f - totalWidth / 2f;
        float y = 70; // Position above the XP bar
        float borderWidth = 2f;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < 4; i++) {
            float slotX = x + (i * (slotSize + slotSpacing));
            
            // Drop shadow
            shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
            shapeRenderer.rect(slotX + 2, y - 2, slotSize, slotSize);
            
            // Outer border
            shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 1f);
            shapeRenderer.rect(slotX - borderWidth, y - borderWidth, 
                              slotSize + borderWidth * 2, slotSize + borderWidth * 2);
            
            // Slot background
            shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 0.95f);
            shapeRenderer.rect(slotX, y, slotSize, slotSize);
            
            // Highlight if key is pressed
            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.NUM_1 + i) ||
                Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.NUMPAD_1 + i)) {
                shapeRenderer.setColor(0.3f, 0.6f, 1f, 0.3f);
                shapeRenderer.rect(slotX + 2, y + 2, slotSize - 4, slotSize - 4);
            }
        }
        
        shapeRenderer.end();
        
        // Draw hotkey numbers
        batch.begin();
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.5f);
        
        for (int i = 0; i < 4; i++) {
            float slotX = x + (i * (slotSize + slotSpacing));
            String keyText = String.valueOf(i + 1);
            
            glyphLayout.setText(font, keyText);
            float textX = slotX + slotSize - glyphLayout.width - 4;
            float textY = y + glyphLayout.height + 4;
            
            // Shadow
            font.setColor(0, 0, 0, 0.8f);
            font.draw(batch, keyText, textX + 0.5f, textY - 0.5f);
            
            // Key number in bottom-right corner
            font.setColor(0.7f, 0.7f, 0.7f, 1f);
            font.draw(batch, keyText, textX, textY);
        }
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawDodgeCharges() {
        float chargeSize = 20f; // Size of each charge circle
        float chargeSpacing = 6f; // Space between charges
        int maxCharges = player.getMaxDodgeCharges();
        int currentCharges = player.getDodgeCharges();
        float rechargeProgress = player.getDodgeRechargeProgress();
        
        // Position to the right of the health bar
        float healthBarWidth = 210f;
        float healthBarX = Gdx.graphics.getWidth() / 2f - healthBarWidth / 2f;
        float x = healthBarX + healthBarWidth + 15f; // 15px gap from health bar
        float y = 25f + 9f; // Center vertically with health bar (18px height / 2 = 9px)
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < maxCharges; i++) {
            float chargeX = x + (i * (chargeSize + chargeSpacing));
            float chargeY = y;
            
            // Drop shadow
            shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
            shapeRenderer.circle(chargeX + 1, chargeY - 1, chargeSize / 2f, 16);
            
            // Border
            shapeRenderer.setColor(0.3f, 0.3f, 0.35f, 1f);
            shapeRenderer.circle(chargeX, chargeY, chargeSize / 2f + 1, 16);
            
            // Background
            shapeRenderer.setColor(0.1f, 0.1f, 0.12f, 0.95f);
            shapeRenderer.circle(chargeX, chargeY, chargeSize / 2f, 16);
            
            // Fill based on charge status
            if (i < currentCharges) {
                // Full charge - cyan/blue color
                shapeRenderer.setColor(0.3f, 0.85f, 1f, 1f);
                shapeRenderer.circle(chargeX, chargeY, (chargeSize / 2f) - 2, 16);
                
                // Inner highlight
                shapeRenderer.setColor(0.6f, 0.95f, 1f, 0.6f);
                shapeRenderer.circle(chargeX - 2, chargeY + 2, (chargeSize / 2f) - 5, 12);
            } else if (i == currentCharges && rechargeProgress > 0) {
                // Recharging - show progress
                shapeRenderer.setColor(0.3f, 0.85f, 1f, 0.5f);
                // Draw a partial circle based on progress
                float angle = 360f * rechargeProgress;
                shapeRenderer.arc(chargeX, chargeY, (chargeSize / 2f) - 2, 90, angle, 16);
            }
        }
        
        shapeRenderer.end();
        
        // Draw "DODGE" label below charges
        batch.begin();
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.3f);
        
        String label = "DODGE";
        glyphLayout.setText(font, label);
        float labelX = x + ((maxCharges * (chargeSize + chargeSpacing)) - chargeSpacing) / 2f - glyphLayout.width / 2f;
        float labelY = y - chargeSize / 2f - 3f;
        
        // Shadow
        font.setColor(0, 0, 0, 0.8f);
        font.draw(batch, label, labelX + 0.5f, labelY - 0.5f);
        
        // Label text
        font.setColor(0.3f, 0.85f, 1f, 1f);
        font.draw(batch, label, labelX, labelY);
        
        font.getData().setScale(originalScale);
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
