package net.zic.zenithlib.tooltip.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.tooltip.api.ThemeDefinition;
import net.zic.zenithlib.tooltip.api.TooltipTheme;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

import java.util.List;

/**
 * Paints the complete tooltip with all its components.
 * Updated for NeoForge 26.1 with proper extraction-based rendering.
 */
public class TooltipPainter {

    private static final int ITEM_SIZE = 16;
    private static final int BADGE_SIZE = 24;
    private static final int PADDING_X = 12;
    private static final int PADDING_Y = 10;
    private static final int LINE_SPACING = 2;
    private static final int HEADER_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 12;
    private static final int SEPARATOR_PADDING = 4;

    /**
     * Paints the complete tooltip.
     */
    public static void paint(GuiGraphicsExtractor graphics, ItemStack stack, List<Component> lines,
                             ThemeDefinition theme, int x, int y, int screenWidth, int screenHeight,
                             int currentPage, int totalPages) {
        if (lines == null || lines.isEmpty()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        TooltipTheme colors = theme.colors();

        // Calculate dimensions
        int contentWidth = calculateContentWidth(font, lines);
        int contentHeight = calculateContentHeight(font, lines);

        // Calculate title width separately since it has special positioning
        int titleWidth = font.width(lines.get(0));

        // When diamond badge is shown, the title is offset, so we need to account for that
        int titleOffset = theme.showDiamondBadge() ? BADGE_SIZE + 12 : 0;
        int requiredWidth = Math.max(contentWidth, titleWidth + titleOffset);

        int tooltipWidth = requiredWidth + (PADDING_X * 2);
        int tooltipHeight = HEADER_HEIGHT + contentHeight + (PADDING_Y * 2);

        // Add footer space if multiple pages
        if (totalPages > 1 && theme.showPageIndicator()) {
            tooltipHeight += FOOTER_HEIGHT;
        }

        // Adjust position to keep tooltip on screen
        int tooltipX = adjustX(x, tooltipWidth, screenWidth);
        int tooltipY = adjustY(y, tooltipHeight, screenHeight);

        // Draw background
        BorderRenderer.drawTooltipBackground(graphics, tooltipX, tooltipY, tooltipWidth, tooltipHeight, colors);

        // Draw diamond badge with item
        if (theme.showDiamondBadge()) {
            int badgeX = tooltipX + PADDING_X + 4;
            int badgeY = tooltipY + 6;
            drawItemBadge(graphics, stack, badgeX, badgeY, theme);
        }

        // Draw title (item name) - position depends on whether badge is shown
        int titleX = tooltipX + PADDING_X + (theme.showDiamondBadge() ? BADGE_SIZE + 12 : 0);
        int titleY = tooltipY + 10;
        drawTitle(graphics, lines.get(0), titleX, titleY, colors);

        // Draw content lines
        int contentX = tooltipX + PADDING_X;
        int contentY = tooltipY + HEADER_HEIGHT;

        // Skip the first line (title) as it's already drawn
        for (int i = 1; i < lines.size(); i++) {
            Component line = lines.get(i);
            drawLine(graphics, line, contentX, contentY, colors);
            contentY += font.lineHeight + LINE_SPACING;
        }

        // Draw page indicators if multiple pages
        if (totalPages > 1 && theme.showPageIndicator()) {
            int indicatorY = tooltipY + tooltipHeight - FOOTER_HEIGHT + 4;
            BorderRenderer.drawPageIndicators(graphics, tooltipX + tooltipWidth / 2, indicatorY,
                    currentPage, totalPages, colors);
        }
    }

    /**
     * Draws the item badge with the diamond frame.
     */
    private static void drawItemBadge(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y,
                                      ThemeDefinition theme) {
        TooltipTheme colors = theme.colors();
        String shape = theme.itemBorderShape();

        int centerX = x + BADGE_SIZE / 2;
        int centerY = y + BADGE_SIZE / 2;

        // Draw the frame based on shape
        switch (shape.toLowerCase()) {
            case "square":
                BorderRenderer.drawSquareFrame(graphics, x, y, BADGE_SIZE, colors);
                break;
            case "circle":
                BorderRenderer.drawCircleFrame(graphics, centerX, centerY, BADGE_SIZE / 2, colors);
                break;
            case "diamond":
            default:
                BorderRenderer.drawDiamondFrame(graphics, centerX, centerY, BADGE_SIZE, colors);
                break;
        }

        // Draw the item icon with animation
        drawAnimatedItem(graphics, stack, x + 4, y + 4, theme);
    }

    /**
     * Draws the item with animation based on the theme.
     * Updated for 26.1 with proper extraction-based rendering.
     */
    private static void drawAnimatedItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y,
                                         ThemeDefinition theme) {
        String animStyle = theme.itemAnimStyle();
        long elapsed = PageState.getElapsedTime();

        float scale = 1.0f;
        float offsetY = 0.0f;
        float rotation = 0.0f;

        switch (animStyle.toLowerCase()) {
            case "breathe_spin_bob":
                scale = 1.0f + (float) Math.sin(elapsed / 500.0) * 0.1f;
                offsetY = (float) Math.sin(elapsed / 300.0) * 1.5f;
                rotation = (float) Math.sin(elapsed / 800.0) * 5.0f;
                break;
            case "spin":
                rotation = (elapsed / 20.0f) % 360.0f;
                break;
            case "bob":
                offsetY = (float) Math.sin(elapsed / 300.0) * 2.0f;
                break;
            case "breathe":
                scale = 1.0f + (float) Math.sin(elapsed / 500.0) * 0.1f;
                break;
            case "static":
            default:
                // No animation
                break;
        }

        // Get the matrix stack for transformations
        Matrix3x2fStack matrices = graphics.pose();

        matrices.pushMatrix();
        try {
            // Translate to center of item position
            float centerX = x + ITEM_SIZE / 2.0f;
            float centerY = y + ITEM_SIZE / 2.0f + offsetY;

            matrices.translate(centerX, centerY);
            matrices.scale(scale, scale);

            // Apply rotation if needed (Matrix3x2fStack only supports 2D rotation)
            if (rotation != 0.0f) {
                matrices.rotate((float) Math.toRadians(rotation));
            }

            // Translate back so item renders at origin
            matrices.translate(-ITEM_SIZE / 2.0f, -ITEM_SIZE / 2.0f);

            // Render the item - in 26.1, we use the extraction method
            // The graphics extractor handles the actual rendering
            renderItemAtCurrentTransform(graphics, stack);

        } finally {
            matrices.popMatrix();
        }
    }

