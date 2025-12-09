package com.bonechild.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.bonechild.world.Mob;
import com.bonechild.world.Player;
import com.bonechild.world.TileMap;

/**
 * Handles rendering of all game objects
 */
public class Renderer {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Assets assets;
    private float deltaTime;
    private TileMap tileMap;
    
    public Renderer(OrthographicCamera camera, Assets assets) {
        this.camera = camera;
        this.assets = assets;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.deltaTime = 0;
        
        // Create tile map when tileset is loaded
        if (assets.getTilesetTexture() != null) {
            this.tileMap = new TileMap(assets.getTilesetTexture(), 32);
        }
    }
    
    /**
     * Set delta time for animations
     */
    public void setDeltaTime(float delta) {
        this.deltaTime = delta;
    }
    
    /**
     * Render the tile map background
     */
    public void renderBackground() {
        if (tileMap == null) return;
        
        // Get viewport dimensions
        float viewportWidth = camera.viewportWidth;
        float viewportHeight = camera.viewportHeight;
        float camX = camera.position.x - viewportWidth / 2;
        float camY = camera.position.y - viewportHeight / 2;
        
        // Render the visible portion of the tile map
        tileMap.render(batch, camX, camY, viewportWidth, viewportHeight);
    }
    
    /**
     * Render the player with animations
     */
    public void renderPlayer(Player player) {
        if (player == null || player.isDead()) return;
        
        // Get the appropriate animation based on player state
        Animation currentAnimation;
        switch (player.getCurrentState()) {
            case WALKING:
                currentAnimation = assets.getWalkAnimation();
                break;
            case ATTACKING:
                currentAnimation = assets.getAttackAnimation();
                // Return to idle after attack finishes
                if (currentAnimation.isFinished()) {
                    currentAnimation.reset();
                }
                break;
            case IDLE:
            default:
                currentAnimation = assets.getIdleAnimation();
                break;
        }
        
        // Reset animation if state changed
        if (player.stateChanged()) {
            currentAnimation.reset();
        }
        
        // Update animation
        currentAnimation.update(deltaTime);
        
        batch.begin();
        
        // Get current frame
        var frame = currentAnimation.getCurrentFrame();
        
        // Flip sprite if facing left
        if (!player.isFacingRight() && !frame.isFlipX()) {
            frame.flip(true, false);
        } else if (player.isFacingRight() && frame.isFlipX()) {
            frame.flip(true, false);
        }
        
        // Draw the animated sprite
        batch.draw(
            frame,
            player.getPosition().x,
            player.getPosition().y,
            player.getWidth(),
            player.getHeight()
        );
        
        batch.end();
        
        // Draw health bar above player
        drawHealthBar(
            player.getPosition().x,
            player.getPosition().y + player.getHeight() + 5,
            player.getWidth(),
            5,
            player.getHealthPercentage()
        );
    }
    
    /**
     * Render all mobs
     */
    public void renderMobs(Array<Mob> mobs) {
        if (mobs == null || mobs.size == 0) return;
        
        // Use the walk animation for all mobs (they're always chasing)
        Animation mobAnimation = assets.getWalkAnimation();
        mobAnimation.update(deltaTime);
        
        batch.begin();
        
        for (Mob mob : mobs) {
            if (mob.isActive() && !mob.isDead()) {
                // Get current frame
                var frame = mobAnimation.getCurrentFrame();
                
                // Draw the animated sprite (mobs use skeleton sprite)
                batch.draw(
                    frame,
                    mob.getPosition().x,
                    mob.getPosition().y,
                    mob.getWidth(),
                    mob.getHeight()
                );
            }
        }
        
        batch.end();
        
        // Draw health bars for mobs
        for (Mob mob : mobs) {
            if (mob.isActive() && !mob.isDead()) {
                drawHealthBar(
                    mob.getPosition().x,
                    mob.getPosition().y + mob.getHeight() + 2,
                    mob.getWidth(),
                    3,
                    mob.getHealthPercentage()
                );
            }
        }
    }
    
    /**
     * Draw a health bar
     */
    private void drawHealthBar(float x, float y, float width, float height, float percentage) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background (red)
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, width, height);
        
        // Foreground (green)
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x, y, width * percentage, height);
        
        shapeRenderer.end();
    }
    
    /**
     * Update camera
     */
    public void updateCamera() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }
    
    /**
     * Dispose renderer resources
     */
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
    
    public SpriteBatch getBatch() { return batch; }
    public ShapeRenderer getShapeRenderer() { return shapeRenderer; }
}
