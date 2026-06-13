package net.zic.zenithlib.tooltip.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Resource-defined rule that connects matching items to a reusable tooltip document
 * and visual theme.
 */

public record ZenithTooltipRule(
        int priority,
        Selector selector,
        Identifier document,
        Identifier theme
) {
    public static final Identifier DEFAULT_THEME = Identifier.fromNamespaceAndPath("zenithlib", "mana_blue");

    public static final MapCodec<ZenithTooltipRule> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(ZenithTooltipRule::priority),
                    Selector.CODEC.fieldOf("selector").forGetter(ZenithTooltipRule::selector),
                    Identifier.CODEC.fieldOf("document").forGetter(ZenithTooltipRule::document),
                    Identifier.CODEC.optionalFieldOf("theme", DEFAULT_THEME).forGetter(ZenithTooltipRule::theme)
            ).apply(instance, ZenithTooltipRule::new)
    );
    public static final Codec<ZenithTooltipRule> CODEC = MAP_CODEC.codec();

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
            items = List.copyOf(items);
            tags = List.copyOf(tags);
            namespaces = List.copyOf(namespaces);

            if (!all && items.isEmpty() && tags.isEmpty() && namespaces.isEmpty()) {
                throw new IllegalArgumentException("Tooltip selector must match items, tags, namespaces, or all items");
            }

            for (String namespace : namespaces) {
                if (namespace.isBlank() || namespace.contains(":")) {
                    throw new IllegalArgumentException("Tooltip selector namespace must be a plain namespace: " + namespace);
                }
            }
        }
    }
}
