package net.zic.zenithlib.tooltip.manager;

import net.minecraft.network.chat.Component;
import net.zic.zenithlib.classification.ZenithClassification;
import net.zic.zenithlib.classification.ZenithClassifications;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
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
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElementTypes;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipSources;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves source-backed authored data into an ordinary draw-ready tooltip document.
 */
public final class ZenithTooltipResolver {
    private ZenithTooltipResolver() {}

    public static ZenithTooltipDocument resolve(
            ZenithTooltipDocument document,
            ZenithTooltipContext context
    ) {
        List<ZenithTooltipPage> pages = new ArrayList<>(document.pages().size());
        for (ZenithTooltipPage page : document.pages()) {
            pages.add(resolvePage(page, context));
        }
        return new ZenithTooltipDocument(document.theme(), pages, document.animationPresets());
    }

    private static ZenithTooltipPage resolvePage(
            ZenithTooltipPage page,
            ZenithTooltipContext context
    ) {
        List<ZenithTooltipElement> elements = new ArrayList<>(page.elements().size());
        for (ZenithTooltipElement element : page.elements()) {
            elements.addAll(resolveElement(element, context));
        }
        return new ZenithTooltipPage(resolveText(page.title(), context), page.titleEffect(), elements);
    }

    private static List<ZenithTooltipElement> resolveElement(
            ZenithTooltipElement element,
            ZenithTooltipContext context
    ) {
        if (element instanceof DynamicElement dynamic) {
            return resolveDynamic(dynamic, context);
        }
        if (element instanceof SectionElement section) {
            return resolveSection(section, context);
        }
        if (element instanceof TextElement text) {
            return List.of(new TextElement(resolveText(text.text(), context), text.color(), text.effect()));
        }
        if (element instanceof HeaderElement header) {
            return List.of(new HeaderElement(resolveText(header.text(), context), header.color(), header.effect()));
        }
        if (element instanceof RowElement row) {
            return List.of(new RowElement(
                    resolveText(row.left(), context),
                    resolveText(row.right(), context),
                    row.leftColor(),
                    row.rightColor(),
                    row.icon()
            ));
        }
        if (element instanceof BadgeElement badge) {
            return List.of(new BadgeElement(
                    resolveText(badge.text(), context),
                    badge.textColor(),
                    badge.backgroundColor(),
                    badge.borderColor(),
                    badge.icon(),
                    badge.effect()
            ));
        }
        if (element instanceof ClassificationElement classification) {
            return resolveClassification(classification, context);
        }
        if (element instanceof TitleIconElement titleIcon) {
            return List.of(new TitleIconElement(
                    resolveText(titleIcon.title(), context),
                    resolveText(titleIcon.subtitle(), context),
                    titleIcon.onAllPages(),
                    titleIcon.titleEffect(),
                    titleIcon.subtitleEffect()
            ));
        }
        if (element instanceof BarElement bar) {
            return List.of(resolveBar(bar, context));
        }
        if (element instanceof DividerElement
                || element instanceof SpacerElement
                || element instanceof IconElement
                || element instanceof EntityPreviewElement) {
            return List.of(element);
        }
        return resolveResolvedElements(element, ZenithTooltipElementTypes.resolve(element, context), context);
    }


    private static List<ZenithTooltipElement> resolveSection(
            SectionElement section,
            ZenithTooltipContext context
    ) {
        if (!conditionMatches(section.condition(), context)) {
            return List.of();
        }

        return resolveResolvedElements(section, section.elements(), context);
    }

    private static boolean conditionMatches(
            net.minecraft.resources.Identifier condition,
            ZenithTooltipContext context
    ) {
        String namespace = condition.getNamespace();
        String path = condition.getPath();

        if (!"zenithlib".equals(namespace)) {
            return context.data(condition, Boolean.class).orElse(false);
        }

        return switch (path) {
            case "always" -> true;
            case "never" -> false;
            case "shift_down" -> modifier(context, "shift_down");
            case "ctrl_down", "control_down" -> modifier(context, "ctrl_down");
            case "alt_down" -> modifier(context, "alt_down");
            case "not_shift_down" -> !modifier(context, "shift_down");
            case "not_ctrl_down", "not_control_down" -> !modifier(context, "ctrl_down");
            case "not_alt_down" -> !modifier(context, "alt_down");
            default -> context.data(condition, Boolean.class).orElse(false);
        };
    }

    private static boolean modifier(ZenithTooltipContext context, String path) {
        return context.data(net.minecraft.resources.Identifier.fromNamespaceAndPath("zenithlib", path), Boolean.class).orElse(false);
    }

