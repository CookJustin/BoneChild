package com.bonechild.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.bonechild.input.PlayerInput;
import com.bonechild.rendering.Assets;

/**
 * Settings screen for BoneChild with submenu system
 */
public class SettingsScreen {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final GlyphLayout glyphLayout;
    
    // Screen projection matrix for UI
    private final com.badlogic.gdx.graphics.OrthographicCamera uiCamera;
    
    // Menu states
    private enum MenuState {
        MAIN, VOLUME, KEYBINDS
    }
    
    private MenuState currentState = MenuState.MAIN;
    
    // Main menu buttons
    private Rectangle volumeButton;
    private Rectangle keybindsButton;
    private Rectangle backButton;
    private float buttonWidth = 250f;
    private float buttonHeight = 50f;
    
    // Volume controls
    private Rectangle musicVolumeSlider;
    private Rectangle sfxVolumeSlider;
    private float musicVolume;
    private float sfxVolume;
    private boolean draggingMusicSlider;
    private boolean draggingSFXSlider;
    
    // Keybind controls
    private Rectangle[] keybindButtons;
    private Rectangle[] hotbarKeybindButtons; // Second column for hotbar
    private String[] keybindLabels;
    private String[] hotbarKeybindLabels; // Labels for hotbar
    private int[] currentKeybinds;
    private int[] hotbarKeybinds; // Hotbar keybinds (1-5)
    private int rebindingIndex = -1;
    private boolean rebindingHotbar = false; // Track if rebinding hotbar or movement
    private float rebindWaitTime = 0;
    private float ignoreInputTimer = 0;
    
    private boolean isVisible;
    private SettingsCallback callback;
    private Assets assets;
    
    public interface SettingsCallback {
        void onBack();
    }
    
    public SettingsScreen(Assets assets, SettingsCallback callback, PlayerInput playerInput) {
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = assets.getFont();
        this.glyphLayout = new GlyphLayout();
        this.callback = callback;
        this.assets = assets;
        this.isVisible = false;
        
        // Create UI camera for proper screen-space rendering
        this.uiCamera = new com.badlogic.gdx.graphics.OrthographicCamera();
        this.uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Create title font
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(2.0f);
        this.titleFont.setColor(Color.WHITE);
        this.titleFont.setUseIntegerPositions(false);
        this.titleFont.getRegion().getTexture().setFilter(
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
        );
        
        // Audio setup
        this.musicVolume = assets.getBackgroundMusic() != null ? assets.getBackgroundMusic().getVolume() : 0.5f;
        this.sfxVolume = 0.6f;
        
        // Movement keybind setup (left column)
        this.keybindLabels = new String[]{
            "Move Up", "Move Down", "Move Left", "Move Right", "Stats", "Dodge"
        };
        this.currentKeybinds = new int[]{
            Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.C, Input.Keys.SPACE
        };
        this.keybindButtons = new Rectangle[currentKeybinds.length];
        
        // Hotbar keybind setup (right column)
        this.hotbarKeybindLabels = new String[]{
            "Hotbar Slot 1", "Hotbar Slot 2", "Hotbar Slot 3", "Hotbar Slot 4", "Hotbar Slot 5"
        };
        this.hotbarKeybinds = new int[]{
            Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3, Input.Keys.NUM_4, Input.Keys.NUM_5
        };
        this.hotbarKeybindButtons = new Rectangle[hotbarKeybinds.length];
        
        setupUI();
    }
    
    private void setupUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float centerX = screenWidth / 2f - buttonWidth / 2f;
        
        // Back button (top left)
        backButton = new Rectangle(20, screenHeight - 70, 150f, 40f);
        
        // Main menu buttons
        float mainMenuY = screenHeight / 2f + 50f;
        volumeButton = new Rectangle(centerX, mainMenuY, buttonWidth, buttonHeight);
        keybindsButton = new Rectangle(centerX, mainMenuY - buttonHeight - 20f, buttonWidth, buttonHeight);
        
        // Volume sliders (with more spacing for text above)
        float sliderX = screenWidth / 2f - 150f;
        float sliderY = screenHeight / 2f - 30f;
        musicVolumeSlider = new Rectangle(sliderX, sliderY, 300f, 20f);
        sfxVolumeSlider = new Rectangle(sliderX, sliderY - 80f, 300f, 20f);
        
        // Keybind buttons - TWO COLUMNS
        float leftColumnX = screenWidth / 2f - 320f; // Left column
        float rightColumnX = screenWidth / 2f + 20f; // Right column
        float keybindY = screenHeight / 2f + 80f;
        float buttonWidthSmall = 280f;
        
        // Left column (movement keys)
        for (int i = 0; i < keybindButtons.length; i++) {
            keybindButtons[i] = new Rectangle(leftColumnX, keybindY - (i * 60f), buttonWidthSmall, 45f);
        }
        
