package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/* Tooltip element that reserves a configurable amount of vertical whitespace */

public record SpacerElement(
        int height
) implements ZenithTooltipElement {

    public static final MapCodec<SpacerElement> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            com.mojang.serialization.Codec.INT.optionalFieldOf("height", 4).forGetter(SpacerElement::height)
                    ).apply(instance, SpacerElement::new)
            );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}