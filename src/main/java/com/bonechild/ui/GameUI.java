package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bonechild.rendering.Assets;
import com.bonechild.world.Player;
import com.bonechild.world.WorldManager;

/**
 * Game HUD displaying health, level, wave info, etc.
 */
public class GameUI {
    // Virtual resolution for UI (same as game world for consistency)
    private static final float VIRTUAL_WIDTH = 1280f;
    private static final float VIRTUAL_HEIGHT = 720f;
    private static final float UI_PADDING = 20f;

    // UI Layout constants for better organization
    private static final float BOTTOM_MARGIN = 30f;
    private static final float ELEMENT_SPACING = 12f;
    private static final float GROUP_SPACING = 25f;

    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    private final SpriteBatch batch;
    private final Assets assets;
    
    // Screen projection matrix for UI
    private final com.badlogic.gdx.graphics.OrthographicCamera uiCamera;
    
    // References
    private final Player player;
    private final WorldManager worldManager;

    // Animation timers
    private float pulseTimer = 0f;
    private float shimmerTimer = 0f;
    private float glowTimer = 0f;
    
    // Smooth animation values
    private float currentHealthWidth = 0f;
    private float targetHealthWidth = 0f;
    private float currentExpWidth = 0f;
    private float targetExpWidth = 0f;
    
    // Enhanced visual effects
    private float lowHealthIntensity = 0f;
    private float levelUpFlashTimer = 0f;
    private int lastLevel = 1;
    
    public GameUI(Assets assets, Player player, WorldManager worldManager) {
        this.player = player;
        this.worldManager = worldManager;
        this.assets = assets;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = assets.getFont();
        this.glyphLayout = new GlyphLayout();
        this.lastLevel = player.getLevel();
        
        // Create UI camera with fixed virtual resolution
        this.uiCamera = new com.badlogic.gdx.graphics.OrthographicCamera();
        this.uiCamera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
    }
    
    /**
     * Update UI with current game state
     */
    public void update(float delta) {
        // Update animation timers
        pulseTimer += delta * 2f; // Pulse speed
        shimmerTimer += delta * 3f; // Shimmer speed
        glowTimer += delta * 1.5f; // Glow speed
        
        // Smooth bar animations with easing
        float barWidth = 240f;
        float innerPadding = 2f;
        
        // Update health bar animation with smooth easing
        targetHealthWidth = (barWidth - innerPadding * 2) * (player.getCurrentHealth() / player.getMaxHealth());
        currentHealthWidth = smoothLerp(currentHealthWidth, targetHealthWidth, delta * 10f);
        
        // Update XP bar animation with smooth easing
        targetExpWidth = barWidth * player.getExperiencePercentage();
        currentExpWidth = smoothLerp(currentExpWidth, targetExpWidth, delta * 8f);
        
        // Low health pulsing intensity
        if ((player.getCurrentHealth() / player.getMaxHealth()) < 0.3f) {
            lowHealthIntensity = Math.min(1f, lowHealthIntensity + delta * 2f);
        } else {
            lowHealthIntensity = Math.max(0f, lowHealthIntensity - delta * 3f);
        }
        
        // Level up flash effect
        if (player.getLevel() > lastLevel) {
            levelUpFlashTimer = 1.5f; // Flash for 1.5 seconds
            lastLevel = player.getLevel();
        }
        if (levelUpFlashTimer > 0f) {
            levelUpFlashTimer -= delta;
        }
    }
    
    private float lerp(float start, float end, float alpha) {
        return start + (end - start) * Math.min(alpha, 1f);
    }
    
    // Smooth easing function for more natural animations
    private float smoothLerp(float start, float end, float alpha) {
        alpha = Math.min(alpha, 1f);
        // Ease-out cubic for smooth deceleration
        float t = 1f - alpha;
        float eased = 1f - (t * t * t);
        return start + (end - start) * eased;
    }
    
