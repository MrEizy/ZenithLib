package net.zic.zenithlib.tooltip.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Data-driven rule that selects item stacks and assigns them to a reusable
 * tooltip template plus a visual theme.
 */
public record ZenithTooltipRule(
        int priority,
        Selector selector,
        Identifier template,
        Identifier theme,
        Optional<ZenithTooltipThemeOverride> themeOverrides
) {
    public static final Identifier DEFAULT_THEME =
            Identifier.fromNamespaceAndPath(
                    "zenithlib",
                    "mana_blue"
            );

    public static final MapCodec<ZenithTooltipRule> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            Codec.INT.optionalFieldOf("priority", 0).forGetter(ZenithTooltipRule::priority),
                            Selector.CODEC.optionalFieldOf("selector", Selector.inferred()).forGetter(ZenithTooltipRule::selector),
                            Identifier.CODEC.fieldOf("template").forGetter(ZenithTooltipRule::template),
                            Identifier.CODEC.optionalFieldOf("theme", DEFAULT_THEME).forGetter(ZenithTooltipRule::theme),
                            ZenithTooltipThemeOverride.CODEC.optionalFieldOf("theme_overrides").forGetter(ZenithTooltipRule::themeOverrides)
                    ).apply(instance, ZenithTooltipRule::new)
            );

    public static final Codec<ZenithTooltipRule> CODEC =
            MAP_CODEC.codec();

    public ZenithTooltipRule {
        selector = selector == null ? Selector.inferred() : selector;
        template = Objects.requireNonNull(template, "template");
        theme = theme == null ? DEFAULT_THEME : theme;
        themeOverrides = themeOverrides == null ? Optional.empty() : themeOverrides;
    }

    public ZenithTooltipRule(
            int priority,
            Selector selector,
            Identifier template,
            Identifier theme
    ) {
        this(priority, selector, template, theme, Optional.empty());
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