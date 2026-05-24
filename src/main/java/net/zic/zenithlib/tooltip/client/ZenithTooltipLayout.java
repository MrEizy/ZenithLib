package net.zic.zenithlib.tooltip.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.element.DividerElement;
import net.zic.zenithlib.tooltip.api.element.HeaderElement;
import net.zic.zenithlib.tooltip.api.element.IconElement;
import net.zic.zenithlib.tooltip.api.element.RowElement;
import net.zic.zenithlib.tooltip.api.element.SpacerElement;
import net.zic.zenithlib.tooltip.api.element.TextElement;
import net.zic.zenithlib.tooltip.api.element.TitleIconElement;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Prepares complete, draw-ready layouts for resolved Zenith tooltip documents.
 *
 * <p>The layout pass selects the currently displayed page, handles Alt-key page
 * cycling per hovered item, wraps titles and element text, resolves semantic colours,
 * and calculates stable dimensions. It converts public document elements into internal
 * prepared element records so {@link ZenithTooltipRenderer} can draw the tooltip
 * without repeating measurement or text-splitting work.</p>
 *
 * <p>Layout constants centralise spacing and icon geometry, making the visual system
 * predictable for themes while protecting narrow custom widths from unreadable output.</p>
 */

public final class ZenithTooltipLayout {
    public static final int MIN_WRAP_WIDTH = ZenithTooltipTheme.MIN_INNER_WIDTH;
    public static final int LINE_GAP = 2;
    public static final int TITLE_BODY_GAP = 5;
    public static final int PAGE_HINT_TOP_GAP = 4;
    public static final int ELEMENT_GAP = 3;
    public static final int SECTION_GAP = 5;
    public static final int ROW_COLUMN_GAP = 8;
    public static final int ICON_LINE_GAP = 1;

    public static final int ICON_BOX_SIZE = 28;
    public static final int ICON_SIZE = 16;
    public static final int ICON_TEXT_GAP = 8;
    public static final int ICON_ELEMENT_HEIGHT = 32;
    public static final int TITLE_ICON_BOTTOM_GAP = 8;

    private static boolean wasAltDown;
    private static int selectedPage;
    private static @Nullable Identifier lastHoveredItem;

    private ZenithTooltipLayout() {}

    public static Layout prepare(Font font, Identifier itemId, ZenithTooltipDocument document) {
        ZenithTooltipTheme theme = document.theme();
        int innerWidth = safeInnerWidth(theme);
        int pageIndex = pageIndex(itemId, document.pages().size());
        ZenithTooltipPage page = document.page(pageIndex);
        List<FormattedCharSequence> titleLines = split(
                font,
                page.title().component().copy().withStyle(ChatFormatting.BOLD),
                innerWidth
        );

        List<PreparedElement> elements = new ArrayList<>(page.elements().size());
        int bodyHeight = 0;

        for (ZenithTooltipElement element : page.elements()) {
            PreparedElement prepared = prepareElement(font, theme, innerWidth, element);
            elements.add(prepared);
            bodyHeight += prepared.height() + ELEMENT_GAP;
        }

        if (!elements.isEmpty()) {
            bodyHeight -= ELEMENT_GAP;
        }

        List<FormattedCharSequence> pageHint = document.pages().size() <= 1
                ? List.of()
                : split(font, pageHintComponent(pageIndex, document.pages().size()), innerWidth);

        int hintHeight = pageHint.isEmpty()
                ? 0
                : PAGE_HINT_TOP_GAP + lineBlockHeight(font, pageHint.size(), LINE_GAP);

        int titleHeight = titleLines.isEmpty() ? 0 : lineBlockHeight(font, titleLines.size(), LINE_GAP) + TITLE_BODY_GAP;
        int height = theme.padding() * 2 + titleHeight + bodyHeight + hintHeight;

        return new Layout(
                theme,
                titleLines,
                List.copyOf(elements),
                pageHint,
                theme.maxWidth(),
                height,
                innerWidth
        );
    }

    public static int safeInnerWidth(ZenithTooltipTheme theme) {
        return Math.max(MIN_WRAP_WIDTH, theme.maxWidth() - theme.padding() * 2);
    }

