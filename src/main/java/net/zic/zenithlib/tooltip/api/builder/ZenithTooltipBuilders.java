package net.zic.zenithlib.tooltip.api.builder;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.animation.ScrambleRevealTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect;
import net.zic.zenithlib.tooltip.api.element.BadgeElement;
import net.zic.zenithlib.tooltip.api.element.BarElement;
import net.zic.zenithlib.tooltip.api.element.DividerElement;
import net.zic.zenithlib.tooltip.api.element.HeaderElement;
import net.zic.zenithlib.tooltip.api.element.IconElement;
import net.zic.zenithlib.tooltip.api.element.RowElement;
import net.zic.zenithlib.tooltip.api.element.SpacerElement;
import net.zic.zenithlib.tooltip.api.element.TextElement;
import net.zic.zenithlib.tooltip.api.element.TitleIconElement;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipValueSources;

/**
 * Compact factory helpers for authoring Zenith tooltip resources in Java.
 *
 * <p>The returned objects are the same immutable API records encoded by the runtime
 * codecs. These helpers are intended for datagen and do not register runtime-only
 * tooltip state.</p>
 */
public final class ZenithTooltipBuilders {
    private static final ZenithTooltipText EMPTY_TEXT = ZenithTooltipText.literal("");

    private ZenithTooltipBuilders() {}

    public static Identifier identifier(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static ZenithTooltipText literal(String text) {
        return ZenithTooltipText.literal(text);
    }

    public static ZenithTooltipText translated(String key) {
        return ZenithTooltipText.translatable(key);
    }

    /** Creates text whose runtime value is supplied by a registered source. */
    public static ZenithTooltipText sourced(Identifier source) {
        return ZenithTooltipText.source(source);
    }

    /** Creates text whose runtime value is supplied by a source identifier. */
    public static ZenithTooltipText sourced(String source) {
        return ZenithTooltipText.source(source);
    }

    /** Creates text whose runtime value is supplied by a namespaced source. */
    public static ZenithTooltipText sourced(String namespace, String path) {
        return sourced(identifier(namespace, path));
    }

    public static ZenithTooltipColor color(String tokenOrHex) {
        return new ZenithTooltipColor(tokenOrHex);
    }

    public static ZenithTooltipColor hex(int argb) {
        return ZenithTooltipColor.hex(argb);
    }

    public static ZenithTooltipPageBuilder page(ZenithTooltipText title) {
        return new ZenithTooltipPageBuilder(title);
    }

    public static TextElement text(ZenithTooltipText text) {
        return new TextElement(text, ZenithTooltipColor.TEXT);
    }

    public static TextElement text(ZenithTooltipText text, ZenithTooltipColor color) {
        return new TextElement(text, color);
    }

    public static TextElement text(
            ZenithTooltipText text,
            ZenithTooltipColor color,
            ZenithTooltipTextEffect effect
    ) {
        return TextElement.animated(text, color, effect);
    }

    public static ScrambleRevealTextEffect scrambleReveal(float reveal, int speed) {
        return new ScrambleRevealTextEffect(
                reveal,
                speed,
                ScrambleRevealTextEffect.Mode.SCATTERED,
                ScrambleRevealTextEffect.DEFAULT_GLYPHS
        );
    }

    public static ScrambleRevealTextEffect scrambleReveal(
            float reveal,
            int speed,
            ScrambleRevealTextEffect.Mode mode
    ) {
        return new ScrambleRevealTextEffect(
                reveal,
                speed,
                mode,
                ScrambleRevealTextEffect.DEFAULT_GLYPHS
        );
    }

    public static HeaderElement header(ZenithTooltipText text) {
        return new HeaderElement(text, ZenithTooltipColor.ACCENT);
    }

    public static HeaderElement header(ZenithTooltipText text, ZenithTooltipColor color) {
        return new HeaderElement(text, color);
    }

    public static DividerElement divider() {
        return new DividerElement();
    }

    public static SpacerElement spacer() {
        return new SpacerElement(4);
    }

    public static SpacerElement spacer(int height) {
        return new SpacerElement(height);
    }

    public static RowElement row(ZenithTooltipText left, ZenithTooltipText right) {
        return new RowElement(left, right, ZenithTooltipColor.TEXT, ZenithTooltipColor.ACCENT);
    }

    public static RowElement row(
            ZenithTooltipText left,
            ZenithTooltipText right,
            ZenithTooltipColor leftColor,
            ZenithTooltipColor rightColor
    ) {
        return new RowElement(left, right, leftColor, rightColor);
    }

    public static IconElement icon() {
        return new IconElement();
    }

    public static TitleIconElement titleIcon(ZenithTooltipText title) {
        return new TitleIconElement(title, EMPTY_TEXT);
    }

    public static TitleIconElement titleIcon(ZenithTooltipText title, ZenithTooltipText subtitle) {
        return new TitleIconElement(title, subtitle);
    }

    /** Creates an accent-filled badge with background-coloured text. */
    public static BadgeElement badge(ZenithTooltipText text) {
        return badge(text, ZenithTooltipColor.ACCENT);
    }

    /** Creates a filled badge whose border shares the supplied background colour. */
    public static BadgeElement badge(ZenithTooltipText text, ZenithTooltipColor backgroundColor) {
        return new BadgeElement(text, ZenithTooltipColor.BACKGROUND, backgroundColor, backgroundColor);
    }

    public static BadgeElement badge(
            ZenithTooltipText text,
            ZenithTooltipColor textColor,
            ZenithTooltipColor backgroundColor,
            ZenithTooltipColor borderColor
    ) {
        return new BadgeElement(text, textColor, backgroundColor, borderColor);
    }

    public static BarElement bar(
            ZenithTooltipText label,
            int value,
            int max,
            ZenithTooltipColor color
    ) {
        return new BarElement(label, value, max, EMPTY_TEXT, color);
    }

    public static BarElement bar(
            ZenithTooltipText label,
            int value,
            int max,
            ZenithTooltipText valueText,
            ZenithTooltipColor color
    ) {
        return new BarElement(label, value, max, valueText, color);
    }

    public static BarElement dynamicBar(
            ZenithTooltipText label,
            String source,
            ZenithTooltipColor color
    ) {
        return BarElement.dynamic(label, source, color);
    }

    public static BarElement dynamicBar(
            ZenithTooltipText label,
            Identifier source,
            ZenithTooltipColor color
    ) {
        return BarElement.dynamic(label, source, color);
    }

    public static BarElement durabilityBar(ZenithTooltipText label, ZenithTooltipColor color) {
        return dynamicBar(label, ZenithTooltipValueSources.DURABILITY, color);
    }
}
