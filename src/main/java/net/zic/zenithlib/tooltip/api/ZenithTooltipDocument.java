package net.zic.zenithlib.tooltip.api;

import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Resolved tooltip content ready to be laid out and rendered.
 */
public record ZenithTooltipDocument(
        ZenithTooltipTheme theme,
        List<ZenithTooltipPage> pages,
        List<Identifier> animationPresets
) {
    public ZenithTooltipDocument(ZenithTooltipTheme theme, List<ZenithTooltipPage> pages) {
        this(theme, pages, List.of());
    }

    public ZenithTooltipDocument {
        pages = List.copyOf(pages);
        animationPresets = animationPresets == null ? List.of() : List.copyOf(animationPresets);
    }

    public ZenithTooltipPage page(int index) {
        if (pages.isEmpty()) {
            return new ZenithTooltipPage(ZenithTooltipText.literal(""), List.of());
        }

        int safeIndex = Math.max(0, Math.min(index, pages.size() - 1));
        return pages.get(safeIndex);
    }
}
