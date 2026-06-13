package net.zic.zenithlib.tooltip.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Tooltip payload passed from NeoForge's tooltip gathering event into the client
 * tooltip component rendering pipeline.
 */

public record ZenithTooltipData(
        Identifier itemId,
        ItemStack stack,
        ZenithTooltipDocument document
) implements TooltipComponent {
}