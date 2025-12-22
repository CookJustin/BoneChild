package com.bonechild.saves;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import java.io.File;

public class SaveStateManager {
    private final Json json;

    public SaveStateManager() {
        this.json = new Json();
    }

    private FileHandle saveFileHandle() {
        File saveFile = SavePaths.getSaveFile();
        File parent = saveFile.getParentFile();
        if (parent != null && !parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }
        // Use absolute path to avoid backend-dependent local storage locations
        return new FileHandle(saveFile);
    }

    public void saveGame(SaveState state) {
        try {
            FileHandle file = saveFileHandle();
            String jsonData = json.prettyPrint(state);
            file.writeString(jsonData, false);
            Gdx.app.log("SaveStateManager", "Game saved: " + file.file().getAbsolutePath());
        } catch (Exception e) {
            Gdx.app.error("SaveStateManager", "Save failed: " + e.getMessage());
        }
    }

    public SaveState loadGame() {
        try {
            FileHandle file = saveFileHandle();
            if (!file.exists()) return null;
            String jsonData = file.readString();
            return json.fromJson(SaveState.class, jsonData);
        } catch (Exception e) {
            Gdx.app.error("SaveStateManager", "Load failed: " + e.getMessage());
            return null;
        }
    }

    public boolean hasSaveFile() {
        try {
            return saveFileHandle().exists();
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteSave() {
        try {
            FileHandle file = saveFileHandle();
            if (file.exists()) file.delete();
        } catch (Exception e) {
            Gdx.app.error("SaveStateManager", "Delete failed: " + e.getMessage());
        }
    }
}
