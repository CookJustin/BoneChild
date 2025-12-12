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
    
    // Keybinds
    private int keyMoveUp;
    private int keyMoveDown;
    private int keyMoveLeft;
    private int keyMoveRight;
    private int keyDodge;
    
    public PlayerInput(Player player) {
        this.player = player;
        // Default keybinds
        this.keyMoveUp = Input.Keys.W;
        this.keyMoveDown = Input.Keys.S;
        this.keyMoveLeft = Input.Keys.A;
        this.keyMoveRight = Input.Keys.D;
        this.keyDodge = Input.Keys.SPACE;
    }
    
    /**
     * Process input and update player velocity
     */
    public void update() {
        if (player == null || player.isDead()) {
            return;
        }
        
        // Check for dodge input (don't allow if already dodging)
        if (Gdx.input.isKeyJustPressed(keyDodge) && !player.isDodging()) {
            // Get current movement direction for dodge
            float dirX = 0, dirY = 0;
            if (Gdx.input.isKeyPressed(keyMoveUp) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
                dirY += 1;
            }
            if (Gdx.input.isKeyPressed(keyMoveDown) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                dirY -= 1;
            }
            if (Gdx.input.isKeyPressed(keyMoveLeft) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                dirX -= 1;
            }
            if (Gdx.input.isKeyPressed(keyMoveRight) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                dirX += 1;
            }
            
            // Perform dodge in movement direction (or facing direction if not moving)
            player.dodge(dirX, dirY);
        }
        
        // Don't allow manual movement during dodge
        if (player.isDodging()) {
            return;
        }
        
        Vector2 movement = new Vector2(0, 0);
        
        // WASD movement (using keybinds)
        if (Gdx.input.isKeyPressed(keyMoveUp) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            movement.y += 1;
        }
        if (Gdx.input.isKeyPressed(keyMoveDown) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            movement.y -= 1;
        }
        if (Gdx.input.isKeyPressed(keyMoveLeft) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            movement.x -= 1;
        }
        if (Gdx.input.isKeyPressed(keyMoveRight) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            movement.x += 1;
        }
        
        // Normalize diagonal movement
        if (movement.len() > 0) {
            movement.nor();
            movement.scl(player.getSpeed());
        }
        
        player.setVelocity(movement.x, movement.y);
    }
    
    /**
     * Check for action inputs (shooting, abilities, etc.)
     */
    public boolean isActionPressed() {
        return Gdx.input.isButtonPressed(Input.Buttons.LEFT);
    }
    
    /**
     * Check for pause/menu input
     */
    public boolean isPausePressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
               Gdx.input.isKeyJustPressed(Input.Keys.P);
    }
    
    /**
     * Update keybinds from settings
     */
    public void setKeybinds(int[] keybinds) {
        if (keybinds == null || keybinds.length < 5) {
            return;
        }
        this.keyMoveUp = keybinds[0];
        this.keyMoveDown = keybinds[1];
        this.keyMoveLeft = keybinds[2];
        this.keyMoveRight = keybinds[3];
        this.keyDodge = Input.Keys.SPACE; // Dodge is always SPACE for now
    }
    
    public int getDodgeKey() {
        return keyDodge;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
}
