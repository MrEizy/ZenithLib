package net.zic.zenithlib.tooltip.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;

/**
 * Draws a prepared Zenith tooltip layout through Minecraft's client graphics extractor.
 *
 * <p>The renderer is intentionally free of rule lookup and text measurement. It draws
 * the themed background and bevelled border, wrapped titles and elements, page hints,
 * and the decorative diamond item-icon box using the already prepared layout values.
 * Keeping drawing separate from layout makes render behaviour consistent with reported
 * tooltip dimensions and reduces repeated work while hovering an item.</p>
 */

public final class ZenithTooltipRenderer {
    private static final int INNER_HIGHLIGHT = 0x33FFFFFF;
    private static final int ICON_BOX_ALPHA = 0x55;

    private ZenithTooltipRenderer() {}

    public static void render(
            Font font,
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            ItemStack stack,
            ZenithTooltipLayout.Layout layout
    ) {
        ZenithTooltipTheme theme = layout.theme();
        renderBackground(graphics, x, y, layout.width(), layout.height(), theme);

        int textX = x + theme.padding();
        int textY = y + theme.padding();

        renderLines(font, graphics, textX, textY, layout.titleLines(), theme.text(), ZenithTooltipLayout.LINE_GAP);
        if (!layout.titleLines().isEmpty()) {
            textY += layout.titleLines().size() * (font.lineHeight + ZenithTooltipLayout.LINE_GAP)
                    + ZenithTooltipLayout.TITLE_BODY_GAP;
        }

        for (ZenithTooltipLayout.PreparedElement element : layout.elements()) {
            renderElement(font, graphics, textX, textY, stack, theme, layout.innerWidth(), element);
            textY += element.height() + ZenithTooltipLayout.ELEMENT_GAP;
        }

        if (!layout.elements().isEmpty()) {
            textY -= ZenithTooltipLayout.ELEMENT_GAP;
        }

        renderPageHint(font, graphics, textX, textY, layout.pageHintLines(), theme.muted());
    }

    private static void renderBackground(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            ZenithTooltipTheme theme
    ) {
        graphics.fill(x, y, x + width, y + height, theme.background());

        if (width > 1 && height > 1) {
            graphics.fill(x, y, x + width, y + 1, theme.borderTop());
            graphics.fill(x, y, x + 1, y + height, theme.borderTop());
            graphics.fill(x, y + height - 1, x + width, y + height, theme.borderBottom());
            graphics.fill(x + width - 1, y, x + width, y + height, theme.borderBottom());
        }

        if (width > 4 && height > 4) {
            graphics.outline(x + 1, y + 1, width - 2, height - 2, INNER_HIGHLIGHT);
        }
    }

    private static void renderElement(
            Font font,
            GuiGraphicsExtractor graphics,
            int textX,
            int textY,
            ItemStack stack,
            ZenithTooltipTheme theme,
            int innerWidth,
            ZenithTooltipLayout.PreparedElement element
    ) {
        if (element instanceof ZenithTooltipLayout.PreparedText text) {
            renderLines(font, graphics, textX, textY, text.lines(), text.color(), ZenithTooltipLayout.LINE_GAP);
            return;
        }

        if (element instanceof ZenithTooltipLayout.PreparedHeader header) {
            renderLines(font, graphics, textX, textY, header.lines(), header.color(), ZenithTooltipLayout.LINE_GAP);
            return;
        }

        if (element instanceof ZenithTooltipLayout.PreparedDivider) {
            int dividerY = textY + ZenithTooltipLayout.SECTION_GAP / 2;
            graphics.fill(textX, dividerY, textX + innerWidth, dividerY + 1, theme.accent());
            return;
        }

        if (element instanceof ZenithTooltipLayout.PreparedRow row) {
            renderLines(font, graphics, textX, textY, row.leftLines(), row.leftColor(), ZenithTooltipLayout.LINE_GAP);
            int rightY = textY;
            for (FormattedCharSequence line : row.rightLines()) {
                graphics.text(font, line, textX + innerWidth - font.width(line), rightY, row.rightColor(), false);
                rightY += font.lineHeight + ZenithTooltipLayout.LINE_GAP;
            }
            return;
        }

        if (element instanceof ZenithTooltipLayout.PreparedIcon) {
            renderCenteredIcon(graphics, textX, textY, stack, theme, innerWidth);
            return;
        }

        if (element instanceof ZenithTooltipLayout.PreparedTitleIcon titleIcon) {
            renderTitleIcon(font, graphics, textX, textY, stack, theme, titleIcon);
        }
    }

