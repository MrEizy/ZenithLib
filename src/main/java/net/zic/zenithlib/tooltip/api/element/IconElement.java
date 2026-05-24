package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Tooltip element that renders the hovered {@link ItemStack} as a centred decorative
 * icon block.
 *
 * <p>The element has no JSON properties because it always uses the current tooltip
 * item and the active theme's icon-box styling. It provides a content author with a
 * compact visual focus point without needing to encode an item identifier separately
 * in each document.</p>
 */

public record IconElement() implements ZenithTooltipElement {
    public static final MapCodec<IconElement> CODEC = MapCodec.unit(new IconElement());

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}