package net.zic.zenithlib.tooltip.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.tooltip.api.ZenithTooltipData;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import org.jspecify.annotations.Nullable;

/**
 * Minecraft client tooltip component responsible for rendering one resolved Zenith
 * tooltip payload.
 *
 * <p>This class bridges {@link ZenithTooltipData} to the client tooltip component API.
 * It reports themed dimensions, prepares a {@link ZenithTooltipLayout.Layout} during
 * height measurement, and reuses that same draw-ready layout when the image pass is
 * extracted. The short-lived cache prevents text wrapping and element measurement from
 * being repeated during a single tooltip render cycle.</p>
 */

public final class ClientZenithTooltip implements net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent {
    private final Identifier itemId;
    private final ItemStack stack;
    private final ZenithTooltipDocument document;

    private @Nullable Font preparedFont;
    private ZenithTooltipLayout.@org.jspecify.annotations.Nullable Layout preparedLayout;

    public ClientZenithTooltip(ZenithTooltipData data) {
        this.itemId = data.itemId();
        this.stack = data.stack();
        this.document = data.document();
    }

    @Override
    public int getHeight(Font font) {
        preparedFont = font;
        preparedLayout = ZenithTooltipLayout.prepare(font, itemId, document);
        return preparedLayout.height();
    }

    @Override
    public int getWidth(Font font) {
        return document.theme().maxWidth();
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        ZenithTooltipLayout.Layout layout = preparedLayout != null && preparedFont == font
                ? preparedLayout
                : ZenithTooltipLayout.prepare(font, itemId, document);

        ZenithTooltipRenderer.render(font, graphics, x, y, stack, layout);

        preparedFont = null;
        preparedLayout = null;
    }
}