    private static void renderLines(
            Font font,
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            Iterable<FormattedCharSequence> lines,
            int color,
            int gap
    ) {
        for (FormattedCharSequence line : lines) {
            graphics.text(font, line, x, y, color, false);
            y += font.lineHeight + gap;
        }
    }

    private static void renderCenteredIcon(
            GuiGraphicsExtractor graphics,
            int textX,
            int textY,
            ItemStack stack,
            ZenithTooltipTheme theme,
            int innerWidth
    ) {
        int boxX = textX + innerWidth / 2 - ZenithTooltipLayout.ICON_BOX_SIZE / 2;
        renderItemBox(graphics, boxX, textY, stack, theme);
    }

    private static void renderTitleIcon(
            Font font,
            GuiGraphicsExtractor graphics,
            int textX,
            int textY,
            ItemStack stack,
            ZenithTooltipTheme theme,
            ZenithTooltipLayout.PreparedTitleIcon titleIcon
    ) {
        renderItemBox(graphics, textX, textY + 2, stack, theme);

        int labelX = textX + ZenithTooltipLayout.ICON_BOX_SIZE + ZenithTooltipLayout.ICON_TEXT_GAP;
        int labelY = textY + 2;

        renderLines(font, graphics, labelX, labelY, titleIcon.titleLines(), theme.text(), ZenithTooltipLayout.ICON_LINE_GAP);
        labelY += titleIcon.titleLines().size() * (font.lineHeight + ZenithTooltipLayout.ICON_LINE_GAP);
        renderLines(font, graphics, labelX, labelY, titleIcon.subtitleLines(), theme.accent(), ZenithTooltipLayout.ICON_LINE_GAP);
    }

    private static void renderItemBox(
            GuiGraphicsExtractor graphics,
            int boxX,
            int boxY,
            ItemStack stack,
            ZenithTooltipTheme theme
    ) {
        renderDiamondBox(
                graphics,
                boxX,
                boxY,
                ZenithTooltipLayout.ICON_BOX_SIZE,
                theme.accent(),
                withAlpha(theme.background(), ICON_BOX_ALPHA)
        );

        int centerX = boxX + ZenithTooltipLayout.ICON_BOX_SIZE / 2;
        int centerY = boxY + ZenithTooltipLayout.ICON_BOX_SIZE / 2;

        graphics.item(
                stack,
                centerX - ZenithTooltipLayout.ICON_SIZE / 2,
                centerY - ZenithTooltipLayout.ICON_SIZE / 2
        );
    }

    private static void renderPageHint(
            Font font,
            GuiGraphicsExtractor graphics,
            int textX,
            int textY,
            Iterable<FormattedCharSequence> lines,
            int color
    ) {
        textY += ZenithTooltipLayout.PAGE_HINT_TOP_GAP;
        renderLines(font, graphics, textX, textY, lines, color, ZenithTooltipLayout.LINE_GAP);
    }

    private static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private static void renderDiamondBox(
            GuiGraphicsExtractor graphics,
            int boxX,
            int boxY,
            int boxSize,
            int border,
            int fill
    ) {
        int centerX = boxX + boxSize / 2;

        for (int dy = 0; dy < boxSize; dy++) {
            int distanceFromCenter = Math.abs(dy - boxSize / 2);
            int halfWidth = boxSize / 2 - distanceFromCenter;
            int yLine = boxY + dy;

            graphics.fill(centerX - halfWidth, yLine, centerX + halfWidth + 1, yLine + 1, fill);
            graphics.fill(centerX - halfWidth, yLine, centerX - halfWidth + 1, yLine + 1, border);
            graphics.fill(centerX + halfWidth, yLine, centerX + halfWidth + 1, yLine + 1, border);
        }
    }
}
