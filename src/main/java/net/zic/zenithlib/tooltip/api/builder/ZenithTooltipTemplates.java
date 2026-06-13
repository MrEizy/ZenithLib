package net.zic.zenithlib.tooltip.api.builder;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;

import java.util.ArrayList;
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


    public static LoreTooltipBuilder loreTooltip() {
        return new LoreTooltipBuilder();
    }

    public static ZenithTooltipTemplateBuilder loreTooltip(
            ZenithTooltipText title,
            ZenithTooltipText summary,
            ZenithTooltipText lore
    ) {
        return loreTooltip()
                .title(title)
                .summary(summary)
                .lore(lore)
                .build();
    }

    public static ProgressTooltipBuilder progressTooltip() {
        return new ProgressTooltipBuilder();
    }

    public static ZenithTooltipTemplateBuilder itemNameSummary(ZenithTooltipText summary) {
        return loreTooltip()
                .title(itemName())
                .icon(true)
                .summary(summary)
                .build();
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


    public static final class LoreTooltipBuilder {
        private ZenithTooltipText title = itemName();
        private ZenithTooltipText subtitle = ZenithTooltipText.literal("");
        private ZenithTooltipText summary = ZenithTooltipText.literal("");
        private ZenithTooltipText footer = ZenithTooltipText.literal("");
        private boolean icon = true;
        private boolean dividerBeforeLore = true;
        private final List<ZenithTooltipElement> beforeBody = new ArrayList<>();
        private final List<ZenithTooltipElement> lore = new ArrayList<>();
        private final List<ZenithTooltipElement> afterBody = new ArrayList<>();

        private LoreTooltipBuilder() {}

        public LoreTooltipBuilder title(ZenithTooltipText title) { this.title = Objects.requireNonNull(title, "title"); return this; }
        public LoreTooltipBuilder subtitle(ZenithTooltipText subtitle) { this.subtitle = Objects.requireNonNull(subtitle, "subtitle"); return this; }
        public LoreTooltipBuilder summary(ZenithTooltipText summary) { this.summary = Objects.requireNonNull(summary, "summary"); return this; }
        public LoreTooltipBuilder footer(ZenithTooltipText footer) { this.footer = Objects.requireNonNull(footer, "footer"); return this; }
        public LoreTooltipBuilder icon(boolean icon) { this.icon = icon; return this; }
        public LoreTooltipBuilder dividerBeforeLore(boolean dividerBeforeLore) { this.dividerBeforeLore = dividerBeforeLore; return this; }

        public LoreTooltipBuilder beforeBody(ZenithTooltipElement element) { this.beforeBody.add(Objects.requireNonNull(element, "element")); return this; }
        public LoreTooltipBuilder lore(ZenithTooltipText text) { this.lore.add(text(text, ZenithTooltipColor.MUTED)); return this; }
        public LoreTooltipBuilder lore(ZenithTooltipElement element) { this.lore.add(Objects.requireNonNull(element, "element")); return this; }
        public LoreTooltipBuilder afterBody(ZenithTooltipElement element) { this.afterBody.add(Objects.requireNonNull(element, "element")); return this; }

        public ZenithTooltipTemplateBuilder build() {
            ZenithTooltipPageBuilder page = page(title);
            if (icon) {
                page.add(titleIcon(title, subtitle));
            } else {
                page.add(header(title));
            }
            beforeBody.forEach(page::add);
            if (!summary.isBlank()) {
                page.add(text(summary));
            }
            if (!lore.isEmpty()) {
                if (dividerBeforeLore) {
                    page.add(divider());
                }
                lore.forEach(page::add);
            }
            afterBody.forEach(page::add);
            if (!footer.isBlank()) {
                page.add(divider());
                page.add(text(footer, ZenithTooltipColor.MUTED));
            }
            return new ZenithTooltipTemplateBuilder().page(page);
        }
    }

    public static final class ProgressTooltipBuilder {
        private ZenithTooltipText title = itemName();
        private ZenithTooltipText summary = ZenithTooltipText.literal("");
        private ZenithTooltipText label = ZenithTooltipText.literal("Progress");
        private ZenithTooltipText footer = ZenithTooltipText.literal("");
        private ZenithTooltipColor color = ZenithTooltipColor.ACCENT;
        private int value;
        private int max = 1;
        private Identifier source;

        private ProgressTooltipBuilder() {}

        public ProgressTooltipBuilder title(ZenithTooltipText title) { this.title = Objects.requireNonNull(title, "title"); return this; }
        public ProgressTooltipBuilder summary(ZenithTooltipText summary) { this.summary = Objects.requireNonNull(summary, "summary"); return this; }
        public ProgressTooltipBuilder label(ZenithTooltipText label) { this.label = Objects.requireNonNull(label, "label"); return this; }
        public ProgressTooltipBuilder fixed(int value, int max) { this.value = value; this.max = max; this.source = null; return this; }
        public ProgressTooltipBuilder source(Identifier source) { this.source = Objects.requireNonNull(source, "source"); return this; }
        public ProgressTooltipBuilder source(String source) { return source(sourceId(source)); }
        public ProgressTooltipBuilder color(ZenithTooltipColor color) { this.color = Objects.requireNonNull(color, "color"); return this; }
        public ProgressTooltipBuilder footer(ZenithTooltipText footer) { this.footer = Objects.requireNonNull(footer, "footer"); return this; }

        public ZenithTooltipTemplateBuilder build() {
            ZenithTooltipPageBuilder page = page(title)
                    .titleIcon(title, summary);
            if (!summary.isBlank()) {
                page.text(summary, ZenithTooltipColor.MUTED);
            }
            page.add(source == null ? bar(label, value, max, color) : dynamicBar(label, source, color));
            if (!footer.isBlank()) {
                page.divider().muted(footer);
            }
            return new ZenithTooltipTemplateBuilder().page(page);
        }
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
