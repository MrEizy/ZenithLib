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

import java.util.List;

/**
 * Paints the complete tooltip with all its components.
 */
public class TooltipPainter {

    private static final int ITEM_SIZE = 16;
    private static final int BADGE_SIZE = 24;
    private static final int PADDING_X = 12;
    private static final int PADDING_Y = 10;
    private static final int LINE_SPACING = 2;
    private static final int HEADER_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 16;
    private static final int BUTTON_SIZE = 12;

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

        int titleWidth = font.width(lines.get(0));
        int titleOffset = theme.showDiamondBadge() ? BADGE_SIZE + 12 : 0;
        int requiredWidth = Math.max(contentWidth, titleWidth + titleOffset);

        int tooltipWidth = requiredWidth + (PADDING_X * 2);
        int headerHeight = theme.showDiamondBadge() ? Math.max(HEADER_HEIGHT, BADGE_SIZE + 16) : HEADER_HEIGHT;
        int tooltipHeight = headerHeight + contentHeight + (PADDING_Y * 2);

        if (totalPages > 1) {
            tooltipHeight += FOOTER_HEIGHT + 4;
        }

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

        // Draw title
        int titleX = tooltipX + PADDING_X + (theme.showDiamondBadge() ? BADGE_SIZE + 12 : 0);
        int titleY = tooltipY + 10;
        drawTitle(graphics, lines.get(0), titleX, titleY, colors);

        // Draw content
        int contentX = tooltipX + PADDING_X;
        int contentY = tooltipY + headerHeight;

        for (int i = 1; i < lines.size(); i++) {
            Component line = lines.get(i);
            drawLine(graphics, line, contentX, contentY, colors);
            contentY += font.lineHeight + LINE_SPACING;
        }

        // Draw page indicators and keybind buttons
        if (totalPages > 1) {
            int footerY = tooltipY + tooltipHeight - FOOTER_HEIGHT - 4;

            if (theme.showPageIndicator()) {
                BorderRenderer.drawPageIndicators(graphics, tooltipX + tooltipWidth / 2, footerY,
                        currentPage, totalPages, colors);
            }

            int buttonY = footerY + 8;
            drawKeybindButtons(graphics, tooltipX + tooltipWidth / 2, buttonY, colors);
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

        // Render the item icon - FIXED: Use proper item rendering
        renderItemIcon(graphics, stack, x + 4, y + 4, theme);
    }

    /**
     * Renders item icon with animation.
     */
    private static void renderItemIcon(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y,
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
                break;
        }

        // Use the graphics extractor's built-in item rendering
        Matrix3x2fStack matrices = graphics.pose();
        matrices.pushMatrix();