    /**
     * Render the UI
     */
    public void render() {
        // Use fixed virtual resolution instead of screen pixels
        uiCamera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        // Draw wave/enemies info in top-left
        drawWaveInfo();
        
        // Draw UI elements at bottom of screen
        drawLevelOrb();
        drawHotbar();
        drawHealthBar(player);
        drawHealthText(font, player);
        drawExperienceBar(player);
        drawLevelText(font, player);
        drawDodgeCharges();
        drawGoldCounter();
    }
    
    private void drawWaveInfo() {
        if (player == null || worldManager == null) return;
        
        batch.begin();
        
        float x = 15f;
        float y = VIRTUAL_HEIGHT - 15f;  // Use VIRTUAL_HEIGHT instead of Gdx.graphics.getHeight()
        float lineHeight = 25f;
        
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.8f);
        
        // Wave info
        String waveText = "Wave: " + worldManager.getCurrentWave();
        font.setColor(1f, 0.6f, 0.1f, 1f); // Bright orange
        font.draw(batch, waveText, x, y);
        
        // Mob count
        String mobText = "Enemies: " + worldManager.getMobCount();
        font.setColor(1f, 0.3f, 0.3f, 1f); // Bright red
        font.draw(batch, mobText, x, y - lineHeight);
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawLevelOrb() {
        float orbRadius = 30f; // Slightly larger
        float orbX = UI_PADDING + orbRadius + 10f;
        float orbY = BOTTOM_MARGIN + orbRadius + 10f;
        
        // Pulsing effect for the orb
        float pulse = (float) Math.sin(pulseTimer) * 0.1f + 1.0f;
        float glowPulse = (float) Math.sin(glowTimer) * 0.3f + 0.7f;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Animated outer glow
        shapeRenderer.setColor(0.3f, 0.85f, 1f, 0.3f * glowPulse);
        shapeRenderer.circle(orbX, orbY, (orbRadius + 6) * pulse, 32);
        
        shapeRenderer.setColor(0.3f, 0.85f, 1f, 0.2f * glowPulse);
        shapeRenderer.circle(orbX, orbY, (orbRadius + 10) * pulse, 32);
        
        // Outer border with glow
        shapeRenderer.setColor(0.3f, 0.85f, 1f, 1f);
        shapeRenderer.circle(orbX, orbY, orbRadius + 2, 32);
        
        // Main orb background
        shapeRenderer.setColor(0.05f, 0.05f, 0.1f, 0.95f);
        shapeRenderer.circle(orbX, orbY, orbRadius, 32);
        
        // Animated inner highlight
        float highlightPulse = (float) Math.sin(pulseTimer * 0.5f) * 0.1f + 0.6f;
        shapeRenderer.setColor(0.15f, 0.25f, 0.35f, highlightPulse);
        shapeRenderer.circle(orbX, orbY + orbRadius * 0.15f, orbRadius * 0.6f, 24);
        
        // Inner glow ring
        shapeRenderer.setColor(0.3f, 0.7f, 1f, 0.4f * glowPulse);
        shapeRenderer.circle(orbX, orbY, orbRadius * 0.8f, 24);
        
        shapeRenderer.end();
        
        // Draw level text with glow effect
        batch.begin();
        String levelText = String.valueOf(player.getLevel());
        
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.9f);
        
        glyphLayout.setText(font, levelText);
        float textX = orbX - glyphLayout.width / 2f;
        float textY = orbY + glyphLayout.height / 2f;
        
        // Glow layers
        font.setColor(0.3f, 0.9f, 1f, 0.4f * glowPulse);
        for (int i = 0; i < 3; i++) {
            float offset = (3 - i) * 0.5f;
            font.draw(batch, levelText, textX, textY);
        }
        
        // Shadow
        font.setColor(0, 0, 0, 0.8f);
        font.draw(batch, levelText, textX + 1, textY - 1);
        
