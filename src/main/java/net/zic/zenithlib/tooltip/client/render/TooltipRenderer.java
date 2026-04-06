package net.zic.zenithlib.tooltip.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.tooltip.api.ThemeDefinition;
import net.zic.zenithlib.tooltip.api.TooltipProvider;
import net.zic.zenithlib.tooltip.api.TooltipProviderRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Main renderer for modern tooltips.
 * Handles page calculation, section organization, and rendering.
 * Updated for NeoForge 26.1
 */
public class TooltipRenderer {

    /**
     * Renders the tooltip for the given item stack.
     */
    public static void render(GuiGraphicsExtractor graphics, Font font, ItemStack stack,
                              List<Component> originalLines, TooltipProvider provider,
                              int x, int y, int screenWidth, int screenHeight) {

        // Update page state with current stack
        PageState.setCurrentStack(stack);

        // Get the theme for this stack
        String themeKey = provider.getThemeKey(stack).orElse(null);
        ThemeDefinition theme;
        if (themeKey != null && ThemeRegistry.has(themeKey)) {
            theme = ThemeRegistry.get(themeKey);
        } else if (ItemThemeRegistry.hasThemeForStack(stack)) {
            theme = ThemeRegistry.get(ItemThemeRegistry.getThemeKey(stack));
        } else {
            theme = ThemeRegistry.getDefault();
        }

        if (!theme.enabled()) {
            // Theme is disabled, don't render
            return;
        }

        // Get sections from provider
        List<TooltipProvider.Section> sections = provider.getSections(stack);

        // Build pages from lines and sections
        List<Page> pages = buildPages(originalLines, sections, theme.maxLinesPerPage());

        // Update page count
        int totalPages = Math.max(1, pages.size());
        PageState.setPageCount(stack, totalPages);

        // Get current page index
        int currentPageIndex = PageState.getCurrentPage(stack);
        currentPageIndex = Math.min(currentPageIndex, totalPages - 1);

        // Get lines for current page
        List<Component> pageLines = pages.isEmpty()
                ? originalLines
                : pages.get(currentPageIndex).lines();

        // Render the tooltip
        TooltipPainter.paint(graphics, stack, pageLines, theme, x, y,
                screenWidth, screenHeight, currentPageIndex, totalPages);
    }

    /**
     * Builds pages from tooltip lines and provider sections.
     */
    private static List<Page> buildPages(List<Component> originalLines,
                                         List<TooltipProvider.Section> sections,
                                         int maxLinesPerPage) {
        List<Page> pages = new ArrayList<>();
        List<Component> currentPageLines = new ArrayList<>();

        // Add title to first page
        if (!originalLines.isEmpty()) {
            currentPageLines.add(originalLines.get(0));
        }

        int lineCount = currentPageLines.size();

        // Add original tooltip lines (excluding title)
        for (int i = 1; i < originalLines.size(); i++) {
            Component line = originalLines.get(i);

            // Check if we need a new page
            if (lineCount >= maxLinesPerPage) {
                pages.add(new Page(new ArrayList<>(currentPageLines)));
                currentPageLines.clear();
                lineCount = 0;
            }

            currentPageLines.add(line);
            lineCount++;
        }

        // Add provider sections
        for (TooltipProvider.Section section : sections) {
            // Check if section needs a new page
            if (section.newPage() && !currentPageLines.isEmpty()) {
                pages.add(new Page(new ArrayList<>(currentPageLines)));
                currentPageLines.clear();
                lineCount = 0;
            }

            // Add section header if present
            if (section.header() != null && !section.header().isEmpty()) {
                if (lineCount >= maxLinesPerPage) {
                    pages.add(new Page(new ArrayList<>(currentPageLines)));
                    currentPageLines.clear();
                    lineCount = 0;
                }
                currentPageLines.add(Component.literal("§6" + section.header()));
                lineCount++;
            }

            // Add section lines
            for (String lineText : section.lines()) {
                if (lineCount >= maxLinesPerPage) {
                    pages.add(new Page(new ArrayList<>(currentPageLines)));
                    currentPageLines.clear();
                    lineCount = 0;
                }
                currentPageLines.add(Component.literal("  " + lineText));
                lineCount++;
            }
        }

        // Add remaining lines as final page
        if (!currentPageLines.isEmpty()) {
            pages.add(new Page(currentPageLines));
        }

        return pages;
    }

    /**
     * Represents a single page of tooltip content.
     */
    private record Page(List<Component> lines) {}

    /**
     * Renders a simple tooltip without the modern styling.
     * Used as fallback.
     */
    public static void renderSimple(GuiGraphicsExtractor graphics, Font font, List<Component> lines,
                                    int x, int y, int screenWidth, int screenHeight) {
        // Use vanilla tooltip rendering as fallback
        // In 26.1, this would call the vanilla extraction methods
    }

    /**
     * Checks if modern tooltip rendering should be used for the given stack.
     */
    public static boolean shouldUseModernTooltip(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        // Check if there's a theme for this item
        if (ItemThemeRegistry.hasThemeForStack(stack)) {
            String themeKey = ItemThemeRegistry.getThemeKey(stack);
            ThemeDefinition theme = ThemeRegistry.get(themeKey);
            return theme.enabled();
        }

        // Check if there's a provider with a theme
        return TooltipProviderRegistry.find(stack)
                .flatMap(p -> p.getThemeKey(stack))
                .map(ThemeRegistry::get)
                .map(ThemeDefinition::enabled)
                .orElse(false);
    }
}