package net.zic.zenithlib.tooltip.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;

/**
 * Draws a prepared Zenith tooltip layout through Minecraft's client graphics extractor.
 *
 * <p>The renderer is intentionally free of resource lookup and text wrapping. Long
 * tooltip pages are drawn inside a scissored body viewport while their header and hint
 * remain stationary; ordinary pages follow the same draw path without clipping.</p>
 */
public final class ZenithTooltipRenderer {
    private static final int INNER_HIGHLIGHT = 0x33FFFFFF;
    private static final int ITEM_ICON_SIZE = 16;

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

        if (!layout.titleLines().isEmpty()) {
            renderLines(font, graphics, textX, textY, layout.titleLines(), theme.text(), ZenithTooltipLayout.LINE_GAP);
            textY += ZenithTooltipLayout.lineBlockHeight(font, layout.titleLines().size(), ZenithTooltipLayout.LINE_GAP)
                    + ZenithTooltipLayout.TITLE_BODY_GAP;
        } else if (layout.titleIconHeader() != null) {
            ZenithTooltipLayout.PreparedTitleIcon titleIconHeader = layout.titleIconHeader();
            renderTitleIcon(font, graphics, textX, textY, stack, theme, titleIconHeader);
            textY += titleIconHeader.height();
            if (!layout.elements().isEmpty()) {
                textY += ZenithTooltipLayout.gapAfter(theme, titleIconHeader, layout.elements().get(0));
            }
        }

        int bodyY = textY;
        if (layout.scrollable()) {
            graphics.enableScissor(
                    textX,
                    bodyY,
                    textX + layout.innerWidth(),
                    bodyY + layout.bodyViewportHeight()
            );
            textY -= layout.scrollOffset();
        }

        for (int i = 0; i < layout.elements().size(); i++) {
            ZenithTooltipLayout.PreparedElement element = layout.elements().get(i);
            renderElement(font, graphics, textX, textY, stack, theme, layout.innerWidth(), element);
            textY += element.height();

            if (i + 1 < layout.elements().size()) {
                textY += ZenithTooltipLayout.gapAfter(theme, element, layout.elements().get(i + 1));
            }
        }

        if (layout.scrollable()) {
            graphics.disableScissor();
        }