        try {
            // Center position with offset
            float centerX = x + ITEM_SIZE / 2.0f;
            float centerY = y + ITEM_SIZE / 2.0f + offsetY;

            matrices.translate(centerX, centerY);
            matrices.scale(scale, scale);

            if (rotation != 0.0f) {
                matrices.rotate((float) Math.toRadians(rotation));
            }

            matrices.translate(-ITEM_SIZE / 2.0f, -ITEM_SIZE / 2.0f);

            // Render item using the extractor's item method
            graphics.item(stack, 0, 0);

        } finally {
            matrices.popMatrix();
        }
    }

    // ... rest of methods (drawKeybindButtons, drawKeyButton, getKeySymbol, etc.) same as before

    private static void drawKeybindButtons(GuiGraphicsExtractor graphics, int centerX, int y,
                                           TooltipTheme colors) {
        String prevKey = TooltipKeybinds.getPreviousPageKeyName();
        String nextKey = TooltipKeybinds.getNextPageKeyName();

        int spacing = 4;

        int prevX = centerX - spacing - BUTTON_SIZE;
        int nextX = centerX + spacing;

        drawKeyButton(graphics, prevX, y, BUTTON_SIZE, BUTTON_SIZE, prevKey, colors);
        drawKeyButton(graphics, nextX, y, BUTTON_SIZE, BUTTON_SIZE, nextKey, colors);
    }

    private static void drawKeyButton(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                      String keyName, TooltipTheme colors) {
        Font font = Minecraft.getInstance().font;

        int bgColor = colors.bgBottom() | 0xFF000000;
        int borderColor = colors.border();

        graphics.fill(x, y, x + width, y + height, bgColor);
        graphics.fill(x, y, x + width, y + 1, borderColor);
        graphics.fill(x, y + height - 1, x + width, y + height, borderColor);
        graphics.fill(x, y, x + 1, y + height, borderColor);
        graphics.fill(x + width - 1, y, x + width, y + height, borderColor);

        String symbol = getKeySymbol(keyName);
        int symbolWidth = font.width(symbol);
        int symbolX = x + (width - symbolWidth) / 2;
        int symbolY = y + (height - font.lineHeight) / 2 + 1;

        graphics.text(font, Component.literal(symbol), symbolX, symbolY, colors.name(), false);
    }

    private static String getKeySymbol(String keyName) {
        String lower = keyName.toLowerCase();

        if (lower.contains("left")) return "←";
        if (lower.contains("right")) return "→";
        if (lower.contains("up")) return "↑";
        if (lower.contains("down")) return "↓";
        if (lower.contains("space")) return "␣";
        if (lower.contains("enter") || lower.contains("return")) return "↵";
        if (lower.contains("tab")) return "⇥";
        if (lower.contains("shift")) return "⇧";
        if (lower.contains("ctrl") || lower.contains("control")) return "⌃";
        if (lower.contains("alt")) return "⌥";
        if (lower.contains("esc") || lower.contains("escape")) return "⎋";
        if (lower.contains("backspace")) return "⌫";
        if (lower.contains("delete") || lower.contains("del")) return "⌦";
        if (lower.contains("page up") || lower.contains("pgup")) return "⇞";
        if (lower.contains("page down") || lower.contains("pgdn")) return "⇟";
        if (lower.contains("home")) return "⇱";
        if (lower.contains("end")) return "⇲";

        if (lower.matches("f\\d+")) return lower.toUpperCase();
        if (keyName.length() == 1) return keyName.toUpperCase();
        if (keyName.length() > 3) return keyName.substring(0, 2).toUpperCase();

        return keyName.toUpperCase();
    }

    private static void drawTitle(GuiGraphicsExtractor graphics, Component title, int x, int y,
                                  TooltipTheme colors) {
        Font font = Minecraft.getInstance().font;
        graphics.text(font, title, x, y, colors.name(), true);
    }

    private static void drawLine(GuiGraphicsExtractor graphics, Component line, int x, int y,
                                 TooltipTheme colors) {
        Font font = Minecraft.getInstance().font;

        String text = line.getString();
        int color = colors.body();

        // Check for formatting
        if (text.startsWith("§6")) {
            color = colors.sectionHeader();
        } else if (text.startsWith("§")) {
            // Keep existing color code
        }

        graphics.text(font, line, x, y, color, false);
    }

    private static int calculateContentWidth(Font font, List<Component> lines) {
        int maxWidth = 0;
        for (Component line : lines) {
            maxWidth = Math.max(maxWidth, font.width(line));
        }
        return maxWidth;
    }

    private static int calculateContentHeight(Font font, List<Component> lines) {
        if (lines.size() <= 1) return 0;
        int lineCount = lines.size() - 1;
        return (lineCount * font.lineHeight) + ((lineCount - 1) * LINE_SPACING);
    }

    private static int adjustX(int x, int width, int screenWidth) {
        if (x + width > screenWidth) {
            return Math.max(4, x - width - 8);
        }
        return Math.max(4, x + 8);
    }

    private static int adjustY(int y, int height, int screenHeight) {
        if (y + height > screenHeight) {
            return Math.max(4, screenHeight - height - 4);
        }
        return Math.max(4, y - 4);
    }
}