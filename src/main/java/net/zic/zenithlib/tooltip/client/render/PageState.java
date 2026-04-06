package net.zic.zenithlib.tooltip.client.render;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the current page state for tooltips.
 * Each item stack can have its own page state.
 */
public class PageState {

    // Map of item stack hash to current page index
    private static final Map<Integer, Integer> PAGE_INDICES = new HashMap<>();

    // Map of item stack hash to total page count
    private static final Map<Integer, Integer> PAGE_COUNTS = new HashMap<>();

    // The currently hovered item stack
    private static ItemStack currentStack = ItemStack.EMPTY;

    // Time when the tooltip was first shown (for animations)
    private static long tooltipStartTime = 0;

    /**
     * Sets the current item stack being hovered.
     */
    public static void setCurrentStack(ItemStack stack) {
        if (stack == null) {
            stack = ItemStack.EMPTY;
        }

        // If it's a different stack, reset the page index
        if (!ItemStack.matches(currentStack, stack)) {
            currentStack = stack;
            tooltipStartTime = System.currentTimeMillis();
        }
    }

    /**
     * Gets the current item stack being hovered.
     */
    public static ItemStack getCurrentStack() {
        return currentStack;
    }

    /**
     * Clears the current stack (called when mouse leaves item).
     */
    public static void clearCurrentStack() {
        currentStack = ItemStack.EMPTY;
        PAGE_INDICES.clear();
        PAGE_COUNTS.clear();
    }

    /**
     * Gets the current page index for the given stack.
     */
    public static int getCurrentPage(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        return PAGE_INDICES.getOrDefault(stack.hashCode(), 0);
    }

    /**
     * Sets the current page index for the given stack.
     */
    public static void setCurrentPage(ItemStack stack, int page) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        int maxPage = getPageCount(stack) - 1;
        PAGE_INDICES.put(stack.hashCode(), Math.max(0, Math.min(page, maxPage)));
    }

    /**
     * Gets the total page count for the given stack.
     */
    public static int getPageCount(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 1;
        }
        return PAGE_COUNTS.getOrDefault(stack.hashCode(), 1);
    }

    /**
     * Sets the total page count for the given stack.
     */
    public static void setPageCount(ItemStack stack, int count) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        PAGE_COUNTS.put(stack.hashCode(), Math.max(1, count));

        // Ensure current page is valid
        int currentPage = getCurrentPage(stack);
        if (currentPage >= count) {
            setCurrentPage(stack, count - 1);
        }
    }

    /**
     * Advances to the next page for the current stack.
     */
    public static void nextPage() {
        if (currentStack.isEmpty()) {
            return;
        }
        int currentPage = getCurrentPage(currentStack);
        int pageCount = getPageCount(currentStack);
        setCurrentPage(currentStack, (currentPage + 1) % pageCount);
    }

    /**
     * Goes to the previous page for the current stack.
     */
    public static void previousPage() {
        if (currentStack.isEmpty()) {
            return;
        }
        int currentPage = getCurrentPage(currentStack);
        int pageCount = getPageCount(currentStack);
        setCurrentPage(currentStack, (currentPage - 1 + pageCount) % pageCount);
    }

    /**
     * Returns the time elapsed since the tooltip was shown (in milliseconds).
     */
    public static long getElapsedTime() {
        return System.currentTimeMillis() - tooltipStartTime;
    }

    /**
     * Resets the elapsed time (for animation purposes).
     */
    public static void resetElapsedTime() {
        tooltipStartTime = System.currentTimeMillis();
    }

    /**
     * Checks if the given stack has multiple pages.
     */
    public static boolean hasMultiplePages(ItemStack stack) {
        return getPageCount(stack) > 1;
    }

    /**
     * Clears all page state data.
     */
    public static void clearAll() {
        PAGE_INDICES.clear();
        PAGE_COUNTS.clear();
        currentStack = ItemStack.EMPTY;
        tooltipStartTime = 0;
    }
}