    private static List<ZenithTooltipElement> resolveClassification(
            ClassificationElement element,
            ZenithTooltipContext context
    ) {
        Optional<ZenithClassification> resolved = ZenithClassifications.get(context.stack(), context.itemId());
        if (resolved.isEmpty()) {
            return List.of();
        }

        ZenithClassification classification = resolved.orElseThrow();
        Optional<ZenithClassification.Category> category = element.showCategory()
                ? classification.category()
                : Optional.empty();
        Optional<ZenithClassification.Rank> rank = element.showRank()
                ? classification.rank()
                : Optional.empty();

        if (category.isEmpty() && rank.isEmpty()) {
            return List.of();
        }

        if (element.style() == ClassificationElement.Style.BADGE) {
            return resolveClassificationBadge(category, rank, context);
        }

        List<ZenithTooltipElement> elements = new ArrayList<>();
        category.ifPresent(value -> elements.add(new RowElement(
                resolveText(element.categoryLabel(), context),
                resolveText(value.label(), context),
                element.labelColor(),
                value.color()
        )));
        rank.ifPresent(value -> elements.add(new RowElement(
                resolveText(element.rankLabel(), context),
                resolveText(value.label(), context),
                element.labelColor(),
                value.color()
        )));
        return List.copyOf(elements);
    }

    private static List<ZenithTooltipElement> resolveClassificationBadge(
            Optional<ZenithClassification.Category> category,
            Optional<ZenithClassification.Rank> rank,
            ZenithTooltipContext context
    ) {
        Component text = Component.empty();
        if (category.isPresent()) {
            text = resolveText(category.orElseThrow().label(), context).component();
        }
        if (rank.isPresent()) {
            if (!text.getString().isBlank()) {
                text = text.copy().append(Component.literal(" • "));
            }
            text = text.copy().append(resolveText(rank.orElseThrow().label(), context).component());
        }

        if (text.getString().isBlank()) {
            return List.of();
        }

        net.zic.zenithlib.tooltip.api.ZenithTooltipColor accent = rank
                .map(ZenithClassification.Rank::color)
                .orElseGet(() -> category.map(ZenithClassification.Category::color)
                        .orElse(net.zic.zenithlib.tooltip.api.ZenithTooltipColor.ACCENT));

        return List.of(new BadgeElement(
                ZenithTooltipText.resolved(text),
                net.zic.zenithlib.tooltip.api.ZenithTooltipColor.BACKGROUND,
                accent,
                accent
        ));
    }

    private static List<ZenithTooltipElement> resolveDynamic(
            DynamicElement dynamic,
            ZenithTooltipContext context
    ) {
        List<ZenithTooltipElement> resolved = ZenithTooltipSources.resolveElements(dynamic.source(), context)
                .orElseGet(dynamic::fallback);

        if (resolved.isEmpty() && dynamic.hideWhenEmpty()) {
            return List.of();
        }

        return resolveResolvedElements(dynamic, resolved, context);
    }

    private static List<ZenithTooltipElement> resolveResolvedElements(
            ZenithTooltipElement original,
            List<ZenithTooltipElement> resolved,
            ZenithTooltipContext context
    ) {
        if (resolved.isEmpty()) {
            return List.of();
        }
        if (resolved.size() == 1 && resolved.get(0) == original) {
            return List.of(original);
        }

        List<ZenithTooltipElement> flattened = new ArrayList<>();
        for (ZenithTooltipElement element : resolved) {
            if (element == original) {
                flattened.add(element);
            } else {
                flattened.addAll(resolveElement(element, context));
            }
        }
        return List.copyOf(flattened);
    }


    private static BarElement resolveBar(
            BarElement bar,
            ZenithTooltipContext context
    ) {
        ZenithTooltipText label = resolveText(bar.label(), context);
        ZenithTooltipText valueText = resolveText(bar.valueText(), context);
        int value = bar.value();
        int max = bar.max();

        if (bar.isDynamic()) {
            Optional<ZenithTooltipValue> resolved = ZenithTooltipSources.resolveValue(bar.source(), context);
            if (resolved.orElse(null) instanceof ZenithTooltipValue.Progress progress) {
                value = progress.value();
                max = progress.max();

                if (valueText.isBlank() && progress.displayText().isPresent()) {
                    valueText = ZenithTooltipText.resolved(progress.displayText().orElseThrow());
                }
            }
        }

        return new BarElement(label, value, max, valueText, bar.color(), "");
    }

    private static ZenithTooltipText resolveText(
            ZenithTooltipText text,
            ZenithTooltipContext context
    ) {
        if (!text.isDynamic()) {
            return text;
        }

        Optional<ZenithTooltipValue> resolved = text.source()
                .flatMap(source -> ZenithTooltipSources.resolveValue(source, context));

        if (resolved.isEmpty()) {
            return ZenithTooltipText.literal("");
        }

        ZenithTooltipValue value = resolved.orElseThrow();
        if (value instanceof ZenithTooltipValue.Text textValue) {
            return ZenithTooltipText.resolved(textValue.component());
        }
        if (value instanceof ZenithTooltipValue.Progress progress) {
            Component display = progress.displayText()
                    .orElseGet(() -> Component.literal(progress.value() + " / " + progress.max()));
            return ZenithTooltipText.resolved(display);
        }

        return ZenithTooltipText.literal("");
    }
}
