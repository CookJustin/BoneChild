package com.bonechild.playablecharacters;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.bonechild.assets.AssetRegistry;
import com.bonechild.rendering.Animation;

/**
 * Projectile fired by the player (or by mobs later).
 *
 * Design: Projectile owns its visual identity via animationId. Renderer is generic.
 *
 * NOTE: Collision is handled by the engine-level CollisionSystem.
 */
public class Projectile {
    private final String animationId;
    private Animation animation; // lazily obtained from AssetRegistry

    private final Vector2 position;
    private final Vector2 velocity;
    private final float radius;
    private final float damage;
    private final float maxDistance;

    private float distanceTraveled;
    private boolean active;
    private final boolean isCritical;

    public Projectile(
            float startX,
            float startY,
            float targetX,
            float targetY,
            float damage,
            boolean isCritical,
            String animationId
    ) {
        this.position = new Vector2(startX, startY);
        this.radius = 5f;
        this.damage = damage;
        this.maxDistance = 1000f;
        this.distanceTraveled = 0f;
        this.active = true;
        this.isCritical = isCritical;
        this.animationId = animationId;

        // Calculate direction to target
        float dx = targetX - startX;
        float dy = targetY - startY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Normalize and apply speed
        float speed = 300f; // pixels/second
        if (distance > 0f) {
            this.velocity = new Vector2(dx / distance * speed, dy / distance * speed);
        } else {
            this.velocity = new Vector2(0f, 0f);
        }
    }

    /**
     * Update projectile position
     */
    public void update(float delta) {
        if (!active) return;

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        distanceTraveled += velocity.len() * delta;
        if (distanceTraveled >= maxDistance) {
            active = false;
        }
    }

    /**
     * Render this projectile using its animation id.
     * Renderer stays generic and does not hardcode fireball frames.
     */
    public void render(SpriteBatch batch, AssetRegistry registry, float delta) {
        if (!active) return;
        if (registry == null || animationId == null) return;

        if (animation == null) {
            animation = registry.getAnimation(animationId);
        }
        if (animation == null) return;

        animation.update(delta);
        var frame = animation.getCurrentFrame();
        if (frame == null) return;

        float x = position.x;
        float y = position.y;
        float size = radius * 8f; // visual size

        float angle = (float) Math.toDegrees(Math.atan2(velocity.y, velocity.x));

        batch.draw(
                frame,
                x - size / 2f,
                y - size / 2f,
                size / 2f,
                size / 2f,
                size,
                size,
                1f,
                1f,
                angle
        );
    }

    public void deactivate() {
        active = false;
    }

    // Getters
    public Vector2 getPosition() { return position; }
    public Vector2 getVelocity() { return velocity; }
    public float getRadius() { return radius; }
    public float getDamage() { return damage; }
    public boolean isActive() { return active; }
    public boolean isCritical() { return isCritical; }
    public String getAnimationId() { return animationId; }
}
