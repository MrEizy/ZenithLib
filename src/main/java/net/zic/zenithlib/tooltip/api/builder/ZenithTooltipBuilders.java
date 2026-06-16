package net.zic.zenithlib.tooltip.api.builder;

import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipInlineIcon;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.animation.RainbowTextEffect;
import net.zic.zenithlib.tooltip.api.animation.RuneDecipherTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ScrambleRevealTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ShimmerTextEffect;
import net.zic.zenithlib.tooltip.api.animation.TextEffectStack;
import net.zic.zenithlib.tooltip.api.animation.TypewriterTextEffect;
import net.zic.zenithlib.tooltip.api.animation.WaveTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect;
import net.zic.zenithlib.tooltip.api.condition.ZenithTooltipConditions;
import net.zic.zenithlib.tooltip.api.element.BadgeElement;
import net.zic.zenithlib.tooltip.api.element.BarElement;
import net.zic.zenithlib.tooltip.api.element.ClassificationElement;
import net.zic.zenithlib.tooltip.api.element.DividerElement;
import net.zic.zenithlib.tooltip.api.element.DynamicElement;
import net.zic.zenithlib.tooltip.api.element.EntityPreviewElement;
import net.zic.zenithlib.tooltip.api.element.HeaderElement;
import net.zic.zenithlib.tooltip.api.element.IconElement;
import net.zic.zenithlib.tooltip.api.element.RowElement;
import net.zic.zenithlib.tooltip.api.element.SectionElement;
import net.zic.zenithlib.tooltip.api.element.SpacerElement;
import net.zic.zenithlib.tooltip.api.element.TextElement;
import net.zic.zenithlib.tooltip.api.element.TitleIconElement;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipSources;

import java.util.List;
import java.util.Objects;

/**
 * Compact factory helpers for Zenith tooltip resources in Java.
 */
public final class ZenithTooltipBuilders {
    private static final ZenithTooltipText EMPTY_TEXT = ZenithTooltipText.literal("");

    private ZenithTooltipBuilders() {}

