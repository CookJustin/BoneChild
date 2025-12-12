package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bonechild.rendering.Assets;
import com.bonechild.world.Player;

/**
 * Character stats screen shown when C is pressed
 */
public class CharacterStatsScreen {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final GlyphLayout glyphLayout;
    
    // Screen projection matrix for UI
    private final com.badlogic.gdx.graphics.OrthographicCamera uiCamera;
    
    private boolean isVisible;
    private Player player;
    private int currentPage = 0; // 0 = stats page, 1 = power-ups page
    private static final int TOTAL_PAGES = 2;
    
    public CharacterStatsScreen(Assets assets, Player player) {
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = assets.getFont();
        this.glyphLayout = new GlyphLayout();
        this.player = player;
        this.isVisible = false;
        
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
    }
    
    /**
     * Handle character stats screen input
     */
    public void update(float delta) {
        if (!isVisible) return;
        
        // Input handling is now done in BoneChildGame.handleInput() to avoid conflicts
        // Only handle page navigation here - use ONLY arrow keys, not WASD
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            currentPage = (currentPage + 1) % TOTAL_PAGES;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            currentPage = (currentPage - 1 + TOTAL_PAGES) % TOTAL_PAGES;
        }
    }
    
    /**
     * Render the character stats screen
     */
    public void render() {
        if (!isVisible || player == null) return;
        
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
        
        // Draw black box background for stats (centered)
        float boxWidth = 450f;
        float boxHeight = 500f;
        float boxX = screenWidth / 2f - boxWidth / 2f;
        float boxY = screenHeight / 2f - boxHeight / 2f;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.05f, 0.05f, 0.95f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();
        
        // Draw box border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.85f, 1f, 1f);
        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();
        
        batch.begin();
        
        // Draw title based on current page
        String title = currentPage == 0 ? "STATS" : "POWER-UPS";
        glyphLayout.setText(titleFont, title);
        float titleX = screenWidth / 2f - glyphLayout.width / 2f;
        float titleY = boxY + boxHeight - 40f;
        
        titleFont.setColor(0.3f, 0.85f, 1f, 1f);
        titleFont.draw(batch, title, titleX, titleY);
        
        // Draw page indicator with arrow symbols instead of key names
        String pageIndicator = "Page " + (currentPage + 1) + "/" + TOTAL_PAGES + " (< >)";
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.6f);
        glyphLayout.setText(font, pageIndicator);
        float pageX = screenWidth / 2f - glyphLayout.width / 2f;
        float pageY = boxY + 30f;
        
        font.setColor(0.7f, 0.7f, 0.7f, 1f);
        font.draw(batch, pageIndicator, pageX, pageY);
        font.getData().setScale(originalScale);
        
        batch.end();
        
        // Draw content based on current page
        if (currentPage == 0) {
            drawStatsPage(boxX, boxY, boxWidth, boxHeight);
        } else {
            drawPowerUpsPage(boxX, boxY, boxWidth, boxHeight);
        }
    }
    
    /**
     * Draw the stats page (page 1)
     */
    private void drawStatsPage(float boxX, float boxY, float boxWidth, float boxHeight) {
        batch.begin();
        
        float statX = boxX + 30f;
        float statY = boxY + boxHeight - 100f;
        float lineHeight = 30f; // Reduced for tighter spacing
        
        // Scale down the font significantly for better fit
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.65f); // Scale to 65% of original size
        
        font.setColor(0.9f, 0.9f, 0.9f, 1f);
        
        // Level and Experience
        font.draw(batch, "Level: " + player.getLevel(), statX, statY);
        statY -= lineHeight;
        
        float expPercent = player.getExperiencePercentage() * 100f;
        font.draw(batch, "Experience: " + String.format("%.0f%%", expPercent), statX, statY);
        statY -= lineHeight;
        
        // Health
        font.setColor(1f, 0.3f, 0.3f, 1f);
        font.draw(batch, "Health: " + (int)player.getCurrentHealth() + " / " + (int)player.getMaxHealth(), statX, statY);
        statY -= lineHeight;
        
        // Base Stats
        font.setColor(0.9f, 0.9f, 0.9f, 1f);
        font.draw(batch, "Speed: " + (int)player.getSpeed(), statX, statY);
        statY -= lineHeight;
        
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.draw(batch, "Attack Damage: " + String.format("%.0f", player.getAttackDamage()), statX, statY);
        statY -= lineHeight;
        
        // Resources
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.draw(batch, "Gold: " + player.getGold(), statX, statY);
        statY -= lineHeight;
        
        // Restore original font scale
        font.getData().setScale(originalScale);
        
        batch.end();
    }
    
    /**
     * Draw the power-ups page (page 2)
     */
    private void drawPowerUpsPage(float boxX, float boxY, float boxWidth, float boxHeight) {
        batch.begin();
        
        float statX = boxX + 30f;
        float statY = boxY + boxHeight - 100f;
        float lineHeight = 30f; // Reduced for tighter spacing
        
        // Scale down the font significantly to fit better in the box
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.6f); // Scale to 60% to ensure it fits
        
        font.setColor(0.9f, 0.9f, 0.9f, 1f);
        font.draw(batch, "--- All Power-ups Chosen ---", statX, statY);
        statY -= lineHeight * 1.2f;
        
        // Speed
        if (player.getSpeedLevel() > 0) {
            font.setColor(0.3f, 0.9f, 1f, 1f);
            font.draw(batch, "Speed: Level " + player.getSpeedLevel(), statX, statY);
            statY -= lineHeight;
        }
        
        // Strength
        if (player.getStrengthLevel() > 0) {
            font.setColor(1f, 0.3f, 0.3f, 1f);
            font.draw(batch, "Strength: Level " + player.getStrengthLevel(), statX, statY);
            statY -= lineHeight;
        }
        
        // Grab
        if (player.getGrabLevel() > 0) {
            font.setColor(1f, 0.85f, 0.2f, 1f);
            font.draw(batch, "Grab: Level " + player.getGrabLevel(), statX, statY);
            statY -= lineHeight;
        }
        
        // Attack Speed
        if (player.getAttackSpeedLevel() > 0) {
            font.setColor(1f, 0.6f, 0.2f, 1f);
            font.draw(batch, "Attack Speed: Level " + player.getAttackSpeedLevel(), statX, statY);
            statY -= lineHeight;
        }
        
        // Max HP
        if (player.getMaxHpLevel() > 0) {
            font.setColor(0.2f, 1f, 0.3f, 1f);
            font.draw(batch, "Max HP: Level " + player.getMaxHpLevel(), statX, statY);
            statY -= lineHeight;
        }
        
        // XP Boost
        if (player.getXpBoostLevel() > 0) {
            font.setColor(0.8f, 0.3f, 1f, 1f);
            font.draw(batch, "XP Boost: Level " + player.getXpBoostLevel(), statX, statY);
            statY -= lineHeight;
        }
        
        // Explosion Chance
        if (player.getExplosionChanceLevel() > 0) {
            font.setColor(1f, 0.5f, 0f, 1f);
            font.draw(batch, "Explosion: Level " + player.getExplosionChanceLevel(), statX, statY);
            statY -= lineHeight;
        }
        
        // If no power-ups chosen yet
        if (player.getSpeedLevel() == 0 && player.getStrengthLevel() == 0 && 
            player.getGrabLevel() == 0 && player.getAttackSpeedLevel() == 0 &&
            player.getMaxHpLevel() == 0 && player.getXpBoostLevel() == 0 &&
            player.getExplosionChanceLevel() == 0) {
            font.setColor(0.7f, 0.7f, 0.7f, 1f);
            font.draw(batch, "No power-ups chosen yet.", statX, statY);
        }
        
        // Restore original font scale
        font.getData().setScale(originalScale);
        
        batch.end();
    }
    
    /**
     * Dispose resources
     */
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (titleFont != null) titleFont.dispose();
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
    
    /**
     * Resize method (required for proper fullscreen support)
     */
    public void resize(int width, int height) {
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
    }
}
