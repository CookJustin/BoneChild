package com.bonechild.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.bonechild.rendering.Animation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads assets from JSON configuration into AssetRegistry.
 * Supports:
 * - Single textures
 * - Frame sequence animations (Player1.png, Player2.png, etc.)
 * - Sprite sheet animations (single row or multi-row)
 */
public class AssetLoader {
    private final AssetRegistry registry;
    private final JsonReader jsonReader;

    public AssetLoader(AssetRegistry registry) {
        this.registry = registry;
        this.jsonReader = new JsonReader();
    }

    /**
     * Load assets from multiple JSON files (modular approach).
     * Each module can provide its own asset definitions.
     */
    public void loadFromMultipleFiles(String... jsonPaths) {
        Gdx.app.log("AssetLoader", "Loading assets from " + jsonPaths.length + " modules");

        for (String jsonPath : jsonPaths) {
            loadFromJson(jsonPath);
        }

        Gdx.app.log("AssetLoader", "All asset loading complete: " + registry.getStats());
    }

    /**
     * Load assets from a single JSON file
     */
    public void loadFromJson(String jsonPath) {
        Gdx.app.log("AssetLoader", "Loading assets from: " + jsonPath);

        FileHandle file = Gdx.files.internal(jsonPath);
        if (!file.exists()) {
            Gdx.app.error("AssetLoader", "Asset config not found: " + jsonPath + " (skipping)");
            return;
        }

        JsonValue root = jsonReader.parse(file);

        // Load textures first
        if (root.has("textures")) {
            loadTextures(root.get("textures"));
        }

        // Then load animations (may reference textures)
        if (root.has("animations")) {
            loadAnimations(root.get("animations"));
        }

        Gdx.app.log("AssetLoader", "Loaded assets from: " + jsonPath);
    }

    /**
     * Auto-discover and load all asset files from standard locations
     */
    public void loadFromModules() {
        Gdx.app.log("AssetLoader", "Auto-loading module assets...");

        // Load core game assets from json/ folder
        loadFromJson("json/player-assets.json");
        loadFromJson("json/effects-assets.json");
        loadFromJson("json/ui-assets.json");
        loadFromJson("json/stages-assets.json");

        // Load all monster asset files from json/monsters/ directory
        loadFromDirectory("json/monsters/");

        Gdx.app.log("AssetLoader", "Module loading complete: " + registry.getStats());
    }

    /**
     * Load all JSON files from a directory
     */
    private void loadFromDirectory(String directory) {
        Gdx.app.log("AssetLoader", "Scanning directory: " + directory);

        FileHandle dirHandle = Gdx.files.internal(directory);
        if (!dirHandle.exists() || !dirHandle.isDirectory()) {
            Gdx.app.log("AssetLoader", "Directory not found: " + directory + " (skipping)");
            return;
        }

        int count = 0;
        for (FileHandle file : dirHandle.list()) {
            if (file.extension().equals("json")) {
                loadFromJson(directory + file.name());
                count++;
            }
        }

        Gdx.app.log("AssetLoader", "Loaded " + count + " asset files from " + directory);
    }

    private void loadTextures(JsonValue texturesNode) {
        for (JsonValue entry = texturesNode.child; entry != null; entry = entry.next) {
            String id = entry.name;
            String path = entry.asString();

            try {
                Texture texture = new Texture(Gdx.files.internal(path));
                registry.registerTexture(id, texture);
                Gdx.app.log("AssetLoader", "Loaded texture: " + id + " from " + path);
            } catch (Exception e) {
                Gdx.app.error("AssetLoader", "Failed to load texture " + id + ": " + e.getMessage());
            }
        }
    }

    private void loadAnimations(JsonValue animationsNode) {
        for (JsonValue entry = animationsNode.child; entry != null; entry = entry.next) {
            String id = entry.name;
            String type = entry.getString("type");

            try {
                Animation animation = null;

                switch (type) {
                    case "frame_sequence":
                        animation = loadFrameSequence(entry);
                        break;
                    case "sprite_sheet":
                        animation = loadSpriteSheet(entry);
                        break;
                    case "sprite_sheet_multi_row":
                        animation = loadSpriteSheetMultiRow(entry);
                        break;
                    default:
                        Gdx.app.error("AssetLoader", "Unknown animation type: " + type);
                }

                if (animation != null) {
                    registry.registerAnimation(id, animation);
                    Gdx.app.log("AssetLoader", "Loaded animation: " + id);
                }
            } catch (Exception e) {
                Gdx.app.error("AssetLoader", "Failed to load animation " + id + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Load animation from frame sequence pattern like "Player{1-6}.png"
     */
    private Animation loadFrameSequence(JsonValue node) {
        String pattern = node.getString("pattern");
        float frameTime = node.getFloat("frameTime");
        boolean loop = node.getBoolean("loop");

        Texture[] frames = expandPattern(pattern);
        return new Animation(frames, frameTime, loop);
    }

    /**
     * Load animation from sprite sheet (single row)
     */
    private Animation loadSpriteSheet(JsonValue node) {
        String textureId = node.getString("texture");
        int row = node.getInt("row");
        int frameCount = node.getInt("frames");
        int frameWidth = node.getInt("frameWidth");
        int frameHeight = node.getInt("frameHeight");
        float frameTime = node.getFloat("frameTime");
        boolean loop = node.getBoolean("loop");

        Texture sheet = registry.getTexture(textureId);
        return Animation.fromSpriteSheet(sheet, row, frameCount, frameWidth, frameHeight, frameTime, loop);
    }

    /**
     * Load animation from sprite sheet (multi-row)
     */
    private Animation loadSpriteSheetMultiRow(JsonValue node) {
        String textureId = node.getString("texture");
        int startRow = node.getInt("startRow");
        int rows = node.getInt("rows");
        int framesPerRow = node.getInt("framesPerRow");
        int frameWidth = node.getInt("frameWidth");
        int frameHeight = node.getInt("frameHeight");
        float frameTime = node.getFloat("frameTime");
        boolean loop = node.getBoolean("loop");

        Texture sheet = registry.getTexture(textureId);
        return Animation.fromSpriteSheetMultiRow(sheet, startRow, rows, framesPerRow, frameWidth, frameHeight, frameTime, loop);
    }

    /**
     * Expand pattern like "Player{1-6}.png" or "explode{0000-0081}.png" into texture array
     */
    private Texture[] expandPattern(String pattern) {
        // Match patterns like {1-6} or {0000-0081}
        Pattern p = Pattern.compile("\\{(\\d+)-(\\d+)\\}");
        Matcher m = p.matcher(pattern);

        if (!m.find()) {
            throw new IllegalArgumentException("Invalid pattern: " + pattern);
        }

        String startStr = m.group(1);
        String endStr = m.group(2);
        int start = Integer.parseInt(startStr);
        int end = Integer.parseInt(endStr);
        int padding = startStr.length(); // Detect zero-padding from start string

        int count = end - start + 1;
        Texture[] textures = new Texture[count];

        for (int i = 0; i < count; i++) {
            int frameNum = start + i;
            String frameNumStr = padding > 1 ? String.format("%0" + padding + "d", frameNum) : String.valueOf(frameNum);
            String path = m.replaceFirst(frameNumStr);

            textures[i] = new Texture(Gdx.files.internal(path));
        }

        return textures;
    }
}

