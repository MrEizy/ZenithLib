package net.zic.zenithlib.tooltip.manager;

import net.minecraft.network.chat.Component;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.element.BadgeElement;
import net.zic.zenithlib.tooltip.api.element.BarElement;
import net.zic.zenithlib.tooltip.api.element.DividerElement;
import net.zic.zenithlib.tooltip.api.element.EntityPreviewElement;
import net.zic.zenithlib.tooltip.api.element.HeaderElement;
import net.zic.zenithlib.tooltip.api.element.IconElement;
import net.zic.zenithlib.tooltip.api.element.RowElement;
import net.zic.zenithlib.tooltip.api.element.SpacerElement;
import net.zic.zenithlib.tooltip.api.element.TextElement;
import net.zic.zenithlib.tooltip.api.element.TitleIconElement;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipValue;
import net.zic.zenithlib.tooltip.api.value.ZenithTooltipValueSources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves source-backed authored data into an ordinary draw-ready tooltip document.
 *
 * <p>The layout and renderer never need to know which mod supplied a value. Dynamic
 * text becomes runtime component-backed text and dynamic bars become fixed bars before
 * the document enters the rendering pipeline.</p>
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
        return new ZenithTooltipDocument(document.theme(), pages);
    }

    private static ZenithTooltipPage resolvePage(
            ZenithTooltipPage page,
            ZenithTooltipContext context
    ) {
        List<ZenithTooltipElement> elements = new ArrayList<>(page.elements().size());
        for (ZenithTooltipElement element : page.elements()) {
            elements.add(resolveElement(element, context));
        }
        return new ZenithTooltipPage(resolveText(page.title(), context), elements);
    }

    private static ZenithTooltipElement resolveElement(
            ZenithTooltipElement element,
            ZenithTooltipContext context
    ) {
        if (element instanceof TextElement text) {
            return new TextElement(resolveText(text.text(), context), text.color(), text.effect());
        }
        if (element instanceof HeaderElement header) {
            return new HeaderElement(resolveText(header.text(), context), header.color());
        }
        if (element instanceof RowElement row) {
            return new RowElement(
                    resolveText(row.left(), context),
                    resolveText(row.right(), context),
                    row.leftColor(),
                    row.rightColor()
            );
        }
        if (element instanceof BadgeElement badge) {
            return new BadgeElement(
                    resolveText(badge.text(), context),
                    badge.textColor(),
                    badge.backgroundColor(),
                    badge.borderColor()
            );
        }
        if (element instanceof TitleIconElement titleIcon) {
            return new TitleIconElement(
                    resolveText(titleIcon.title(), context),
                    resolveText(titleIcon.subtitle(), context)
            );
        }
        if (element instanceof BarElement bar) {
            return resolveBar(bar, context);
        }
        if (element instanceof DividerElement
                || element instanceof SpacerElement
                || element instanceof IconElement
                || element instanceof EntityPreviewElement) {
            return element;
        }
        return element;
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
