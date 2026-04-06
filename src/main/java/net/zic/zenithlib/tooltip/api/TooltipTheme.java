package net.zic.zenithlib.tooltip.api;

import com.google.gson.JsonObject;

/**
 * Color palette record for tooltip themes.
 * Defines all colors used in rendering tooltips.
 */
public record TooltipTheme(
        // Border colors
        int border,
        int borderInner,

        // Background colors (gradient)
        int bgTop,
        int bgBottom,

        // Text colors
        int name,
        int sectionHeader,
        int body,
        int hint,

        // Diamond badge colors
        int badgeBg,
        int badgeCutout,
        int diamondFrame,
        int diamondFrameInner,

        // Separator line color
        int separator,

        // Page indicator colors
        int pageDotFilled,
        int pageDotEmpty,

        // Additional accent colors
        int accentPrimary,
        int accentSecondary
) {
    /**
     * Returns the default golden theme.
     */
    public static TooltipTheme defaultTheme() {
        return new TooltipTheme(
                0xFFE2A834,     // border - golden
                0xFF8A6A1E,     // borderInner - darker golden
                0xF02E2210,     // bgTop - dark brown
                0xF0181208,     // bgBottom - darker brown
                0xFFFFF0CC,     // name - cream white
                0xFFFFD5A0,     // sectionHeader - light golden
                0xFFE6ECF5,     // body - light gray-blue
                0xFFC7D2E2,     // hint - muted blue-gray
                0xFFEEEEEE,     // badgeBg - light gray
                0xFF141008,     // badgeCutout - very dark
                0xFFE2A834,     // diamondFrame - golden
                0xFF2A1E0A,     // diamondFrameInner - dark brown
                0xFF8A6A1E,     // separator - golden brown
                0xFFE2A834,     // pageDotFilled - golden
                0xFF3D3020,     // pageDotEmpty - dark brown
                0xFF9D62CA,     // accentPrimary - purple
                0xFF5E8ACF      // accentSecondary - blue
        );
    }

    /**
     * Parses a TooltipTheme from a JSON object.
     * Each field is expected as a hex string (e.g. "0xFFE2A834").
     * Missing fields fall back to the default theme.
     */
    public static TooltipTheme fromJson(JsonObject json) {
        TooltipTheme d = defaultTheme();
        return new TooltipTheme(
                color(json, "border", d.border()),
                color(json, "borderInner", d.borderInner()),
                color(json, "bgTop", d.bgTop()),
                color(json, "bgBottom", d.bgBottom()),
                color(json, "name", d.name()),
                color(json, "sectionHeader", d.sectionHeader()),
                color(json, "body", d.body()),
                color(json, "hint", d.hint()),
                color(json, "badgeBg", d.badgeBg()),
                color(json, "badgeCutout", d.badgeCutout()),
                color(json, "diamondFrame", d.diamondFrame()),
                color(json, "diamondFrameInner", d.diamondFrameInner()),
                color(json, "separator", d.separator()),
                color(json, "pageDotFilled", d.pageDotFilled()),
                color(json, "pageDotEmpty", d.pageDotEmpty()),
                color(json, "accentPrimary", d.accentPrimary()),
                color(json, "accentSecondary", d.accentSecondary())
        );
    }

    private static int color(JsonObject json, String key, int fallback) {
        if (!json.has(key)) return fallback;
        try {
            String value = json.get(key).getAsString().replace("0x", "");
            return (int) Long.parseLong(value, 16);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
