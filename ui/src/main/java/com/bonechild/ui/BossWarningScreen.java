package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Retro arcade-style boss warning banner with scrolling text
 */
public class BossWarningScreen {
    private boolean active;
    private float timer;
    private float scrollOffset;
    private String bossType; // NEW: Store the boss type
    
    private static final float SCROLL_SPEED = 200f; // Pixels per second
    private static final float AUTO_DISMISS_TIME = 3.0f; // Auto-dismiss after 3 seconds
    private static final float BANNER_HEIGHT = 80f; // Height of the warning banner
    
    private ShapeRenderer shapeRenderer;
    private BitmapFont warningFont;
    private GlyphLayout layout; // Reusable layout for measuring text
    
    private String warningText;
    private float repeatWidth; // Width including spacing for seamless loop
    
    public BossWarningScreen() {
        this.active = false;
        this.timer = 0f;
        this.scrollOffset = 0f;
        this.shapeRenderer = new ShapeRenderer();
        
        // Create warning font
        this.warningFont = new BitmapFont();
        this.warningFont.getData().setScale(3.0f); // Large text
        
        // Create reusable layout
        this.layout = new GlyphLayout();
        
        // Calculate the actual rendered width including spacing
        layout.setText(warningFont, "WARNING:");
        float warningWidth = layout.width;
        layout.setText(warningFont, "BOSS FIGHT");
        float bossFightWidth = layout.width;
        
        // Total width: "WARNING:" + gap + "BOSS FIGHT" + extra spacing before next repeat
        this.repeatWidth = warningWidth + 80f + bossFightWidth + 200f; // Added 200f extra spacing between repeats
    }
    
    public void show(String bossName) {
        this.active = true;
        this.timer = 0f;
        this.scrollOffset = 0f;
        this.bossType = bossName; // Store the boss type
        
        Gdx.app.log("BossWarningScreen", "🚨 BOSS WARNING: " + bossName);
    }
    
    public void update(float delta) {
        if (!active) return;
        
        timer += delta;
        scrollOffset += SCROLL_SPEED * delta;
        
        // Keep scrolling indefinitely without wrapping - we'll handle the wrap in rendering
        // This prevents glitches from modulo operations
        
        // Auto-dismiss after time
        if (timer >= AUTO_DISMISS_TIME) {
            active = false;
        }
    }
    
    public void render(SpriteBatch batch) {
        if (!active) return;
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Calculate banner position (center of screen)
        float bannerY = (screenHeight - BANNER_HEIGHT) / 2f;
        
        // End the batch if it's active before using ShapeRenderer
        boolean batchWasActive = batch.isDrawing();
        if (batchWasActive) {
            batch.end();
        }

        // Draw black background banner with ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(0, bannerY, screenWidth, BANNER_HEIGHT);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(0, bannerY, screenWidth, 4); // Top border
        shapeRenderer.rect(0, bannerY + BANNER_HEIGHT - 4, screenWidth, 4); // Bottom border
        shapeRenderer.end();
        
        // Always begin the batch for text rendering
        batch.begin();

        // Draw scrolling text
        float textY = bannerY + BANNER_HEIGHT / 2f + 15f; // Center text vertically
        int numCopies = (int) Math.ceil(screenWidth / repeatWidth) + 3;
        float baseX = screenWidth - (scrollOffset % repeatWidth);
        
        for (int i = -1; i < numCopies; i++) {
            float x = baseX + (i * repeatWidth);
            if (x + repeatWidth >= 0 && x <= screenWidth) {
                warningFont.setColor(Color.RED);
                layout.setText(warningFont, "WARNING:");
                warningFont.draw(batch, "WARNING:", x, textY);
                float warningTextWidth = layout.width;
                
                warningFont.setColor(Color.YELLOW);
                layout.setText(warningFont, "BOSS FIGHT");
                warningFont.draw(batch, "BOSS FIGHT", x + warningTextWidth + 80f, textY);
            }
        }
        
        // Draw "Press SPACE" instruction at bottom
        warningFont.getData().setScale(1.5f);
        warningFont.setColor(Color.WHITE);
        String instruction = "Press SPACE to continue";
        layout.setText(warningFont, instruction);
        float instructWidth = layout.width;
        warningFont.draw(batch, instruction,
                         (screenWidth - instructWidth) / 2f,
                         bannerY - 30f);
        warningFont.getData().setScale(3.0f); // Reset scale
        
        // End the batch (we started it for text rendering)
        batch.end();
        
        // Restore original batch state
        if (batchWasActive) {
            batch.begin();
        }
    }
    
    public void dismiss() {
        this.active = false;
    }
    
    public boolean isActive() {
        return active;
    }
    
    /**
     * Get the type of boss being warned about
     */
    public String getBossType() {
        return bossType;
    }
    
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (warningFont != null) {
            warningFont.dispose();
        }
    }
}
