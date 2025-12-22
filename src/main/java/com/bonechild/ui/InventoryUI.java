package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.bonechild.rendering.Assets;
import com.bonechild.world.Player;

/**
 * Minimal inventory UI stub to satisfy BoneChildGame and provide a hook
 * for future item/equipment visualization. It behaves as a simple overlay
 * that can be shown/hidden, resized, and disposed.
 */
public class InventoryUI {
    private final Assets assets;
    private final Player player;
    private final Stage stage;
    private boolean visible = false;

    public InventoryUI(Assets assets, Player player) {
        this.assets = assets;
        this.player = player;
        this.stage = new Stage(new ScreenViewport());
        // In the future, add actual inventory widgets/actors to the stage.
    }

    public boolean isVisible() {
        return visible;
    }

    public void show() {
        visible = true;
        Gdx.app.log("InventoryUI", "Inventory opened");
    }

    public void hide() {
        visible = false;
        Gdx.app.log("InventoryUI", "Inventory closed");
    }

    /**
     * Update logic for the inventory; call each frame while visible if needed.
     */
    public void update(float delta) {
        if (!visible) return;
        stage.act(delta);
    }

    /**
     * Render the inventory overlay. Caller is responsible for beginning/ending
     * the batch if integrating with existing SpriteBatch flows.
     */
    public void render(SpriteBatch batch) {
        if (!visible) return;
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }
}

