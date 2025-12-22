package com.bonechild.saves;

import java.io.File;

/**
 * Centralized save file paths.
 *
 * We intentionally do NOT rely on LibGDX local storage here because its location
 * can vary by backend/working directory. For desktop, we prefer OS-standard
 * application data directories.
 */
public final class SavePaths {
    private SavePaths() {}

    public static final String SAVE_FILE_NAME = "bonechild_save.json";
    public static final String APP_DIR_NAME = "BoneChild";

    /**
     * Returns the absolute save file path for the current OS.
     */
    public static File getSaveFile() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home", ".");

        File baseDir;
        if (os.contains("mac")) {
            baseDir = new File(home, "Library/Application Support/" + APP_DIR_NAME);
        } else if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) {
                baseDir = new File(appData, APP_DIR_NAME);
            } else {
                baseDir = new File(home, "." + APP_DIR_NAME);
            }
        } else {
            // Linux / other
            baseDir = new File(home, ".local/share/" + APP_DIR_NAME);
        }

        return new File(baseDir, SAVE_FILE_NAME);
    }
}