        // Main text
        font.setColor(0.3f, 0.9f, 1f, 1f);
        font.draw(batch, levelText, textX, textY);
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawHealthBar(Player player) {
        float barWidth = 240f; // Wider for better visibility
        float barHeight = 28f; // Taller for better readability
        float x = VIRTUAL_WIDTH / 2f - barWidth / 2f;
        float y = BOTTOM_MARGIN;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Low health pulsing glow
        if (lowHealthIntensity > 0f) {
            float healthPulse = (float) Math.sin(pulseTimer * 3f) * 0.5f + 0.5f;
            shapeRenderer.setColor(1f, 0.2f, 0.2f, lowHealthIntensity * healthPulse * 0.6f);
            shapeRenderer.rect(x - 6, y - 6, barWidth + 12, barHeight + 12);
            shapeRenderer.setColor(1f, 0.2f, 0.2f, lowHealthIntensity * healthPulse * 0.3f);
            shapeRenderer.rect(x - 10, y - 10, barWidth + 20, barHeight + 20);
        }
        
        // Outer glow
        shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 0.3f);
        shapeRenderer.rect(x - 2, y - 2, barWidth + 4, barHeight + 4);
        
        // Dark background
        shapeRenderer.setColor(0.05f, 0.05f, 0.1f, 0.95f);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        
        // Health bar fill with gradient effect
        if (currentHealthWidth > 2) {
            float healthPercent = player.getCurrentHealth() / player.getMaxHealth();
            float innerPadding = 2f;
            
            // Determine health color based on percentage
            float r, g, b;
            if (healthPercent > 0.5f) {
                // Green to yellow (100% to 50%)
                r = (1f - healthPercent) * 2f;
                g = 0.8f;
                b = 0.2f;
            } else {
                // Yellow to red (50% to 0%)
                r = 0.9f;
                g = healthPercent * 1.6f;
                b = 0.2f;
            }
            
            // Bottom layer
            shapeRenderer.setColor(r * 0.6f, g * 0.6f, b * 0.6f, 0.9f);
            shapeRenderer.rect(x + innerPadding, y + innerPadding, 
                             currentHealthWidth, barHeight - innerPadding * 2);
            
            // Top highlight with pulse
            float healthPulse = (float) Math.sin(pulseTimer) * 0.1f + 0.9f;
            shapeRenderer.setColor(r * healthPulse, g * healthPulse, b * healthPulse, 0.9f);
            shapeRenderer.rect(x + innerPadding, y + barHeight - 7, 
                             currentHealthWidth, 3);
        }
        
