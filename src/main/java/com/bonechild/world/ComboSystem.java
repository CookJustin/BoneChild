package com.bonechild.world;

import com.badlogic.gdx.Gdx;

/**
 * Tracks combo multiplier from rapid kills
 */
public class ComboSystem {
    private int comboCount;
    private float comboTimer;
    private float comboWindow;
    private float comboMultiplier;
    private int maxCombo;
    
    public ComboSystem() {
        this.comboCount = 0;
        this.comboTimer = 0f;
        this.comboWindow = 3f; // 3 seconds to maintain combo
        this.comboMultiplier = 1f;
        this.maxCombo = 0;
    }
    
    /**
     * Add a kill to the combo
     */
    public void addKill() {
        comboCount++;
        comboTimer = comboWindow;
        
        // Update max combo
        if (comboCount > maxCombo) {
            maxCombo = comboCount;
        }
        
        // Calculate multiplier (caps at 3x)
        comboMultiplier = 1f + Math.min(2f, comboCount * 0.1f);
        
        // Log combo milestones
        if (comboCount % 10 == 0) {
            Gdx.app.log("ComboSystem", "🔥 " + comboCount + " HIT COMBO! Multiplier: " + String.format("%.1fx", comboMultiplier));
        }
    }
    
    /**
     * Update combo timer
     */
    public void update(float delta) {
        if (comboCount > 0) {
            comboTimer -= delta;
            
            if (comboTimer <= 0) {
                // Combo expired
                if (comboCount >= 10) {
                    Gdx.app.log("ComboSystem", "💥 Combo ended at " + comboCount + " kills!");
                }
                comboCount = 0;
                comboMultiplier = 1f;
            }
        }
    }
    
    /**
     * Reset combo (on player death)
     */
    public void reset() {
        comboCount = 0;
        comboTimer = 0f;
        comboMultiplier = 1f;
    }
    
    public int getComboCount() { return comboCount; }
    public float getComboMultiplier() { return comboMultiplier; }
    public float getComboTimer() { return comboTimer; }
    public float getComboWindow() { return comboWindow; }
    public int getMaxCombo() { return maxCombo; }
    public boolean isActive() { return comboCount > 0; }
}
