package com.bonechild.playablecharacters;

/**
 * Ghost sprite for dodge trail effect
 */
public class GhostSprite {
    private float x;
    private float y;
    private float width;
    private float height;
    private boolean facingRight;
    private float opacity;
    private float lifetime;
    private static final float MAX_LIFETIME = 0.4f; // Ghost lasts 0.4 seconds
    
    public GhostSprite(float x, float y, float width, float height, boolean facingRight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.facingRight = facingRight;
        this.opacity = 0.6f; // Start at 60% opacity
        this.lifetime = 0f;
    }
    
    public void update(float delta) {
        lifetime += delta;
        // Fade out over time
        opacity = 0.6f * (1.0f - (lifetime / MAX_LIFETIME));
    }
    
    public boolean isExpired() {
        return lifetime >= MAX_LIFETIME;
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean isFacingRight() { return facingRight; }
    public float getOpacity() { return opacity; }
}