        // Border
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        shapeRenderer.setColor(0.3f, 0.3f, 0.35f, 1f);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        Gdx.gl.glLineWidth(1);
        shapeRenderer.end();
    }

    private void drawHealthText(BitmapFont font, Player player) {
        float barWidth = 240f;
        float x = VIRTUAL_WIDTH / 2f - barWidth / 2f;
        float y = BOTTOM_MARGIN;

        batch.begin();
        String healthText = String.format("%d / %d", (int)player.getCurrentHealth(), (int)player.getMaxHealth());
        
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.55f); // Slightly larger text
        
        glyphLayout.setText(font, healthText);
        float textX = x + (barWidth - glyphLayout.width) / 2f;
        float textY = y + 20f;
        
        // Shadow
        font.setColor(0f, 0f, 0f, 0.8f);
        font.draw(batch, healthText, textX + 1, textY - 1);
        
        // Main text
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, healthText, textX, textY);
        
        font.getData().setScale(originalScale);
        batch.end();
    }

    private void drawExperienceBar(Player player) {
        float barWidth = 240f; // Match health bar width
        float barHeight = 18f; // Slightly taller
        float x = VIRTUAL_WIDTH / 2f - barWidth / 2f;
        float y = BOTTOM_MARGIN + 28f + ELEMENT_SPACING; // Position above health bar with spacing

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Level up flash effect
        if (levelUpFlashTimer > 0f) {
            float flashIntensity = (levelUpFlashTimer / 1.5f) * 0.5f;
            shapeRenderer.setColor(1f, 0.8f, 0.2f, flashIntensity);
            shapeRenderer.rect(x - 8, y - 8, barWidth + 16, barHeight + 16);
            shapeRenderer.setColor(1f, 1f, 0.5f, flashIntensity * 0.5f);
            shapeRenderer.rect(x - 12, y - 12, barWidth + 24, barHeight + 24);
        }

        // Enhanced outer glow
        float xpGlow = 0.25f + (float)Math.sin(glowTimer * 1.3f) * 0.15f;
        shapeRenderer.setColor(0.2f, 0.5f, 1f, xpGlow * 0.4f);
        shapeRenderer.rect(x - 2, y - 2, barWidth + 4, barHeight + 4);
        shapeRenderer.setColor(0.2f, 0.5f, 1f, xpGlow * 0.2f);
        shapeRenderer.rect(x - 4, y - 4, barWidth + 8, barHeight + 8);

        // Dark background
        shapeRenderer.setColor(0.05f, 0.05f, 0.1f, 0.95f);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // XP bar with beautiful gradient and animation
        if (currentExpWidth > 0) {
            float xpPulse = 0.9f + (float)Math.sin(pulseTimer * 1.5f) * 0.1f;
            
            // Bottom layer
            shapeRenderer.setColor(0.1f * xpPulse, 0.2f * xpPulse, 0.6f * xpPulse, 0.9f);
            shapeRenderer.rect(x + 1, y + 1, currentExpWidth - 2, barHeight - 2);
            
            // Middle layer with gradient effect
            shapeRenderer.setColor(0.2f * xpPulse, 0.4f * xpPulse, 0.9f * xpPulse, 0.9f);
            shapeRenderer.rect(x + 1, y + 2, currentExpWidth - 2, barHeight - 4);
            
            // Top highlight with moving shimmer
            float xpShimmer = 0.5f + (float)Math.sin(shimmerTimer * 1.5f + currentExpWidth * 0.15f) * 0.3f;
            shapeRenderer.setColor(0.5f, 0.7f + xpShimmer * 0.3f, 1f, xpShimmer * 0.7f);
            shapeRenderer.rect(x + 1, y + barHeight - 5, currentExpWidth - 2, 3);
        }

        shapeRenderer.end();
    }

    private void drawLevelText(BitmapFont font, Player player) {
        float barWidth = 240f;
        float x = VIRTUAL_WIDTH / 2f - barWidth / 2f;
        float y = BOTTOM_MARGIN + 28f + ELEMENT_SPACING + 18f + 8f; // Position above XP bar

        batch.begin();
        String xpText = String.format("Level %d - %.0f / %.0f XP", 
            player.getLevel(), player.getExperience(), player.getExperienceToNextLevel());
        
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.38f);
        
        glyphLayout.setText(font, xpText);
        float textX = x + (barWidth - glyphLayout.width) / 2f;
        float textY = y;
        
        // Glow effect
        font.setColor(0.3f, 0.5f, 1f, 0.4f);
        font.draw(batch, xpText, textX - 1, textY + 1);
        font.draw(batch, xpText, textX + 1, textY + 1);
        
        // Shadow
        font.setColor(0f, 0f, 0f, 0.7f);
        font.draw(batch, xpText, textX + 1, textY - 1);
        
        // Main text
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, xpText, textX, textY);
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawGoldCounter() {
        float x = VIRTUAL_WIDTH - UI_PADDING - 130f;
        float y = VIRTUAL_HEIGHT - UI_PADDING - 40f;
        float boxWidth = 130f;
        float boxHeight = 40f;
        
        // Subtle pulse for gold counter
        float goldPulse = (float) Math.sin(glowTimer * 0.8f) * 0.05f + 0.95f;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Subtle gold glow
        shapeRenderer.setColor(1f, 0.85f, 0f, 0.2f * goldPulse);
        shapeRenderer.rect(x - 4, y - 4, boxWidth + 8, boxHeight + 8);
        
        // Box background
        shapeRenderer.setColor(0.05f, 0.05f, 0.1f, 0.95f);
        shapeRenderer.rect(x, y, boxWidth, boxHeight);
        
        // Animated gold accent border
        shapeRenderer.setColor(1f, 0.85f, 0f, 0.9f * goldPulse);
        shapeRenderer.rect(x, y + boxHeight - 3f, boxWidth, 3f);
        
        shapeRenderer.end();
        
        // Draw animated coin sprite and gold text
        batch.begin();
        
        var coinAnim = assets.getCoinAnimation();
        if (coinAnim != null) {
            var coinFrame = coinAnim.getCurrentFrame();
            float coinSize = 24f; // Slightly larger coin
            batch.draw(coinFrame, x + 10, y + boxHeight / 2 - coinSize / 2, coinSize, coinSize);
        }
        
        String goldText = String.valueOf(player.getGold());
        
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.75f); // Larger gold text
        
        glyphLayout.setText(font, goldText);
        float textX = x + 38f;
        float textY = y + boxHeight / 2 + glyphLayout.height / 2;
        
        // Shadow
        font.setColor(0, 0, 0, 0.9f);
        font.draw(batch, goldText, textX + 1.5f, textY - 1.5f);
        
        // Main text
        font.setColor(1f, 0.85f, 0f, 1f);
        font.draw(batch, goldText, textX, textY);
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawHotbar() {
        float slotSize = 54f; // Larger slots
        float slotSpacing = 12f; // More spacing between slots
        float totalWidth = (slotSize * 4) + (slotSpacing * 3);
        float x = VIRTUAL_WIDTH / 2f - totalWidth / 2f;
        float y = BOTTOM_MARGIN + 28f + ELEMENT_SPACING + 18f + GROUP_SPACING + 15f; // Above level/XP section
        float borderWidth = 2.5f;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < 4; i++) {
            float slotX = x + (i * (slotSize + slotSpacing));
            
            // Drop shadow
            shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
            shapeRenderer.rect(slotX + 3, y - 3, slotSize, slotSize);
            
            // Check if key is pressed
            boolean isPressed = Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.NUM_1 + i) ||
                               Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.NUMPAD_1 + i);
            
            // Outer glow when pressed
            if (isPressed) {
                float pressGlow = (float) Math.sin(pulseTimer * 4f) * 0.3f + 0.5f;
                shapeRenderer.setColor(0.3f, 0.6f, 1f, pressGlow * 0.6f);
                shapeRenderer.rect(slotX - 4, y - 4, slotSize + 8, slotSize + 8);
            }
            
            // Outer border
            shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 1f);
            shapeRenderer.rect(slotX - borderWidth, y - borderWidth, 
                              slotSize + borderWidth * 2, slotSize + borderWidth * 2);
            
            // Slot background
            shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 0.95f);
            shapeRenderer.rect(slotX, y, slotSize, slotSize);
            
            // Highlight when pressed
            if (isPressed) {
                shapeRenderer.setColor(0.3f, 0.6f, 1f, 0.4f);
                shapeRenderer.rect(slotX + 2, y + 2, slotSize - 4, slotSize - 4);
            }
        }
        
        shapeRenderer.end();
        
        // Draw hotkey numbers
        batch.begin();
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.65f); // Slightly larger hotkey numbers
        
        for (int i = 0; i < 4; i++) {
            float slotX = x + (i * (slotSize + slotSpacing));
            String keyText = String.valueOf(i + 1);
            
            glyphLayout.setText(font, keyText);
            float textX = slotX + slotSize - glyphLayout.width - 6;
            float textY = y + glyphLayout.height + 6;
            
            // Shadow
            font.setColor(0, 0, 0, 0.8f);
            font.draw(batch, keyText, textX + 1f, textY - 1f);
            
            // Main text
            font.setColor(0.7f, 0.7f, 0.7f, 1f);
            font.draw(batch, keyText, textX, textY);
        }
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    private void drawDodgeCharges() {
        float chargeSize = 28f; // Larger charges
        float chargeSpacing = 10f; // More spacing
        int maxCharges = player.getMaxDodgeCharges();
        int currentCharges = player.getDodgeCharges();
        float rechargeProgress = player.getDodgeRechargeProgress();
        
        // Position to the right of the level orb
        float orbRadius = 30f;
        float orbX = UI_PADDING + orbRadius + 10f;
        float x = orbX + orbRadius + 25f; // Start to the right of the orb with some spacing
        float y = BOTTOM_MARGIN + orbRadius + 10f; // Same vertical center as the orb
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < maxCharges; i++) {
            float chargeX = x + (i * (chargeSize + chargeSpacing));
            float chargeY = y;
            
            // Drop shadow
            shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
            shapeRenderer.circle(chargeX + 1.5f, chargeY - 1.5f, chargeSize / 2f, 20);
            
            // Glow for full charges
            if (i < currentCharges) {
                float chargeGlow = (float) Math.sin(glowTimer + i * 0.5f) * 0.3f + 0.5f;
                shapeRenderer.setColor(0.3f, 0.85f, 1f, chargeGlow * 0.4f);
                shapeRenderer.circle(chargeX, chargeY, chargeSize / 2f + 5, 20);
            }
            
            // Border
            shapeRenderer.setColor(0.3f, 0.3f, 0.35f, 1f);
            shapeRenderer.circle(chargeX, chargeY, chargeSize / 2f + 1.5f, 20);
            
            // Background
            shapeRenderer.setColor(0.1f, 0.1f, 0.12f, 0.95f);
            shapeRenderer.circle(chargeX, chargeY, chargeSize / 2f, 20);
            
            // Fill
            if (i < currentCharges) {
                // Full charge with pulse
                float chargePulse = (float) Math.sin(pulseTimer + i * 0.3f) * 0.1f + 0.9f;
                shapeRenderer.setColor(0.3f, 0.85f, 1f, chargePulse);
                shapeRenderer.circle(chargeX, chargeY, (chargeSize / 2f) - 2, 20);
                
                // Bright highlight
                shapeRenderer.setColor(0.6f, 0.95f, 1f, 0.7f * chargePulse);
                shapeRenderer.circle(chargeX - 2, chargeY + 2, (chargeSize / 2f) - 5, 16);
            } else if (i == currentCharges && rechargeProgress > 0) {
                // Recharging with glow
                shapeRenderer.setColor(0.3f, 0.85f, 1f, 0.6f);
                float angle = 360f * rechargeProgress;
                shapeRenderer.arc(chargeX, chargeY, (chargeSize / 2f) - 2, 90, angle, 20);
            }
        }
        
        shapeRenderer.end();
        
        // Draw "DODGE" label
        batch.begin();
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.45f); // Slightly larger label
        
        String label = "DODGE";
        glyphLayout.setText(font, label);
        float labelX = x + ((maxCharges * (chargeSize + chargeSpacing)) - chargeSpacing) / 2f - glyphLayout.width / 2f;
        float labelY = y - chargeSize / 2f - 8f;
        
        // Shadow
        font.setColor(0, 0, 0, 0.8f);
        font.draw(batch, label, labelX + 1f, labelY - 1f);
        
        // Main text
        font.setColor(0.3f, 0.85f, 1f, 1f);
        font.draw(batch, label, labelX, labelY);
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    /**
     * Resize the UI
     */
    public void resize(int width, int height) {
        // Keep using fixed virtual resolution - don't update based on screen size
        uiCamera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        uiCamera.update();
    }
    
    /**
     * Dispose UI resources
     */
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
    }
}
