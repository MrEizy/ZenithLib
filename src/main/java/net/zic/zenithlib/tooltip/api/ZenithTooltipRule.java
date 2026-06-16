package net.zic.zenithlib.tooltip.api;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Resource-defined rule that connects matching items to a reusable tooltip
 * template and visual theme.
 */
public record ZenithTooltipRule(
        int priority,
        Selector selector,
        Identifier template,
        Identifier theme
) {
    public static final Identifier DEFAULT_THEME = Identifier.fromNamespaceAndPath("zenithlib", "mana_blue");

    private static final MapCodec<ZenithTooltipRule> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(ZenithTooltipRule::priority),
                    Selector.CODEC.optionalFieldOf("selector", Selector.inferred()).forGetter(ZenithTooltipRule::selector),
                    Identifier.CODEC.optionalFieldOf("template").forGetter(rule -> Optional.of(rule.template())),
                    Identifier.CODEC.optionalFieldOf("document").forGetter(rule -> Optional.<Identifier>empty()),
                    Identifier.CODEC.optionalFieldOf("theme", DEFAULT_THEME).forGetter(ZenithTooltipRule::theme)
            ).apply(instance, ZenithTooltipRule::create)
    );

    private static final MapCodec<ZenithTooltipRule> WRAPPED_CODEC = DIRECT_CODEC.codec().fieldOf("item_tooltip");

    public static final MapCodec<ZenithTooltipRule> MAP_CODEC = DIRECT_CODEC;
    public static final Codec<ZenithTooltipRule> CODEC = Codec.either(WRAPPED_CODEC.codec(), DIRECT_CODEC.codec()).xmap(
            either -> either.map(rule -> rule, rule -> rule),
            rule -> Either.right(rule)
    );

    private static ZenithTooltipRule create(
            int priority,
            Selector selector,
            Optional<Identifier> template,
            Optional<Identifier> document,
            Identifier theme
    ) {
        Identifier resolvedTemplate = template.or(() -> document).orElseThrow(() ->
                new IllegalArgumentException("A Zenith tooltip rule must reference a template"));
        return new ZenithTooltipRule(priority, selector, resolvedTemplate, theme);
    }

    public ZenithTooltipRule {
        selector = selector == null ? Selector.inferred() : selector;
        template = Objects.requireNonNull(template, "template");
        theme = theme == null ? DEFAULT_THEME : theme;
    }

    /**
     * Backwards-compatible alias for older generated resources and Java callers.
     */
    public Identifier document() {
        return template;
    }

    public record Selector(
            boolean all,
            List<Identifier> items,
            List<Identifier> tags,
            List<String> namespaces
    ) {
        public static final Codec<Selector> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.BOOL.optionalFieldOf("all", false).forGetter(Selector::all),
                        Identifier.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(Selector::items),
                        Identifier.CODEC.listOf().optionalFieldOf("tags", List.of()).forGetter(Selector::tags),
                        Codec.STRING.listOf().optionalFieldOf("namespaces", List.of()).forGetter(Selector::namespaces)
                ).apply(instance, Selector::new)
        );

        public Selector {
            items = items == null ? List.of() : List.copyOf(items);
            tags = tags == null ? List.of() : List.copyOf(tags);
            namespaces = namespaces == null ? List.of() : List.copyOf(namespaces);

            for (String namespace : namespaces) {
                if (namespace.isBlank() || namespace.contains(":")) {
                    throw new IllegalArgumentException("Tooltip selector namespace must be a plain namespace: " + namespace);
                }
            }
        }

        public static Selector inferred() {
            return new Selector(false, List.of(), List.of(), List.of());
        }

        public boolean inferredFromDefinitionId() {
            return !all && items.isEmpty() && tags.isEmpty() && namespaces.isEmpty();
        }
    }
}
