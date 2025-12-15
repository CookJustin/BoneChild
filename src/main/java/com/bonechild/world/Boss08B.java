package com.bonechild.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.bonechild.rendering.Animation;
import com.bonechild.rendering.Assets;

/**
 * Boss08_B - Epic animated boss with multiple attack patterns
 * Uses Boss08_B sprite sheet with:
 * - Row 1: Idle animation (6 frames)
 * - Row 2: Walking animation (6 frames)
 * - Row 3, 4, 5: Attack animations (6 frames each)
 * - Row 7-8: Death animation (12 frames total)
 */
public class Boss08B extends Mob {
    private Animation idleAnimation;
    private Animation walkAnimation;
    private Animation attack1Animation;
    private Animation attack2Animation;
    private Animation attack3Animation;
    private Animation deathAnimation;
    
    private BossState currentState;
    private float stateTimer;
    private float attackCooldown;
    private static final float ATTACK_COOLDOWN_TIME = 3.0f;
    
    private boolean deathAnimationComplete;
    private float deathAnimationTimer;
    
    private Player player; // Store player reference for Boss08B
    
    private enum BossState {
        IDLE, WALKING, ATTACK1, ATTACK2, ATTACK3, DYING
    }
    
    public Boss08B(float x, float y, Player player, Assets assets) {
        super(x, y, player);
        
        this.player = player; // Store player reference
        
        // Boss stats - much stronger than regular mobs!
        this.maxHealth = 500f;
        this.currentHealth = 500f;
        this.speed = 40f; // Slower but menacing
        
        // Large hitbox for boss
        this.hitboxWidth = 80f;
        this.hitboxHeight = 80f;
        this.hitboxOffsetX = 60f;
        this.hitboxOffsetY = 20f;
        
        // Large sprite size
        this.width = 200f;
        this.height = 200f;
        
        this.currentState = BossState.WALKING;
        this.stateTimer = 0f;
        this.attackCooldown = ATTACK_COOLDOWN_TIME;
        this.deathAnimationComplete = false;
        this.deathAnimationTimer = 0f;
        
        // Load all animations from Boss08_B sprite sheet
        if (assets != null) {
            this.idleAnimation = assets.createBoss08BIdleAnimation();
            this.walkAnimation = assets.createBoss08BWalkAnimation();
            this.attack1Animation = assets.createBoss08BAttack1Animation();
            this.attack2Animation = assets.createBoss08BAttack2Animation();
            this.attack3Animation = assets.createBoss08BAttack3Animation();
            this.deathAnimation = assets.createBoss08BDeathAnimation();
        }
    }
    
    @Override
    public void update(float delta) {
        if (isDead()) {
            // Update death animation
            if (deathAnimation != null && !deathAnimationComplete) {
                deathAnimation.update(delta);
                deathAnimationTimer += delta;
                
                // Death animation is 12 frames at 0.1s per frame = 1.2 seconds
                if (deathAnimationTimer >= 1.2f) {
                    deathAnimationComplete = true;
                }
            }
            return;
        }
        
        stateTimer += delta;
        attackCooldown -= delta;
        
        // Get distance to player
        float distanceToPlayer = position.dst(player.getPosition());
        
        // Boss AI state machine
        switch (currentState) {
            case IDLE:
                if (stateTimer >= 1.0f) {
                    currentState = BossState.WALKING;
                    stateTimer = 0f;
                }
                break;
                
            case WALKING:
                // Move towards player
                if (distanceToPlayer > 100f) {
                    Vector2 direction = new Vector2(player.getPosition()).sub(position).nor();
                    position.x += direction.x * speed * delta;
                    position.y += direction.y * speed * delta;
                } else {
                    // Close enough to attack
                    if (attackCooldown <= 0f) {
                        // Randomly choose an attack
                        int attackChoice = (int)(Math.random() * 3);
                        switch (attackChoice) {
                            case 0:
                                currentState = BossState.ATTACK1;
                                break;
                            case 1:
                                currentState = BossState.ATTACK2;
                                break;
                            case 2:
                                currentState = BossState.ATTACK3;
                                break;
                        }
                        stateTimer = 0f;
                        attackCooldown = ATTACK_COOLDOWN_TIME;
                    }
                }
                break;
                
            case ATTACK1:
            case ATTACK2:
            case ATTACK3:
                // Attack animations last about 0.6 seconds (6 frames * 0.1s)
                if (stateTimer >= 0.6f) {
                    currentState = BossState.WALKING;
                    stateTimer = 0f;
                }
                break;
        }
        
        // Update the appropriate animation
        Animation currentAnimation = getCurrentAnimation();
        if (currentAnimation != null) {
            currentAnimation.update(delta);
        }
    }
    
    private Animation getCurrentAnimation() {
        switch (currentState) {
            case IDLE:
                return idleAnimation;
            case WALKING:
                return walkAnimation;
            case ATTACK1:
                return attack1Animation;
            case ATTACK2:
                return attack2Animation;
            case ATTACK3:
                return attack3Animation;
            case DYING:
                return deathAnimation;
            default:
                return walkAnimation;
        }
    }
    
    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        
        // Trigger death state when health reaches 0
        if (isDead() && currentState != BossState.DYING) {
            currentState = BossState.DYING;
            stateTimer = 0f;
            deathAnimationTimer = 0f;
            if (deathAnimation != null) {
                deathAnimation.reset();
            }
        }
    }
    
    public void render(SpriteBatch batch, float delta) {
        Animation currentAnimation = getCurrentAnimation();
        if (currentAnimation == null) return;
        
        TextureRegion frame = currentAnimation.getCurrentFrame();
        if (frame == null) return;
        
        // Flip sprite to face player
        boolean shouldFaceLeft = player.getPosition().x < position.x;
        if (shouldFaceLeft && !frame.isFlipX()) {
            frame.flip(true, false);
        } else if (!shouldFaceLeft && frame.isFlipX()) {
            frame.flip(true, false);
        }
        
        // Draw the boss sprite
        batch.draw(frame, position.x, position.y, width, height);
    }
    
    public boolean isDeathAnimationComplete() {
        return deathAnimationComplete;
    }
}
