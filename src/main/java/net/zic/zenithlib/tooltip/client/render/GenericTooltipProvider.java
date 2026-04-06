package net.zic.zenithlib.tooltip.client.render;

import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.tooltip.api.TooltipProvider;

import java.util.List;
import java.util.Optional;

/**
 * Generic tooltip provider that applies themes based on item theme registry.
 * This is the default provider for items with theme mappings.
 */
public class GenericTooltipProvider implements TooltipProvider {

    public static final GenericTooltipProvider INSTANCE = new GenericTooltipProvider();

    private GenericTooltipProvider() {}

    @Override
    public boolean canProvideFor(ItemStack stack) {
        // This provider can handle any item that has a theme mapping
        return ItemThemeRegistry.hasThemeForStack(stack);
    }

    @Override
    public Optional<String> getThemeKey(ItemStack stack) {
        return Optional.ofNullable(ItemThemeRegistry.getThemeKey(stack));
    }

    @Override
    public List<Section> getSections(ItemStack stack) {
        // Generic provider doesn't add custom sections
        // Sections are added by specialized providers
        return List.of();
    }

    @Override
    public int getPriority() {
        // Low priority - specialized providers should be checked first
        return -100;
    }
}
