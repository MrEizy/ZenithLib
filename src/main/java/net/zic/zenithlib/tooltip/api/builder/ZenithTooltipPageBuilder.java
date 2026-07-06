package net.zic.zenithlib.tooltip.api.builder;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.ZenithTooltipInlineIcon;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect;
import net.zic.zenithlib.tooltip.api.condition.ZenithTooltipConditions;
import net.zic.zenithlib.tooltip.api.element.BadgeElement;
import net.zic.zenithlib.tooltip.api.element.BadgeRowElement;
import net.zic.zenithlib.tooltip.api.element.ClassificationElement;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Builder for one ordered tooltip page. */
public final class ZenithTooltipPageBuilder {
    private final ZenithTooltipText title;
    private ZenithTooltipTextEffect titleEffect;
    private final List<ZenithTooltipElement> elements = new java.util.ArrayList<>();

    public ZenithTooltipPageBuilder(ZenithTooltipText title) {
        this.title = Objects.requireNonNull(title, "title");
    }

    public ZenithTooltipPageBuilder titleEffect(ZenithTooltipTextEffect effect) {
        this.titleEffect = Objects.requireNonNull(effect, "effect");
        return this;
    }

    public ZenithTooltipPageBuilder add(ZenithTooltipElement element) {
        this.elements.add(Objects.requireNonNull(element, "element"));
        return this;
    }

    public ZenithTooltipPageBuilder addIf(boolean condition, ZenithTooltipElement element) {
        return condition ? add(element) : this;
    }

    public ZenithTooltipPageBuilder addIf(BooleanSupplier condition, ZenithTooltipElement element) {
        return addIf(Objects.requireNonNull(condition, "condition").getAsBoolean(), element);
    }

    public ZenithTooltipPageBuilder addAll(ZenithTooltipElement... elements) {
        Arrays.stream(elements).forEach(this::add);
        return this;
    }

    public ZenithTooltipPageBuilder addAll(Collection<? extends ZenithTooltipElement> elements) {
        Objects.requireNonNull(elements, "elements").forEach(this::add);
        return this;
    }

    public ZenithTooltipPageBuilder configure(Consumer<ZenithTooltipPageBuilder> action) {
        Objects.requireNonNull(action, "action").accept(this);
        return this;
    }

    public ZenithTooltipPageBuilder section(ZenithTooltipText heading, ZenithTooltipElement... body) {
        header(heading);
        addAll(body);
        return this;
    }

    public ZenithTooltipPageBuilder text(ZenithTooltipText text) {
        return add(ZenithTooltipBuilders.text(text));
    }

    public ZenithTooltipPageBuilder text(String translationKey) {
        return text(ZenithTooltipBuilders.t(translationKey));
    }

    public ZenithTooltipPageBuilder text(ZenithTooltipText text, ZenithTooltipColor color) {
        return add(ZenithTooltipBuilders.text(text, color));
    }

    public ZenithTooltipPageBuilder text(ZenithTooltipText text, ZenithTooltipColor color, ZenithTooltipTextEffect effect) {
        return add(ZenithTooltipBuilders.text(text, color, effect));
    }

    public ZenithTooltipPageBuilder muted(ZenithTooltipText text) {
        return text(text, ZenithTooltipColor.MUTED);
    }

    public ZenithTooltipPageBuilder accent(ZenithTooltipText text) {
        return text(text, ZenithTooltipColor.ACCENT);
    }

    public ZenithTooltipPageBuilder header(ZenithTooltipText text) {
        return add(ZenithTooltipBuilders.header(text));
    }

    public ZenithTooltipPageBuilder header(ZenithTooltipText text, ZenithTooltipColor color) {
        return add(ZenithTooltipBuilders.header(text, color));
    }

    public ZenithTooltipPageBuilder header(ZenithTooltipText text, ZenithTooltipColor color, ZenithTooltipTextEffect effect) {
        return add(ZenithTooltipBuilders.header(text, color, effect));
    }

    public ZenithTooltipPageBuilder divider() {
        return add(ZenithTooltipBuilders.divider());
    }

    public ZenithTooltipPageBuilder spacer() {
        return add(ZenithTooltipBuilders.spacer());
    }

    public ZenithTooltipPageBuilder spacer(int height) {
        return add(ZenithTooltipBuilders.spacer(height));
    }

    public ZenithTooltipPageBuilder row(ZenithTooltipText left, ZenithTooltipText right) {
        return add(ZenithTooltipBuilders.row(left, right));
    }

    public ZenithTooltipPageBuilder row(
            ZenithTooltipText left,
            ZenithTooltipText right,
            ZenithTooltipColor leftColor,
            ZenithTooltipColor rightColor
    ) {
        return add(ZenithTooltipBuilders.row(left, right, leftColor, rightColor));
    }

    public ZenithTooltipPageBuilder row(
            ZenithTooltipInlineIcon icon,
            ZenithTooltipText left,
            ZenithTooltipText right,
            ZenithTooltipColor leftColor,
            ZenithTooltipColor rightColor
    ) {
        return add(ZenithTooltipBuilders.row(icon, left, right, leftColor, rightColor));
    }

    public ZenithTooltipPageBuilder badge(ZenithTooltipText text) {
        return add(ZenithTooltipBuilders.badge(text));
    }