    private static PreparedElement prepareElement(
            Font font,
            ZenithTooltipTheme theme,
            int innerWidth,
            ZenithTooltipElement element
    ) {
        if (element instanceof TextElement text) {
            List<FormattedCharSequence> lines = split(font, text.text().component(), innerWidth);
            return new PreparedText(lines, text.color().resolve(theme), lineBlockHeight(font, lines.size(), LINE_GAP));
        }

        if (element instanceof HeaderElement header) {
            List<FormattedCharSequence> lines = split(
                    font,
                    header.text().component().copy().withStyle(ChatFormatting.BOLD),
                    innerWidth
            );
            return new PreparedHeader(lines, header.color().resolve(theme), lineBlockHeight(font, lines.size(), LINE_GAP) + 2);
        }

        if (element instanceof DividerElement) {
            return new PreparedDivider(SECTION_GAP + 1);
        }

        if (element instanceof SpacerElement spacer) {
            return new PreparedSpacer(Math.max(0, spacer.height()));
        }

        if (element instanceof RowElement row) {
            int rightMaxWidth = Math.max(1, (innerWidth - ROW_COLUMN_GAP) / 2);
            List<FormattedCharSequence> rightLines = split(font, row.right().component(), rightMaxWidth);
            int rightWidth = maxLineWidth(font, rightLines);
            int leftWidth = Math.max(1, innerWidth - rightWidth - ROW_COLUMN_GAP);
            List<FormattedCharSequence> leftLines = split(font, row.left().component(), leftWidth);
            int height = Math.max(
                    lineBlockHeight(font, leftLines.size(), LINE_GAP),
                    lineBlockHeight(font, rightLines.size(), LINE_GAP)
            );
            return new PreparedRow(
                    leftLines,
                    rightLines,
                    row.leftColor().resolve(theme),
                    row.rightColor().resolve(theme),
                    height
            );
        }

        if (element instanceof IconElement) {
            return new PreparedIcon(ICON_ELEMENT_HEIGHT);
        }

        if (element instanceof TitleIconElement titleIcon) {
            int labelWidth = Math.max(1, innerWidth - ICON_BOX_SIZE - ICON_TEXT_GAP);
            List<FormattedCharSequence> titleLines = split(
                    font,
                    titleIcon.title().component().copy().withStyle(ChatFormatting.BOLD),
                    labelWidth
            );
            List<FormattedCharSequence> subtitleLines = titleIcon.subtitle().isBlank()
                    ? List.of()
                    : split(font, titleIcon.subtitle().component(), labelWidth);
            int labelHeight = lineBlockHeight(font, titleLines.size() + subtitleLines.size(), ICON_LINE_GAP);
            int height = Math.max(ICON_BOX_SIZE, labelHeight) + TITLE_ICON_BOTTOM_GAP;
            return new PreparedTitleIcon(titleLines, subtitleLines, height);
        }

        return new PreparedSpacer(0);
    }

    private static List<FormattedCharSequence> split(Font font, Component component, int width) {
        return List.copyOf(font.split(component, width));
    }

    private static int maxLineWidth(Font font, List<FormattedCharSequence> lines) {
        int width = 0;

        for (FormattedCharSequence line : lines) {
            width = Math.max(width, font.width(line));
        }

        return width;
    }

    private static int lineBlockHeight(Font font, int lineCount, int gap) {
        return lineCount * (font.lineHeight + gap);
    }

    private static int pageIndex(Identifier itemId, int pageCount) {
        if (pageCount <= 0) {
            resetPageState();
            return 0;
        }

        if (!itemId.equals(lastHoveredItem)) {
            selectedPage = 0;
            wasAltDown = false;
            lastHoveredItem = itemId;
        }

        boolean altDown = Minecraft.getInstance().hasAltDown();

        if (altDown && !wasAltDown) {
            selectedPage = (selectedPage + 1) % pageCount;
        }

        wasAltDown = altDown;
        return Math.min(selectedPage, pageCount - 1);
    }

    private static void resetPageState() {
        selectedPage = 0;
        wasAltDown = false;
        lastHoveredItem = null;
    }

    private static Component pageHintComponent(int pageIndex, int pageCount) {
        return Component.translatable("tooltip.zenithlib.page_hint", pageIndex + 1, pageCount)
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    public record Layout(
            ZenithTooltipTheme theme,
            List<FormattedCharSequence> titleLines,
            List<PreparedElement> elements,
            List<FormattedCharSequence> pageHintLines,
            int width,
            int height,
            int innerWidth
    ) {}

    public sealed interface PreparedElement
            permits PreparedText, PreparedHeader, PreparedDivider, PreparedSpacer, PreparedRow, PreparedIcon, PreparedTitleIcon {
        int height();
    }

    public record PreparedText(List<FormattedCharSequence> lines, int color, int height) implements PreparedElement {}

    public record PreparedHeader(List<FormattedCharSequence> lines, int color, int height) implements PreparedElement {}

    public record PreparedDivider(int height) implements PreparedElement {}

    public record PreparedSpacer(int height) implements PreparedElement {}

    public record PreparedRow(
            List<FormattedCharSequence> leftLines,
            List<FormattedCharSequence> rightLines,
            int leftColor,
            int rightColor,
            int height
    ) implements PreparedElement {}

    public record PreparedIcon(int height) implements PreparedElement {}

    public record PreparedTitleIcon(
            List<FormattedCharSequence> titleLines,
            List<FormattedCharSequence> subtitleLines,
            int height
    ) implements PreparedElement {}
}
