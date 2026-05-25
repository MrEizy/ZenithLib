package net.zic.zenithlib.tooltip.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;

/**
 * Defines the visual palette and sizing metrics used to render a Zenith tooltip.
 *
 * <p>The palette contains semantic colours for the background, border edges, text,
 * accents, and status-like content. Documents can therefore refer to palette meanings
 * instead of fixed colours and remain visually coherent under themes created by other
 * mods or resource packs. The layout metrics control padding and maximum tooltip width,
 * while {@link IconHolder} controls the decorative item holder used by icon elements
 * and title-icon headers.</p>
 *
 * <p>This type also owns RGBA string conversion and semantic colour resolution. Its
 * built-in fallback is the ZIC mana-blue theme used whenever a requested theme is not
 * available.</p>
 */

public record ZenithTooltipTheme(
        Palette colors,
        Metrics layout,
        IconHolder iconHolder,
        BarStyle barStyle,
        BadgeStyle badgeStyle,
        DividerStyle dividerStyle
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
            new Metrics(6, 240, 220, 3, 1),
            IconHolder.DEFAULT,
            BarStyle.DEFAULT,
            BadgeStyle.DEFAULT,
            DividerStyle.DEFAULT
    );

    public static final Codec<ZenithTooltipTheme> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Palette.CODEC.fieldOf("colors").forGetter(ZenithTooltipTheme::colors),
                    Metrics.CODEC.optionalFieldOf("layout", DEFAULT.layout()).forGetter(ZenithTooltipTheme::layout),
                    IconHolder.CODEC.optionalFieldOf("icon_holder", DEFAULT.iconHolder()).forGetter(ZenithTooltipTheme::iconHolder),
                    BarStyle.CODEC.optionalFieldOf("bar_style", DEFAULT.barStyle()).forGetter(ZenithTooltipTheme::barStyle),
                    BadgeStyle.CODEC.optionalFieldOf("badge_style", DEFAULT.badgeStyle()).forGetter(ZenithTooltipTheme::badgeStyle),
                    DividerStyle.CODEC.optionalFieldOf("divider_style", DEFAULT.dividerStyle()).forGetter(ZenithTooltipTheme::dividerStyle)
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

    public int maxHeight() {
        return layout.maxHeight();
    }

    public int elementGap() {
        return layout.elementGap();
    }

    public int rowGap() {
        return layout.rowGap();
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

    public record Metrics(int padding, int maxWidth, int maxHeight, int elementGap, int rowGap) {
        public static final Codec<Metrics> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("padding", 6).forGetter(Metrics::padding),
                        Codec.INT.optionalFieldOf("max_width", 240).forGetter(Metrics::maxWidth),
                        Codec.INT.optionalFieldOf("max_height", 220).forGetter(Metrics::maxHeight),
                        Codec.INT.optionalFieldOf("element_gap", 3).forGetter(Metrics::elementGap),
                        Codec.INT.optionalFieldOf("row_gap", 1).forGetter(Metrics::rowGap)
                ).apply(instance, Metrics::new)
        );

        public Metrics {
            padding = Math.max(0, padding);
            maxWidth = Math.max(MIN_INNER_WIDTH + padding * 2, maxWidth);
            maxHeight = Math.max(80, maxHeight);
            elementGap = Math.max(0, elementGap);
            rowGap = Math.max(0, rowGap);
        }
    }


    /** Theme-controlled presentation of labelled progress bars. */
    public record BarStyle(
            int height,
            int labelGap,
            String track,
            int trackAlpha,
            String border,
            int borderWidth,
            int fillAlpha
    ) {
        public static final BarStyle DEFAULT = new BarStyle(5, 2, "muted", 0x55, "border_bottom", 0, 255);

        public static final Codec<BarStyle> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("height", DEFAULT.height()).forGetter(BarStyle::height),
                        Codec.INT.optionalFieldOf("label_gap", DEFAULT.labelGap()).forGetter(BarStyle::labelGap),
                        Codec.STRING.optionalFieldOf("track", DEFAULT.track()).forGetter(BarStyle::track),
                        Codec.INT.optionalFieldOf("track_alpha", DEFAULT.trackAlpha()).forGetter(BarStyle::trackAlpha),
                        Codec.STRING.optionalFieldOf("border", DEFAULT.border()).forGetter(BarStyle::border),
                        Codec.INT.optionalFieldOf("border_width", DEFAULT.borderWidth()).forGetter(BarStyle::borderWidth),
                        Codec.INT.optionalFieldOf("fill_alpha", DEFAULT.fillAlpha()).forGetter(BarStyle::fillAlpha)
                ).apply(instance, BarStyle::new)
        );

        public BarStyle {
            height = Math.max(1, height);
            labelGap = Math.max(0, labelGap);
            track = track == null || track.isBlank() ? "muted" : track;
            trackAlpha = Math.max(0, Math.min(255, trackAlpha));
            border = border == null || border.isBlank() ? "border_bottom" : border;
            borderWidth = Math.max(0, Math.min(borderWidth, height / 2));
            fillAlpha = Math.max(0, Math.min(255, fillAlpha));
        }

        public int trackColor(ZenithTooltipTheme theme) {
            return withAlpha(theme.resolveColor(track), trackAlpha);
        }

        public int borderColor(ZenithTooltipTheme theme) {
            return theme.resolveColor(border);
        }

        public int fillColor(int color) {
            return withAlpha(color, fillAlpha);
        }

        private static int withAlpha(int color, int alpha) {
            return (alpha << 24) | (color & 0x00FFFFFF);
        }
    }

    /** Theme-controlled sizing and opacity for compact badge labels. */
    public record BadgeStyle(
            int horizontalPadding,
            int verticalPadding,
            int borderWidth,
            int fillAlpha
    ) {
        public static final BadgeStyle DEFAULT = new BadgeStyle(5, 2, 1, 255);

        public static final Codec<BadgeStyle> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("horizontal_padding", DEFAULT.horizontalPadding()).forGetter(BadgeStyle::horizontalPadding),
                        Codec.INT.optionalFieldOf("vertical_padding", DEFAULT.verticalPadding()).forGetter(BadgeStyle::verticalPadding),
                        Codec.INT.optionalFieldOf("border_width", DEFAULT.borderWidth()).forGetter(BadgeStyle::borderWidth),
                        Codec.INT.optionalFieldOf("fill_alpha", DEFAULT.fillAlpha()).forGetter(BadgeStyle::fillAlpha)
                ).apply(instance, BadgeStyle::new)
        );

        public BadgeStyle {
            horizontalPadding = Math.max(0, horizontalPadding);
            verticalPadding = Math.max(0, verticalPadding);
            borderWidth = Math.max(0, borderWidth);
            fillAlpha = Math.max(0, Math.min(255, fillAlpha));
        }

        public int fillColor(int color) {
            return (fillAlpha << 24) | (color & 0x00FFFFFF);
        }
    }

    /** Theme-controlled divider line geometry and optional centre ornament. */
    public record DividerStyle(
            int thickness,
            int gapAbove,
            int gapBelow,
            String color,
            Decoration decoration
    ) {
        public static final DividerStyle DEFAULT = new DividerStyle(1, 2, 3, "accent", Decoration.NONE);

        public static final Codec<DividerStyle> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.optionalFieldOf("thickness", DEFAULT.thickness()).forGetter(DividerStyle::thickness),
                        Codec.INT.optionalFieldOf("gap_above", DEFAULT.gapAbove()).forGetter(DividerStyle::gapAbove),
                        Codec.INT.optionalFieldOf("gap_below", DEFAULT.gapBelow()).forGetter(DividerStyle::gapBelow),
                        Codec.STRING.optionalFieldOf("color", DEFAULT.color()).forGetter(DividerStyle::color),
                        Decoration.CODEC.optionalFieldOf("decoration", DEFAULT.decoration()).forGetter(DividerStyle::decoration)
                ).apply(instance, DividerStyle::new)
        );

        public DividerStyle {
            thickness = Math.max(1, thickness);
            gapAbove = Math.max(0, gapAbove);
            gapBelow = Math.max(0, gapBelow);
            color = color == null || color.isBlank() ? "accent" : color;
            decoration = decoration == null ? Decoration.NONE : decoration;
        }

        public int colorValue(ZenithTooltipTheme theme) {
            return theme.resolveColor(color);
        }

        public int ornamentSize() {
            return decoration == Decoration.DIAMOND ? 5 : thickness;
        }

        public int height() {
            return gapAbove + Math.max(thickness, ornamentSize()) + gapBelow;
        }
    }

    public enum Decoration {
        NONE("none"),
        DIAMOND("diamond");

        private static final Codec<Decoration> CODEC = Codec.STRING.comapFlatMap(
                Decoration::decode,
                Decoration::serializedName
        );

        private final String serializedName;

        Decoration(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        private static DataResult<Decoration> decode(String raw) {
            String normalized = raw.toLowerCase(Locale.ROOT);

            for (Decoration decoration : values()) {
                if (decoration.serializedName.equals(normalized)) {
                    return DataResult.success(decoration);
                }
            }

            return DataResult.error(() -> "Unsupported tooltip divider_style decoration: " + raw);
        }
    }

    /**
     * Theme-controlled styling for item icons rendered by {@code icon} and
     * {@code title_icon} document elements.
     *
     * <p>Border and fill accept either semantic palette keys such as {@code accent}
     * and {@code background}, or literal RGBA hex strings. {@code fill_alpha} is
     * applied after colour resolution so a holder can reuse a theme's background hue
     * while remaining translucent. Item stacks themselves retain Minecraft's native
     * sixteen-pixel rendering size; this record styles their surrounding holder.</p>
     */
    public record IconHolder(
            Shape shape,
            int boxSize,
            int borderWidth,
            int gap,
            String border,
            String fill,
            int fillAlpha
    ) {
        public static final IconHolder DEFAULT = new IconHolder(
                Shape.DIAMOND,
                31,
                2,
                8,
                "accent",
                "background",
                0x55
        );

        public static final Codec<IconHolder> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Shape.CODEC.optionalFieldOf("shape", DEFAULT.shape()).forGetter(IconHolder::shape),
                        Codec.INT.optionalFieldOf("box_size", DEFAULT.boxSize()).forGetter(IconHolder::boxSize),
                        Codec.INT.optionalFieldOf("border_width", DEFAULT.borderWidth()).forGetter(IconHolder::borderWidth),
                        Codec.INT.optionalFieldOf("gap", DEFAULT.gap()).forGetter(IconHolder::gap),
                        Codec.STRING.optionalFieldOf("border", DEFAULT.border()).forGetter(IconHolder::border),
                        Codec.STRING.optionalFieldOf("fill", DEFAULT.fill()).forGetter(IconHolder::fill),
                        Codec.INT.optionalFieldOf("fill_alpha", DEFAULT.fillAlpha()).forGetter(IconHolder::fillAlpha)
                ).apply(instance, IconHolder::new)
        );

        public IconHolder {
            shape = shape == null ? Shape.DIAMOND : shape;
            boxSize = Math.max(16, boxSize);

            // Pixel diamonds need an odd diameter so both points land symmetrically.
            if (shape == Shape.DIAMOND && boxSize % 2 == 0) {
                boxSize++;
            }

            borderWidth = Math.max(0, Math.min(borderWidth, boxSize / 2));
            gap = Math.max(0, gap);
            border = border == null || border.isBlank() ? "accent" : border;
            fill = fill == null || fill.isBlank() ? "background" : fill;
            fillAlpha = Math.max(0, Math.min(255, fillAlpha));
        }

        public int borderColor(ZenithTooltipTheme theme) {
            return theme.resolveColor(border);
        }

        public int fillColor(ZenithTooltipTheme theme) {
            return withAlpha(theme.resolveColor(fill), fillAlpha);
        }

        private static int withAlpha(int color, int alpha) {
            return (alpha << 24) | (color & 0x00FFFFFF);
        }
    }

    public enum Shape {
        DIAMOND("diamond"),
        SQUARE("square"),
        NONE("none");

        private static final Codec<Shape> CODEC = Codec.STRING.comapFlatMap(
                Shape::decode,
                Shape::serializedName
        );

        private final String serializedName;

        Shape(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        private static DataResult<Shape> decode(String raw) {
            String normalized = raw.toLowerCase(Locale.ROOT);

            for (Shape shape : values()) {
                if (shape.serializedName.equals(normalized)) {
                    return DataResult.success(shape);
                }
            }

            return DataResult.error(() -> "Unsupported tooltip icon_holder shape: " + raw);
        }
    }
}
