package com.bonechild.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;
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
        
        // Create separate player animation instances using factory methods
        playerIdleAnimation = assets.createPlayerIdleAnimation();
        playerWalkAnimation = assets.createPlayerWalkAnimation();
        playerHurtAnimation = assets.createPlayerHurtAnimation();
        playerDeathAnimation = assets.createPlayerDeathAnimation();
        
        // Create a separate mob animation instance (not shared with player)
        mobWalkAnimation = assets.createWalkAnimation();
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
        
        // Check if animations are loaded - if not, skip rendering
        if (playerIdleAnimation == null || playerWalkAnimation == null || 
            playerHurtAnimation == null || playerDeathAnimation == null) {
            Gdx.app.log("Renderer", "Player animations not loaded yet, skipping render");
            return;
        }
        
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
        
        // Render ghost trail first (behind player)
        var ghostTrail = player.getGhostTrail();
        if (ghostTrail != null && !ghostTrail.isEmpty()) {
            var frame = currentAnimation.getCurrentFrame();
            
            for (var ghost : ghostTrail) {
                // Set ghost opacity
                batch.setColor(1f, 1f, 1f, ghost.getOpacity());
                
                // Flip sprite if facing left
                boolean needsFlip = !ghost.isFacingRight() && !frame.isFlipX();
                boolean needsUnflip = ghost.isFacingRight() && frame.isFlipX();
                
                if (needsFlip || needsUnflip) {
                    frame.flip(true, false);
                }
                
                // Draw ghost sprite
                batch.draw(
                    frame,
                    ghost.getX(),
                    ghost.getY(),
                    32,
                    64
                );
                
                // Flip back if needed
                if (needsFlip || needsUnflip) {
                    frame.flip(true, false);
                }
            }
            
            // Reset color to white for normal player rendering
            batch.setColor(Color.WHITE);
        }
        
        // Apply flashing effect if player is invincible
        if (player.isInvincible()) {
            // Flash every 0.1 seconds (10 times per second)
            float flashSpeed = 10f;
            float alpha = (float)((Math.sin(player.getInvincibilityTimer() * flashSpeed * Math.PI) + 1.0) / 2.0);
            // Oscillate alpha between 0.3 and 1.0 for visibility
            alpha = 0.3f + (alpha * 0.7f);
            batch.setColor(1f, 1f, 1f, alpha);
        }
        
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
        
        // Check if mob animations are loaded - if not, skip rendering
        if (mobWalkAnimation == null) {
            Gdx.app.log("Renderer", "Mob animations not loaded yet, skipping render");
            return;
        }
        
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
                // Use hitbox dimensions for health bar (smaller, more appropriate size)
                float barWidth = mob.getHitboxWidth() * 2; // 2x hitbox width for visibility
                float barHeight = 4;
                
                // Position health bar just above the hitbox (not above the full sprite)
                // Hitbox is at offset (105, 105) with size 30x30
                float hitboxTop = mob.getPosition().y + 105 + mob.getHitboxHeight();
                float barX = mob.getPosition().x + (mob.getWidth() / 2) - (barWidth / 2);
                float barY = hitboxTop + 5; // 5 pixels above the hitbox
                
                drawHealthBar(barX, barY, barWidth, barHeight, mob.getHealthPercentage());
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
        
        // Get the fireball animation
        Animation fireballAnim = assets.getFireballAnimation();
        if (fireballAnim == null) return;
        
        // Update the fireball animation
        fireballAnim.update(deltaTime);
        
        batch.begin();
        
        for (Projectile projectile : projectiles) {
            if (projectile.isActive()) {
                // Get current frame
                var frame = fireballAnim.getCurrentFrame();
                
                float x = projectile.getPosition().x;
                float y = projectile.getPosition().y;
                float size = projectile.getRadius() * 8; // Increased from 4 to 8 for better visibility
                
                // Calculate rotation angle based on velocity direction
                // atan2 returns angle in radians, convert to degrees
                float angle = (float) Math.toDegrees(Math.atan2(
                    projectile.getVelocity().y, 
                    projectile.getVelocity().x
                ));
                
                // Draw fireball sprite centered and rotated to face direction of travel
                batch.draw(
                    frame,
                    x - size / 2,  // X position (centered)
                    y - size / 2,  // Y position (centered)
                    size / 2,      // Origin X (center of sprite for rotation)
                    size / 2,      // Origin Y (center of sprite for rotation)
                    size,          // Width
                    size,          // Height
                    1f,            // Scale X
                    1f,            // Scale Y
                    angle          // Rotation angle in degrees
                );
            }
        }
        
        batch.end();
    }
    
    /**
     * Render all explosions
     */
    public void renderExplosions(Array<com.bonechild.world.Explosion> explosions) {
        if (explosions == null || explosions.size == 0) return;
        
        batch.begin();
        
        for (com.bonechild.world.Explosion explosion : explosions) {
            if (explosion.isActive()) {
                Animation explosionAnim = explosion.getAnimation();
                if (explosionAnim != null) {
                    var frame = explosionAnim.getCurrentFrame();
                    
                    float x = explosion.getPosition().x;
                    float y = explosion.getPosition().y;
                    float width = explosion.getWidth();
                    float height = explosion.getHeight();
                    
                    // Draw explosion sprite
                    batch.draw(frame, x, y, width, height);
                }
            }
        }
        
        batch.end();
    }
    
    /**
     * Draw a health bar using simple colored rectangles (old style)
     */
    private void drawHealthBar(float x, float y, float width, float height, float healthPct) {
        // Use basic colored bars (old style)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background (dark red)
        shapeRenderer.setColor(0.3f, 0.1f, 0.1f, 0.9f);
        shapeRenderer.rect(x, y, width, height);
        
        // Health bar (green to yellow to red gradient based on health)
        float healthWidth = width * healthPct;
        if (healthPct > 0.5f) {
            shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 1f);  // Green
        } else if (healthPct > 0.25f) {
            shapeRenderer.setColor(0.9f, 0.9f, 0.2f, 1f);  // Yellow
        } else {
            shapeRenderer.setColor(0.9f, 0.2f, 0.2f, 1f);  // Red
        }
        shapeRenderer.rect(x, y, healthWidth, height);
        
        // Border
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(x, y, width, height);
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
