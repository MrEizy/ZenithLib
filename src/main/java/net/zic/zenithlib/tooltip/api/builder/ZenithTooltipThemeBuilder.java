package net.zic.zenithlib.tooltip.api.builder;

import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;

import java.util.Objects;

/** Builder for visual theme resources encoded through {@link ZenithTooltipTheme#CODEC}. */
public final class ZenithTooltipThemeBuilder {
    private ZenithTooltipTheme.Palette colors = ZenithTooltipTheme.defaultTheme().colors();
    private ZenithTooltipTheme.Metrics layout = ZenithTooltipTheme.defaultTheme().layout();
    private ZenithTooltipTheme.IconHolder iconHolder = ZenithTooltipTheme.defaultTheme().iconHolder();
    private ZenithTooltipTheme.BarStyle barStyle = ZenithTooltipTheme.defaultTheme().barStyle();
    private ZenithTooltipTheme.BadgeStyle badgeStyle = ZenithTooltipTheme.defaultTheme().badgeStyle();
    private ZenithTooltipTheme.DividerStyle dividerStyle = ZenithTooltipTheme.defaultTheme().dividerStyle();
    private ZenithTooltipTheme.FrameStyle frameStyle = ZenithTooltipTheme.defaultTheme().frameStyle();
    private ZenithTooltipTheme.HeaderStyle headerStyle = ZenithTooltipTheme.defaultTheme().headerStyle();
    private ZenithTooltipTheme.BackgroundStyle backgroundStyle = ZenithTooltipTheme.defaultTheme().backgroundStyle();

    public ZenithTooltipThemeBuilder colors(ZenithTooltipTheme.Palette colors) {
        this.colors = Objects.requireNonNull(colors, "colors");
        return this;
    }

    /** Palette colours use authored RGBA values in the runtime theme schema. */
    public ZenithTooltipThemeBuilder colors(
            String background,
            String borderTop,
            String borderBottom,
            String text,
            String accent,
            String muted,
            String positive,
            String warning,
            String negative
    ) {
        return colors(new ZenithTooltipTheme.Palette(
                rgba(background),
                rgba(borderTop),
                rgba(borderBottom),
                rgba(text),
                rgba(accent),
                rgba(muted),
                rgba(positive),
                rgba(warning),
                rgba(negative)
        ));
    }

    public ZenithTooltipThemeBuilder layout(ZenithTooltipTheme.Metrics layout) {
        this.layout = Objects.requireNonNull(layout, "layout");
        return this;
    }

    public ZenithTooltipThemeBuilder layout(
            int padding,
            int maxWidth,
            int maxHeight,
            int elementGap,
            int rowGap
    ) {
        return layout(new ZenithTooltipTheme.Metrics(padding, maxWidth, maxHeight, elementGap, rowGap));
    }

    public ZenithTooltipThemeBuilder iconHolder(ZenithTooltipTheme.IconHolder iconHolder) {
        this.iconHolder = Objects.requireNonNull(iconHolder, "iconHolder");
        return this;
    }

    public ZenithTooltipThemeBuilder iconHolder(
            ZenithTooltipTheme.Shape shape,
            int boxSize,
            int borderWidth,
            int gap,
            String border,
            String fill,
            int fillAlpha
    ) {
        validateColorReference(border);
        validateColorReference(fill);
        return iconHolder(new ZenithTooltipTheme.IconHolder(shape, boxSize, borderWidth, gap, border, fill, fillAlpha));
    }

    public ZenithTooltipThemeBuilder barStyle(ZenithTooltipTheme.BarStyle barStyle) {
        this.barStyle = Objects.requireNonNull(barStyle, "barStyle");
        return this;
    }

    public ZenithTooltipThemeBuilder barStyle(
            int height,
            int labelGap,
            String track,
            int trackAlpha,
            String border,
            int borderWidth,
            int fillAlpha
    ) {
        validateColorReference(track);
        validateColorReference(border);
        return barStyle(new ZenithTooltipTheme.BarStyle(height, labelGap, track, trackAlpha, border, borderWidth, fillAlpha));
    }