        int hintY = bodyY + layout.bodyViewportHeight();
        renderPageHint(font, graphics, textX, hintY, layout.pageHintLines(), theme.muted());
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
            renderDivider(graphics, textX, textY, innerWidth, theme);
            return;
        }

        if (element instanceof ZenithTooltipLayout.PreparedRow row) {
            renderLines(font, graphics, textX, textY, row.leftLines(), row.leftColor(), ZenithTooltipLayout.LINE_GAP);
            renderRightAlignedLines(font, graphics, textX, textY, innerWidth, row.rightLines(), row.rightColor());
            return;
        }

        if (element instanceof ZenithTooltipLayout.PreparedBadge badge) {
            renderBadge(font, graphics, textX, textY, theme, badge);
            return;
        }

        if (element instanceof ZenithTooltipLayout.PreparedBar bar) {
            renderBar(font, graphics, textX, textY, innerWidth, theme, bar);
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

    private static void renderRightAlignedLines(
            Font font,
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            Iterable<FormattedCharSequence> lines,
            int color
    ) {
        for (FormattedCharSequence line : lines) {
            graphics.text(font, line, x + width - font.width(line), y, color, false);
            y += font.lineHeight + ZenithTooltipLayout.LINE_GAP;
        }
    }

    private static void renderDivider(
            GuiGraphicsExtractor graphics,
            int textX,
            int textY,
            int innerWidth,
            ZenithTooltipTheme theme
    ) {
        ZenithTooltipTheme.DividerStyle style = theme.dividerStyle();
        int color = style.colorValue(theme);
        int ornamentSize = style.ornamentSize();
        int lineY = textY + style.gapAbove() + (ornamentSize - style.thickness()) / 2;

        if (style.decoration() == ZenithTooltipTheme.Decoration.NONE) {
            graphics.fill(textX, lineY, textX + innerWidth, lineY + style.thickness(), color);
            return;
        }

        int centerX = textX + innerWidth / 2;
        int radius = ornamentSize / 2;
        int leftEnd = centerX - radius - 2;
        int rightStart = centerX + radius + 2;
        graphics.fill(textX, lineY, leftEnd, lineY + style.thickness(), color);
        graphics.fill(rightStart, lineY, textX + innerWidth, lineY + style.thickness(), color);
        renderSolidDiamond(graphics, centerX, textY + style.gapAbove() + radius, radius, color);
    }

    private static void renderSolidDiamond(
            GuiGraphicsExtractor graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {
        for (int dy = -radius; dy <= radius; dy++) {
            int halfWidth = radius - Math.abs(dy);
            graphics.fill(centerX - halfWidth, centerY + dy, centerX + halfWidth + 1, centerY + dy + 1, color);
        }
    }

    private static void renderBadge(
            Font font,
            GuiGraphicsExtractor graphics,
            int textX,
            int textY,
            ZenithTooltipTheme theme,
            ZenithTooltipLayout.PreparedBadge badge
    ) {
        ZenithTooltipTheme.BadgeStyle style = theme.badgeStyle();
        int width = 0;
        for (FormattedCharSequence line : badge.lines()) {
            width = Math.max(width, font.width(line));
        }
        width += style.horizontalPadding() * 2;

        graphics.fill(textX, textY, textX + width, textY + badge.height(), style.fillColor(badge.backgroundColor()));
        for (int inset = 0; inset < style.borderWidth() && width - inset * 2 > 0 && badge.height() - inset * 2 > 0; inset++) {
            graphics.outline(textX + inset, textY + inset, width - inset * 2, badge.height() - inset * 2, badge.borderColor());
        }
        renderLines(
                font,
                graphics,
                textX + style.horizontalPadding(),
                textY + style.verticalPadding(),
                badge.lines(),
                badge.textColor(),
                0
        );
    }

    private static void renderBar(
            Font font,
            GuiGraphicsExtractor graphics,
            int textX,
            int textY,
            int innerWidth,
            ZenithTooltipTheme theme,
            ZenithTooltipLayout.PreparedBar bar
    ) {
        ZenithTooltipTheme.BarStyle style = theme.barStyle();
        renderLines(font, graphics, textX, textY, bar.labelLines(), theme.text(), ZenithTooltipLayout.LINE_GAP);
        renderRightAlignedLines(font, graphics, textX, textY, innerWidth, bar.valueLines(), bar.color());

        int barY = textY + bar.labelHeight() + style.labelGap();
        graphics.fill(textX, barY, textX + innerWidth, barY + style.height(), style.trackColor(theme));

        int inset = style.borderWidth();
        int fillAreaWidth = Math.max(0, innerWidth - inset * 2);
        int fillHeight = Math.max(0, style.height() - inset * 2);
        int fillWidth = Math.round(fillAreaWidth * bar.progress());
        if (fillWidth > 0 && fillHeight > 0) {
            graphics.fill(
                    textX + inset,
                    barY + inset,
                    textX + inset + fillWidth,
                    barY + inset + fillHeight,
                    style.fillColor(bar.color())
            );
        }

        for (int borderInset = 0; borderInset < style.borderWidth(); borderInset++) {
            graphics.outline(
                    textX + borderInset,
                    barY + borderInset,
                    innerWidth - borderInset * 2,
                    style.height() - borderInset * 2,
                    style.borderColor(theme)
            );
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
        ZenithTooltipTheme.IconHolder holder = theme.iconHolder();
        int boxX = textX + innerWidth / 2 - holder.boxSize() / 2;
        renderItemHolder(graphics, boxX, textY, stack, theme);
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
        ZenithTooltipTheme.IconHolder holder = theme.iconHolder();
        renderItemHolder(graphics, textX, textY, stack, theme);

        int labelX = textX + holder.boxSize() + holder.gap();
        int labelLineCount = titleIcon.titleLines().size() + titleIcon.subtitleLines().size();
        int labelHeight = ZenithTooltipLayout.lineBlockHeight(font, labelLineCount, ZenithTooltipLayout.ICON_LINE_GAP);
        int labelY = textY + Math.max(0, (holder.boxSize() - labelHeight) / 2);

        renderLines(font, graphics, labelX, labelY, titleIcon.titleLines(), theme.text(), ZenithTooltipLayout.ICON_LINE_GAP);
        if (!titleIcon.titleLines().isEmpty() && !titleIcon.subtitleLines().isEmpty()) {
            labelY += ZenithTooltipLayout.lineBlockHeight(font, titleIcon.titleLines().size(), ZenithTooltipLayout.ICON_LINE_GAP)
                    + ZenithTooltipLayout.ICON_LINE_GAP;
        } else {
            labelY += ZenithTooltipLayout.lineBlockHeight(font, titleIcon.titleLines().size(), ZenithTooltipLayout.ICON_LINE_GAP);
        }
        renderLines(font, graphics, labelX, labelY, titleIcon.subtitleLines(), theme.accent(), ZenithTooltipLayout.ICON_LINE_GAP);
    }

    private static void renderItemHolder(
            GuiGraphicsExtractor graphics,
            int boxX,
            int boxY,
            ItemStack stack,
            ZenithTooltipTheme theme
    ) {
        ZenithTooltipTheme.IconHolder holder = theme.iconHolder();

        switch (holder.shape()) {
            case DIAMOND -> renderDiamondBox(
                    graphics,
                    boxX,
                    boxY,
                    holder.boxSize(),
                    holder.borderWidth(),
                    holder.borderColor(theme),
                    holder.fillColor(theme)
            );
            case SQUARE -> renderSquareBox(
                    graphics,
                    boxX,
                    boxY,
                    holder.boxSize(),
                    holder.borderWidth(),
                    holder.borderColor(theme),
                    holder.fillColor(theme)
            );
            case NONE -> {
                // Intentionally render only the item itself for minimal themes.
            }
        }

        int centerX = boxX + holder.boxSize() / 2;
        int centerY = boxY + holder.boxSize() / 2;
        graphics.item(stack, centerX - ITEM_ICON_SIZE / 2, centerY - ITEM_ICON_SIZE / 2);
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

    private static void renderSquareBox(
            GuiGraphicsExtractor graphics,
            int boxX,
            int boxY,
            int boxSize,
            int borderWidth,
            int border,
            int fill
    ) {
        graphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, fill);
        for (int inset = 0; inset < borderWidth && boxSize - inset * 2 > 0; inset++) {
            graphics.outline(boxX + inset, boxY + inset, boxSize - inset * 2, boxSize - inset * 2, border);
        }
    }

    private static void renderDiamondBox(
            GuiGraphicsExtractor graphics,
            int boxX,
            int boxY,
            int boxSize,
            int borderWidth,
            int border,
            int fill
    ) {
        int radius = boxSize / 2;
        int centerX = boxX + radius;
        int centerY = boxY + radius;
        int innerRadius = Math.max(0, radius - borderWidth);

        for (int dy = -radius; dy <= radius; dy++) {
            int halfWidth = radius - Math.abs(dy);
            int yLine = centerY + dy;
            graphics.fill(centerX - halfWidth, yLine, centerX + halfWidth + 1, yLine + 1, border);

            if (Math.abs(dy) <= innerRadius) {
                int innerHalfWidth = innerRadius - Math.abs(dy);
                graphics.fill(centerX - innerHalfWidth, yLine, centerX + innerHalfWidth + 1, yLine + 1, fill);
            }
        }
    }

}
