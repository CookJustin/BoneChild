package com.bonechild.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.bonechild.world.Player;

/**
 * Handles player input for movement and actions
 */
public class PlayerInput {
    private Player player;
    private boolean wasSpacePressed = false;
    
    public PlayerInput(Player player) {
        this.player = player;
    }
    
    /**
     * Process input and update player velocity
     */
    public void update() {
        if (player == null || player.isDead()) {
            return;
        }
        
        Vector2 movement = new Vector2(0, 0);
        
        // WASD movement
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            movement.y += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            movement.y -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            movement.x -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            movement.x += 1;
        }
        
        // Normalize diagonal movement
        if (movement.len() > 0) {
            movement.nor();
            movement.scl(player.getSpeed());
        }
        
        player.setVelocity(movement.x, movement.y);
        
        // Attack on space bar - handle press and release
        boolean spaceCurrentlyPressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        
        if (spaceCurrentlyPressed && !wasSpacePressed) {
            // Space was just pressed
            player.attack();
            Gdx.app.log("PlayerInput", "Attack triggered!");
        } else if (!spaceCurrentlyPressed && wasSpacePressed) {
            // Space was just released
            player.stopAttack();
            Gdx.app.log("PlayerInput", "Attack stopped!");
        }
        
        wasSpacePressed = spaceCurrentlyPressed;
    }
    
    /**
     * Check for action inputs (shooting, abilities, etc.)
     */
    public boolean isActionPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.SPACE) || 
               Gdx.input.isButtonPressed(Input.Buttons.LEFT);
    }
    
    /**
     * Check for pause/menu input
     */
    public boolean isPausePressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
               Gdx.input.isKeyJustPressed(Input.Keys.P);
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
}
