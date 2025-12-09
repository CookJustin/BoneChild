package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.bonechild.rendering.Assets;

/**
 * Inventory/upgrade UI
 * TODO: Implement upgrade selection when player levels up
 */
public class InventoryUI {
    private Stage stage;
    private Skin skin;
    private Table table;
    private boolean visible;
    
    public InventoryUI(Assets assets) {
        this.stage = new Stage(new ScreenViewport());
        this.visible = false;
        
        // Create skin
        skin = new Skin();
        skin.add("default", assets.getFont());
        
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = assets.getFont();
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);
        
        setupUI();
    }
    
    private void setupUI() {
        table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);
        
        Label titleLabel = new Label("Inventory / Upgrades", skin);
        titleLabel.setColor(Color.GOLD);
        table.add(titleLabel).padBottom(20);
        table.row();
        
        Label placeholderLabel = new Label("(Coming Soon)", skin);
        placeholderLabel.setColor(Color.GRAY);
        table.add(placeholderLabel);
        
        table.setVisible(false);
    }
    
    /**
     * Toggle inventory visibility
     */
    public void toggle() {
        visible = !visible;
        table.setVisible(visible);
        Gdx.app.log("InventoryUI", "Inventory " + (visible ? "shown" : "hidden"));
    }
    
    /**
     * Show inventory
     */
    public void show() {
        visible = true;
        table.setVisible(true);
    }
    
    /**
     * Hide inventory
     */
    public void hide() {
        visible = false;
        table.setVisible(false);
    }
    
    /**
     * Update inventory
     */
    public void update(float delta) {
        if (visible) {
            stage.act(delta);
        }
    }
    
    /**
     * Render inventory
     */
    public void render() {
        if (visible) {
            stage.draw();
        }
    }
    
    /**
     * Resize inventory UI
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    /**
     * Dispose resources
     */
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
    
    public boolean isVisible() { return visible; }
    public Stage getStage() { return stage; }
}
