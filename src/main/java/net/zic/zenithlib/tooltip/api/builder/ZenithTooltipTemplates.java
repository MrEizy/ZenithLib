package net.zic.zenithlib.tooltip.api.builder;

import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.badge;
import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.bar;
import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.divider;
import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.header;
import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.icon;
import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.page;
import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.row;
import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.spacer;
import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.text;
import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.titleIcon;

/**
 * Beginner-friendly template to create simple Zenith tooltip pages and elements.
 */
public final class ZenithTooltipTemplates {
    private ZenithTooltipTemplates() {}

    public static ZenithTooltipTemplateBuilder simpleTitleBody(ZenithTooltipText title, ZenithTooltipText body) {
        return new ZenithTooltipTemplateBuilder()
                .page(page(title)
                        .add(text(body)));
    }

    public static ZenithTooltipTemplateBuilder headerBodyFooter(
            ZenithTooltipText title,
            ZenithTooltipText heading,
            ZenithTooltipText body,
            ZenithTooltipText footer
    ) {
        return new ZenithTooltipTemplateBuilder()
                .page(page(title)
                        .add(header(heading))
                        .add(text(body))
                        .add(divider())
                        .add(text(footer, ZenithTooltipColor.MUTED)));
    }

    public static ZenithTooltipTemplateBuilder iconTitleSummary(
            ZenithTooltipText title,
            ZenithTooltipText subtitle,
            ZenithTooltipText summary
    ) {
        return new ZenithTooltipTemplateBuilder()
                .page(page(ZenithTooltipText.literal(""))
                        .add(titleIcon(title, subtitle))
                        .add(spacer(2))
                        .add(text(summary)));
    }

    public static ZenithTooltipTemplateBuilder itemShowcase(
            ZenithTooltipText title,
            ZenithTooltipText summary,
            ZenithTooltipText lore
    ) {
        return new ZenithTooltipTemplateBuilder()
                .page(page(title)
                        .add(icon())
                        .add(text(summary))
                        .add(divider())
                        .add(text(lore, ZenithTooltipColor.MUTED)));
    }

    public static ZenithTooltipTemplateBuilder statCard(
            ZenithTooltipText title,
            ZenithTooltipText statName,
            ZenithTooltipText statValue,
            ZenithTooltipText note
    ) {
        return new ZenithTooltipTemplateBuilder()
                .page(page(title)
                        .add(row(statName, statValue))
                        .add(text(note, ZenithTooltipColor.MUTED)));
    }

    public static ZenithTooltipTemplateBuilder progressDisplay(
            ZenithTooltipText title,
            ZenithTooltipText label,
            int value,
            int max,
            ZenithTooltipText valueText,
            ZenithTooltipColor color
    ) {
        return new ZenithTooltipTemplateBuilder()
                .page(page(title)
                        .add(bar(label, value, max, valueText, color)));
    }

    public static ZenithTooltipTemplateBuilder requirementsDisplay(
            ZenithTooltipText title,
            ZenithTooltipText heading,
            ZenithTooltipText requirement,
            ZenithTooltipText status
    ) {
        return new ZenithTooltipTemplateBuilder()
                .page(page(title)
                        .add(header(heading))
                        .add(row(requirement, status)));
    }

    public static ZenithTooltipTemplateBuilder animatedRarityHeader(
            ZenithTooltipText title,
            ZenithTooltipText rarity,
            ZenithTooltipText body,
            ZenithTooltipColor rarityColor
    ) {
        return new ZenithTooltipTemplateBuilder()
                .page(page(title)
                        .add(badge(rarity, rarityColor))
                        .add(text(body)));
    }
}
