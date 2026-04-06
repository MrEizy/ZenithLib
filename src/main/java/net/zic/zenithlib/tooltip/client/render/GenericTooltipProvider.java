package net.zic.zenithlib.tooltip.client.render;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
        // Can handle if: has explicit theme mapping, OR is vanilla with config enabled, OR is mod item with config enabled
        if (ItemThemeRegistry.hasThemeForStack(stack)) {
            return true;
        }

        if (!stack.isEmpty()) {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (itemId != null) {
                boolean isVanilla = "minecraft".equals(itemId.getNamespace());

                if (isVanilla && TooltipNavigationConfig.applyTooltipsToVanillaItems()) {
                    return true;
                }

                if (!isVanilla && TooltipNavigationConfig.applyTooltipsToModItems()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Optional<String> getThemeKey(ItemStack stack) {
        // First check explicit mapping
        String explicitKey = ItemThemeRegistry.getThemeKey(stack);
        if (explicitKey != null) {
            return Optional.of(explicitKey);
        }

        // Return default theme for auto-converted items
        return Optional.of("default");
    }

    @Override
    public List<Section> getSections(ItemStack stack) {
        return List.of();
    }

    @Override
    public int getPriority() {
        // Low priority - specialized providers should be checked first
        return -100;
    }
}
