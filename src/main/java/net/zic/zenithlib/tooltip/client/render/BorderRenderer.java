package net.zic.zenithlib.tooltip.client.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.zic.zenithlib.tooltip.api.TooltipTheme;

/**
 * Renders borders, frames, and the diamond badge for tooltips.
 * Updated for NeoForge 26.1 with extraction-based rendering.
 */
public class BorderRenderer {

    /**
     * Draws a diamond-shaped frame around the item icon.
     */
    public static void drawDiamondFrame(GuiGraphicsExtractor graphics, int centerX, int centerY, int size,
                                        TooltipTheme theme) {
        int halfSize = size / 2;

        // Draw the outer diamond frame
        drawDiamond(graphics, centerX, centerY, halfSize + 2, theme.diamondFrame());

        // Draw the inner diamond frame
        drawDiamond(graphics, centerX, centerY, halfSize, theme.diamondFrameInner());

        // Draw the background cutout
        drawDiamond(graphics, centerX, centerY, halfSize - 2, theme.badgeCutout());
    }

    /**
     * Draws a square frame around the item icon.
     */
    public static void drawSquareFrame(GuiGraphicsExtractor graphics, int x, int y, int size,
                                       TooltipTheme theme) {
        // Outer border
        drawRect(graphics, x - 2, y - 2, size + 4, size + 4, theme.diamondFrame());

        // Inner border
        drawRect(graphics, x, y, size, size, theme.diamondFrameInner());

        // Background cutout
        drawRect(graphics, x + 2, y + 2, size - 4, size - 4, theme.badgeCutout());
    }

    /**
     * Draws a circular frame around the item icon.
     */
    public static void drawCircleFrame(GuiGraphicsExtractor graphics, int centerX, int centerY, int radius,
                                       TooltipTheme theme) {
        // Outer circle
        drawCircle(graphics, centerX, centerY, radius + 2, theme.diamondFrame());

        // Inner circle
        drawCircle(graphics, centerX, centerY, radius, theme.diamondFrameInner());

        // Background cutout
        drawCircle(graphics, centerX, centerY, radius - 2, theme.badgeCutout());
    }

    /**
     * Draws the tooltip background with border.
     */
    public static void drawTooltipBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                             TooltipTheme theme) {
        // Draw outer border
        drawRect(graphics, x - 1, y - 1, width + 2, height + 2, theme.border());

        // Draw inner border
        drawRect(graphics, x, y, width, height, theme.borderInner());

        // Draw background gradient
        drawGradientRect(graphics, x + 1, y + 1, width - 2, height - 2, theme.bgTop(), theme.bgBottom());
    }

    /**
     * Draws a separator line.
     */
    public static void drawSeparator(GuiGraphicsExtractor graphics, int x, int y, int width, TooltipTheme theme) {
        drawHorizontalLine(graphics, x, y, width, theme.separator());
    }

    /**
     * Draws page indicator dots at the bottom of the tooltip.
     */
    public static void drawPageIndicators(GuiGraphicsExtractor graphics, int centerX, int y,
                                          int currentPage, int totalPages, TooltipTheme theme) {
        if (totalPages <= 1) {
            return;
        }

        int dotSize = 4;
        int dotSpacing = 8;
        int totalWidth = (totalPages * dotSize) + ((totalPages - 1) * dotSpacing);
        int startX = centerX - (totalWidth / 2);

        for (int i = 0; i < totalPages; i++) {
            int dotX = startX + (i * (dotSize + dotSpacing));
            int color = (i == currentPage) ? theme.pageDotFilled() : theme.pageDotEmpty();

            // Draw filled or hollow dot
            if (i == currentPage) {
                drawRect(graphics, dotX, y, dotSize, dotSize, color);
            } else {
                drawHollowRect(graphics, dotX, y, dotSize, dotSize, color);
            }
        }
    }

    /**
     * Draws a diamond shape.
     */
    private static void drawDiamond(GuiGraphicsExtractor graphics, int centerX, int centerY, int halfSize, int color) {
        for (int i = 0; i <= halfSize; i++) {
            int width = halfSize - i;
            // Top half
            drawHorizontalLine(graphics, centerX - width, centerY - halfSize + i, width * 2 + 1, color);
            // Bottom half
            drawHorizontalLine(graphics, centerX - width, centerY + halfSize - i, width * 2 + 1, color);
        }
    }

    /**
     * Draws a filled rectangle.
     */
    private static void drawRect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        graphics.fill(x, y, x + width, y + height, color);
    }

    /**
     * Draws a hollow rectangle (outline only).
     */
    private static void drawHollowRect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        // Top line
        drawHorizontalLine(graphics, x, y, width, color);
        // Bottom line
        drawHorizontalLine(graphics, x, y + height - 1, width, color);
        // Left line
        drawVerticalLine(graphics, x, y, height, color);
        // Right line
        drawVerticalLine(graphics, x + width - 1, y, height, color);
    }

    /**
     * Draws a gradient rectangle.
     */
    private static void drawGradientRect(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                         int colorTop, int colorBottom) {
        if (width <= 0 || height <= 0) {
            return;
        }
        graphics.fillGradient(x, y, x + width, y + height, colorTop, colorBottom);
    }

    /**
     * Draws a horizontal line.
     */
    private static void drawHorizontalLine(GuiGraphicsExtractor graphics, int x, int y, int width, int color) {
        if (width <= 0) {
            return;
        }
        graphics.fill(x, y, x + width, y + 1, color);
    }

    /**
     * Draws a vertical line.
     */
    private static void drawVerticalLine(GuiGraphicsExtractor graphics, int x, int y, int height, int color) {
        if (height <= 0) {
            return;
        }
        graphics.fill(x, y, x + 1, y + height, color);
    }

    /**
     * Draws a circle (approximated with pixels).
     */
    private static void drawCircle(GuiGraphicsExtractor graphics, int centerX, int centerY, int radius, int color) {
        if (radius <= 0) {
            return;
        }
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    graphics.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
                }
            }
        }
    }
}