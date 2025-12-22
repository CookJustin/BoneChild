package com.bonechild.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.bonechild.monsters.api.MobEntity;
import com.bonechild.playablecharacters.Player;
import com.bonechild.playablecharacters.Pickup;
import com.bonechild.playablecharacters.GhostSprite;
import com.bonechild.world.TileMap;
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
    private CameraShake cameraShake;
    private Array<DamageNumber> damageNumbers;
    private BitmapFont damageFont;
    private ParticleSystem particleSystem; // NEW: Particle system
    private ScreenEffects screenEffects; // EPIC: Screen effects for maximum juice!
    
    // Separate animation instances for player (not shared with mobs)
    private Animation playerIdleAnimation;
    private Animation playerWalkAnimation;
    private Animation playerHurtAnimation;
    private Animation playerDeathAnimation;
    
    public Renderer(OrthographicCamera camera, Assets assets) {
        this.camera = camera;
        this.assets = assets;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.deltaTime = 0;
        this.cameraShake = new CameraShake();
        this.damageNumbers = new Array<>();
        this.particleSystem = new ParticleSystem(camera); // Fixed: Pass camera to constructor
        this.screenEffects = new ScreenEffects(camera); // EPIC: Initialize screen effects!
        
        // Create damage number font with proper scaling for world coordinates
        this.damageFont = new BitmapFont();
        this.damageFont.getData().setScale(0.5f); // Smaller scale for world coordinates
        
        var registry = assets.getRegistry();

        // Create tile map when tileset is loaded
        if (registry.hasTexture("tileset")) {
            this.tileMap = new TileMap(registry.getTexture("tileset"), 16); // Changed from 32 to 16 for Dungeon_Tileset
        }
        
        // Create separate player animation instances using registry
        playerIdleAnimation = registry.getAnimation("player_idle");
        playerWalkAnimation = registry.getAnimation("player_walk");
        playerHurtAnimation = registry.getAnimation("player_hurt");
        playerDeathAnimation = registry.getAnimation("player_death");
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
            
            float spriteScale = 2.0f; // Match player sprite scale (2x instead of 3x)
            float ghostWidth = 64 * spriteScale;
            float ghostHeight = 64 * spriteScale;
            
            for (var ghost : ghostTrail) {
                // Set ghost opacity
                batch.setColor(1f, 1f, 1f, ghost.getOpacity());
                
                // Flip sprite if facing left
                boolean needsFlip = !ghost.isFacingRight() && !frame.isFlipX();
                boolean needsUnflip = ghost.isFacingRight() && frame.isFlipX();
                
                if (needsFlip || needsUnflip) {
                    frame.flip(true, false);
                }
                
                // Draw ghost sprite - centered on ghost position (which is 64x64 hitbox)
                float ghostX = ghost.getX() - (ghostWidth - 64) / 2;
                float ghostY = ghost.getY() - (ghostHeight - 64) / 2;
                
                batch.draw(
                    frame,
                    ghostX,
                    ghostY,
                    ghostWidth,
                    ghostHeight
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
        
        // The 48x48 PNG sprite scaled to 144x144 for visibility
        // IMPORTANT: The character art appears to be at the BOTTOM of the 48x48 PNG
        // So we need to align the bottom of the scaled sprite with the bottom of the entity
        float spriteScale = 3.0f;
        float spriteWidth = 48 * spriteScale;   // 144 pixels
        float spriteHeight = 48 * spriteScale;  // 144 pixels
        
        // Draw sprite with bottom-left corner at entity position
        // This way the character art (at bottom of PNG) aligns with entity position
        float spriteX = player.getPosition().x + (player.getWidth() - spriteWidth) / 2; // Center horizontally
        float spriteY = player.getPosition().y; // Align bottom edge with entity bottom
        
        batch.draw(
            frame,
            spriteX,
            spriteY,
            spriteWidth,
            spriteHeight
        );
        
        // Reset batch color to default white to prevent color tinting issues
        batch.setColor(Color.WHITE);
        
        batch.end();
        
        // Draw health bar above player (unless dead)
        if (!player.isDead()) {
            // Position health bar above the player's actual hitbox (not entity bounds)
            float healthBarCenterX = player.getPosition().x + player.getHitboxOffsetX() + player.getHitboxWidth() / 2;
            float hitboxTop = player.getPosition().y + player.getHitboxOffsetY() + player.getHitboxHeight();
            float barWidth = 64;
            float barHeight = 5;
            
            drawHealthBar(
                healthBarCenterX - barWidth / 2,  // Center bar over hitbox
                hitboxTop + 5,                  // Position above the hitbox
                barWidth,
                barHeight,
                player.getHealthPercentage()
            );
        }
    }
    
    /**
     * Render all mobs - completely generic, works with any mob type
     */
    public void renderMobs(Array<MobEntity> mobs) {
        if (mobs == null || mobs.size == 0) return;
        
        batch.begin();
        
        // Each mob renders itself - no specific type checking!
        for (MobEntity mob : mobs) {
            if (mob.isActive()) {
                // Cast to concrete Mob implementation which has render method
                if (mob instanceof com.bonechild.monsters.impl.Goblin) {
                    ((com.bonechild.monsters.impl.Goblin) mob).render(batch);
                } else if (mob instanceof com.bonechild.monsters.impl.Boss08B) {
                    ((com.bonechild.monsters.impl.Boss08B) mob).render(batch);
                }
                // Add more mob types here as you create them
            }
        }
        
        batch.end();
        
        // Draw health bars for all alive mobs - completely generic!
        for (MobEntity mob : mobs) {
            if (mob.isActive() && !mob.isDead()) {
                // Boss gets larger health bar (uses isBoss() from interface)
                float barWidth = mob.isBoss() ? 100f : 60f;
                float barHeight = mob.isBoss() ? 8f : 5f;

                // Calculate position - centered above hitbox top
                float hitboxCenterX = mob.getX() + mob.getHitboxOffsetX() + (mob.getHitboxWidth() / 2);
                float hitboxTop = mob.getY() + mob.getHitboxOffsetY() + mob.getHitboxHeight();

                float barX = hitboxCenterX - (barWidth / 2);
                float barY = hitboxTop + 8;

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
    
    // Explosions removed - use ParticleSystem.spawnExplosion() instead

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
        // Update camera shake effect
        cameraShake.update(camera, deltaTime);
        
        // Update damage numbers
        for (int i = damageNumbers.size - 1; i >= 0; i--) {
            DamageNumber dn = damageNumbers.get(i);
            dn.update(deltaTime);
            if (!dn.isActive()) {
                damageNumbers.removeIndex(i);
            }
        }
        
        // NEW: Update particle system
        particleSystem.update(deltaTime);
        
        // EPIC: Update screen effects!
        screenEffects.update(deltaTime);
        
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }
    
    /**
     * Render particles, damage numbers, and screen effects (call after entities)
     */
    public void renderEffects() {
        // NEW: Render particle system
        particleSystem.render();

        // Render damage numbers
        if (damageNumbers.size > 0) {
            batch.begin();
            for (DamageNumber dn : damageNumbers) {
                if (dn.isActive()) {
                    dn.render(batch, damageFont);
                }
            }
            batch.end();
        }
        
        // EPIC: Render screen effects last (on top of everything!)
        screenEffects.render();
    }
    
    /**
     * Handle window resize - updates projection matrices
     */
    public void resize(int width, int height) {
        // In case viewport changes camera parameters, ensure matrices are refreshed
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    /**
     * Trigger a camera shake effect
     */
    public void shake(float intensity, float duration) {
        cameraShake.shake(intensity, duration);
    }
    
    /**
     * Get the camera shake instance
     */
    public CameraShake getCameraShake() {
        return cameraShake;
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
        if (damageFont != null) {
            damageFont.dispose();
        }
    }
    
    public SpriteBatch getBatch() { return batch; }
    public ShapeRenderer getShapeRenderer() { return shapeRenderer; }
    
    /**
     * Spawn damage number at location
     */
    public void spawnDamageNumber(float x, float y, float damage, boolean isCritical) {
        damageNumbers.add(new DamageNumber(x, y, damage, isCritical));
    }
    
    // NEW: Particle effect methods
    
    /**
     * Spawn blood particles when enemy is hit
     */
    public void spawnBloodParticles(float x, float y, int count) {
        particleSystem.spawnBlood(x, y, count); // Fixed: Use spawnBlood
    }
    
    /**
     * Spawn hit spark particles on impact
     */
    public void spawnHitSparks(float x, float y, int count) {
        particleSystem.spawnSparks(x, y, Color.YELLOW, count); // Fixed: Use spawnSparks with color
    }
    
    /**
     * Spawn celebration particles when player levels up
     */
    public void spawnLevelUpParticles(float x, float y) {
        particleSystem.spawnLevelUp(x, y); // Fixed: Use spawnLevelUp
    }
    
    /**
     * Spawn celebration particles (alias for level up)
     */
    public void spawnCelebrationParticles(float x, float y) {
        particleSystem.spawnLevelUp(x, y);
    }
    
    /**
     * Get particle system for direct access
     */
    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }
    
    /**
     * EPIC: Get screen effects for maximum juice!
     */
    public ScreenEffects getScreenEffects() {
        return screenEffects;
    }
    
    /**
     * DEBUG: Render hitboxes for debugging
     */
    public void renderHitboxes(Player player, Array<MobEntity> mobs) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        // Draw player hitbox in green
        if (player != null && !player.isDead()) {
            shapeRenderer.setColor(0f, 1f, 0f, 1f);
            float hitboxX = player.getPosition().x + player.getHitboxOffsetX();
            float hitboxY = player.getPosition().y + player.getHitboxOffsetY();
            shapeRenderer.rect(hitboxX, hitboxY, player.getHitboxWidth(), player.getHitboxHeight());
            
            // Draw entity bounds in blue
            shapeRenderer.setColor(0f, 0f, 1f, 1f);
            shapeRenderer.rect(player.getPosition().x, player.getPosition().y, player.getWidth(), player.getHeight());
            
            // Draw a crosshair at the hitbox center (where mobs should target)
            float centerX = hitboxX + player.getHitboxWidth() / 2;
            float centerY = hitboxY + player.getHitboxHeight() / 2;
            shapeRenderer.setColor(1f, 0f, 0f, 1f);
            shapeRenderer.line(centerX - 5, centerY, centerX + 5, centerY);
            shapeRenderer.line(centerX, centerY - 5, centerX, centerY + 5);
        }
        
        // Draw mob hitboxes in red
        if (mobs != null) {
            for (MobEntity mob : mobs) {
                if (mob.isActive() && !mob.isDead()) {
                    shapeRenderer.setColor(1f, 0f, 0f, 1f);
                    float hitboxX = mob.getPosition().x + mob.getHitboxOffsetX();
                    float hitboxY = mob.getPosition().y + mob.getHitboxOffsetY();
                    shapeRenderer.rect(hitboxX, hitboxY, mob.getHitboxWidth(), mob.getHitboxHeight());
                    
                    // Draw entity bounds in yellow
                    shapeRenderer.setColor(1f, 1f, 0f, 1f);
                    shapeRenderer.rect(mob.getPosition().x, mob.getPosition().y, mob.getWidth(), mob.getHeight());
                }
            }
        }
        
        shapeRenderer.end();
    }
}
