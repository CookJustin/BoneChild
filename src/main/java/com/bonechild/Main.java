package com.bonechild;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Main entry point for BoneChild game
 */
public class Main {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        // Window configuration
        config.setTitle("BoneChild - Vampire Survivors Style Game");
        config.setWindowedMode(1280, 720);
        config.setResizable(true);
        
        // Performance settings
        config.useVsync(true);
        config.setForegroundFPS(60);
        
        // Window icon (optional - uncomment when icon is available)
        // config.setWindowIcon("icon.png");
        
        new Lwjgl3Application(new BoneChildGame(), config);
    }
}
