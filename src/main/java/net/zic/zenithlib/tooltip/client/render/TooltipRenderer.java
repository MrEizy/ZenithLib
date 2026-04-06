package net.zic.zenithlib.tooltip.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.ThemeDefinition;
import net.zic.zenithlib.tooltip.api.TooltipProvider;
import net.zic.zenithlib.tooltip.api.TooltipProviderRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Main renderer for modern tooltips.
 */
public class TooltipRenderer {

    public static void render(GuiGraphicsExtractor graphics, Font font, ItemStack stack,
                              List<Component> originalLines, TooltipProvider provider,
                              int x, int y, int screenWidth, int screenHeight) {

        PageState.setCurrentStack(stack);

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
            return;
        }

        // For diamond sword, inject custom stats at the TOP of page 1
        // Keep vanilla lines but reorder so Combat Statistics comes first
        List<Component> reorderedLines = reorderForSword(originalLines, provider, stack);

        List<TooltipProvider.Section> sections = provider.getSections(stack);
        List<Page> pages = buildPagesWithOverlay(reorderedLines, sections, theme.maxLinesPerPage());

        int totalPages = Math.max(1, pages.size());
        PageState.setPageCount(stack, totalPages);

        int currentPage = PageState.getCurrentPage(stack);
        currentPage = Math.min(currentPage, totalPages - 1);

        List<Component> pageLines = pages.isEmpty() ? reorderedLines : pages.get(currentPage).lines();

        TooltipPainter.paint(graphics, stack, pageLines, theme, x, y,
                screenWidth, screenHeight, currentPage, totalPages);
    }

    /**
     * Reorders lines so Combat Statistics section appears BEFORE vanilla stats.
     * For diamond sword: Name -> Combat Statistics -> (rest of vanilla including When in Main Hand)
     */
    private static List<Component> reorderForSword(List<Component> lines, TooltipProvider provider, ItemStack stack) {
        // Only for diamond sword with custom provider
        if (!stack.is(Items.DIAMOND_SWORD)) {
            return new ArrayList<>(lines);
        }

        String providerClass = provider.getClass().getName();
        if (!providerClass.contains("SwordTooltipProvider")) {
            return new ArrayList<>(lines);
        }

        List<Component> result = new ArrayList<>();

        // 1. Add title first
        if (!lines.isEmpty()) {
            result.add(lines.get(0));
        }

        // 2. Add custom Combat Statistics header and lines (from sections)
        // These will be the first custom section with newPage=false
        List<TooltipProvider.Section> sections = provider.getSections(stack);
        for (TooltipProvider.Section section : sections) {
            if (!section.newPage() && section.header().contains("Combat Statistics")) {
                // Add the custom stats FIRST (before vanilla stats)
                result.add(Component.literal(section.header()));
                for (String line : section.lines()) {
                    result.add(Component.literal(line));
                }
                break; // Only add the first non-newPage section (Combat Statistics)
            }
        }

        // 3. Add remaining vanilla lines (including When in Main Hand, enchantments, etc.)
        // Skip the title since we already added it
        for (int i = 1; i < lines.size(); i++) {
            result.add(lines.get(i));
        }

        return result;
    }

    /**
     * Build pages, skipping the Combat Statistics section since we already added it
     */
    private static List<Page> buildPagesWithOverlay(List<Component> baseLines,
                                                    List<TooltipProvider.Section> sections,
                                                    int maxLines) {
        List<Page> pages = new ArrayList<>();

        if (baseLines.isEmpty()) return pages;

        Component title = baseLines.get(0);
        List<Component> content = baseLines.size() > 1 ?
                new ArrayList<>(baseLines.subList(1, baseLines.size())) : new ArrayList<>();

        List<Component> currentPage = new ArrayList<>();
        currentPage.add(title);
        int lineCount = 1;

        int contentIdx = 0;

        // Add content lines first (includes Combat Statistics + vanilla stats)
        while (contentIdx < content.size()) {
            if (lineCount >= maxLines) {
                pages.add(new Page(new ArrayList<>(currentPage)));
                currentPage = new ArrayList<>();
                currentPage.add(title);
                lineCount = 1;
            }

            currentPage.add(content.get(contentIdx));
            lineCount++;
            contentIdx++;
        }

        // Now add remaining sections (those with newPage=true)
        for (TooltipProvider.Section section : sections) {
            // Skip Combat Statistics since we already added it
            if (!section.newPage() && section.header().contains("Combat Statistics")) {
                continue;
            }

            // This section forces new page
            if (section.newPage() && !currentPage.isEmpty() && lineCount > 1) {
                pages.add(new Page(new ArrayList<>(currentPage)));
                currentPage = new ArrayList<>();
                currentPage.add(title);
                lineCount = 1;
            }

            // Add section header
            if (section.header() != null && !section.header().isEmpty()) {
                if (lineCount >= maxLines) {
                    pages.add(new Page(new ArrayList<>(currentPage)));
                    currentPage = new ArrayList<>();
                    currentPage.add(title);
                    lineCount = 1;
                }
                currentPage.add(Component.literal(section.header()));
                lineCount++;
            }

            // Add section lines
            for (String txt : section.lines()) {
                if (lineCount >= maxLines) {
                    pages.add(new Page(new ArrayList<>(currentPage)));
                    currentPage = new ArrayList<>();
                    currentPage.add(title);
                    lineCount = 1;
                }
                currentPage.add(Component.literal(txt));
                lineCount++;
            }
        }

        // Add final page
        if (lineCount > 1) {
            pages.add(new Page(currentPage));
        }

        if (pages.isEmpty() && !baseLines.isEmpty()) {
            pages.add(new Page(baseLines));
        }

        return pages;
    }

    private record Page(List<Component> lines) {}

    public static boolean shouldUseModernTooltip(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        var provider = TooltipProviderRegistry.find(stack);
        if (provider.isPresent()) {
            var themeKey = provider.get().getThemeKey(stack);
            if (themeKey.isPresent()) {
                return ThemeRegistry.get(themeKey.get()).enabled();
            }
        }

        if (ItemThemeRegistry.hasThemeForStack(stack)) {
            return ThemeRegistry.get(ItemThemeRegistry.getThemeKey(stack)).enabled();
        }

        return false;
    }
}