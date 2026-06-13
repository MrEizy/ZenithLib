package net.zic.zenithlib.tooltip.api.builder;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.animation.RainbowTextEffect;
import net.zic.zenithlib.tooltip.api.animation.RuneDecipherTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ScrambleRevealTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ShimmerTextEffect;
import net.zic.zenithlib.tooltip.api.animation.TextEffectStack;
import net.zic.zenithlib.tooltip.api.animation.TypewriterTextEffect;
import net.zic.zenithlib.tooltip.api.animation.WaveTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect;
import net.zic.zenithlib.tooltip.api.element.BadgeElement;
import net.zic.zenithlib.tooltip.api.element.BarElement;
import net.zic.zenithlib.tooltip.api.element.CollectionElement;
import net.zic.zenithlib.tooltip.api.element.DividerElement;
import net.zic.zenithlib.tooltip.api.element.EntityPreviewElement;
import net.zic.zenithlib.tooltip.api.element.HeaderElement;
import net.zic.zenithlib.tooltip.api.element.IconElement;
import net.zic.zenithlib.tooltip.api.element.RowElement;
import net.zic.zenithlib.tooltip.api.element.SpacerElement;
import net.zic.zenithlib.tooltip.api.element.TextElement;
import net.zic.zenithlib.tooltip.api.element.TitleIconElement;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipValueSources;

import java.util.List;

/**
 * Compact factory helpers for Zenith tooltip resources in Java.
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

    public static ZenithTooltipText sourced(Identifier source) {
        return ZenithTooltipText.source(source);
    }

    public static ZenithTooltipText sourced(String source) {
        return ZenithTooltipText.source(source);
    }

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


    public static TypewriterTextEffect typewriter() {
        return new TypewriterTextEffect(700, 0, true, false);
    }

    public static TypewriterTextEffect typewriter(int duration, int delay) {
        return new TypewriterTextEffect(duration, delay, true, false);
    }

    public static RuneDecipherTextEffect runeDecipher() {
        return new RuneDecipherTextEffect(950, 0, 45, ScrambleRevealTextEffect.Mode.PREFIX, RuneDecipherTextEffect.DEFAULT_RUNE_GLYPHS);
    }

    public static RuneDecipherTextEffect runeDecipher(int duration, int delay, int speed) {
        return new RuneDecipherTextEffect(duration, delay, speed, ScrambleRevealTextEffect.Mode.PREFIX, RuneDecipherTextEffect.DEFAULT_RUNE_GLYPHS);
    }

    public static ShimmerTextEffect shimmer() {
        return new ShimmerTextEffect(2200, 0.18F, 0.55F, false);
    }

    public static ShimmerTextEffect shimmer(int period, float width, float brightness) {
        return new ShimmerTextEffect(period, width, brightness, false);
    }

    public static RainbowTextEffect rainbow() {
        return rainbow(2400, 0.045F);
    }

    public static RainbowTextEffect rainbow(int period, float spread) {
        return new RainbowTextEffect(
                period,
                spread,
                0.9F,
                1.0F,
                RainbowTextEffect.Mode.SPECTRUM,
                0.0F,
                1.0F,
                false
        );
    }

    public static RainbowTextEffect gradient(
            int period,
            float spread,
            float minHue,
            float maxHue
    ) {
        return new RainbowTextEffect(
                period,
                spread,
                0.9F,
                1.0F,
                RainbowTextEffect.Mode.PING_PONG,
                minHue,
                maxHue,
                false
        );
    }

    public static WaveTextEffect wave() {
        return wave(900, 7.0F, 2);
    }

    public static WaveTextEffect wave(int period, float wavelength, int amplitude) {
        return new WaveTextEffect(
                period,
                wavelength,
                amplitude,
                WaveTextEffect.Mode.BOUNCE,
                false
        );
    }

    public static TextEffectStack combine(ZenithTooltipTextEffect... effects) {
        return new TextEffectStack(List.of(effects));
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

    public static EntityPreviewElement entityPreview() {
        return EntityPreviewElement.automaticSpawnEggPreview();
    }

    public static EntityPreviewElement entityPreview(int width, int height, boolean rotate) {
        return new EntityPreviewElement(width, height, rotate);
    }

    public static TitleIconElement titleIcon(ZenithTooltipText title) {
        return new TitleIconElement(title, EMPTY_TEXT);
    }

    public static TitleIconElement titleIcon(ZenithTooltipText title, ZenithTooltipText subtitle) {
        return new TitleIconElement(title, subtitle);
    }

    public static BadgeElement badge(ZenithTooltipText text) {
        return badge(text, ZenithTooltipColor.ACCENT);
    }

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

    public static CollectionElement dynamicBadges(
            Identifier source,
            ZenithTooltipText header
    ) {
        return CollectionElement.badges(source.toString(), header);
    }

    public static CollectionElement dynamicBadges(
            String source,
            ZenithTooltipText header
    ) {
        return CollectionElement.badges(source, header);
    }

    public static CollectionElement dynamicRows(
            Identifier source,
            ZenithTooltipText header
    ) {
        return CollectionElement.rows(source.toString(), header);
    }

    public static CollectionElement dynamicRows(
            String source,
            ZenithTooltipText header
    ) {
        return CollectionElement.rows(source, header);
    }

    public static BarElement durabilityBar(ZenithTooltipText label, ZenithTooltipColor color) {
        return dynamicBar(label, ZenithTooltipValueSources.DURABILITY, color);
    }
}
