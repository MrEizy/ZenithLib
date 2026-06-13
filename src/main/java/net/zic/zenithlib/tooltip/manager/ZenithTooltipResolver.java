package net.zic.zenithlib.tooltip.manager;

import net.minecraft.network.chat.Component;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
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
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElementTypes;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipValue;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipValueSources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves source-backed authored data into an ordinary draw-ready tooltip document.
 *
 * <p>The layout and renderer never need to know which mod supplied a value. Dynamic
 * text becomes runtime component-backed text, dynamic bars become fixed bars, and
 * source-backed collections expand into ordinary badges or rows before the document
 * enters the rendering pipeline.</p>
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
        return new ZenithTooltipPage(resolveText(page.title(), context), elements);
    }

    private static List<ZenithTooltipElement> resolveElement(
            ZenithTooltipElement element,
            ZenithTooltipContext context
    ) {
        if (element instanceof TextElement text) {
            return List.of(new TextElement(resolveText(text.text(), context), text.color(), text.effect()));
        }
        if (element instanceof HeaderElement header) {
            return List.of(new HeaderElement(resolveText(header.text(), context), header.color()));
        }
        if (element instanceof RowElement row) {
            return List.of(new RowElement(
                    resolveText(row.left(), context),
                    resolveText(row.right(), context),
                    row.leftColor(),
                    row.rightColor()
            ));
        }
        if (element instanceof BadgeElement badge) {
            return List.of(new BadgeElement(
                    resolveText(badge.text(), context),
                    badge.textColor(),
                    badge.backgroundColor(),
                    badge.borderColor()
            ));
        }
        if (element instanceof TitleIconElement titleIcon) {
            return List.of(new TitleIconElement(
                    resolveText(titleIcon.title(), context),
                    resolveText(titleIcon.subtitle(), context)
            ));
        }
        if (element instanceof BarElement bar) {
            return List.of(resolveBar(bar, context));
        }
        if (element instanceof CollectionElement collection) {
            return resolveCollection(collection, context);
        }
        if (element instanceof DividerElement
                || element instanceof SpacerElement
                || element instanceof IconElement
                || element instanceof EntityPreviewElement) {
            return List.of(element);
        }
        return ZenithTooltipElementTypes.resolve(element, context);
    }

    private static List<ZenithTooltipElement> resolveCollection(
            CollectionElement collection,
            ZenithTooltipContext context
    ) {
        Optional<ZenithTooltipValue> resolved = ZenithTooltipValueSources.resolve(collection.source(), context);
        List<ZenithTooltipElement> entries = resolved
                .map(value -> resolveCollectionEntries(collection, value))
                .orElseGet(List::of);

        if (entries.isEmpty() && collection.hideWhenEmpty()) {
            return List.of();
        }

        List<ZenithTooltipElement> section = new ArrayList<>();
        if (collection.dividerBefore()) {
            section.add(new DividerElement());
        }
        collection.header().ifPresent(header -> section.add(
                new HeaderElement(resolveText(header, context), ZenithTooltipColor.ACCENT)
        ));
        section.addAll(entries);

        if (collection.dividerAfter() && !section.isEmpty()) {
            section.add(new DividerElement());
        }

        return List.copyOf(section);
    }

    private static List<ZenithTooltipElement> resolveCollectionEntries(
            CollectionElement collection,
            ZenithTooltipValue value
    ) {
        if (collection.presentation() == CollectionElement.Presentation.BADGES) {
            if (value instanceof ZenithTooltipValue.Text text) {
                return List.of(collectionBadge(collection, text.component()));
            }
            if (value instanceof ZenithTooltipValue.TextList list) {
                return list.entries().stream()
                        .map(component -> collectionBadge(collection, component))
                        .map(ZenithTooltipElement.class::cast)
                        .toList();
            }
            return List.of();
        }

        if (collection.presentation() == CollectionElement.Presentation.ROWS
                && value instanceof ZenithTooltipValue.Rows rows) {
            return rows.entries().stream()
                    .map(entry -> new RowElement(
                            ZenithTooltipText.resolved(entry.left()),
                            ZenithTooltipText.resolved(entry.right()),
                            collection.rowLeftColor(),
                            colorFor(entry.tone())
                    ))
                    .map(ZenithTooltipElement.class::cast)
                    .toList();
        }

        return List.of();
    }

    private static BadgeElement collectionBadge(
            CollectionElement collection,
            Component component
    ) {
        return new BadgeElement(
                ZenithTooltipText.resolved(component),
                collection.badgeTextColor(),
                collection.badgeBackgroundColor(),
                collection.badgeBorderColor()
        );
    }

    private static ZenithTooltipColor colorFor(ZenithTooltipValue.Tone tone) {
        return switch (tone) {
            case POSITIVE -> ZenithTooltipColor.POSITIVE;
            case NEGATIVE -> ZenithTooltipColor.NEGATIVE;
            case SPECIAL -> ZenithTooltipColor.ACCENT;
            case NEUTRAL -> ZenithTooltipColor.TEXT;
        };
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
            Optional<ZenithTooltipValue> resolved = ZenithTooltipValueSources.resolve(bar.source(), context);
            if (resolved.isPresent() && resolved.orElseThrow() instanceof ZenithTooltipValue.Progress progress) {
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
                .flatMap(source -> ZenithTooltipValueSources.resolve(source, context));

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
