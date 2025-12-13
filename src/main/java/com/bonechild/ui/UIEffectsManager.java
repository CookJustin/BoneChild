package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class UIEffectsManager {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    
    // Screen shake
    private float shakeTimer = 0f;
    private float shakeIntensity = 0f;
    private Vector2 shakeOffset = new Vector2();
    
    // Screen flash
    private float flashTimer = 0f;
    private float flashAlpha = 0f;
    private Color flashColor = new Color(Color.WHITE);
    
    // Kill streak
    private int killStreak = 0;
    private float streakTimer = 0f;
    private static final float STREAK_TIMEOUT = 3f;
    private float streakDisplayTimer = 0f;
    private String streakText = "";
    
    // Wave banner
    private float waveBannerTimer = 0f;
    private String waveBannerText = "";
    private float bannerScale = 0f;
    
    // Combo counter
    private int comboCount = 0;
    private float comboTimer = 0f;
    private static final float COMBO_TIMEOUT = 2f;
    private float comboDisplayAlpha = 0f;
    
    // Floating notifications
    private Array<FloatingNotification> notifications = new Array<>();
    
    // Vignette effect for low health
    private float vignetteIntensity = 0f;
    private float vignettePulse = 0f;
    
    // UI particles
    private Array<UIParticle> particles = new Array<>();
    
    public UIEffectsManager(OrthographicCamera camera) {
        this.camera = camera;
        this.shapeRenderer = new ShapeRenderer();
    }
    
    // Screen shake effect
    public void addScreenShake(float intensity, float duration) {
        shakeIntensity = Math.max(shakeIntensity, intensity);
        shakeTimer = Math.max(shakeTimer, duration);
    }
    
    // Screen flash effect
    public void addScreenFlash(Color color, float duration) {
        flashColor.set(color);
        flashTimer = duration;
        flashAlpha = 1f;
    }
    
    // Kill streak tracking
    public void addKill() {
        killStreak++;
        streakTimer = STREAK_TIMEOUT;
        streakDisplayTimer = 2f;
        
        // Update streak text
        if (killStreak >= 20) {
            streakText = "LEGENDARY!!!";
            addScreenFlash(Color.PURPLE, 0.3f);
            addScreenShake(15f, 0.4f);
        } else if (killStreak >= 15) {
            streakText = "GODLIKE!!";
            addScreenFlash(Color.GOLD, 0.25f);
            addScreenShake(12f, 0.35f);
        } else if (killStreak >= 10) {
            streakText = "RAMPAGE!";
            addScreenFlash(Color.ORANGE, 0.2f);
            addScreenShake(10f, 0.3f);
        } else if (killStreak >= 5) {
            streakText = "KILLING SPREE!";
            addScreenFlash(Color.RED, 0.15f);
            addScreenShake(7f, 0.25f);
        } else if (killStreak >= 3) {
            streakText = "DOUBLE KILL!";
        }
        
        // Add UI particles for big streaks
        if (killStreak >= 5) {
            spawnStreakParticles();
        }
    }
    
    // Combo system
    public void addCombo() {
        comboCount++;
        comboTimer = COMBO_TIMEOUT;
        comboDisplayAlpha = 1f;
    }
    
    public void resetCombo() {
        comboCount = 0;
        comboTimer = 0f;
    }
    
    // Wave banner
    public void showWaveBanner(int waveNumber) {
        waveBannerText = "WAVE " + waveNumber;
        waveBannerTimer = 3f;
        bannerScale = 0f;
        addScreenFlash(new Color(0.2f, 0.5f, 1f, 1f), 0.5f);
        addScreenShake(20f, 0.5f);
    }
    
    // Floating notifications
    public void addNotification(String text, Color color, float x, float y) {
        notifications.add(new FloatingNotification(text, color, x, y));
    }
    
    // Spawn UI particles
    private void spawnStreakParticles() {
        for (int i = 0; i < 20; i++) {
            float angle = MathUtils.random(360f);
            float speed = MathUtils.random(100f, 300f);
            Color color = new Color(
                MathUtils.random(0.5f, 1f),
                MathUtils.random(0f, 0.5f),
                MathUtils.random(0f, 0.5f),
                1f
            );
            particles.add(new UIParticle(640, 360, angle, speed, color));
        }
    }
    
    public void update(float delta, float healthPercent) {
        // Update screen shake
        if (shakeTimer > 0f) {
            shakeTimer -= delta;
            float intensity = shakeIntensity * (shakeTimer / 0.5f);
            shakeOffset.x = MathUtils.random(-intensity, intensity);
            shakeOffset.y = MathUtils.random(-intensity, intensity);
            
            // Apply shake to camera
            camera.position.x += shakeOffset.x;
            camera.position.y += shakeOffset.y;
            camera.update();
            
            if (shakeTimer <= 0f) {
                shakeOffset.set(0, 0);
                shakeIntensity = 0f;
            }
        }
        
        // Update screen flash
        if (flashTimer > 0f) {
            flashTimer -= delta;
            flashAlpha = Math.max(0f, flashTimer / 0.5f);
        }
        
        // Update kill streak
        if (streakTimer > 0f) {
            streakTimer -= delta;
            if (streakTimer <= 0f) {
                killStreak = 0;
                streakText = "";
            }
        }
        if (streakDisplayTimer > 0f) {
            streakDisplayTimer -= delta;
        }
        
        // Update combo
        if (comboTimer > 0f) {
            comboTimer -= delta;
            comboDisplayAlpha = Math.max(0f, comboTimer / COMBO_TIMEOUT);
            if (comboTimer <= 0f) {
                comboCount = 0;
            }
        }
        
        // Update wave banner
        if (waveBannerTimer > 0f) {
            waveBannerTimer -= delta;
            // Elastic in-out animation
            float progress = 1f - (waveBannerTimer / 3f);
            if (progress < 0.5f) {
                bannerScale = easeElasticOut(progress * 2f);
            } else {
                bannerScale = 1f - easeBackIn((progress - 0.5f) * 2f);
            }
        }
        
        // Update vignette based on health
        float targetVignette = healthPercent < 0.3f ? (1f - healthPercent / 0.3f) * 0.7f : 0f;
        vignetteIntensity = MathUtils.lerp(vignetteIntensity, targetVignette, delta * 3f);
        vignettePulse += delta * 5f;
        
        // Update notifications
        for (int i = notifications.size - 1; i >= 0; i--) {
            FloatingNotification notif = notifications.get(i);
            notif.update(delta);
            if (notif.isDead()) {
                notifications.removeIndex(i);
            }
        }
        
        // Update particles
        for (int i = particles.size - 1; i >= 0; i--) {
            UIParticle particle = particles.get(i);
            particle.update(delta);
            if (particle.isDead()) {
                particles.removeIndex(i);
            }
        }
    }
    
    public void render(SpriteBatch batch, BitmapFont font, BitmapFont largeFont) {
        // Render vignette
        if (vignetteIntensity > 0.01f) {
            renderVignette();
        }
        
        // Render screen flash
        if (flashAlpha > 0f) {
            renderFlash();
        }
        
        batch.begin();
        
        // Render wave banner
        if (waveBannerTimer > 0f && bannerScale > 0.01f) {
            renderWaveBanner(batch, largeFont);
        }
        
        // Render kill streak
        if (streakDisplayTimer > 0f && !streakText.isEmpty()) {
            renderKillStreak(batch, largeFont);
        }
        
        // Render combo counter
        if (comboCount > 1 && comboDisplayAlpha > 0f) {
            renderCombo(batch, font);
        }
        
        // Render UI particles
        for (UIParticle particle : particles) {
            particle.render(batch);
        }
        
        // Render notifications
        for (FloatingNotification notif : notifications) {
            notif.render(batch, font);
        }
        
        batch.end();
    }
    
    private void renderVignette() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        float pulse = (float) Math.sin(vignettePulse) * 0.1f + 0.9f;
        float alpha = vignetteIntensity * pulse;
        
        // Draw dark rectangles from edges
        shapeRenderer.setColor(0.3f, 0f, 0f, alpha * 0.3f);
        
        float edgeSize = 200f;
        // Top
        shapeRenderer.rect(0, 720 - edgeSize, 1280, edgeSize);
        // Bottom
        shapeRenderer.rect(0, 0, 1280, edgeSize);
        // Left
        shapeRenderer.rect(0, 0, edgeSize, 720);
        // Right
        shapeRenderer.rect(1280 - edgeSize, 0, edgeSize, 720);
        
        shapeRenderer.end();
    }
    
    private void renderFlash() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(flashColor.r, flashColor.g, flashColor.b, flashAlpha * flashColor.a);
        shapeRenderer.rect(0, 0, 1280, 720);
        shapeRenderer.end();
    }
    
    private void renderWaveBanner(SpriteBatch batch, BitmapFont font) {
        float alpha = waveBannerTimer > 2f ? 1f : waveBannerTimer / 2f;
        
        // Draw background using ShapeRenderer
        float bannerHeight = 150f * bannerScale;
        float bannerY = 360 - bannerHeight / 2f;
        
        batch.end();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f * alpha);
        shapeRenderer.rect(0, bannerY, 1280, bannerHeight);
        shapeRenderer.end();
        batch.begin();
        
        // Draw text
        font.getData().setScale(3f * bannerScale);
        font.setColor(1f, 1f, 1f, alpha);
        
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, waveBannerText);
        font.draw(batch, waveBannerText, 640 - layout.width / 2f, 360 + layout.height / 2f);
        
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
    }
    
    private void renderKillStreak(SpriteBatch batch, BitmapFont font) {
        float alpha = Math.min(1f, streakDisplayTimer / 0.5f);
        float bounce = (float) Math.sin(streakDisplayTimer * 10f) * 5f;
        
        font.getData().setScale(2.5f);
        
        // Color based on streak
        Color textColor = Color.WHITE;
        if (killStreak >= 20) {
            textColor = Color.PURPLE;
        } else if (killStreak >= 15) {
            textColor = Color.GOLD;
        } else if (killStreak >= 10) {
            textColor = Color.ORANGE;
        } else if (killStreak >= 5) {
            textColor = Color.RED;
        }
        
        font.setColor(textColor.r, textColor.g, textColor.b, alpha);
        
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, streakText);
        font.draw(batch, streakText, 640 - layout.width / 2f, 600 + bounce);
        
        // Draw streak count
        font.getData().setScale(1.5f);
        String countText = killStreak + " KILLS";
        layout.setText(font, countText);
        font.draw(batch, countText, 640 - layout.width / 2f, 550 + bounce);
        
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
    }
    
    private void renderCombo(SpriteBatch batch, BitmapFont font) {
        font.getData().setScale(2f);
        font.setColor(1f, 0.8f, 0f, comboDisplayAlpha);
        
        String text = "COMBO x" + comboCount;
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, text);
        font.draw(batch, text, 1280 - layout.width - 20, 650);
        
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
    }
    
    // Easing functions
    private float easeElasticOut(float t) {
        if (t == 0f || t == 1f) return t;
        float p = 0.3f;
        return (float) (Math.pow(2f, -10f * t) * Math.sin((t - p / 4f) * (2f * Math.PI) / p) + 1f);
    }
    
    private float easeBackIn(float t) {
        float s = 1.70158f;
        return t * t * ((s + 1f) * t - s);
    }
    
    public void dispose() {
        shapeRenderer.dispose();
    }
    
    // Inner classes for effects
    private static class FloatingNotification {
        String text;
        Color color;
        float x, y;
        float timer;
        float velocity;
        
        FloatingNotification(String text, Color color, float x, float y) {
            this.text = text;
            this.color = new Color(color);
            this.x = x;
            this.y = y;
            this.timer = 2f;
            this.velocity = 50f;
        }
        
        void update(float delta) {
            timer -= delta;
            y += velocity * delta;
            velocity -= 20f * delta; // Gravity
            color.a = Math.max(0f, timer / 2f);
        }
        
        void render(SpriteBatch batch, BitmapFont font) {
            font.setColor(color);
            font.draw(batch, text, x, y);
            font.setColor(Color.WHITE);
        }
        
        boolean isDead() {
            return timer <= 0f;
        }
    }
    
    private static class UIParticle {
        float x, y;
        float vx, vy;
        Color color;
        float timer;
        float size;
        
        UIParticle(float x, float y, float angle, float speed, Color color) {
            this.x = x;
            this.y = y;
            this.vx = MathUtils.cosDeg(angle) * speed;
            this.vy = MathUtils.sinDeg(angle) * speed;
            this.color = new Color(color);
            this.timer = 1f;
            this.size = MathUtils.random(2f, 6f);
        }
        
        void update(float delta) {
            timer -= delta;
            x += vx * delta;
            y += vy * delta;
            vx *= 0.95f;
            vy *= 0.95f;
            color.a = Math.max(0f, timer);
        }
        
        void render(SpriteBatch batch) {
            batch.setColor(color);
            // Use a small white pixel or draw a colored rectangle
            // For now, just set color (you'd draw a texture here)
            batch.setColor(Color.WHITE);
        }
        
        boolean isDead() {
            return timer <= 0f;
        }
    }
    
    public int getKillStreak() {
        return killStreak;
    }
    
    public int getComboCount() {
        return comboCount;
    }
}
