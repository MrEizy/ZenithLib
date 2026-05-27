package net.zic.zenithlib.tooltip.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;

import java.util.List;

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
            renderHeaderOrnament(
                    graphics,
                    textX,
                    textY,
                    layout.innerWidth(),
                    maxLineWidth(font, layout.titleLines()),
                    ZenithTooltipLayout.lineBlockHeight(font, layout.titleLines().size(), ZenithTooltipLayout.LINE_GAP),
                    theme
            );
            textY += ZenithTooltipLayout.lineBlockHeight(font, layout.titleLines().size(), ZenithTooltipLayout.LINE_GAP)
                    + ZenithTooltipLayout.TITLE_BODY_GAP;
        } else if (layout.titleIconHeader() != null) {
            ZenithTooltipLayout.PreparedTitleIcon titleIconHeader = layout.titleIconHeader();
            renderTitleIcon(font, graphics, textX, textY, stack, theme, layout.innerWidth(), titleIconHeader);
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
        renderBackgroundPattern(graphics, x, y, width, height, theme);

        if (width > 1 && height > 1) {
            graphics.fill(x, y, x + width, y + 1, theme.borderTop());
            graphics.fill(x, y, x + 1, y + height, theme.borderTop());
            graphics.fill(x, y + height - 1, x + width, y + height, theme.borderBottom());
            graphics.fill(x + width - 1, y, x + width, y + height, theme.borderBottom());
        }

        renderFrameDecorations(graphics, x, y, width, height, theme);
    }

    private static void renderFrameDecorations(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            ZenithTooltipTheme theme
    ) {
        ZenithTooltipTheme.FrameStyle style = theme.frameStyle();

        if (width > 4 && height > 4) {
            if (style.innerBorder()) {
                int inset = style.innerBorderInset();
                int innerWidth = width - inset * 2;
                int innerHeight = height - inset * 2;
                if (innerWidth > 0 && innerHeight > 0) {
                    graphics.outline(x + inset, y + inset, innerWidth, innerHeight, style.innerBorderColorValue(theme));
                }
            } else {
                graphics.outline(x + 1, y + 1, width - 2, height - 2, INNER_HIGHLIGHT);
            }
        }

        if (style.cornerDecoration() == ZenithTooltipTheme.CornerDecoration.NONE || width < 8 || height < 8) {
            return;
        }

        int color = style.cornerColorValue(theme);
        int size = Math.min(style.cornerSize(), Math.min(width, height) / 2);
        int inset = style.cornerInset();

        renderCornerDecoration(graphics, x + inset, y + inset, size, color, style.cornerDecoration(), Corner.TOP_LEFT);
        renderCornerDecoration(graphics, x + width - inset - size, y + inset, size, color, style.cornerDecoration(), Corner.TOP_RIGHT);
        renderCornerDecoration(graphics, x + inset, y + height - inset - size, size, color, style.cornerDecoration(), Corner.BOTTOM_LEFT);
        renderCornerDecoration(graphics, x + width - inset - size, y + height - inset - size, size, color, style.cornerDecoration(), Corner.BOTTOM_RIGHT);
    }

    private static void renderBackgroundPattern(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            ZenithTooltipTheme theme
    ) {
        ZenithTooltipTheme.BackgroundStyle style = theme.backgroundStyle();
        if (style.pattern() == ZenithTooltipTheme.Pattern.NONE || style.alpha() <= 0 || width <= 2 || height <= 2) {
            return;
        }

        int color = style.colorValue(theme);
        int left = x + 1;
        int top = y + 1;
        int right = x + width - 1;
        int bottom = y + height - 1;
        int spacing = style.spacing();

        switch (style.pattern()) {
            case DIAGONAL_LINES -> {
                for (int start = -height; start < width; start += spacing) {
                    for (int step = 0; step < width + height; step++) {
                        int px = left + start + step;
                        int py = top + step;
                        if (px >= left && px < right && py >= top && py < bottom) {
                            graphics.fill(px, py, px + 1, py + 1, color);
                        }
                    }
                }
            }
            case GRID -> {
                for (int px = left + spacing; px < right; px += spacing) {
                    graphics.fill(px, top, px + 1, bottom, color);
                }
                for (int py = top + spacing; py < bottom; py += spacing) {
                    graphics.fill(left, py, right, py + 1, color);
                }
            }
            case STARS -> {
                for (int py = top + 3; py < bottom; py += spacing) {
                    int rowOffset = ((py - top) / spacing) % 2 == 0 ? 0 : spacing / 2;
                    for (int px = left + 3 + rowOffset; px < right; px += spacing) {
                        renderStar(graphics, px, py, color, left, top, right, bottom);
                    }
                }
            }
            case RUNES -> {
                for (int py = top + 3; py < bottom; py += spacing) {
                    int rowOffset = ((py - top) / spacing) % 2 == 0 ? 0 : spacing / 2;
                    for (int px = left + 3 + rowOffset; px < right; px += spacing) {
                        renderTinyRune(graphics, px, py, color, left, top, right, bottom);
                    }
                }
            }
            case NONE -> {
                // Already handled.
            }
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
            renderHeaderOrnament(
                    graphics,
                    textX,
                    textY,
                    innerWidth,
                    maxLineWidth(font, header.lines()),
                    ZenithTooltipLayout.lineBlockHeight(font, header.lines().size(), ZenithTooltipLayout.LINE_GAP),
                    theme
            );
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
            renderTitleIcon(font, graphics, textX, textY, stack, theme, innerWidth, titleIcon);
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
        int centerX = textX + innerWidth / 2;
        int radius = ornamentSize / 2;

        switch (style.decoration()) {
            case NONE -> graphics.fill(textX, lineY, textX + innerWidth, lineY + style.thickness(), color);
            case DIAMOND -> {
                int leftEnd = centerX - radius - 2;
                int rightStart = centerX + radius + 2;
                graphics.fill(textX, lineY, leftEnd, lineY + style.thickness(), color);
                graphics.fill(rightStart, lineY, textX + innerWidth, lineY + style.thickness(), color);
                renderSolidDiamond(graphics, centerX, textY + style.gapAbove() + radius, radius, color);
            }
            case DOUBLE_DIAMOND -> {
                int offset = radius + 3;
                int leftCenter = centerX - offset;
                int rightCenter = centerX + offset;
                graphics.fill(textX, lineY, leftCenter - radius - 2, lineY + style.thickness(), color);
                graphics.fill(leftCenter + radius + 2, lineY, rightCenter - radius - 2, lineY + style.thickness(), color);
                graphics.fill(rightCenter + radius + 2, lineY, textX + innerWidth, lineY + style.thickness(), color);
                int centerY = textY + style.gapAbove() + radius;
                renderSolidDiamond(graphics, leftCenter, centerY, radius, color);
                renderSolidDiamond(graphics, rightCenter, centerY, radius, color);
            }
            case CENTER_RUNE -> {
                int leftEnd = centerX - radius - 3;
                int rightStart = centerX + radius + 4;
                graphics.fill(textX, lineY, leftEnd, lineY + style.thickness(), color);
                graphics.fill(rightStart, lineY, textX + innerWidth, lineY + style.thickness(), color);
                int centerY = textY + style.gapAbove() + radius;
                renderSolidDiamond(graphics, centerX, centerY, radius, color);
                graphics.fill(centerX, centerY - radius - 1, centerX + 1, centerY + radius + 2, color);
            }
            case DOTTED -> {
                int step = style.thickness() + 3;
                for (int px = textX; px < textX + innerWidth; px += step) {
                    int dotWidth = Math.min(style.thickness() + 1, textX + innerWidth - px);
                    graphics.fill(px, lineY, px + dotWidth, lineY + style.thickness(), color);
                }
            }
        }
    }

    private static void renderHeaderOrnament(
            GuiGraphicsExtractor graphics,
            int textX,
            int textY,
            int innerWidth,
            int contentWidth,
            int contentHeight,
            ZenithTooltipTheme theme
    ) {
        ZenithTooltipTheme.HeaderStyle style = theme.headerStyle();
        if (style.ornament() == ZenithTooltipTheme.Ornament.NONE) {
            return;
        }

        int color = style.colorValue(theme);
        int lineY = textY + Math.max(0, contentHeight) + 1;
        int centerX = textX + innerWidth / 2;
        int left = textX;
        int right = textX + innerWidth;

        switch (style.ornament()) {
            case SIDE_LINES -> {
                int gap = Math.max(10, Math.min(innerWidth / 3, contentWidth / 2 + 8));
                graphics.fill(left, lineY, Math.max(left, centerX - gap), lineY + 1, color);
                graphics.fill(Math.min(right, centerX + gap), lineY, right, lineY + 1, color);
            }
            case CORNER_TICKS -> {
                graphics.fill(left, lineY, right, lineY + 1, color);
                graphics.fill(left, lineY - 2, left + 1, lineY + 1, color);
                graphics.fill(right - 1, lineY - 2, right, lineY + 1, color);
            }
            case SMALL_DIAMONDS -> {
                int radius = 1;
                graphics.fill(left, lineY, centerX - 8, lineY + 1, color);
                graphics.fill(centerX + 8, lineY, right, lineY + 1, color);
                renderSolidDiamond(graphics, centerX, lineY, radius, color);
                renderSolidDiamond(graphics, centerX - 6, lineY, radius, color);
                renderSolidDiamond(graphics, centerX + 6, lineY, radius, color);
            }
            case NONE -> {
                // Already handled.
            }
        }
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
            int innerWidth,
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

        int textBlockWidth = Math.max(
                maxLineWidth(font, titleIcon.titleLines()),
                maxLineWidth(font, titleIcon.subtitleLines())
        );
        renderHeaderOrnament(
                graphics,
                labelX,
                textY,
                innerWidth - holder.boxSize() - holder.gap(),
                textBlockWidth,
                holder.boxSize() - 2,
                theme
        );
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
            case OCTAGON -> renderOctagonBox(
                    graphics,
                    boxX,
                    boxY,
                    holder.boxSize(),
                    holder.borderWidth(),
                    holder.borderColor(theme),
                    holder.fillColor(theme)
            );
            case CIRCLE -> renderCircleBox(
                    graphics,
                    boxX,
                    boxY,
                    holder.boxSize(),
                    holder.borderWidth(),
                    holder.borderColor(theme),
                    holder.fillColor(theme)
            );
            case GEM -> renderGemBox(
                    graphics,
                    boxX,
                    boxY,
                    holder.boxSize(),
                    holder.borderWidth(),
                    holder.borderColor(theme),
                    holder.fillColor(theme)
            );
            case BRACKET -> renderBracketBox(
                    graphics,
                    boxX,
                    boxY,
                    holder.boxSize(),
                    holder.borderWidth(),
                    holder.borderColor(theme),
                    holder.fillColor(theme)
            );
            case NONE -> {
                // Already handled.
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

    private static void renderBracketBox(
            GuiGraphicsExtractor graphics,
            int boxX,
            int boxY,
            int boxSize,
            int borderWidth,
            int border,
            int fill
    ) {
        int inset = Math.max(1, borderWidth);
        graphics.fill(boxX + inset, boxY + inset, boxX + boxSize - inset, boxY + boxSize - inset, fill);

        int arm = Math.max(4, boxSize / 3);
        int thickness = Math.max(1, borderWidth);
        renderBracketCorner(graphics, boxX, boxY, arm, thickness, border, Corner.TOP_LEFT);
        renderBracketCorner(graphics, boxX + boxSize - arm, boxY, arm, thickness, border, Corner.TOP_RIGHT);
        renderBracketCorner(graphics, boxX, boxY + boxSize - arm, arm, thickness, border, Corner.BOTTOM_LEFT);
        renderBracketCorner(graphics, boxX + boxSize - arm, boxY + boxSize - arm, arm, thickness, border, Corner.BOTTOM_RIGHT);
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

    private static void renderOctagonBox(
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
        int cut = Math.max(3, (boxSize + 2) / 3);
        int innerCut = Math.max(0, cut - borderWidth);
        int innerRadius = Math.max(0, radius - borderWidth);

        for (int dy = -radius; dy <= radius; dy++) {
            int yLine = centerY + dy;
            int absDy = Math.abs(dy);
            int borderHalfWidth = absDy > radius - cut ? radius - (absDy - (radius - cut)) : radius;
            graphics.fill(centerX - borderHalfWidth, yLine, centerX + borderHalfWidth + 1, yLine + 1, border);

            if (absDy <= innerRadius) {
                int innerHalfWidth = absDy > innerRadius - innerCut
                        ? innerRadius - (absDy - (innerRadius - innerCut))
                        : innerRadius;
                graphics.fill(centerX - innerHalfWidth, yLine, centerX + innerHalfWidth + 1, yLine + 1, fill);
            }
        }
    }

    private static void renderCircleBox(
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
        int outerSquared = radius * radius;
        int innerRadius = Math.max(0, radius - borderWidth);
        int innerSquared = innerRadius * innerRadius;

        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int distance = dx * dx + dy * dy;
                if (distance <= outerSquared) {
                    int px = centerX + dx;
                    int py = centerY + dy;
                    graphics.fill(px, py, px + 1, py + 1, distance <= innerSquared ? fill : border);
                }
            }
        }
    }

    private static void renderGemBox(
            GuiGraphicsExtractor graphics,
            int boxX,
            int boxY,
            int boxSize,
            int borderWidth,
            int border,
            int fill
    ) {
        renderOctagonBox(graphics, boxX, boxY, boxSize, borderWidth, border, fill);
        int centerX = boxX + boxSize / 2;
        int centerY = boxY + boxSize / 2;
        int radius = Math.max(1, boxSize / 4);
        renderSolidDiamond(graphics, centerX, centerY, radius, border);
        if (radius > 1) {
            renderSolidDiamond(graphics, centerX, centerY, radius - 1, fill);
        }
    }

    private static void renderCornerDecoration(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int size,
            int color,
            ZenithTooltipTheme.CornerDecoration decoration,
            Corner corner
    ) {
        switch (decoration) {
            case DIAMOND -> renderSolidDiamond(graphics, x + size / 2, y + size / 2, Math.max(1, size / 2), color);
            case BRACKET -> renderBracketCorner(graphics, x, y, size, 1, color, corner);
            case NOTCHED -> renderNotchedCorner(graphics, x, y, size, color, corner);
            case RUNE -> {
                renderBracketCorner(graphics, x, y, size, 1, color, corner);
                int centerX = x + size / 2;
                int centerY = y + size / 2;
                renderSolidDiamond(graphics, centerX, centerY, 1, color);
                graphics.fill(centerX, centerY - 2, centerX + 1, centerY + 3, color);
            }
            case NONE -> {
                // Already handled.
            }
        }
    }

    private static void renderBracketCorner(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int size,
            int thickness,
            int color,
            Corner corner
    ) {
        switch (corner) {
            case TOP_LEFT -> {
                graphics.fill(x, y, x + size, y + thickness, color);
                graphics.fill(x, y, x + thickness, y + size, color);
            }
            case TOP_RIGHT -> {
                graphics.fill(x, y, x + size, y + thickness, color);
                graphics.fill(x + size - thickness, y, x + size, y + size, color);
            }
            case BOTTOM_LEFT -> {
                graphics.fill(x, y + size - thickness, x + size, y + size, color);
                graphics.fill(x, y, x + thickness, y + size, color);
            }
            case BOTTOM_RIGHT -> {
                graphics.fill(x, y + size - thickness, x + size, y + size, color);
                graphics.fill(x + size - thickness, y, x + size, y + size, color);
            }
        }
    }

    private static void renderNotchedCorner(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int size,
            int color,
            Corner corner
    ) {
        int step = Math.max(1, size / 3);
        switch (corner) {
            case TOP_LEFT -> {
                graphics.fill(x + step, y, x + size, y + 1, color);
                graphics.fill(x, y + step, x + 1, y + size, color);
                graphics.fill(x + 1, y + step - 1, x + step + 1, y + step, color);
                graphics.fill(x + step - 1, y + 1, x + step, y + step + 1, color);
            }
            case TOP_RIGHT -> {
                graphics.fill(x, y, x + size - step, y + 1, color);
                graphics.fill(x + size - 1, y + step, x + size, y + size, color);
                graphics.fill(x + size - step - 1, y + 1, x + size - step, y + step + 1, color);
                graphics.fill(x + size - step, y + step - 1, x + size - 1, y + step, color);
            }
            case BOTTOM_LEFT -> {
                graphics.fill(x + step, y + size - 1, x + size, y + size, color);
                graphics.fill(x, y, x + 1, y + size - step, color);
                graphics.fill(x + 1, y + size - step, x + step + 1, y + size - step + 1, color);
                graphics.fill(x + step - 1, y + size - step - 1, x + step, y + size - 1, color);
            }
            case BOTTOM_RIGHT -> {
                graphics.fill(x, y + size - 1, x + size - step, y + size, color);
                graphics.fill(x + size - 1, y, x + size, y + size - step, color);
                graphics.fill(x + size - step - 1, y + size - step, x + size - 1, y + size - step + 1, color);
                graphics.fill(x + size - step, y + size - step - 1, x + size - step + 1, y + size - 1, color);
            }
        }
    }

    private static void renderStar(
            GuiGraphicsExtractor graphics,
            int centerX,
            int centerY,
            int color,
            int left,
            int top,
            int right,
            int bottom
    ) {
        if (centerX <= left || centerX >= right || centerY <= top || centerY >= bottom) {
            return;
        }

        graphics.fill(centerX, centerY, centerX + 1, centerY + 1, color);
        if (centerX + 2 < right) {
            graphics.fill(centerX + 2, centerY, centerX + 3, centerY + 1, color);
        }
        if (centerY + 2 < bottom) {
            graphics.fill(centerX + 1, centerY + 1, centerX + 2, centerY + 2, color);
        }
    }

    private static void renderTinyRune(
            GuiGraphicsExtractor graphics,
            int centerX,
            int centerY,
            int color,
            int left,
            int top,
            int right,
            int bottom
    ) {
        if (centerX - 1 < left || centerX + 1 >= right || centerY - 1 < top || centerY + 3 >= bottom) {
            return;
        }

        renderSolidDiamond(graphics, centerX, centerY, 1, color);
        graphics.fill(centerX, centerY + 2, centerX + 1, centerY + 4, color);
    }

    private static int maxLineWidth(Font font, List<FormattedCharSequence> lines) {
        int width = 0;
        for (FormattedCharSequence line : lines) {
            width = Math.max(width, font.width(line));
        }
        return width;
    }

    private enum Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}