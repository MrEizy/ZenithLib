package net.zic.zenithlib.tooltip.api;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry for tooltip providers.
 * Providers are sorted by priority (higher first).
 */
public class TooltipProviderRegistry {

    private static final List<TooltipProvider> PROVIDERS = new CopyOnWriteArrayList<>();
    private static final TooltipProvider DEFAULT_PROVIDER = new TooltipProvider() {
        @Override
        public boolean canProvideFor(ItemStack stack) {
            return true;
        }

        @Override
        public int getPriority() {
            return Integer.MIN_VALUE;
        }
    };

    static {
        // Register the default provider with lowest priority
        register(DEFAULT_PROVIDER);
    }

    /**
     * Registers a tooltip provider.
     */
    public static void register(TooltipProvider provider) {
        PROVIDERS.add(provider);
        PROVIDERS.sort(Comparator.comparingInt(TooltipProvider::getPriority).reversed());
    }

    /**
     * Unregisters a tooltip provider.
     */
    public static void unregister(TooltipProvider provider) {
        PROVIDERS.remove(provider);
    }

    /**
     * Finds the highest priority provider that can handle the given item stack.
     */
    public static Optional<TooltipProvider> find(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        for (TooltipProvider provider : PROVIDERS) {
            if (provider.canProvideFor(stack)) {
                return Optional.of(provider);
            }
        }

        return Optional.of(DEFAULT_PROVIDER);
    }

    /**
     * Finds all providers that can handle the given item stack.
     */
    public static List<TooltipProvider> findAll(ItemStack stack) {
        List<TooltipProvider> result = new ArrayList<>();

        if (stack == null || stack.isEmpty()) {
            return result;
        }

        for (TooltipProvider provider : PROVIDERS) {
            if (provider.canProvideFor(stack)) {
                result.add(provider);
            }
        }

        return result;
    }

    /**
     * Clears all registered providers except the default.
     */
    public static void clear() {
        PROVIDERS.clear();
        PROVIDERS.add(DEFAULT_PROVIDER);
    }

    /**
     * Returns all registered providers.
     */
    public static List<TooltipProvider> getAllProviders() {
        return new ArrayList<>(PROVIDERS);
    }
}
