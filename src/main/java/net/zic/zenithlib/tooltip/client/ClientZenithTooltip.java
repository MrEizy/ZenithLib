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
 * Width and height both reuse the same prepared {@link ZenithTooltipLayout.Layout}, so
 * content-aware sizing always matches the final render pass while avoiding repeated
 * wrapping work during a single tooltip render cycle.</p>
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
        return layout(font).height();
    }

    @Override
    public int getWidth(Font font) {
        return layout(font).width();
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        ZenithTooltipLayout.Layout layout = layout(font);
        ZenithTooltipRenderer.render(font, graphics, x, y, stack, layout);

        preparedFont = null;
        preparedLayout = null;
    }

    private ZenithTooltipLayout.Layout layout(Font font) {
        if (preparedLayout == null || preparedFont != font) {
            preparedFont = font;
            preparedLayout = ZenithTooltipLayout.prepare(font, itemId, stack, document);
        }

        return preparedLayout;
    }
}
