package com.bonechild.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.bonechild.world.Mob;
import com.bonechild.world.Player;

/**
 * Handles rendering of all game objects
 */
public class Renderer {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Assets assets;
    private float deltaTime;
    
    public Renderer(OrthographicCamera camera, Assets assets) {
        this.camera = camera;
        this.assets = assets;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.deltaTime = 0;
    }
    
    /**
     * Set delta time for animations
     */
    public void setDeltaTime(float delta) {
        this.deltaTime = delta;
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
        
        batch.begin();
        
        for (Mob mob : mobs) {
            if (mob.isActive() && !mob.isDead()) {
                // For now, render mobs as red squares
                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.rect(
                    mob.getPosition().x,
                    mob.getPosition().y,
                    mob.getWidth(),
                    mob.getHeight()
                );
                shapeRenderer.end();
                batch.begin();
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
