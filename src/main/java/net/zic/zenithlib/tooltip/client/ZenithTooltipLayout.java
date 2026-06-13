package net.zic.zenithlib.tooltip.client;

import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.FormattedCharSequence;
import net.zic.zenithlib.input.InputHandler;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.element.TitleIconElement;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Prepares complete, draw-ready layouts for resolved Zenith tooltip documents.
 *
 * <p>The layout pass selects the current page, wraps its content, prepares compact
 * elements such as bars and badges, and constrains oversized bodies to a themed
 * scrolling viewport. Page headers and the footer hint remain fixed while scrolling,
 * so long tooltip pages behave like readable information cards instead of wandering
 * off screen.</p>
 *
 * <p>A theme's maximum width is a wrapping ceiling rather than a forced box width.
 * Its maximum height is likewise a ceiling for ordinary body content; pages exceeding
 * it become scrollable through the screen mouse-wheel handler.</p>
 */
public final class ZenithTooltipLayout {
    public static final int MIN_WRAP_WIDTH = ZenithTooltipTheme.MIN_INNER_WIDTH;
    public static final int LINE_GAP = 2;
    public static final int TITLE_BODY_GAP = 5;
    public static final int PAGE_HINT_TOP_GAP = 4;
    public static final int ROW_COLUMN_GAP = 8;
    public static final int ROW_MIN_COLUMN_WIDTH = 34;
    public static final int ICON_LINE_GAP = 1;
    public static final int ICON_ELEMENT_BOTTOM_GAP = 4;
    public static final int TITLE_ICON_BOTTOM_GAP = 3;

    private static final int SCROLL_STEP = 12;
    private static final long NAVIGATION_ACTIVE_WINDOW_MS = 500L;

    private static int selectedPage;
    private static int lastPageCount;
    private static int scrollOffset;
    private static int lastMaxScrollOffset;
    private static long lastPreparedAtMs;

    private ZenithTooltipLayout() {}

    public static Layout prepare(Font font, Identifier itemId, ItemStack stack, ZenithTooltipDocument document) {
        ZenithTooltipAnimationState.Update animationUpdate = ZenithTooltipAnimationState.update(itemId, stack);
        ZenithTooltipTheme theme = document.theme();
        int maxInnerWidth = safeInnerWidth(theme);
        int pageIndex = pageIndex(animationUpdate.changed(), document.pages().size());
        ZenithTooltipPage page = document.page(pageIndex);
        ZenithTooltipAnimationState.Frame animationFrame = ZenithTooltipAnimationState.pageFrame(pageIndex);

        boolean titleProvidedByHeader = hasLeadingTitleIcon(page);
        List<FormattedCharSequence> titleLines = titleProvidedByHeader
                ? List.of()
                : split(
                        font,
                        page.title().component().copy().withStyle(ChatFormatting.BOLD),
                        maxInnerWidth
                );

        List<PreparedElement> prepared = new ArrayList<>(page.elements().size());
        for (int elementIndex = 0; elementIndex < page.elements().size(); elementIndex++) {
            ZenithTooltipElement element = page.elements().get(elementIndex);
            long elementSeed = ((long) elementIndex << 32) ^ element.hashCode();
            prepareElement(font, theme, stack, maxInnerWidth, element, elementSeed).ifPresent(prepared::add);
        }

        PreparedTitleIcon titleIconHeader = null;
        List<PreparedElement> elements = List.copyOf(prepared);
        if (!prepared.isEmpty() && prepared.get(0) instanceof PreparedTitleIcon header) {
            titleIconHeader = header;
            elements = List.copyOf(prepared.subList(1, prepared.size()));
        }

        int headerHeight = measuredHeaderHeight(font, theme, titleLines, titleIconHeader, elements);
        int bodyContentHeight = measuredBodyHeight(theme, elements);
        boolean hasMultiplePages = document.pages().size() > 1;

        List<FormattedCharSequence> pageHint = footerLines(
                font,
                maxInnerWidth,
                hasMultiplePages,
                pageIndex,
                document.pages().size(),
                false,
                0,
                0
        );
        int hintHeight = hintHeight(font, pageHint);
        int naturalHeight = theme.padding() * 2 + headerHeight + bodyContentHeight + hintHeight;
        boolean scrollable = naturalHeight > theme.maxHeight();

        if (scrollable) {
            pageHint = footerLines(
                    font,
                    maxInnerWidth,
                    hasMultiplePages,
                    pageIndex,
                    document.pages().size(),
                    true,
                    0,
                    1
            );
            hintHeight = hintHeight(font, pageHint);
        }

        int innerWidth = measuredInnerWidth(
                font,
                theme,
                titleLines,
                titleIconHeader,
                elements,
                pageHint,
                maxInnerWidth
        );

        if (!scrollable) {
            scrollOffset = 0;
            lastMaxScrollOffset = 0;
            return new Layout(
                    theme,
                    document.animationPresets(),
                    titleLines,
                    titleIconHeader,
                    elements,
                    pageHint,
                    innerWidth + theme.padding() * 2,
                    naturalHeight,
                    innerWidth,
                    bodyContentHeight,
                    bodyContentHeight,
                    0,
                    false,
                    animationFrame
            );
        }

        int minimumVisibleHeight = theme.padding() * 2 + headerHeight + hintHeight + font.lineHeight;
        int height = Math.max(theme.maxHeight(), minimumVisibleHeight);
        int bodyViewportHeight = Math.max(1, height - theme.padding() * 2 - headerHeight - hintHeight);
        lastMaxScrollOffset = Math.max(0, bodyContentHeight - bodyViewportHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, lastMaxScrollOffset));
        pageHint = footerLines(
                font,
                maxInnerWidth,
                hasMultiplePages,
                pageIndex,
                document.pages().size(),
                true,
                scrollOffset,
                lastMaxScrollOffset
        );

