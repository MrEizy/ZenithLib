package net.zic.zenithlib.tooltip.client.render;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.ThemeDefinition;
import net.zic.zenithlib.tooltip.api.TooltipTheme;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for loading and managing data-driven tooltip themes.
 * Themes are loaded from assets/zenithlib/themes/*.json
 */
public class ThemeRegistry implements ResourceManagerReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String THEME_PATH = "themes";
    private static final String DEFAULT_THEME_KEY = "default";

    private static final Map<String, ThemeDefinition> THEMES = new HashMap<>();

    /**
     * Returns the theme with the given key, or the default theme if not found.
     */
    public static ThemeDefinition get(String key) {
        return THEMES.getOrDefault(key, ThemeDefinition.defaultDefinition());
    }

    /**
     * Returns the default theme.
     */
    public static ThemeDefinition getDefault() {
        return THEMES.getOrDefault(DEFAULT_THEME_KEY, ThemeDefinition.defaultDefinition());
    }

    /**
     * Checks if a theme with the given key exists.
     */
    public static boolean has(String key) {
        return THEMES.containsKey(key);
    }

    /**
     * Returns all registered theme keys.
     */
    public static Set<String> getThemeKeys() {
        return Collections.unmodifiableSet(THEMES.keySet());
    }

    /**
     * Clears all registered themes.
     */
    public static void clear() {
        THEMES.clear();
    }

    /**
     * Registers a theme directly (for programmatic registration).
     */
    public static void register(String key, ThemeDefinition definition) {
        THEMES.put(key, definition);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        THEMES.clear();

        // Register the default theme first
        THEMES.put(DEFAULT_THEME_KEY, ThemeDefinition.defaultDefinition());

        // Load all theme JSON files
        Identifier themeLocation = Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, THEME_PATH);

        resourceManager.listResources(THEME_PATH, path -> path.getPath().endsWith(".json")).forEach((location, resource) -> {
            try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                // Extract theme key from filename (e.g., "themes/my_theme.json" -> "my_theme")
                String path = location.getPath();
                String filename = path.substring(path.lastIndexOf('/') + 1);
                String key = filename.replace(".json", "");

                ThemeDefinition definition = parseThemeDefinition(json);
                THEMES.put(key, definition);

                ZenithLib.LOGGER.debug("Loaded tooltip theme: {}", key);
            } catch (Exception e) {
                ZenithLib.LOGGER.error("Failed to load tooltip theme from {}: {}", location, e.getMessage());
            }
        });

        ZenithLib.LOGGER.info("Loaded {} tooltip themes", THEMES.size());
    }

    /**
     * Parses a ThemeDefinition from JSON.
     */
    private ThemeDefinition parseThemeDefinition(JsonObject json) {
        ThemeDefinition defaults = ThemeDefinition.defaultDefinition();

        // Parse colors if present
        TooltipTheme colors = defaults.colors();
        if (json.has("colors")) {
            colors = TooltipTheme.fromJson(json.getAsJsonObject("colors"));
        }

        // Parse other fields with defaults
        String itemAnimStyle = json.has("itemAnimStyle")
                ? json.get("itemAnimStyle").getAsString()
                : defaults.itemAnimStyle();

        String titleAnimStyle = json.has("titleAnimStyle")
                ? json.get("titleAnimStyle").getAsString()
                : defaults.titleAnimStyle();

        String itemBorderShape = json.has("itemBorderShape")
                ? json.get("itemBorderShape").getAsString()
                : defaults.itemBorderShape();

        boolean showPageIndicator = json.has("showPageIndicator")
                ? json.get("showPageIndicator").getAsBoolean()
                : defaults.showPageIndicator();

        boolean showDiamondBadge = json.has("showDiamondBadge")
                ? json.get("showDiamondBadge").getAsBoolean()
                : defaults.showDiamondBadge();

        int maxLinesPerPage = json.has("maxLinesPerPage")
                ? json.get("maxLinesPerPage").getAsInt()
                : defaults.maxLinesPerPage();

        boolean enabled = json.has("enabled")
                ? json.get("enabled").getAsBoolean()
                : defaults.enabled();

        // Parse custom text keys
        java.util.List<String> customTextKeys = defaults.customTextKeys();
        if (json.has("customTextKeys")) {
            customTextKeys = new java.util.ArrayList<>();
            for (var element : json.getAsJsonArray("customTextKeys")) {
                customTextKeys.add(element.getAsString());
            }
        }

        return new ThemeDefinition(
                colors,
                itemAnimStyle,
                titleAnimStyle,
                itemBorderShape,
                showPageIndicator,
                showDiamondBadge,
                customTextKeys,
                maxLinesPerPage,
                enabled
        );
    }
}