package net.zic.zenithlib.tooltip.client.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.ZenithLib;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping items to tooltip themes.
 * Mappings are loaded from assets/zenithlib/item_themes/*.json
 *
 * <p>JSON format:
 * <pre>
 * {
 *   "minecraft:diamond": "rare",
 *   "minecraft:iron_sword": "weapon",
 *   "#minecraft:swords": "weapon",
 *   "modid:custom_item": "custom_theme"
 * }
 * </pre>
 */
public class ItemThemeRegistry implements ResourceManagerReloadListener {

    private static final String ITEM_THEME_PATH = "item_themes";

    // Direct item ID to theme key mappings
    private static final Map<Identifier, String> ITEM_MAPPINGS = new HashMap<Identifier, String>();

    // Tag to theme key mappings
    private static final Map<TagKey<Item>, String> TAG_MAPPINGS = new HashMap<>();

    // Cache for stack -> theme key lookups
    private static final Map<ItemStack, String> STACK_CACHE = new HashMap<>();

    /**
     * Gets the theme key for the given item stack.
     * Returns null if no specific theme is mapped.
     */
    public static String getThemeKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        // Check cache first
        if (STACK_CACHE.containsKey(stack)) {
            return STACK_CACHE.get(stack);
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        // Check direct item mapping first
        if (ITEM_MAPPINGS.containsKey(itemId)) {
            String themeKey = ITEM_MAPPINGS.get(itemId);
            STACK_CACHE.put(stack, themeKey);
            return themeKey;
        }

        // Check tag mappings
        for (Map.Entry<TagKey<Item>, String> entry : TAG_MAPPINGS.entrySet()) {
            if (stack.is(entry.getKey())) {
                String themeKey = entry.getValue();
                STACK_CACHE.put(stack, themeKey);
                return themeKey;
            }
        }

        // No mapping found
        STACK_CACHE.put(stack, null);
        return null;
    }

    /**
     * Checks if the given stack has a specific theme mapped.
     */
    public static boolean hasThemeForStack(ItemStack stack) {
        return getThemeKey(stack) != null;
    }

    /**
     * Registers a direct item-to-theme mapping.
     */
    public static void registerItem(Identifier itemId, String themeKey) {
        ITEM_MAPPINGS.put(itemId, themeKey);
        STACK_CACHE.clear();
    }

    /**
     * Registers a tag-to-theme mapping.
     */
    public static void registerTag(TagKey<Item> tag, String themeKey) {
        TAG_MAPPINGS.put(tag, themeKey);
        STACK_CACHE.clear();
    }

    /**
     * Clears all mappings.
     */
    public static void clear() {
        ITEM_MAPPINGS.clear();
        TAG_MAPPINGS.clear();
        STACK_CACHE.clear();
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ITEM_MAPPINGS.clear();
        TAG_MAPPINGS.clear();
        STACK_CACHE.clear();

        // Load all item theme mapping JSON files
        resourceManager.listResources(ITEM_THEME_PATH, path -> path.getPath().endsWith(".json")).forEach((location, resource) -> {
            try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                for (var entry : json.entrySet()) {
                    String key = entry.getKey();
                    String themeKey = entry.getValue().getAsString();

                    if (key.startsWith("#")) {
                        // Tag mapping
                        Identifier tagId = Identifier.parse(key.substring(1));
                        TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
                        TAG_MAPPINGS.put(tag, themeKey);
                    } else {
                        // Direct item mapping
                        Identifier itemId = Identifier.parse(key);
                        ITEM_MAPPINGS.put(itemId, themeKey);
                    }
                }

                ZenithLib.LOGGER.debug("Loaded item theme mappings from: {}", location);
            } catch (Exception e) {
                ZenithLib.LOGGER.error("Failed to load item theme mappings from {}: {}", location, e.getMessage());
            }
        });

        ZenithLib.LOGGER.info("Loaded {} direct item mappings and {} tag mappings",
                ITEM_MAPPINGS.size(), TAG_MAPPINGS.size());
    }
}