        return new Layout(
                theme,
                document.animationPresets(),
                titleLines,
                titleIconHeader,
                elements,
                pageHint,
                innerWidth + theme.padding() * 2,
                height,
                innerWidth,
                bodyViewportHeight,
                bodyContentHeight,
                scrollOffset,
                lastMaxScrollOffset > 0,
                animationFrame
        );
    }

    public static int safeInnerWidth(ZenithTooltipTheme theme) {
        return Math.max(MIN_WRAP_WIDTH, theme.maxWidth() - theme.padding() * 2);
    }

    static int gapAfter(ZenithTooltipTheme theme, PreparedElement current, PreparedElement next) {
        if (current.gapKind() == GapKind.ROW && next.gapKind() == GapKind.ROW) {
            return theme.rowGap();
        }

        if (current.gapKind() == GapKind.BAR && next.gapKind() == GapKind.BAR) {
            return theme.rowGap() + 1;
        }

        return theme.elementGap();
    }

    private static int measuredHeaderHeight(
            Font font,
            ZenithTooltipTheme theme,
            List<FormattedCharSequence> titleLines,
            @Nullable PreparedTitleIcon titleIconHeader,
            List<PreparedElement> bodyElements
    ) {
        if (!titleLines.isEmpty()) {
            return lineBlockHeight(font, titleLines.size(), LINE_GAP) + TITLE_BODY_GAP;
        }

        if (titleIconHeader == null) {
            return 0;
        }

        int height = titleIconHeader.height();
        if (!bodyElements.isEmpty()) {
            height += gapAfter(theme, titleIconHeader, bodyElements.get(0));
        }
        return height;
    }

    private static int measuredBodyHeight(ZenithTooltipTheme theme, List<PreparedElement> elements) {
        int height = 0;

        for (int i = 0; i < elements.size(); i++) {
            PreparedElement element = elements.get(i);
            height += element.height();

            if (i + 1 < elements.size()) {
                height += gapAfter(theme, element, elements.get(i + 1));
            }
        }

        return height;
    }

    private static int hintHeight(Font font, List<FormattedCharSequence> pageHint) {
        return pageHint.isEmpty()
                ? 0
                : PAGE_HINT_TOP_GAP + lineBlockHeight(font, pageHint.size(), LINE_GAP);
    }

    private static boolean hasLeadingTitleIcon(ZenithTooltipPage page) {
        return !page.elements().isEmpty() && page.elements().get(0) instanceof TitleIconElement;
    }

    private static Optional<PreparedElement> prepareElement(
            Font font,
            ZenithTooltipTheme theme,
            ItemStack stack,
            int innerWidth,
            ZenithTooltipElement element,
            long elementSeed
    ) {
        return ZenithTooltipElementRenderers.prepare(
                font,
                theme,
                stack,
                innerWidth,
                element,
                elementSeed
        );
    }

    private static int measuredInnerWidth(
            Font font,
            ZenithTooltipTheme theme,
            List<FormattedCharSequence> titleLines,
            @Nullable PreparedTitleIcon titleIconHeader,
            List<PreparedElement> elements,
            List<FormattedCharSequence> pageHint,
            int maxInnerWidth
    ) {
        int contentWidth = Math.max(maxLineWidth(font, titleLines), maxLineWidth(font, pageHint));

        if (titleIconHeader != null) {
            contentWidth = Math.max(contentWidth, preparedElementWidth(font, theme, titleIconHeader));
        }

        for (PreparedElement element : elements) {
            contentWidth = Math.max(contentWidth, preparedElementWidth(font, theme, element));
        }

        return Math.max(MIN_WRAP_WIDTH, Math.min(maxInnerWidth, contentWidth));
    }

    private static int preparedElementWidth(Font font, ZenithTooltipTheme theme, PreparedElement element) {
        return element.width();
    }

    static List<FormattedCharSequence> split(Font font, Component component, int width) {
        return List.copyOf(font.split(component, width));
    }

    static int maxLineWidth(Font font, List<FormattedCharSequence> lines) {
        int width = 0;
        for (FormattedCharSequence line : lines) {
            width = Math.max(width, font.width(line));
        }
        return width;
    }

    static int lineBlockHeight(Font font, int lineCount, int gap) {
        return lineBlockHeightFromCount(lineCount, font.lineHeight, gap);
    }

    private static int lineBlockHeightFromCount(int lineCount, int lineHeight, int gap) {
        if (lineCount <= 0) {
            return 0;
        }
        return lineCount * lineHeight + (lineCount - 1) * gap;
    }

    private static int pageIndex(boolean hoveredTooltipChanged, int pageCount) {
        if (pageCount <= 0) {
            resetPageState();
            return 0;
        }

        if (hoveredTooltipChanged) {
            selectedPage = 0;
            scrollOffset = 0;
            lastMaxScrollOffset = 0;
        }

        lastPageCount = pageCount;
        lastPreparedAtMs = Util.getMillis();
        selectedPage = Math.min(selectedPage, pageCount - 1);
        return selectedPage;
    }

    public static boolean previousPage() {
        return movePage(-1);
    }

    public static boolean nextPage() {
        return movePage(1);
    }

    private static boolean movePage(int delta) {
        if (!hasRecentlyRenderedTooltip() || lastPageCount <= 1) {
            return false;
        }

        selectedPage = Math.floorMod(selectedPage + delta, lastPageCount);
        scrollOffset = 0;
        lastMaxScrollOffset = 0;
        return true;
    }

    /**
     * Scrolls the body of the recently rendered tooltip. The input is consumed whenever
     * the active page has overflowing content, including at either limit, so scrolling
     * over a tooltip does not also operate the underlying inventory screen.
     */
    public static boolean scrollBody(double deltaY) {
        if (!hasRecentlyRenderedTooltip() || lastMaxScrollOffset <= 0 || deltaY == 0.0D) {
            return false;
        }

        int wheelSteps = Math.max(1, (int) Math.ceil(Math.abs(deltaY)));
        int delta = (deltaY > 0.0D ? -1 : 1) * SCROLL_STEP * wheelSteps;
        scrollOffset = Math.max(0, Math.min(lastMaxScrollOffset, scrollOffset + delta));
        return true;
    }

    private static boolean hasRecentlyRenderedTooltip() {
        return lastPreparedAtMs != 0L && Util.getMillis() - lastPreparedAtMs <= NAVIGATION_ACTIVE_WINDOW_MS;
    }

    private static void resetPageState() {
        selectedPage = 0;
        lastPageCount = 0;
        scrollOffset = 0;
        lastMaxScrollOffset = 0;
        lastPreparedAtMs = 0L;
    }

    private static Component pageHintComponent(int pageIndex, int pageCount) {
        return Component.translatable(
                        "tooltip.zenithlib.page_hint_navigation",
                        pageIndex + 1,
                        pageCount,
                        InputHandler.TOOLTIP_PREVIOUS_PAGE.getTranslatedKeyMessage(),
                        InputHandler.TOOLTIP_NEXT_PAGE.getTranslatedKeyMessage()
                )
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    private static List<FormattedCharSequence> footerLines(
            Font font,
            int width,
            boolean hasMultiplePages,
            int pageIndex,
            int pageCount,
            boolean scrollable,
            int currentOffset,
            int maxOffset
    ) {
        List<FormattedCharSequence> lines = new ArrayList<>();

        if (hasMultiplePages) {
            lines.addAll(split(font, pageHintComponent(pageIndex, pageCount), width));
        }

        if (scrollable) {
            lines.addAll(split(font, scrollHintComponent(currentOffset, maxOffset), width));
        }

        return List.copyOf(lines);
    }

    private static Component scrollHintComponent(int currentOffset, int maxOffset) {
        String key;

        if (currentOffset <= 0) {
            key = "tooltip.zenithlib.scroll_hint.down";
        } else if (currentOffset >= maxOffset) {
            key = "tooltip.zenithlib.scroll_hint.up";
        } else {
            key = "tooltip.zenithlib.scroll_hint.both";
        }

        return Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY);
    }

    public record Layout(
            ZenithTooltipTheme theme,
            List<Identifier> animationPresets,
            List<FormattedCharSequence> titleLines,
            @Nullable PreparedTitleIcon titleIconHeader,
            List<PreparedElement> elements,
            List<FormattedCharSequence> pageHintLines,
            int width,
            int height,
            int innerWidth,
            int bodyViewportHeight,
            int bodyContentHeight,
            int scrollOffset,
            boolean scrollable,
            ZenithTooltipAnimationState.Frame animationFrame
    ) {}

    public sealed interface PreparedElement
            permits PreparedText, PreparedHeader, PreparedDivider, PreparedSpacer, PreparedRow,
            PreparedIcon, PreparedTitleIcon, PreparedBadge, PreparedBar, PreparedEntityPreview, PreparedCustom {
        int width();

        int height();

        default GapKind gapKind() {
            return GapKind.DEFAULT;
        }
    }

    public enum GapKind {
        DEFAULT,
        ROW,
        BAR
    }

    public record PreparedText(
            List<FormattedCharSequence> lines,
            int color,
            int width,
            int height,
            Optional<net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect> effect,
            int animationPadding,
            long animationSeed
    ) implements PreparedElement {}

    public record PreparedHeader(
            List<FormattedCharSequence> lines,
            int color,
            int width,
            int height
    ) implements PreparedElement {}

    public record PreparedDivider(int width, int height) implements PreparedElement {}

    public record PreparedSpacer(int width, int height) implements PreparedElement {}

    public record PreparedRow(
            List<FormattedCharSequence> leftLines,
            List<FormattedCharSequence> rightLines,
            int leftColor,
            int rightColor,
            int width,
            int height
    ) implements PreparedElement {
        @Override
        public GapKind gapKind() {
            return GapKind.ROW;
        }
    }

    public record PreparedIcon(int width, int height) implements PreparedElement {}

    public record PreparedEntityPreview(
            int width,
            int height,
            boolean rotate,
            net.zic.zenithlib.tooltip.api.element.EntityPreviewElement.Placement placement
    ) implements PreparedElement {}

    public record PreparedTitleIcon(
            List<FormattedCharSequence> titleLines,
            List<FormattedCharSequence> subtitleLines,
            int width,
            int height
    ) implements PreparedElement {}

    public record PreparedBadge(
            List<FormattedCharSequence> lines,
            int textColor,
            int backgroundColor,
            int borderColor,
            int width,
            int height
    ) implements PreparedElement {}

    public record PreparedBar(
            List<FormattedCharSequence> labelLines,
            List<FormattedCharSequence> valueLines,
            int color,
            float progress,
            int labelHeight,
            int width,
            int height
    ) implements PreparedElement {
        @Override
        public GapKind gapKind() {
            return GapKind.BAR;
        }
    }

    public record PreparedCustom(
            int width,
            int height,
            Object data,
            ZenithTooltipElementRenderers.Renderer renderer
    ) implements PreparedElement {}


}
