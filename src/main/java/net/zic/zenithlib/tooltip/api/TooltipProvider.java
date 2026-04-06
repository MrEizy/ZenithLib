package net.zic.zenithlib.tooltip.api;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Interface for providing custom tooltip data.
 * Implement this to add custom sections to tooltips.
 */
public interface TooltipProvider {

    /**
     * Returns the theme key to use for this item.
     * If empty, the default theme will be used.
     */
    default Optional<String> getThemeKey(ItemStack stack) {
        return Optional.empty();
    }

    /**
     * Returns custom sections to add to the tooltip.
     * Each section is a list of lines to display.
     */
    default List<Section> getSections(ItemStack stack) {
        return List.of();
    }

    /**
     * Returns the priority of this provider.
     * Higher priority providers are processed first.
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Checks if this provider should handle the given item stack.
     */
    boolean canProvideFor(ItemStack stack);

    /**
     * Represents a section in the tooltip.
     */
    record Section(
            /** The header/title of this section. */
            String header,
            /** The lines of content in this section. */
            List<String> lines,
            /** The priority of this section (higher = earlier). */
            int priority,
            /** Whether this section should start on a new page. */
            boolean newPage
    ) {}
}