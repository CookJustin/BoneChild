package com.bonechild.saves;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
public class SaveStateManager {
    private static final String SAVE_FILE = "bonechild_save.json";
    private Json json;
    public SaveStateManager() {
        this.json = new Json();
    }
    public void saveGame(SaveState state) {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            String jsonData = json.prettyPrint(state);
            file.writeString(jsonData, false);
            Gdx.app.log("SaveStateManager", "Game saved!");
        } catch (Exception e) {
            Gdx.app.error("SaveStateManager", "Save failed: " + e.getMessage());
        }
    }
    public SaveState loadGame() {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            if (!file.exists()) return null;
            String jsonData = file.readString();
            return json.fromJson(SaveState.class, jsonData);
        } catch (Exception e) {
            Gdx.app.error("SaveStateManager", "Load failed: " + e.getMessage());
            return null;
        }
    }
    public boolean hasSaveFile() {
        return Gdx.files.local(SAVE_FILE).exists();
    }
    public void deleteSave() {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            if (file.exists()) file.delete();
        } catch (Exception e) {
            Gdx.app.error("SaveStateManager", "Delete failed: " + e.getMessage());
        }
    }
}
