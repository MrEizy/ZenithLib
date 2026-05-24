package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;

/* Tooltip element that inserts a themed horizontal section divider */

public record DividerElement() implements ZenithTooltipElement {

    public static final MapCodec<DividerElement> CODEC =
            MapCodec.unit(new DividerElement());

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}