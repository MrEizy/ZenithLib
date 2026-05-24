package net.zic.zenithlib.tooltip.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Tooltip payload passed from NeoForge's tooltip gathering event into the client
 * tooltip component rendering pipeline.
 * The payload associates the hovered item identifier and a copied {@link ItemStack}
 * with the already resolved {@link ZenithTooltipDocument} that should be drawn. Keeping
 * this data together allows the client renderer to draw item icons and track page state
 * without re-running rule lookup or resource decoding during rendering.
 */

public record ZenithTooltipData(
        Identifier itemId,
        ItemStack stack,
        ZenithTooltipDocument document
) implements TooltipComponent {
}