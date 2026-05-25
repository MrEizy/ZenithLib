package net.zic.zenithlib.tooltip.api;

import com.mojang.serialization.Codec;

import java.util.Set;

/**
 * Describes a colour reference used by tooltip document content.
 * A colour may be a semantic palette token, such as {@code accent}, {@code muted},
 * or {@code warning}, or an explicit hexadecimal RGBA colour. Semantic tokens are the
 * preferred form for reusable tooltip documents because they resolve through the
 * selected {@link ZenithTooltipTheme} and therefore adapt automatically to custom
 * themes provided by ZenithLib users or resource packs.
 * The record validates accepted tokens and hex values when created, exposes common
 * palette constants for programmatic document construction, and resolves its value to
 * the final ARGB render colour only when a theme is available.
 */
public record ZenithTooltipColor(String value) {
    private static final Set<String> PALETTE_KEYS = Set.of(
            "text", "accent", "muted", "positive", "warning", "negative",
            "background", "border_top", "border_bottom"
    );

    public static final ZenithTooltipColor TEXT = new ZenithTooltipColor("text");
    public static final ZenithTooltipColor ACCENT = new ZenithTooltipColor("accent");
    public static final ZenithTooltipColor MUTED = new ZenithTooltipColor("muted");
    public static final ZenithTooltipColor POSITIVE = new ZenithTooltipColor("positive");
    public static final ZenithTooltipColor WARNING = new ZenithTooltipColor("warning");
    public static final ZenithTooltipColor NEGATIVE = new ZenithTooltipColor("negative");
    public static final ZenithTooltipColor BACKGROUND = new ZenithTooltipColor("background");
    public static final ZenithTooltipColor BORDER_TOP = new ZenithTooltipColor("border_top");
    public static final ZenithTooltipColor BORDER_BOTTOM = new ZenithTooltipColor("border_bottom");

    public static final Codec<ZenithTooltipColor> CODEC = Codec.STRING.xmap(ZenithTooltipColor::new, ZenithTooltipColor::value);

    public ZenithTooltipColor {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tooltip colour may not be blank");
        }

        if (!value.startsWith("#") && !PALETTE_KEYS.contains(value)) {
            throw new IllegalArgumentException("Unknown tooltip colour token: " + value);
        }

        if (value.startsWith("#")) {
            ZenithTooltipTheme.parseRgbaHex(value);
        }
    }

    public static ZenithTooltipColor hex(int argb) {
        return new ZenithTooltipColor(ZenithTooltipTheme.toRgbaHex(argb));
    }

    public int resolve(ZenithTooltipTheme theme) {
        return theme.resolveColor(value);
    }
}