    public ZenithTooltipPageBuilder badge(ZenithTooltipText text, ZenithTooltipColor backgroundColor) {
        return add(ZenithTooltipBuilders.badge(text, backgroundColor));
    }

    public ZenithTooltipPageBuilder badgeRow(BadgeElement... badges) {
        return add(ZenithTooltipBuilders.badgeRow(badges));
    }

    public ZenithTooltipPageBuilder badgeRow(BadgeRowElement row) {
        return add(row);
    }

    public ZenithTooltipPageBuilder classification() {
        return add(ZenithTooltipBuilders.classification());
    }

    public ZenithTooltipPageBuilder classificationRows() {
        return add(ZenithTooltipBuilders.classificationRows());
    }

    public ZenithTooltipPageBuilder classificationBadge() {
        return add(ZenithTooltipBuilders.classificationBadge());
    }

    public ZenithTooltipPageBuilder classification(
            boolean showCategory,
            boolean showRank,
            ClassificationElement.Style style
    ) {
        return add(ZenithTooltipBuilders.classification(showCategory, showRank, style));
    }

    public ZenithTooltipPageBuilder bar(ZenithTooltipText label, int value, int max, ZenithTooltipColor color) {
        return add(ZenithTooltipBuilders.bar(label, value, max, color));
    }

    public ZenithTooltipPageBuilder bar(
            ZenithTooltipText label,
            int value,
            int max,
            ZenithTooltipText valueText,
            ZenithTooltipColor color
    ) {
        return add(ZenithTooltipBuilders.bar(label, value, max, valueText, color));
    }

    public ZenithTooltipPageBuilder dynamicBar(ZenithTooltipText label, String source, ZenithTooltipColor color) {
        return add(ZenithTooltipBuilders.dynamicBar(label, source, color));
    }

    public ZenithTooltipPageBuilder dynamicBar(ZenithTooltipText label, Identifier source, ZenithTooltipColor color) {
        return add(ZenithTooltipBuilders.dynamicBar(label, source, color));
    }
    public ZenithTooltipPageBuilder sourcedBar(
            ZenithTooltipText label,
            String valueSource,
            String maxSource,
            ZenithTooltipColor color
    ) {
        return add(ZenithTooltipBuilders.sourcedBar(label, valueSource, maxSource, color));
    }

    public ZenithTooltipPageBuilder sourcedBar(
            ZenithTooltipText label,
            Identifier valueSource,
            Identifier maxSource,
            ZenithTooltipColor color
    ) {
        return add(ZenithTooltipBuilders.sourcedBar(label, valueSource, maxSource, color));
    }


    public ZenithTooltipPageBuilder durabilityBar(ZenithTooltipText label, ZenithTooltipColor color) {
        return add(ZenithTooltipBuilders.durabilityBar(label, color));
    }

    public ZenithTooltipPageBuilder dynamic(String source) {
        return add(ZenithTooltipBuilders.dynamic(source));
    }

    public ZenithTooltipPageBuilder dynamic(Identifier source) {
        return add(ZenithTooltipBuilders.dynamic(source));
    }

    public ZenithTooltipPageBuilder icon() {
        return add(ZenithTooltipBuilders.icon());
    }

    public ZenithTooltipPageBuilder entityPreview() {
        return add(ZenithTooltipBuilders.entityPreview());
    }

    public ZenithTooltipPageBuilder titleIcon(ZenithTooltipText title) {
        return add(ZenithTooltipBuilders.titleIcon(title));
    }

    public ZenithTooltipPageBuilder titleIcon(ZenithTooltipText title, boolean onAllPages) {
        return add(ZenithTooltipBuilders.titleIcon(title, onAllPages));
    }

    public ZenithTooltipPageBuilder titleIcon(ZenithTooltipText title, ZenithTooltipText subtitle) {
        return add(ZenithTooltipBuilders.titleIcon(title, subtitle));
    }

    public ZenithTooltipPageBuilder titleIcon(
            ZenithTooltipText title,
            ZenithTooltipText subtitle,
            boolean onAllPages
    ) {
        return add(ZenithTooltipBuilders.titleIcon(title, subtitle, onAllPages));
    }

    public ZenithTooltipPageBuilder titleIcon(
            ZenithTooltipText title,
            ZenithTooltipText subtitle,
            boolean onAllPages,
            ZenithTooltipTextEffect titleEffect
    ) {
        return add(ZenithTooltipBuilders.titleIcon(title, subtitle, onAllPages, titleEffect));
    }

    public ZenithTooltipPageBuilder section(Identifier condition, ZenithTooltipElement... elements) {
        return add(ZenithTooltipBuilders.section(condition, List.of(elements)));
    }

    public ZenithTooltipPageBuilder section(String condition, ZenithTooltipElement... elements) {
        return section(ZenithTooltipConditions.identifier(condition), elements);
    }

    public ZenithTooltipPageBuilder shiftSection(ZenithTooltipElement... elements) {
        return section(ZenithTooltipConditions.SHIFT_DOWN, elements);
    }

    public ZenithTooltipPage build() {
        return new ZenithTooltipPage(this.title, java.util.Optional.ofNullable(this.titleEffect), List.copyOf(this.elements));
    }
}
