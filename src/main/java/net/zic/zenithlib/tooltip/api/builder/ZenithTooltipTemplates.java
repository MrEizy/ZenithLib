package net.zic.zenithlib.tooltip.api.builder;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.*;

/** Beginner-friendly factories for common tooltip document shapes. */
public final class ZenithTooltipTemplates {
    private static final Map<Identifier, RegisteredTemplate<?>> CUSTOM_TEMPLATES = new ConcurrentHashMap<>();

    private ZenithTooltipTemplates() {}

    public static <C> void register(Identifier id, Class<C> configType, TemplateFactory<C> factory) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(configType, "configType");
        Objects.requireNonNull(factory, "factory");
        CUSTOM_TEMPLATES.putIfAbsent(id, new RegisteredTemplate<>(configType, factory));
    }

    public static <C> Optional<ZenithTooltipTemplateBuilder> create(Identifier id, C config) {
        RegisteredTemplate<C> template = lookup(id, config);
        if (template == null) {
            return Optional.empty();
        }
        return Optional.of(template.factory().create(config));
    }

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

    public static ZenithTooltipTemplateBuilder paginatedLore(
            ZenithTooltipText title,
            ZenithTooltipText summary,
            ZenithTooltipText... lorePages
    ) {
        ZenithTooltipTemplateBuilder builder = new ZenithTooltipTemplateBuilder()
                .page(page(title)
                        .add(titleIcon(title, summary))
                        .add(text(summary)));

        for (int i = 0; i < lorePages.length; i++) {
            builder.page(page(ZenithTooltipText.literal("Lore " + (i + 1)))
                    .add(text(lorePages[i], ZenithTooltipColor.MUTED)));
        }
        return builder;
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

    public static ZenithTooltipTemplateBuilder chargeDisplay(
            ZenithTooltipText title,
            ZenithTooltipText summary,
            ZenithTooltipText label,
            int value,
            int max,
            ZenithTooltipColor color
    ) {
        return new ZenithTooltipTemplateBuilder()
                .page(page(title)
                        .add(titleIcon(title, summary))
                        .add(bar(label, value, max, color))
                        .add(text(summary, ZenithTooltipColor.MUTED)));
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

    public static ZenithTooltipTemplateBuilder requirementsDisplay(
            ZenithTooltipText title,
            ZenithTooltipText heading,
            List<ZenithTooltipElement> requirementRows
    ) {
        ZenithTooltipPageBuilder page = page(title).add(header(heading));
        requirementRows.forEach(page::add);
        return new ZenithTooltipTemplateBuilder().page(page);
    }

    public static ZenithTooltipTemplateBuilder entityShowcase(
            ZenithTooltipText title,
            ZenithTooltipText summary
    ) {
        return new ZenithTooltipTemplateBuilder()
                .page(page(title)
                        .add(ZenithTooltipBuilders.entityPreview())
                        .add(text(summary)));
    }

    public static ZenithTooltipTemplateBuilder comparison(
            ZenithTooltipText title,
            ZenithTooltipText leftLabel,
            ZenithTooltipText leftValue,
            ZenithTooltipText rightLabel,
            ZenithTooltipText rightValue
    ) {
        return new ZenithTooltipTemplateBuilder()
                .page(page(title)
                        .add(row(leftLabel, leftValue, ZenithTooltipColor.TEXT, ZenithTooltipColor.POSITIVE))
                        .add(row(rightLabel, rightValue, ZenithTooltipColor.TEXT, ZenithTooltipColor.WARNING)));
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

    @FunctionalInterface
    public interface TemplateFactory<C> {
        ZenithTooltipTemplateBuilder create(C config);
    }

    private static <C> RegisteredTemplate<C> lookup(Identifier id, C config) {
        RegisteredTemplate<?> template = (RegisteredTemplate<?>) CUSTOM_TEMPLATES.get(id);
        if (template == null || config == null || !template.configType().isInstance(config)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        RegisteredTemplate<C> cast = (RegisteredTemplate<C>) template;
        return cast;
    }

    private record RegisteredTemplate<C>(
            Class<C> configType,
            TemplateFactory<C> factory
    ) {}
}
