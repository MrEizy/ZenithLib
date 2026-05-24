package net.zic.zenithlib.tooltip.api;

import java.util.List;

/**
 * Resolved tooltip content ready to be laid out and rendered.
 *
 * <p>A document pairs a selected visual {@link ZenithTooltipTheme} with an immutable
 * sequence of pages. It is produced either by applying a theme to a reusable
 * {@link ZenithTooltipTemplate} or by converting vanilla tooltip lines at runtime.
 * The safe {@link #page(int)} accessor clamps invalid page indices and supplies an
 * empty page if a document contains no pages, keeping the render path defensive.</p>
 */

public record ZenithTooltipDocument(
        ZenithTooltipTheme theme,
        List<ZenithTooltipPage> pages
) {
    public ZenithTooltipDocument {
        pages = List.copyOf(pages);
    }

    public ZenithTooltipPage page(int index) {
        if (pages.isEmpty()) {
            return new ZenithTooltipPage(ZenithTooltipText.literal(""), List.of());
        }

        int safeIndex = Math.max(0, Math.min(index, pages.size() - 1));
        return pages.get(safeIndex);
    }
}
