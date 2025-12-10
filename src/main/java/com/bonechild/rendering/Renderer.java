package com.bonechild.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.bonechild.world.Mob;
import com.bonechild.world.Player;
import com.bonechild.world.TileMap;
import com.bonechild.world.Pickup;
import com.bonechild.world.Projectile;

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
    
    // Separate animation instances for player (not shared with mobs)
    private Animation playerIdleAnimation;
    private Animation playerWalkAnimation;
    private Animation playerHurtAnimation;
    private Animation playerDeathAnimation;
    private Player.AnimationState lastPlayerState;
    
    // Separate animation instance for mobs (shared among all mobs)
    private Animation mobWalkAnimation;
    
    public Renderer(OrthographicCamera camera, Assets assets) {
        this.camera = camera;
        this.assets = assets;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.deltaTime = 0;
        this.lastPlayerState = Player.AnimationState.IDLE;
        
        // Create tile map when tileset is loaded
        if (assets.getTilesetTexture() != null) {
            this.tileMap = new TileMap(assets.getTilesetTexture(), 16); // Changed from 32 to 16 for Dungeon_Tileset
        }
        
        // Create separate player animation instances
        if (assets.getIdleAnimation() != null) {
            playerIdleAnimation = assets.getIdleAnimation();
            playerWalkAnimation = assets.getWalkAnimation();
            playerHurtAnimation = assets.getHurtAnimation();
            playerDeathAnimation = assets.getDeathAnimation();
            
            // Create a separate mob animation instance (not shared with player)
            mobWalkAnimation = assets.createWalkAnimation();
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
        if (player == null) return;
        
        // Get the appropriate animation based on player state
        Animation currentAnimation;
        switch (player.getCurrentState()) {
            case WALKING:
                currentAnimation = playerWalkAnimation;
                break;
            case HURT:
                currentAnimation = playerHurtAnimation;
                break;
            case DEAD:
                currentAnimation = playerDeathAnimation;
                break;
            case IDLE:
            default:
                currentAnimation = playerIdleAnimation;
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
        
        // Draw the sprite at 32x64 size (adjusted for new frame dimensions)
        float spriteX = player.getPosition().x;
        float spriteY = player.getPosition().y;
        
        batch.draw(
            frame,
            spriteX,
            spriteY,
            32,  // New sprite width (32 pixels)
            64   // Sprite height (still 64)
        );
        
        // Reset batch color to default white to prevent color tinting issues
        batch.setColor(Color.WHITE);
        
        batch.end();
        
        // Draw health bar above player (unless dead)
        if (!player.isDead()) {
            drawHealthBar(
                player.getPosition().x - 16,  // Center the 64px bar over the 32px sprite
                player.getPosition().y + 64 + 5,
                64,
                5,
                player.getHealthPercentage()
            );
        }
    }
    
    /**
     * Render all mobs
     */
    public void renderMobs(Array<Mob> mobs) {
        if (mobs == null || mobs.size == 0) return;
        
        // Use the walk animation for all mobs (they're always chasing)
        mobWalkAnimation.update(deltaTime);
        
        batch.begin();
        
        for (Mob mob : mobs) {
            if (mob.isActive() && !mob.isDead()) {
                // Get current frame
                var frame = mobWalkAnimation.getCurrentFrame();
                
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
     * Render all pickups (coins and XP orbs)
     */
    public void renderPickups(Array<Pickup> pickups) {
        if (pickups == null || pickups.size == 0) return;
        
        for (Pickup pickup : pickups) {
            if (pickup.isActive() && !pickup.isCollected()) {
                renderPickup(pickup);
            }
        }
    }
    
    /**
     * Render a single pickup item
     */
    private void renderPickup(Pickup pickup) {
        float x = pickup.getPosition().x;
        float y = pickup.getPosition().y;
        float width = pickup.getWidth();
        float height = pickup.getHeight();
        
        if (pickup.getType() == Pickup.PickupType.GOLD_COIN) {
            // Draw animated coin sprite
            Animation coinAnim = assets.getCoinAnimation();
            if (coinAnim != null) {
                coinAnim.update(deltaTime);
                var frame = coinAnim.getCurrentFrame();
                
                batch.begin();
                batch.draw(frame, x, y, width, height);
                batch.end();
            }
        } else if (pickup.getType() == Pickup.PickupType.HEALTH_ORB) {
            // Draw animated health flask sprite
            Animation healthAnim = assets.getHealthOrbAnimation();
            if (healthAnim != null) {
                healthAnim.update(deltaTime);
                var frame = healthAnim.getCurrentFrame();
                
                batch.begin();
                batch.draw(frame, x, y, width, height);
                batch.end();
            }
        } else {
            // Keep XP orbs as colored circles
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            if (pickup.getType() == Pickup.PickupType.XP_ORB) {
                // Draw XP orb with blue glow
                // Outer glow
                shapeRenderer.setColor(0f, 0.5f, 1f, 0.3f);
                shapeRenderer.circle(x + width / 2, y + height / 2, width * 0.8f, 16);
                
                // Main orb (blue)
                shapeRenderer.setColor(0.2f, 0.6f, 1f, 1f);
                shapeRenderer.circle(x + width / 2, y + height / 2, width * 0.5f, 16);
                
                // Inner bright core
                shapeRenderer.setColor(0.5f, 0.8f, 1f, 0.8f);
                shapeRenderer.circle(x + width / 2, y + height / 2, width * 0.3f, 14);
                
                // Highlight
                shapeRenderer.setColor(0.8f, 1f, 1f, 0.7f);
                shapeRenderer.circle(x + width * 0.3f, y + height * 0.3f, width * 0.15f, 10);
            }
            
            shapeRenderer.end();
        }
    }
    
    /**
     * Render all projectiles (fireballs)
     */
    public void renderProjectiles(Array<Projectile> projectiles) {
        if (projectiles == null || projectiles.size == 0) return;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (Projectile projectile : projectiles) {
            if (projectile.isActive()) {
                // Draw fireball as red ball with glow
                float x = projectile.getPosition().x;
                float y = projectile.getPosition().y;
                float radius = projectile.getRadius();
                
                // Outer glow (orange) - proportional to radius
                shapeRenderer.setColor(1f, 0.5f, 0f, 0.4f);
                shapeRenderer.circle(x, y, radius * 1.8f, 16);
                
                // Main fireball (red)
                shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
                shapeRenderer.circle(x, y, radius, 16);
                
                // Inner core (bright red/yellow) - proportional to radius
                shapeRenderer.setColor(1f, 0.6f, 0.3f, 0.8f);
                shapeRenderer.circle(x, y, radius * 0.6f, 14);
            }
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Draw a health bar with fancy design
     */
    private void drawHealthBar(float x, float y, float width, float height, float percentage) {
        float borderWidth = 1f;
        float padding = 1f;
        
        // Ensure ShapeRenderer is using proper blending
        shapeRenderer.setAutoShapeType(true);
        
        // Draw shadow (offset slightly down and right for depth)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.4f);
        shapeRenderer.rect(x + 1, y - 1, width, height);
        shapeRenderer.end();
        
        // Draw outer border (dark)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        // Draw inner background (very dark red)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0f, 0f, 1f);
        shapeRenderer.rect(x + borderWidth, y + borderWidth, 
                          width - borderWidth * 2, height - borderWidth * 2);
        shapeRenderer.end();
        
        // Draw health fill with color gradient based on percentage
        if (percentage > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            // Color changes from green -> yellow -> red as health decreases
            if (percentage > 0.6f) {
                // Green (high health) - bright and vibrant
                shapeRenderer.setColor(0.2f, 1.0f, 0.2f, 1f);
            } else if (percentage > 0.3f) {
                // Yellow (medium health)
                shapeRenderer.setColor(1.0f, 1.0f, 0.2f, 1f);
            } else {
                // Red (low health)
                shapeRenderer.setColor(1.0f, 0.2f, 0.2f, 1f);
            }
            
            float fillWidth = (width - borderWidth * 2 - padding * 2) * percentage;
            shapeRenderer.rect(x + borderWidth + padding, y + borderWidth + padding, 
                              fillWidth, height - borderWidth * 2 - padding * 2);
            shapeRenderer.end();
            
            // Draw highlight on top of health bar for shine effect
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 1f, 1f, 0.3f);
            shapeRenderer.rect(x + borderWidth + padding, 
                              y + height - borderWidth - padding - 1, 
                              fillWidth, 1);
            shapeRenderer.end();
        }
        
        // Draw inner border highlight (lighter edge on top for 3D effect)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.8f);
        shapeRenderer.rect(x + borderWidth, y + borderWidth, 
                          width - borderWidth * 2, height - borderWidth * 2);
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