    /**
     * Renders an item at the current matrix transform.
     * In 26.1, this uses the extraction-based rendering system.
     */
    private static void renderItemAtCurrentTransform(GuiGraphicsExtractor graphics, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        // Get current transform from the matrix stack
        Matrix3x2fStack matrices = graphics.pose();

        // Save current state
        matrices.pushMatrix();

        try {
            // In 26.1, we need to use the item render state system
            // The graphics extractor will handle the actual extraction
            graphics.item(stack, 0, 0);

            // Render item decorations (count, durability bar, etc.)
            graphics.itemDecorations(minecraft.font, stack, 0, 0);
        } finally {
            matrices.popMatrix();
        }
    }

    /**
     * Draws the title with optional animation.
     */
    private static void drawTitle(GuiGraphicsExtractor graphics, Component title, int x, int y,
                                  TooltipTheme colors) {
        Font font = Minecraft.getInstance().font;

        // For now, draw static title - animation can be added later
        graphics.text(font, title, x, y, colors.name(), true);
    }

    /**
     * Draws a single line of text.
     */
    private static void drawLine(GuiGraphicsExtractor graphics, Component line, int x, int y,
                                 TooltipTheme colors) {
        Font font = Minecraft.getInstance().font;

        // Determine color based on line style
        int color = colors.body();
        String text = line.getString();

        // Check for formatting indicators
        if (text.startsWith("§")) {
            // Already has formatting, use as-is
            graphics.text(font, line, x, y, color, false);
        } else if (text.startsWith("  ") || text.startsWith("\t")) {
            // Indented line - might be a detail
            color = colors.hint();
            graphics.text(font, line, x, y, color, false);
        } else {
            // Regular body text
            graphics.text(font, line, x, y, color, false);
        }
    }

    /**
     * Calculates the width needed for the content.
     */
    private static int calculateContentWidth(Font font, List<Component> lines) {
        int maxWidth = 0;
        for (Component line : lines) {
            int width = font.width(line);
            maxWidth = Math.max(maxWidth, width);
        }
        return maxWidth;
    }

    /**
     * Calculates the height needed for the content.
     */
    private static int calculateContentHeight(Font font, List<Component> lines) {
        if (lines.size() <= 1) {
            return 0;
        }
        // Exclude the title line
        int lineCount = lines.size() - 1;
        return (lineCount * font.lineHeight) + ((lineCount - 1) * LINE_SPACING);
    }

    /**
     * Adjusts X position to keep tooltip on screen.
     */
    private static int adjustX(int x, int width, int screenWidth) {
        if (x + width > screenWidth) {
            return Math.max(4, x - width - 8);
        }
        return Math.max(4, x + 8);
    }

    /**
     * Adjusts Y position to keep tooltip on screen.
     */
    private static int adjustY(int y, int height, int screenHeight) {
        if (y + height > screenHeight) {
            return Math.max(4, screenHeight - height - 4);
        }
        return Math.max(4, y - 4);
    }
}