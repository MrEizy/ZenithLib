package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

/** Groups elements behind a simple runtime condition such as a held modifier key. */
public record SectionElement(
        Identifier condition,
        List<ZenithTooltipElement> elements
) implements ZenithTooltipElement {
    public static final MapCodec<SectionElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("condition").forGetter(SectionElement::condition),
                    ZenithTooltipElement.CODEC.listOf().optionalFieldOf("elements", List.of()).forGetter(SectionElement::elements)
            ).apply(instance, SectionElement::new)
    );

    public SectionElement {
        Objects.requireNonNull(condition, "condition");
        elements = elements == null ? List.of() : List.copyOf(elements);
    }

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
