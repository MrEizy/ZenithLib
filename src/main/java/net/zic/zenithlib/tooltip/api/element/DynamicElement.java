package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

/**
 * Placeholder element whose contents are produced from a registered runtime element
 * source before layout.
 */
public record DynamicElement(
        Identifier source,
        List<ZenithTooltipElement> fallback,
        boolean hideWhenEmpty
) implements ZenithTooltipElement {
    public static final MapCodec<DynamicElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("source").forGetter(DynamicElement::source),
                    ZenithTooltipElement.CODEC.listOf().optionalFieldOf("fallback", List.of()).forGetter(DynamicElement::fallback),
                    Codec.BOOL.optionalFieldOf("hide_when_empty", true).forGetter(DynamicElement::hideWhenEmpty)
            ).apply(instance, DynamicElement::new)
    );

    public DynamicElement {
        Objects.requireNonNull(source, "source");
        fallback = fallback == null ? List.of() : List.copyOf(fallback);
    }

    public DynamicElement(Identifier source) {
        this(source, List.of(), true);
    }

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