    public static Identifier identifier(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static Identifier identifier(String id) {
        return Identifier.parse(Objects.requireNonNull(id, "id"));
    }

    public static Identifier sourceId(String source) {
        return ZenithTooltipSources.identifier(source);
    }

    public static ZenithTooltipInlineIcon inlineIcon(Identifier texture) {
        return new ZenithTooltipInlineIcon(texture);
    }

    public static ZenithTooltipInlineIcon inlineIcon(String texture) {
        return inlineIcon(identifier(texture));
    }

    public static ZenithTooltipInlineIcon inlineIcon(Identifier texture, int size) {
        return new ZenithTooltipInlineIcon(texture, size, 16, 16);
    }

    public static ZenithTooltipText literal(String text) {
        return ZenithTooltipText.literal(text);
    }

    public static ZenithTooltipText translated(String key) {
        return ZenithTooltipText.translatable(key);
    }

    public static ZenithTooltipText t(String key) {
        return translated(key);
    }

    public static ZenithTooltipText lit(String text) {
        return literal(text);
    }

    public static ZenithTooltipText component(Component component) {
        return ZenithTooltipText.resolved(component);
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

    public static ZenithTooltipText src(String source) {
        return sourced(source);
    }

    public static ZenithTooltipText src(Identifier source) {
        return sourced(source);
    }

    public static ZenithTooltipText itemName() {
        return sourced(ZenithTooltipSources.ITEM_NAME);
    }

    public static ZenithTooltipText itemId() {
        return sourced(ZenithTooltipSources.ITEM_ID);
    }

    public static ZenithTooltipText subjectName() {
        return sourced(ZenithTooltipSources.SUBJECT_NAME);
    }

    public static ZenithTooltipText subjectDescription() {
        return sourced(ZenithTooltipSources.SUBJECT_DESCRIPTION);
    }

    public static ZenithTooltipText subjectId() {
        return sourced(ZenithTooltipSources.SUBJECT_ID);
    }

    public static ZenithTooltipText controls() {
        return translated("tooltip.zenithlib.controls");
    }

    public static ZenithTooltipColor color(String tokenOrHex) {
        return new ZenithTooltipColor(tokenOrHex);
    }

    public static ZenithTooltipColor hex(int argb) {
        return ZenithTooltipColor.hex(argb);
    }

    public static ZenithTooltipColor textColor() { return ZenithTooltipColor.TEXT; }
    public static ZenithTooltipColor mutedColor() { return ZenithTooltipColor.MUTED; }
    public static ZenithTooltipColor accentColor() { return ZenithTooltipColor.ACCENT; }
    public static ZenithTooltipColor positiveColor() { return ZenithTooltipColor.POSITIVE; }
    public static ZenithTooltipColor warningColor() { return ZenithTooltipColor.WARNING; }
    public static ZenithTooltipColor negativeColor() { return ZenithTooltipColor.NEGATIVE; }

    public static ZenithTooltipPageBuilder page(ZenithTooltipText title) {
        return new ZenithTooltipPageBuilder(title);
    }

    public static TextElement text(ZenithTooltipText text) {
        return new TextElement(text, ZenithTooltipColor.TEXT);
    }

    public static TextElement text(String translationKey) {
        return text(t(translationKey));
    }

    public static TextElement literalText(String text) {
        return text(lit(text));
    }

    public static TextElement muted(ZenithTooltipText text) {
        return text(text, ZenithTooltipColor.MUTED);
    }

    public static TextElement accent(ZenithTooltipText text) {
        return text(text, ZenithTooltipColor.ACCENT);
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

    public static HeaderElement header(ZenithTooltipText text, ZenithTooltipColor color, ZenithTooltipTextEffect effect) {
        return new HeaderElement(text, color, java.util.Optional.of(effect));
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

    public static RowElement row(
            ZenithTooltipInlineIcon icon,
            ZenithTooltipText left,
            ZenithTooltipText right,
            ZenithTooltipColor leftColor,
            ZenithTooltipColor rightColor
    ) {
        return new RowElement(left, right, leftColor, rightColor, java.util.Optional.of(icon));
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
        return titleIcon(title, EMPTY_TEXT, false);
    }

    public static TitleIconElement titleIcon(ZenithTooltipText title, boolean onAllPages) {
        return titleIcon(title, EMPTY_TEXT, onAllPages);
    }

    public static TitleIconElement titleIcon(ZenithTooltipText title, ZenithTooltipText subtitle) {
        return titleIcon(title, subtitle, false);
    }

    public static TitleIconElement titleIcon(
            ZenithTooltipText title,
            ZenithTooltipText subtitle,
            boolean onAllPages
    ) {
        return new TitleIconElement(title, subtitle, onAllPages);
    }

    public static TitleIconElement titleIcon(
            ZenithTooltipText title,
            ZenithTooltipText subtitle,
            boolean onAllPages,
            ZenithTooltipTextEffect titleEffect
    ) {
        return new TitleIconElement(title, subtitle, onAllPages, java.util.Optional.of(titleEffect), java.util.Optional.empty());
    }

    public static BadgeElement badge(ZenithTooltipText text) {
        return badge(text, ZenithTooltipColor.ACCENT);
    }

    public static BadgeElement badge(ZenithTooltipText text, ZenithTooltipColor backgroundColor) {
        return new BadgeElement(text, ZenithTooltipColor.BACKGROUND, backgroundColor, backgroundColor);
    }

    public static ClassificationElement classificationCategory() {
        return ClassificationElement.categoryRows();
    }

    public static ClassificationElement classificationRank() {
        return ClassificationElement.rankRows();
    }

    public static ClassificationElement classificationCategoryRows() {
        return ClassificationElement.categoryRows();
    }

    public static ClassificationElement classificationRankRows() {
        return ClassificationElement.rankRows();
    }

    public static ClassificationElement classificationCategoryBadge() {
        return ClassificationElement.categoryBadge();
    }

    public static ClassificationElement classificationRankBadge() {
        return ClassificationElement.rankBadge();
    }

    public static BadgeElement badge(
            ZenithTooltipText text,
            ZenithTooltipColor textColor,
            ZenithTooltipColor backgroundColor,
            ZenithTooltipColor borderColor
    ) {
        return new BadgeElement(text, textColor, backgroundColor, borderColor);
    }

    public static BadgeElement badge(
            ZenithTooltipInlineIcon icon,
            ZenithTooltipText text,
            ZenithTooltipColor textColor,
            ZenithTooltipColor backgroundColor,
            ZenithTooltipColor borderColor
    ) {
        return new BadgeElement(text, textColor, backgroundColor, borderColor, java.util.Optional.of(icon), java.util.Optional.empty());
    }

    public static BadgeElement badge(
            ZenithTooltipText text,
            ZenithTooltipColor textColor,
            ZenithTooltipColor backgroundColor,
            ZenithTooltipColor borderColor,
            ZenithTooltipTextEffect effect
    ) {
        return new BadgeElement(text, textColor, backgroundColor, borderColor, java.util.Optional.empty(), java.util.Optional.of(effect));
    }

    public static ClassificationElement classification() {
        return ClassificationElement.rows();
    }

    public static ClassificationElement classificationRows() {
        return ClassificationElement.rows();
    }

    public static ClassificationElement classificationBadge() {
        return ClassificationElement.badge();
    }

    public static ClassificationElement classification(
            boolean showCategory,
            boolean showRank,
            ClassificationElement.Style style
    ) {
        return new ClassificationElement(
                showCategory,
                showRank,
                Objects.requireNonNull(style, "style"),
                translated("tooltip.zenithlib.classification.category"),
                translated("tooltip.zenithlib.classification.rank"),
                ZenithTooltipColor.MUTED
        );
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

    public static BarElement sourcedBar(
            ZenithTooltipText label,
            String valueSource,
            String maxSource,
            ZenithTooltipColor color
    ) {
        return BarElement.sourced(label, valueSource, maxSource, color);
    }

    public static BarElement sourcedBar(
            ZenithTooltipText label,
            Identifier valueSource,
            Identifier maxSource,
            ZenithTooltipColor color
    ) {
        return sourcedBar(label, valueSource.toString(), maxSource.toString(), color);
    }


    public static SectionElement section(Identifier condition, List<net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement> elements) {
        return new SectionElement(condition, elements);
    }

    public static SectionElement section(String condition, List<net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement> elements) {
        return section(ZenithTooltipConditions.identifier(condition), elements);
    }

    public static SectionElement shiftSection(List<net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement> elements) {
        return section(ZenithTooltipConditions.SHIFT_DOWN, elements);
    }

    public static DynamicElement dynamic(Identifier source) {
        return new DynamicElement(source);
    }

    public static DynamicElement dynamic(String source) {
        return new DynamicElement(ZenithTooltipSources.identifier(source));
    }

    public static DynamicElement dynamicSection(String source) {
        return dynamic(source);
    }

    public static DynamicElement dynamicSection(Identifier source) {
        return dynamic(source);
    }

    public static DynamicElement dynamic(Identifier source, List<net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement> fallback) {
        return new DynamicElement(source, fallback, true);
    }

    public static BarElement durabilityBar(ZenithTooltipText label, ZenithTooltipColor color) {
        return dynamicBar(label, ZenithTooltipSources.DURABILITY, color);
    }
}
