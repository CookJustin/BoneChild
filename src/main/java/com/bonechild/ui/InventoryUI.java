package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bonechild.rendering.Assets;

/**
 * Inventory UI - displays hotbar in center of screen with pause overlay
 */
public class InventoryUI {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final GlyphLayout glyphLayout;
    
    private boolean visible;
    
    public InventoryUI(Assets assets) {
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = assets.getFont();
        this.glyphLayout = new GlyphLayout();
        this.visible = false;
        
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
     * Update inventory
     */
    public void update(float delta) {
        if (!visible) return;
        
        // Close on I or ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.I) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            visible = false;
        }
    }
    
    /**
     * Render inventory
     */
    public void render() {
        if (!visible) return;
        
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        
        // Enable blending for proper transparency
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        // Draw semi-transparent overlay (same as pause menu)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.35f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        // Draw inventory box background
        float boxWidth = 500f;
        float boxHeight = 300f;
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
        String title = "INVENTORY";
        glyphLayout.setText(titleFont, title);
        float titleX = screenWidth / 2f - glyphLayout.width / 2f;
        float titleY = boxY + boxHeight - 40f;
        
        titleFont.setColor(0.3f, 0.85f, 1f, 1f);
        titleFont.draw(batch, title, titleX, titleY);
        
        batch.end();
        
        // Draw hotbar in center
        drawCenterHotbar(boxX, boxY, boxWidth, boxHeight);
        
        // Draw instructions
        batch.begin();
        String instructions = "Press I or ESC to close";
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.6f);
        glyphLayout.setText(font, instructions);
        float instrX = screenWidth / 2f - glyphLayout.width / 2f;
        float instrY = boxY + 30f;
        
        font.setColor(0.7f, 0.7f, 0.7f, 1f);
        font.draw(batch, instructions, instrX, instrY);
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    /**
     * Draw the hotbar in the center of the inventory screen
     */
    private void drawCenterHotbar(float boxX, float boxY, float boxWidth, float boxHeight) {
        float slotSize = 80f; // Larger slots for inventory view
        float slotSpacing = 15f;
        float totalWidth = (slotSize * 4) + (slotSpacing * 3);
        float x = (Gdx.graphics.getWidth() / 2f) - (totalWidth / 2f);
        float y = boxY + (boxHeight / 2f) - (slotSize / 2f); // Center vertically in box
        float borderWidth = 3f;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < 4; i++) {
            float slotX = x + (i * (slotSize + slotSpacing));
            
            // Drop shadow
            shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
            shapeRenderer.rect(slotX + 3, y - 3, slotSize, slotSize);
            
            // Outer border
            shapeRenderer.setColor(0.3f, 0.85f, 1f, 1f);
            shapeRenderer.rect(slotX - borderWidth, y - borderWidth, 
                              slotSize + borderWidth * 2, slotSize + borderWidth * 2);
            
            // Slot background
            shapeRenderer.setColor(0.08f, 0.08f, 0.1f, 0.95f);
            shapeRenderer.rect(slotX, y, slotSize, slotSize);
            
            // Highlight if key is pressed
            if (Gdx.input.isKeyPressed(Input.Keys.NUM_1 + i) ||
                Gdx.input.isKeyPressed(Input.Keys.NUMPAD_1 + i)) {
                shapeRenderer.setColor(0.3f, 0.6f, 1f, 0.4f);
                shapeRenderer.rect(slotX + 4, y + 4, slotSize - 8, slotSize - 8);
            }
        }
        
        shapeRenderer.end();
        
        // Draw hotkey numbers and slot labels
        batch.begin();
        float originalScale = font.getData().scaleX;
        font.getData().setScale(originalScale * 0.8f);
        
        for (int i = 0; i < 4; i++) {
            float slotX = x + (i * (slotSize + slotSpacing));
            String keyText = String.valueOf(i + 1);
            
            glyphLayout.setText(font, keyText);
            float textX = slotX + slotSize - glyphLayout.width - 8;
            float textY = y + glyphLayout.height + 8;
            
            // Shadow
            font.setColor(0, 0, 0, 0.8f);
            font.draw(batch, keyText, textX + 1f, textY - 1f);
            
            // Key number in bottom-right corner
            font.setColor(0.3f, 0.85f, 1f, 1f);
            font.draw(batch, keyText, textX, textY);
            
            // Slot label below
            font.getData().setScale(originalScale * 0.5f);
            String slotLabel = "Empty";
            glyphLayout.setText(font, slotLabel);
            float labelX = slotX + (slotSize / 2f) - (glyphLayout.width / 2f);
            float labelY = y - 10f;
            
            font.setColor(0.6f, 0.6f, 0.6f, 1f);
            font.draw(batch, slotLabel, labelX, labelY);
            font.getData().setScale(originalScale * 0.8f);
        }
        
        font.getData().setScale(originalScale);
        batch.end();
    }
    
    /**
     * Toggle inventory visibility
     */
    public void toggle() {
        visible = !visible;
        Gdx.app.log("InventoryUI", "Inventory " + (visible ? "shown" : "hidden"));
    }
    
    /**
     * Show inventory
     */
    public void show() {
        visible = true;
    }
    
    /**
     * Hide inventory
     */
    public void hide() {
        visible = false;
    }
    
    /**
     * Resize inventory UI
     */
    public void resize(int width, int height) {
        // No stage to resize
    }
    
    /**
     * Dispose resources
     */
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (titleFont != null) titleFont.dispose();
    }
    
    public boolean isVisible() { return visible; }
}
