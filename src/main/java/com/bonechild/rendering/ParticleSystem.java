package com.bonechild.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * ENHANCED particle system with MAXIMUM JUICE! 🎨✨
 */
public class ParticleSystem {
    private Array<Particle> particles;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private static final int MAX_PARTICLES = 1000; // MOAR PARTICLES!
    
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
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // Additive blending for GLOW!
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Particle p : particles) {
            if (p.isActive()) {
                p.render(shapeRenderer);
            }
        }
        shapeRenderer.end();
        
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // Reset
    }
    
    /**
     * EPIC impact explosion - ring of particles!
     */
    public void spawnImpact(float x, float y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = (360f / count) * i + MathUtils.random(-10f, 10f);
            float speed = MathUtils.random(80f, 180f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            p.spawn(x, y, vx, vy, color, MathUtils.random(3f, 6f), 
                   MathUtils.random(0.4f, 0.8f), -250f, Particle.ParticleShape.CIRCLE);
        }
        
        // Add extra sparks for juice!
        for (int i = 0; i < count / 2; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(120f, 250f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            Color sparkColor = new Color(color);
            sparkColor.r = Math.min(1f, sparkColor.r + 0.3f);
            sparkColor.g = Math.min(1f, sparkColor.g + 0.3f);
            sparkColor.b = Math.min(1f, sparkColor.b + 0.3f);
            
            p.spawn(x, y, vx, vy, sparkColor, MathUtils.random(1.5f, 3f), 
                   MathUtils.random(0.2f, 0.4f), -150f, Particle.ParticleShape.SPARK);
        }
    }
    
    /**
     * EPIC blood splatter with directional spray!
     */
    public void spawnBlood(float x, float y, int count) {
        Color bloodColor = new Color(0.9f, 0.1f, 0.1f, 1f);
        Color darkBlood = new Color(0.5f, 0.05f, 0.05f, 1f);
        
        for (int i = 0; i < count; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(100f, 250f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            Color color = i % 2 == 0 ? bloodColor : darkBlood;
            
            p.spawn(x, y, vx, vy, color, MathUtils.random(4f, 8f), 
                   MathUtils.random(0.5f, 1.0f), -400f, Particle.ParticleShape.CIRCLE);
        }
    }
    
    /**
     * CRITICAL HIT - massive spark explosion!
     */
    public void spawnCriticalHit(float x, float y) {
        Color goldColor = new Color(1f, 0.9f, 0.2f, 1f);
        Color orangeColor = new Color(1f, 0.5f, 0.1f, 1f);
        
        // Ring of gold particles
        for (int i = 0; i < 20; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = (360f / 20f) * i;
            float speed = MathUtils.random(150f, 300f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            p.spawn(x, y, vx, vy, goldColor, MathUtils.random(4f, 7f), 
                   MathUtils.random(0.6f, 1.0f), -200f, Particle.ParticleShape.SQUARE);
        }
        
        // Orange sparks
        for (int i = 0; i < 30; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(200f, 400f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            p.spawn(x, y, vx, vy, orangeColor, MathUtils.random(2f, 4f), 
                   MathUtils.random(0.3f, 0.6f), -100f, Particle.ParticleShape.SPARK);
        }
    }
    
    /**
     * Regular sparks
     */
    public void spawnSparks(float x, float y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(150f, 300f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            p.spawn(x, y, vx, vy, color, MathUtils.random(2f, 4f), 
                   MathUtils.random(0.3f, 0.6f), -150f, Particle.ParticleShape.SPARK);
        }
    }
    
    /**
     * EPIC level up explosion!
     */
    public void spawnLevelUp(float x, float y) {
        Color goldColor = new Color(1f, 0.9f, 0.2f, 1f);
        Color whiteColor = new Color(1f, 1f, 1f, 1f);
        
        // Massive burst!
        for (int i = 0; i < 50; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(100f, 250f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed + 200f; // Bias upward!
            
            Color color = i % 3 == 0 ? whiteColor : goldColor;
            
            p.spawn(x, y, vx, vy, color, MathUtils.random(4f, 9f), 
                   MathUtils.random(1.0f, 1.5f), -120f, Particle.ParticleShape.SQUARE);
        }
        
        // Sparkles
        for (int i = 0; i < 40; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(150f, 350f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed + 150f;
            
            p.spawn(x, y, vx, vy, whiteColor, MathUtils.random(2f, 4f), 
                   MathUtils.random(0.5f, 1.0f), -80f, Particle.ParticleShape.SPARK);
        }
    }
    
    /**
     * Epic dash trail with motion blur effect!
     */
    public void spawnDashTrail(float x, float y, boolean facingRight) {
        Color trailColor = new Color(0.3f, 0.9f, 1f, 1f);
        Color brightColor = new Color(0.6f, 1f, 1f, 1f);
        
        // Motion trail
        for (int i = 0; i < 5; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float offsetX = facingRight ? -i * 8f : i * 8f;
            float vx = facingRight ? -150f : 150f;
            float vy = MathUtils.random(-40f, 40f);
            
            Color color = i % 2 == 0 ? trailColor : brightColor;
            
            p.spawn(x + offsetX, y + MathUtils.random(0f, 40f), 
                   vx, vy, color, MathUtils.random(6f, 12f), 
                   0.5f, 0f, Particle.ParticleShape.CIRCLE);
        }
        
        // Sparks
        for (int i = 0; i < 8; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = facingRight ? MathUtils.random(120f, 240f) : MathUtils.random(-60f, 60f);
            float speed = MathUtils.random(80f, 180f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            p.spawn(x, y + MathUtils.random(0f, 30f), vx, vy, brightColor, 
                   MathUtils.random(2f, 4f), 0.4f, -100f, Particle.ParticleShape.SPARK);
        }
    }
    
    /**
     * Healing aura particles!
     */
    public void spawnHealing(float x, float y, int count) {
        Color healColor = new Color(0.3f, 1f, 0.4f, 1f);
        Color glowColor = new Color(0.6f, 1f, 0.7f, 1f);
        
        for (int i = 0; i < count; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float vx = MathUtils.random(-60f, 60f);
            float vy = MathUtils.random(80f, 180f); // Float upward!
            
            Color color = i % 2 == 0 ? healColor : glowColor;
            
            p.spawn(x + MathUtils.random(-20f, 20f), y, vx, vy, color, 
                   MathUtils.random(4f, 7f), MathUtils.random(0.8f, 1.2f), 
                   -60f, Particle.ParticleShape.CIRCLE);
        }
    }
    
    /**
     * Explosion particles for projectile impact!
     */
    public void spawnExplosion(float x, float y, Color color) {
        // Ring explosion
        for (int i = 0; i < 16; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = (360f / 16f) * i;
            float speed = MathUtils.random(120f, 220f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            p.spawn(x, y, vx, vy, color, MathUtils.random(4f, 8f), 
                   MathUtils.random(0.5f, 0.8f), -200f, Particle.ParticleShape.CIRCLE);
        }
        
        // Sparks
        for (int i = 0; i < 12; i++) {
            Particle p = getInactiveParticle();
            if (p == null) break;
            
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(180f, 320f);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;
            
            Color sparkColor = new Color(color);
            sparkColor.r = Math.min(1f, sparkColor.r + 0.4f);
            sparkColor.g = Math.min(1f, sparkColor.g + 0.4f);
            sparkColor.b = Math.min(1f, sparkColor.b + 0.4f);
            
            p.spawn(x, y, vx, vy, sparkColor, MathUtils.random(2f, 4f), 
                   MathUtils.random(0.3f, 0.5f), -100f, Particle.ParticleShape.SPARK);
        }
    }
    
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