    public ZenithTooltipThemeBuilder badgeStyle(ZenithTooltipTheme.BadgeStyle badgeStyle) {
        this.badgeStyle = Objects.requireNonNull(badgeStyle, "badgeStyle");
        return this;
    }

    public ZenithTooltipThemeBuilder badgeStyle(
            int horizontalPadding,
            int verticalPadding,
            int borderWidth,
            int fillAlpha
    ) {
        return badgeStyle(new ZenithTooltipTheme.BadgeStyle(horizontalPadding, verticalPadding, borderWidth, fillAlpha));
    }

    public ZenithTooltipThemeBuilder dividerStyle(ZenithTooltipTheme.DividerStyle dividerStyle) {
        this.dividerStyle = Objects.requireNonNull(dividerStyle, "dividerStyle");
        return this;
    }

    public ZenithTooltipThemeBuilder dividerStyle(
            int thickness,
            int gapAbove,
            int gapBelow,
            String color,
            ZenithTooltipTheme.Decoration decoration
    ) {
        validateColorReference(color);
        return dividerStyle(new ZenithTooltipTheme.DividerStyle(thickness, gapAbove, gapBelow, color, decoration));
    }

    public ZenithTooltipThemeBuilder frameStyle(ZenithTooltipTheme.FrameStyle frameStyle) {
        this.frameStyle = Objects.requireNonNull(frameStyle, "frameStyle");
        return this;
    }

    public ZenithTooltipThemeBuilder frameStyle(
            ZenithTooltipTheme.CornerDecoration cornerDecoration,
            int cornerSize,
            int cornerInset,
            String cornerColor,
            boolean innerBorder,
            int innerBorderInset,
            String innerBorderColor,
            int innerBorderAlpha
    ) {
        validateColorReference(cornerColor);
        validateColorReference(innerBorderColor);
        return frameStyle(new ZenithTooltipTheme.FrameStyle(
                cornerDecoration,
                cornerSize,
                cornerInset,
                cornerColor,
                innerBorder,
                innerBorderInset,
                innerBorderColor,
                innerBorderAlpha
        ));
    }

    public ZenithTooltipThemeBuilder headerStyle(ZenithTooltipTheme.HeaderStyle headerStyle) {
        this.headerStyle = Objects.requireNonNull(headerStyle, "headerStyle");
        return this;
    }

    public ZenithTooltipThemeBuilder headerStyle(
            ZenithTooltipTheme.Ornament ornament,
            String color
    ) {
        validateColorReference(color);
        return headerStyle(new ZenithTooltipTheme.HeaderStyle(ornament, color));
    }

    public ZenithTooltipThemeBuilder backgroundStyle(ZenithTooltipTheme.BackgroundStyle backgroundStyle) {
        this.backgroundStyle = Objects.requireNonNull(backgroundStyle, "backgroundStyle");
        return this;
    }

    public ZenithTooltipThemeBuilder backgroundStyle(
            ZenithTooltipTheme.Pattern pattern,
            String color,
            int alpha,
            int spacing
    ) {
        validateColorReference(color);
        return backgroundStyle(new ZenithTooltipTheme.BackgroundStyle(pattern, color, alpha, spacing));
    }

    public ZenithTooltipTheme build() {
        return new ZenithTooltipTheme(
                this.colors,
                this.layout,
                this.iconHolder,
                this.barStyle,
                this.badgeStyle,
                this.dividerStyle,
                this.frameStyle,
                this.headerStyle,
                this.backgroundStyle
        );
    }

    private static int rgba(String color) {
        return ZenithTooltipTheme.parseRgbaHex(Objects.requireNonNull(color, "color"));
    }

    private static void validateColorReference(String color) {
        new ZenithTooltipColor(Objects.requireNonNull(color, "color"));
    }
}