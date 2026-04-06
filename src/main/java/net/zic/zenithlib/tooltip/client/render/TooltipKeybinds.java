package net.zic.zenithlib.tooltip.client.render;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.TooltipProvider;
import net.zic.zenithlib.tooltip.api.TooltipProviderRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class TooltipKeybinds {

    public static final KeyMapping.Category TOOLTIP_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "key.zenithlib.category.tooltip")) ;

    public static final KeyMapping NEXT_PAGE = new KeyMapping("key.zenithlib.tooltip.next_page", GLFW.GLFW_KEY_RIGHT, TOOLTIP_CATEGORY);
    public static final KeyMapping PREVIOUS_PAGE = new KeyMapping("key.zenithlib.tooltip.previous_page", GLFW.GLFW_KEY_LEFT, TOOLTIP_CATEGORY);

    /*NEXT_PAGE = new KeyMapping(

                "key.zenithlib.tooltip.next_page",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_RIGHT,
                TOOLTIP_CATEGORY
                );*/

    /**
     * Call this every client tick (e.g., from ClientTickEvent.Post) to handle page switching.
     */
    public static void handleInput() {
        Minecraft mc = Minecraft.getInstance();

        // Determine which item the player is currently hovering over
        ItemStack hoveredStack = getCurrentlyHoveredItem(mc);
        if (hoveredStack.isEmpty()) {
            // Fallback: when no screen is open, use the last stack from tooltip render (e.g., hand hover)
            hoveredStack = PageState.getCurrentStack();
        }

        if (hoveredStack.isEmpty()) return;

        // Only handle paging if this item actually has a paginated tooltip
        Optional<TooltipProvider> provider = TooltipProviderRegistry.find(hoveredStack);
        if (provider.isEmpty()) return;

        int totalPages = PageState.getPageCount(hoveredStack);
        if (totalPages <= 1) return;

        // Consume key presses and update page
        while (PREVIOUS_PAGE.consumeClick()) {
            // Ensure PageState knows which stack we're paging
            PageState.setCurrentStack(hoveredStack);
            PageState.previousPage();
        }
        while (NEXT_PAGE.consumeClick()) {
            PageState.setCurrentStack(hoveredStack);
            PageState.nextPage();
        }
    }

    /**
     * Returns the item currently under the mouse cursor in any open container screen,
     * or ItemStack.EMPTY if none.
     */
    private static ItemStack getCurrentlyHoveredItem(Minecraft mc) {
        Screen screen = mc.screen;
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            var slot = containerScreen.getSlotUnderMouse();
            if (slot != null && slot.hasItem()) {
                return slot.getItem();
            }
        }
        return ItemStack.EMPTY;
    }

    public static String getPreviousPageKeyName() {
        return PREVIOUS_PAGE.getTranslatedKeyMessage().getString();
    }

    public static String getNextPageKeyName() {
        return NEXT_PAGE.getTranslatedKeyMessage().getString();
    }
}