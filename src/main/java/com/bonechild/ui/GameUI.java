package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.bonechild.rendering.Assets;
import com.bonechild.world.Player;
import com.bonechild.world.WorldManager;

/**
 * Game HUD displaying health, level, wave info, etc.
 */
public class GameUI {
    private Stage stage;
    private Skin skin;
    private ShapeRenderer shapeRenderer;
    
    // UI Labels
    private Label healthLabel;
    private Label levelLabel;
    private Label expLabel;
    private Label waveLabel;
    private Label mobCountLabel;
    
    // References
    private Player player;
    private WorldManager worldManager;
    
    public GameUI(Assets assets, Player player, WorldManager worldManager) {
        this.player = player;
        this.worldManager = worldManager;
        this.stage = new Stage(new ScreenViewport());
        this.shapeRenderer = new ShapeRenderer();
        
        // Create skin for UI
        skin = new Skin();
        skin.add("default", assets.getFont());
        
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = assets.getFont();
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);
        
        setupUI();
    }
    
    private void setupUI() {
        Table table = new Table();
        table.setFillParent(false);
        table.top().left();
        table.setPosition(10, Gdx.graphics.getHeight() - 10);
        stage.addActor(table);
        
        // Health
        healthLabel = new Label("Health: 100/100", skin);
        healthLabel.setColor(Color.GREEN);
        table.add(healthLabel).left().padBottom(5);
        table.row();
        
        // Level
        levelLabel = new Label("Level: 1", skin);
        levelLabel.setColor(Color.CYAN);
        table.add(levelLabel).left().padBottom(5);
        table.row();
        
        // Experience
        expLabel = new Label("Exp: 0/100", skin);
        expLabel.setColor(Color.YELLOW);
        table.add(expLabel).left().padBottom(5);
        table.row();
        
        // Wave info
        waveLabel = new Label("Wave: 0", skin);
        waveLabel.setColor(Color.ORANGE);
        table.add(waveLabel).left().padBottom(5);
        table.row();
        
        // Mob count
        mobCountLabel = new Label("Enemies: 0", skin);
        mobCountLabel.setColor(Color.RED);
        table.add(mobCountLabel).left();
    }
    
    /**
     * Update UI with current game state
     */
    public void update(float delta) {
        if (player == null || worldManager == null) return;
        
        // Update labels
        healthLabel.setText(String.format("Health: %.0f/%.0f", 
            player.getCurrentHealth(), player.getMaxHealth()));
        
        levelLabel.setText("Level: " + player.getLevel());
        
        expLabel.setText(String.format("Exp: %.0f/%.0f", 
            player.getExperience(), player.getExperienceToNextLevel()));
        
        waveLabel.setText("Wave: " + worldManager.getCurrentWave());
        
        mobCountLabel.setText("Enemies: " + worldManager.getMobCount());
        
        // Update health color based on percentage
        float healthPct = player.getHealthPercentage();
        if (healthPct > 0.6f) {
            healthLabel.setColor(Color.GREEN);
        } else if (healthPct > 0.3f) {
            healthLabel.setColor(Color.YELLOW);
        } else {
            healthLabel.setColor(Color.RED);
        }
        
        stage.act(delta);
    }
    
    /**
     * Render the UI
     */
    public void render() {
        stage.draw();
        
        // Draw health bar at bottom of screen
        drawHealthBar();
        drawExpBar();
    }
    
    private void drawHealthBar() {
        float barWidth = 400;
        float barHeight = 20;
        float x = Gdx.graphics.getWidth() / 2f - barWidth / 2f;
        float y = 20;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        
        // Health
        float healthPct = player.getHealthPercentage();
        Color healthColor = healthPct > 0.6f ? Color.GREEN : 
                           healthPct > 0.3f ? Color.YELLOW : Color.RED;
        shapeRenderer.setColor(healthColor);
        shapeRenderer.rect(x, y, barWidth * healthPct, barHeight);
        
        shapeRenderer.end();
    }
    
    private void drawExpBar() {
        float barWidth = 400;
        float barHeight = 10;
        float x = Gdx.graphics.getWidth() / 2f - barWidth / 2f;
        float y = 45;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        
        // Experience
        float expPct = player.getExperiencePercentage();
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(x, y, barWidth * expPct, barHeight);
        
        shapeRenderer.end();
    }
    
    /**
     * Resize the UI
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    /**
     * Dispose UI resources
     */
    public void dispose() {
        stage.dispose();
        skin.dispose();
        shapeRenderer.dispose();
    }
    
    public Stage getStage() { return stage; }
}
