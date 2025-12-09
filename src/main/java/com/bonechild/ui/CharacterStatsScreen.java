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
    
    private boolean isVisible;
    private Player player;
    
    public CharacterStatsScreen(Assets assets, Player player) {
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = assets.getFont();
        this.glyphLayout = new GlyphLayout();
        this.player = player;
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
    }
    
    /**
     * Handle character stats screen input
     */
    public void update(float delta) {
        if (!isVisible) return;
        
        // Close on C or ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.C) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isVisible = false;
        }
    }
    
    /**
     * Render the character stats screen
     */
    public void render() {
        if (!isVisible || player == null) return;
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
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
        
        // Draw title
        String title = "CHARACTER STATS";
        glyphLayout.setText(titleFont, title);
        float titleX = screenWidth / 2f - glyphLayout.width / 2f;
        float titleY = boxY + boxHeight - 40f;
        
        titleFont.setColor(0.3f, 0.85f, 1f, 1f);
        titleFont.draw(batch, title, titleX, titleY);
        
        batch.end();
        
        // Draw stats
        drawStats(boxX, boxY, boxWidth, boxHeight);
    }
    
    /**
     * Draw all character stats
     */
    private void drawStats(float boxX, float boxY, float boxWidth, float boxHeight) {
        batch.begin();
        
        float statX = boxX + 30f;
        float statY = boxY + boxHeight - 100f;
        float lineHeight = 40f;
        
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
        
        // Power-up Upgrades
        font.setColor(0.9f, 0.9f, 0.9f, 1f);
        font.draw(batch, "--- Power-ups ---", statX, statY);
        statY -= lineHeight;
        
        font.setColor(0.3f, 0.9f, 1f, 1f);
        font.draw(batch, "Speed Level: " + player.getSpeedLevel(), statX, statY);
        statY -= lineHeight;
        
        font.setColor(1f, 0.3f, 0.3f, 1f);
        font.draw(batch, "Strength Level: " + player.getStrengthLevel(), statX, statY);
        statY -= lineHeight;
        
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.draw(batch, "Grab Level: " + player.getGrabLevel(), statX, statY);
        statY -= lineHeight;
        
        // Resources
        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.draw(batch, "Gold: " + player.getGold(), statX, statY);
        
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
}
