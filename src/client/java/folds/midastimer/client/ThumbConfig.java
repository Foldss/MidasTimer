package folds.midastimer.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class ThumbConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("thumbtimer.json");
    private static ThumbConfig instance = new ThumbConfig();

    /** Position as a fraction of the screen (0 = left/top, 1 = right/bottom). */
    public float x = 0.5f;
    public float y = 0.05f;
    public float scale = 1.0f;
    public int color = 0xFFFFFF;
    public boolean shadow = true;
    /** Any font id, e.g. minecraft:default, minecraft:uniform, minecraft:alt, or one from a resource pack. */
    public String fontId = "minecraft:default";

    public static ThumbConfig get() {
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                ThumbConfig loaded = GSON.fromJson(Files.readString(PATH), ThumbConfig.class);
                if (loaded != null) instance = loaded;
            }
        } catch (Exception e) {
            ThumbTimerClient.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        try {
            Files.writeString(PATH, GSON.toJson(instance));
        } catch (Exception e) {
            ThumbTimerClient.LOGGER.error("Failed to save config", e);
        }
    }
}
