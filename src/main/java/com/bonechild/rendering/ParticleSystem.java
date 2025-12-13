package com.bonechild.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Manages particle effects for the game
 */
public class ParticleSystem {
    private Array<Particle> particles;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private static final int MAX_PARTICLES = 500;
    
    public ParticleSystem(OrthographicCamera camera) {
        this.camera = camera;
        this.particles = new Array<>(MAX_PARTICLES);
        this.shapeRenderer = new ShapeRenderer();
        
        // Pre-allocate particles
        for (int i = 0; i < MAX_PARTICLES; i++) {
            particles.add(new Particle());
        }
    }
    
    public void update(float delta) {
        for (Particle p : particles) {
            if (p.isActive()) {
                p.update(delta);
            }
        }
    }
    
    public void render() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Particle p : particles) {
            if (p.isActive()) {
                p.render(shapeRenderer);
            }
        }
        shapeRenderer.end();
    }
    
    /**
     * Spawn impact particles (when hitting an enemy)
     */
    public void spawnImpact(float x, float y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(50f, 150f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            p.spawn(x, y, vx, vy, color, MathUtils.random(2f, 4f), 
                   MathUtils.random(0.3f, 0.6f), -200f, Particle.ParticleShape.CIRCLE);
        }
    }
    
    /**
     * Spawn blood splatter particles
     */
    public void spawnBlood(float x, float y, int count) {
        Color bloodColor = new Color(0.8f, 0.1f, 0.1f, 1f);
        for (int i = 0; i < count; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(80f, 200f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            p.spawn(x, y, vx, vy, bloodColor, MathUtils.random(3f, 6f), 
                   MathUtils.random(0.4f, 0.8f), -300f, Particle.ParticleShape.CIRCLE);
        }
    }
    
    /**
     * Spawn sparks (for critical hits)
     */
    public void spawnSparks(float x, float y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(100f, 250f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            p.spawn(x, y, vx, vy, color, MathUtils.random(1.5f, 3f), 
                   MathUtils.random(0.2f, 0.5f), -150f, Particle.ParticleShape.SPARK);
        }
    }
    
    /**
     * Spawn level up particles
     */
    public void spawnLevelUp(float x, float y) {
        Color goldColor = new Color(1f, 0.9f, 0.2f, 1f);
        for (int i = 0; i < 30; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(80f, 180f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed + 150f; // Bias upward
            
            p.spawn(x, y, vx, vy, goldColor, MathUtils.random(3f, 6f), 
                   MathUtils.random(0.8f, 1.2f), -100f, Particle.ParticleShape.SQUARE);
        }
    }
    
    /**
     * Spawn dash trail particles
     */
    public void spawnDashTrail(float x, float y, boolean facingRight) {
        Color trailColor = new Color(0.3f, 0.9f, 1f, 0.8f);
        for (int i = 0; i < 3; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float vx = facingRight ? -100f : 100f;
            float vy = MathUtils.random(-30f, 30f);
            
            p.spawn(x + MathUtils.random(-10f, 10f), y + MathUtils.random(0f, 30f), 
                   vx, vy, trailColor, MathUtils.random(4f, 8f), 
                   0.4f, 0f, Particle.ParticleShape.CIRCLE);
        }
    }
    
    /**
     * Spawn healing particles
     */
    public void spawnHealing(float x, float y, int count) {
        Color healColor = new Color(0.2f, 1f, 0.3f, 1f);
        for (int i = 0; i < count; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float vx = MathUtils.random(-40f, 40f);
            float vy = MathUtils.random(50f, 150f); // Float upward
            
            p.spawn(x + MathUtils.random(-15f, 15f), y, vx, vy, healColor, 
                   MathUtils.random(3f, 5f), MathUtils.random(0.5f, 1f), 
                   -50f, Particle.ParticleShape.CIRCLE);
        }
    }
    
    /**
     * Get an inactive particle from the pool
     */
    private Particle getInactiveParticle() {
        for (Particle p : particles) {
            if (!p.isActive()) {
                return p;
            }
        }
        return null;
    }
    
    public void dispose() {
        shapeRenderer.dispose();
    }
}
