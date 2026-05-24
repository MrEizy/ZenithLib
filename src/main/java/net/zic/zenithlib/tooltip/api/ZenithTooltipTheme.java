package net.zic.zenithlib.tooltip.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Defines the visual palette and sizing metrics used to render a Zenith tooltip.
 *
 * <p>The palette contains semantic colours for the background, border edges, text,
 * accents, and status-like content. Documents can therefore refer to palette meanings
 * instead of fixed colours and remain visually coherent under themes created by other
 * mods or resource packs. The metrics record controls padding and maximum tooltip
 * width while enforcing a readable minimum inner width.</p>
 *
 * <p>This type also owns RGBA string conversion and semantic colour resolution. Its
 * built-in fallback is the ZIC mana-blue theme used whenever a requested theme is not
 * available.</p>
 */

public record ZenithTooltipTheme(
        Palette colors,
        Metrics layout
) {
    public static final int MIN_INNER_WIDTH = 80;

    private static final ZenithTooltipTheme DEFAULT = new ZenithTooltipTheme(
            new Palette(
                    0xDD071426,
                    0xFF5CCBFF,
                    0xFF1B4F8A,
                    0xFFE6F7FF,
                    0xFF5CCBFF,
                    0xFF7CA8C0,
                    0xFF85FF9A,
                    0xFFFFD166,
                    0xFFFF6B6B
            ),
            new Metrics(6, 240)
    );

    public static final Codec<ZenithTooltipTheme> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Palette.CODEC.fieldOf("colors").forGetter(ZenithTooltipTheme::colors),
                    Metrics.CODEC.optionalFieldOf("layout", DEFAULT.layout()).forGetter(ZenithTooltipTheme::layout)
            ).apply(instance, ZenithTooltipTheme::new)
    );

    public static ZenithTooltipTheme defaultTheme() {
        return DEFAULT;
    }

    public int background() {
        return colors.background();
    }

    public int borderTop() {
        return colors.borderTop();
    }

    public int borderBottom() {
        return colors.borderBottom();
    }

    public int text() {
        return colors.text();
    }

    public int accent() {
        return colors.accent();
    }

    public int muted() {
        return colors.muted();
    }

    public int positive() {
        return colors.positive();
    }

    public int warning() {
        return colors.warning();
    }

    public int negative() {
        return colors.negative();
    }

    public int padding() {
        return layout.padding();
    }

    public int maxWidth() {
        return layout.maxWidth();
    }

    public int resolveColor(String keyOrHex) {
        return switch (keyOrHex) {
            case "background" -> background();
            case "border_top" -> borderTop();
            case "border_bottom" -> borderBottom();
            case "text" -> text();
            case "accent" -> accent();
            case "muted" -> muted();
            case "positive" -> positive();
            case "warning" -> warning();
            case "negative" -> negative();
            default -> parseRgbaHex(keyOrHex);
        };
    }

    public static int parseRgbaHex(String hex) {
        String raw = hex.startsWith("#") ? hex.substring(1) : hex;

        if (raw.length() != 6 && raw.length() != 8) {
            throw new IllegalArgumentException("Expected #RRGGBB or #RRGGBBAA, got: " + hex);
        }

        int r = Integer.parseInt(raw.substring(0, 2), 16);
        int g = Integer.parseInt(raw.substring(2, 4), 16);
        int b = Integer.parseInt(raw.substring(4, 6), 16);
        int a = raw.length() == 8 ? Integer.parseInt(raw.substring(6, 8), 16) : 255;

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static String toRgbaHex(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        return String.format("#%02X%02X%02X%02X", r, g, b, a);
    }

    public record Palette(
            int background,
            int borderTop,
            int borderBottom,
            int text,
            int accent,
            int muted,
            int positive,
            int warning,
            int negative
    ) {
        public static final Codec<Palette> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        color("background", "#071426DD").forGetter(Palette::background),
                        color("border_top", "#5CCBFFFF").forGetter(Palette::borderTop),
                        color("border_bottom", "#1B4F8AFF").forGetter(Palette::borderBottom),
                        color("text", "#E6F7FFFF").forGetter(Palette::text),
                        color("accent", "#5CCBFFFF").forGetter(Palette::accent),
                        color("muted", "#7CA8C0FF").forGetter(Palette::muted),
                        color("positive", "#85FF9AFF").forGetter(Palette::positive),
                        color("warning", "#FFD166FF").forGetter(Palette::warning),
                        color("negative", "#FF6B6BFF").forGetter(Palette::negative)
                ).apply(instance, Palette::new)
        );

        private static MapCodec<Integer> color(String key, String fallback) {
            return Codec.STRING.optionalFieldOf(key, fallback).xmap(
                    ZenithTooltipTheme::parseRgbaHex,
                    ZenithTooltipTheme::toRgbaHex
            );
        }
    }

    public record Metrics(int padding, int maxWidth) {
        public static final Codec<Metrics> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("padding", 6).forGetter(Metrics::padding),
                        Codec.INT.optionalFieldOf("max_width", 240).forGetter(Metrics::maxWidth)
                ).apply(instance, Metrics::new)
        );

        public Metrics {
            padding = Math.max(0, padding);
            maxWidth = Math.max(MIN_INNER_WIDTH + padding * 2, maxWidth);
        }
    }
}
