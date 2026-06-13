package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Tooltip element that renders the hovered {@link ItemStack} as a centred decorative
 * icon block.
 */

public record IconElement() implements ZenithTooltipElement {
    public static final MapCodec<IconElement> CODEC = MapCodec.unit(new IconElement());

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}