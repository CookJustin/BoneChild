package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Epic retro-style boss warning screen
 */
public class BossWarningScreen {
    private boolean active;
    private float timer;
    private float flashTimer;
    private boolean flashOn;
    
    private static final float FLASH_SPEED = 0.15f; // Flash every 0.15 seconds
    private static final float AUTO_DISMISS_TIME = 3.0f; // Auto-dismiss after 3 seconds
    
    private ShapeRenderer shapeRenderer;
    private BitmapFont warningFont;
    private BitmapFont subtitleFont;
    
    private String bossName;
    
    public BossWarningScreen() {
        this.active = false;
        this.timer = 0f;
        this.flashTimer = 0f;
        this.flashOn = true;
        this.shapeRenderer = new ShapeRenderer();
        
        // Create large warning font
        this.warningFont = new BitmapFont();
        this.warningFont.getData().setScale(4.0f); // HUGE text
        this.warningFont.setColor(Color.RED);
        
        // Create subtitle font
        this.subtitleFont = new BitmapFont();
        this.subtitleFont.getData().setScale(2.0f);
        this.subtitleFont.setColor(Color.WHITE);
    }
    
    public void show(String bossName) {
        this.active = true;
        this.timer = 0f;
        this.flashTimer = 0f;
        this.flashOn = true;
        this.bossName = bossName;
        
        Gdx.app.log("BossWarningScreen", "🚨 BOSS WARNING: " + bossName);
    }
    
    public void update(float delta) {
        if (!active) return;
        
        timer += delta;
        flashTimer += delta;
        
        // Flash effect
        if (flashTimer >= FLASH_SPEED) {
            flashOn = !flashOn;
            flashTimer = 0f;
        }
        
        // Auto-dismiss after time
        if (timer >= AUTO_DISMISS_TIME) {
            active = false;
        }
    }
    
    public void render(SpriteBatch batch) {
        if (!active) return;
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Draw semi-transparent black background
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.85f); // Dark semi-transparent
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        // Draw warning border (flashing red)
        if (flashOn) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(10f);
            shapeRenderer.setColor(Color.RED);
            
            // Outer border
            float borderPadding = 20f;
            shapeRenderer.rect(borderPadding, borderPadding, 
                             screenWidth - borderPadding * 2, 
                             screenHeight - borderPadding * 2);
            
            // Inner border
            float innerPadding = 40f;
            shapeRenderer.rect(innerPadding, innerPadding, 
                             screenWidth - innerPadding * 2, 
                             screenHeight - innerPadding * 2);
            
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1f);
        }
        
        // Draw text
        batch.begin();
        
        // "WARNING" text (flashing)
        if (flashOn) {
            String warningText = "⚠ WARNING ⚠";
            warningFont.setColor(Color.RED);
            float warningWidth = warningFont.draw(batch, warningText, 0, 0).width;
            warningFont.draw(batch, warningText, 
                           (screenWidth - warningWidth) / 2, 
                           screenHeight * 0.7f);
        }
        
        // "BOSS FIGHT" text (always visible, yellow)
        String bossText = "BOSS FIGHT";
        subtitleFont.setColor(Color.YELLOW);
        float bossWidth = subtitleFont.draw(batch, bossText, 0, 0).width;
        subtitleFont.draw(batch, bossText, 
                         (screenWidth - bossWidth) / 2, 
                         screenHeight * 0.5f);
        
        // Boss name (white)
        if (bossName != null) {
            subtitleFont.setColor(Color.WHITE);
            float nameWidth = subtitleFont.draw(batch, bossName, 0, 0).width;
            subtitleFont.draw(batch, bossName, 
                            (screenWidth - nameWidth) / 2, 
                            screenHeight * 0.4f);
        }
        
        // Instructions (small, blinking)
        if (flashOn) {
            subtitleFont.setColor(Color.LIGHT_GRAY);
            subtitleFont.getData().setScale(1.5f);
            String instructions = "Press SPACE to continue...";
            float instructWidth = subtitleFont.draw(batch, instructions, 0, 0).width;
            subtitleFont.draw(batch, instructions, 
                            (screenWidth - instructWidth) / 2, 
                            screenHeight * 0.25f);
            subtitleFont.getData().setScale(2.0f); // Reset scale
        }
        
        batch.end();
        
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }
    
    public void dismiss() {
        this.active = false;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (warningFont != null) {
            warningFont.dispose();
        }
        if (subtitleFont != null) {
            subtitleFont.dispose();
        }
    }
}
