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
     * Reorders lines so Combat Statistics section appears BEFORE vanilla stats,
     * and strips vanilla enchantment lines (we re-render them in Section 2).
     */
    private static List<Component> reorderForSword(List<Component> lines, TooltipProvider provider, ItemStack stack) {
        if (!stack.is(Items.DIAMOND_SWORD)) {
            return new ArrayList<>(lines);
        }

        String providerClass = provider.getClass().getName();
        if (!providerClass.contains("SwordTooltipProvider")) {
            return new ArrayList<>(lines);
        }

        List<Component> result = new ArrayList<>();

        // 1. Title
        if (!lines.isEmpty()) {
            result.add(lines.get(0));
        }

        // 2. Custom Combat Statistics section (injected before vanilla stats)
        List<TooltipProvider.Section> sections = provider.getSections(stack);
        for (TooltipProvider.Section section : sections) {
            if (!section.newPage() && section.header().contains("Combat Statistics")) {
                result.add(Component.literal(section.header()));
                for (String line : section.lines()) {
                    result.add(Component.literal(line));
                }
                break;
            }
        }

        // 3. Remaining vanilla lines — strip attack stats AND enchantments
        // Enchantments are cyan (§9 or §b) short lines appearing before the blank
        // line that precedes "When in Main Hand:", OR they appear as the first
        // non-title lines. We detect them by checking the vanilla enchantment
        // color codes that Minecraft uses (§9 for normal, §r§9 for curse highlight).
        boolean skipStatsSection = false;

        for (int i = 1; i < lines.size(); i++) {
            String text = lines.get(i).getString();

            // Strip "When in Main Hand:" block (attack damage / speed)
            if (text.contains("When in Main Hand:")) {
                skipStatsSection = true;
                continue;
            }
            if (skipStatsSection) {
                boolean isStat = text.contains("Attack Damage")
                        || text.contains("Attack Speed")
                        || text.matches(".*\\d+\\.?\\d*\\s*(Attack|Damage|Speed).*")
                        || text.trim().isEmpty()
                        || text.startsWith(" ");
                if (isStat) continue;
                else skipStatsSection = false;
            }

            // Strip vanilla enchantment lines.
            // Vanilla renders enchant names via getFullname() which produces a
            // Component whose raw string starts with the enchantment translation.
            // The *formatted* string from getString() won't have § codes, but the
            // raw siblings use Style with color=AQUA (§b) or BLUE (§9).
            // The most reliable cross-version check: ask the Component's style or
            // siblings. We use the serialised plain string + sibling color check.
            if (isVanillaEnchantmentLine(lines.get(i))) {
                continue; // drop it — SwordTooltipProvider re-adds them in Section 2
            }

            result.add(lines.get(i));
        }

        return result;
    }

    /**
     * Returns true if this Component is a vanilla enchantment line.
     *
     * Vanilla produces enchantment Components via Enchantment#getFullname(level),
     * which wraps the name in a TranslatableComponent with Style color AQUA (for
     * normal enchants) or RED (for curses). The resulting Component has no plain
     * text prefix — the entire content is that styled translation.
     *
     * We identify these by checking that the component has exactly one or two
     * siblings (name + optional level suffix) and the first sibling's style color
     * is AQUA or RED — the only two colors Minecraft uses for enchantment lines.
     */
    private static boolean isVanillaEnchantmentLine(Component component) {
        // Vanilla enchantment lines use TranslatableContents with keys like
        // "enchantment.minecraft.sharpness" — this is the most reliable check
        // across all 1.21.x versions regardless of color/style wrapping.
        if (component.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc) {
            if (tc.getKey().startsWith("enchantment.")) {
                return true;
            }
        }

        // Also check siblings — sometimes the enchantment name is wrapped in
        // a plain parent component with the colored translation as a child.
        for (Component sibling : component.getSiblings()) {
            if (sibling.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc) {
                if (tc.getKey().startsWith("enchantment.")) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isEnchantColor(net.minecraft.network.chat.Style style) {
        if (style == null) return false;
        var color = style.getColor();
        if (color == null) return false;
        int rgb = color.getValue();
        // AQUA = 0x55FFFF, RED = 0xFF5555 (Minecraft's named text colors)
        return rgb == 0x55FFFF || rgb == 0xFF5555;
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