        // Right column (hotbar keys)
        for (int i = 0; i < hotbarKeybindButtons.length; i++) {
            hotbarKeybindButtons[i] = new Rectangle(rightColumnX, keybindY - (i * 60f), buttonWidthSmall, 45f);
        }
    }
    
    /**
     * Handle settings input
     */
    public void update(float delta) {
        if (!isVisible) return;
        
        // Decrease ignore input timer
        if (ignoreInputTimer > 0) {
            ignoreInputTimer -= delta;
            return;
        }
        
        // Handle rebinding
        if (rebindingIndex >= 0) {
            rebindWaitTime += delta;
            if (rebindWaitTime > 0.2f) {
                handleRebind();
            }
            return;
        }
        
        // Handle slider dragging
        if (draggingMusicSlider || draggingSFXSlider) {
            if (Gdx.input.isButtonPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
                float mouseX = Gdx.input.getX();
                
                if (draggingMusicSlider) {
                    float sliderProgress = (mouseX - musicVolumeSlider.x) / musicVolumeSlider.width;
                    musicVolume = Math.max(0, Math.min(1, sliderProgress));
                    if (assets.getBackgroundMusic() != null) {
                        assets.getBackgroundMusic().setVolume(musicVolume);
                    }
                }
                
                if (draggingSFXSlider) {
                    float sliderProgress = (mouseX - sfxVolumeSlider.x) / sfxVolumeSlider.width;
                    sfxVolume = Math.max(0, Math.min(1, sliderProgress));
                }
            } else {
                draggingMusicSlider = false;
                draggingSFXSlider = false;
            }
            return;
        }
        
        // Handle mouse clicks
        if (Gdx.input.justTouched()) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
            
            // Check back button
            if (backButton.contains(mouseX, mouseY)) {
                if (currentState == MenuState.MAIN) {
                    if (callback != null) {
                        callback.onBack();
                    }
                    isVisible = false;
                } else {
                    currentState = MenuState.MAIN;
                }
                return;
            }
            
            if (currentState == MenuState.MAIN) {
                if (volumeButton.contains(mouseX, mouseY)) {
                    currentState = MenuState.VOLUME;
                    return;
                }
                if (keybindsButton.contains(mouseX, mouseY)) {
                    currentState = MenuState.KEYBINDS;
                    return;
                }
            } else if (currentState == MenuState.VOLUME) {
                if (musicVolumeSlider.contains(mouseX, mouseY)) {
                    draggingMusicSlider = true;
                    return;
                }
                if (sfxVolumeSlider.contains(mouseX, mouseY)) {
                    draggingSFXSlider = true;
                    return;
                }
            } else if (currentState == MenuState.KEYBINDS) {
                for (int i = 0; i < keybindButtons.length; i++) {
                    if (keybindButtons[i].contains(mouseX, mouseY)) {
                        rebindingIndex = i;
                        rebindingHotbar = false;
                        rebindWaitTime = 0;
                        Gdx.app.log("Settings", "Press a key to rebind: " + keybindLabels[i]);
                        return;
                    }
                }
                for (int i = 0; i < hotbarKeybindButtons.length; i++) {
                    if (hotbarKeybindButtons[i].contains(mouseX, mouseY)) {
                        rebindingIndex = i;
                        rebindingHotbar = true;
                        rebindWaitTime = 0;
                        Gdx.app.log("Settings", "Press a key to rebind: " + hotbarKeybindLabels[i]);
                        return;
                    }
                }
            }
        }
        
        // ESC to go back
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (currentState == MenuState.MAIN) {
                if (callback != null) {
                    callback.onBack();
                }
                isVisible = false;
            } else {
                currentState = MenuState.MAIN;
            }
        }
    }
    
    /**
     * Handle key rebinding
     */
    private void handleRebind() {
        for (int keyCode = 0; keyCode < 256; keyCode++) {
            if (Gdx.input.isKeyPressed(keyCode)) {
                if (keyCode == Input.Keys.ESCAPE) {
                    rebindingIndex = -1;
                    return;
                }
                
                if (rebindWaitTime > 0.3f) {
                    if (rebindingHotbar) {
                        hotbarKeybinds[rebindingIndex] = keyCode;
                        Gdx.app.log("Settings", "Hotbar Keybind updated: " + hotbarKeybindLabels[rebindingIndex] + " -> " + getKeyName(keyCode));
                    } else {
                        currentKeybinds[rebindingIndex] = keyCode;
                        Gdx.app.log("Settings", "Keybind updated: " + keybindLabels[rebindingIndex] + " -> " + getKeyName(keyCode));
                    }
                    rebindingIndex = -1;
                    rebindWaitTime = 0;
                    return;
                }
            }
        }
    }
    
    private String getKeyName(int keyCode) {
        return Input.Keys.toString(keyCode);
    }
    
    /**
     * Render the settings screen
     */
    public void render() {
        if (!isVisible) return;
        
        // Update camera and set projection matrices
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        uiCamera.setToOrtho(false, screenWidth, screenHeight);
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        // Draw overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        
        batch.begin();
        
        // Draw title
        String title = "SETTINGS";
        glyphLayout.setText(titleFont, title);
        float titleX = screenWidth / 2f - glyphLayout.width / 2f;
        float titleY = screenHeight - 100f;
        titleFont.setColor(1f, 0.2f, 0.2f, 1f);
        titleFont.draw(batch, title, titleX, titleY);
        batch.end();
        
        // Draw back button
        drawButton(backButton, "BACK");
        
        // Draw current state
        if (currentState == MenuState.MAIN) {
            drawButton(volumeButton, "VOLUME");
            drawButton(keybindsButton, "KEYBINDS");
        } else if (currentState == MenuState.VOLUME) {
            drawVolumeMenu();
        } else if (currentState == MenuState.KEYBINDS) {
            drawKeybindsMenu();
        }
    }
    
    private void drawVolumeMenu() {
        batch.begin();
        font.setColor(0.9f, 0.9f, 0.9f, 1f);
        font.draw(batch, "Music: " + String.format("%.0f%%", musicVolume * 100), 
                 musicVolumeSlider.x, musicVolumeSlider.y + 50f);
        font.draw(batch, "SFX: " + String.format("%.0f%%", sfxVolume * 100), 
                 sfxVolumeSlider.x, sfxVolumeSlider.y + 50f);
        batch.end();
        
        drawSlider(musicVolumeSlider, musicVolume);
        drawSlider(sfxVolumeSlider, sfxVolume);
    }
    
    private void drawSlider(Rectangle slider, float value) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.9f);
        shapeRenderer.rect(slider.x, slider.y, slider.width, slider.height);
        
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 0.9f);
        shapeRenderer.rect(slider.x, slider.y, slider.width * value, slider.height);
        
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(slider.x, slider.y, slider.width, slider.height);
        shapeRenderer.end();
    }
    
    private void drawKeybindsMenu() {
        for (int i = 0; i < keybindButtons.length; i++) {
            String buttonText;
            if (rebindingIndex == i && !rebindingHotbar) {
                buttonText = "PRESS A KEY...";
            } else {
                buttonText = keybindLabels[i] + ": " + getKeyName(currentKeybinds[i]);
            }
            drawKeybindButton(keybindButtons[i], buttonText, rebindingIndex == i && !rebindingHotbar);
        }
        for (int i = 0; i < hotbarKeybindButtons.length; i++) {
            String buttonText;
            if (rebindingIndex == i && rebindingHotbar) {
                buttonText = "PRESS A KEY...";
            } else {
                buttonText = hotbarKeybindLabels[i] + ": " + getKeyName(hotbarKeybinds[i]);
            }
            drawKeybindButton(hotbarKeybindButtons[i], buttonText, rebindingIndex == i && rebindingHotbar);
        }
    }
    
    private void drawKeybindButton(Rectangle button, String text, boolean isRebinding) {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hovered = button.contains(mouseX, mouseY);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        if (isRebinding) {
            shapeRenderer.setColor(1f, 0.5f, 0.2f, 0.9f);
        } else if (hovered) {
            shapeRenderer.setColor(1f, 0.3f, 0.3f, 0.9f);
        } else {
            shapeRenderer.setColor(0.4f, 0.1f, 0.1f, 0.8f);
        }
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        shapeRenderer.end();
        
        batch.begin();
        glyphLayout.setText(font, text);
        float textX = button.x + button.width / 2f - glyphLayout.width / 2f;
        float textY = button.y + button.height / 2f + glyphLayout.height / 2f;
        
        if (isRebinding || hovered) {
            font.setColor(Color.WHITE);
        } else {
            font.setColor(0.9f, 0.9f, 0.9f, 1f);
        }
        font.draw(batch, text, textX, textY);
        batch.end();
    }
    
    private void drawButton(Rectangle button, String text) {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean hovered = button.contains(mouseX, mouseY);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        if (hovered) {
            shapeRenderer.setColor(1f, 0.3f, 0.3f, 0.9f);
        } else {
            shapeRenderer.setColor(0.4f, 0.1f, 0.1f, 0.8f);
        }
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        shapeRenderer.end();
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(button.x, button.y, button.width, button.height);
        shapeRenderer.end();
        
        batch.begin();
        glyphLayout.setText(font, text);
        float textX = button.x + button.width / 2f - glyphLayout.width / 2f;
        float textY = button.y + button.height / 2f + glyphLayout.height / 2f;
        
        if (hovered) {
            font.setColor(Color.WHITE);
        } else {
            font.setColor(0.9f, 0.9f, 0.9f, 1f);
        }
        font.draw(batch, text, textX, textY);
        batch.end();
    }
    
    public void resize(int width, int height) {
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
        setupUI();
    }
    
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (titleFont != null) titleFont.dispose();
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void show() {
        isVisible = true;
        currentState = MenuState.MAIN;
        ignoreInputTimer = 0.1f;
    }
    
    public void hide() {
        isVisible = false;
    }
    
    public int[] getKeybinds() {
        return currentKeybinds;
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public float getSFXVolume() {
        return sfxVolume;
    }
}
