package net.zic.zenithlib.tooltip.client.render;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration for tooltip navigation and rendering.
 */
public class TooltipNavigationConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue TOOLTIP_RENDERING_ENABLED = BUILDER
            .comment("Enable modern tooltip rendering")
            .define("tooltipRenderingEnabled", true);

    public static final ModConfigSpec.BooleanValue APPLY_TO_VANILLA_ITEMS = BUILDER
            .comment("Apply modern tooltips to vanilla Minecraft items")
            .define("applyToVanillaItems", true);

    public static final ModConfigSpec.BooleanValue APPLY_TO_MOD_ITEMS = BUILDER
            .comment("Apply modern tooltips to modded items (requires theme mapping)")
            .define("applyToModItems", true);

    public static final ModConfigSpec.BooleanValue SHOW_PAGE_INDICATORS = BUILDER
            .comment("Show page indicator dots at the bottom of multi-page tooltips")
            .define("showPageIndicators", true);

    public static final ModConfigSpec.BooleanValue SHOW_DIAMOND_BADGE = BUILDER
            .comment("Show the diamond badge with item icon at the top of tooltips")
            .define("showDiamondBadge", true);

    public static final ModConfigSpec.IntValue DEFAULT_MAX_LINES_PER_PAGE = BUILDER
            .comment("Default maximum number of lines per tooltip page")
            .defineInRange("defaultMaxLinesPerPage", 8, 4, 20);

    public static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * Returns whether modern tooltip rendering is enabled.
     */
    public static boolean tooltipRenderingEnabled() {
        return TOOLTIP_RENDERING_ENABLED.get();
    }

    /**
     * Returns whether to apply tooltips to vanilla items.
     */
    public static boolean applyTooltipsToVanillaItems() {
        return APPLY_TO_VANILLA_ITEMS.get();
    }

    /**
     * Returns whether to apply tooltips to modded items.
     */
    public static boolean applyTooltipsToModItems() {
        return APPLY_TO_MOD_ITEMS.get();
    }

    /**
     * Returns whether to show page indicators.
     */
    public static boolean showPageIndicators() {
        return SHOW_PAGE_INDICATORS.get();
    }

    /**
     * Returns whether to show the diamond badge.
     */
    public static boolean showDiamondBadge() {
        return SHOW_DIAMOND_BADGE.get();
    }

    /**
     * Returns the default maximum lines per page.
     */
    public static int defaultMaxLinesPerPage() {
        return DEFAULT_MAX_LINES_PER_PAGE.get();
    }
}
