package net.zic.zenithlib.tooltip.client.render;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.zic.zenithlib.ZenithLib;

/**
 * Handles keybind registration and input for tooltip page navigation.
 * Note: This class is NOT annotated with @EventBusSubscriber because
 * key registration is handled manually via ZenithLibClient.
 */
public class TooltipKeybinds {

    /** Custom category name (used in controls menu) */
    public static final String CATEGORY = "key.categories.zenithlib.tooltip";

    /** The actual Category object (required for 26.1) */
    public static final KeyMapping.Category TOOLTIP_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "tooltip"));

    // Keybind for next page
    public static KeyMapping NEXT_PAGE;

    // Keybind for previous page
    public static KeyMapping PREVIOUS_PAGE;

    /**
     * Registers the custom category + key mappings.
     * Called manually from ZenithLibClient.registerKeyMappings()
     */
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // Create the category object
        KeyMapping.Category tooltipCategory = new KeyMapping.Category(
                Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "tooltip")
        );

        // Now create the key mappings using the Category object
        NEXT_PAGE = new KeyMapping(
                "key.zenithlib.tooltip.next_page",
                KeyConflictContext.GUI,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_RIGHT,
                tooltipCategory
        );

        PREVIOUS_PAGE = new KeyMapping(
                "key.zenithlib.tooltip.previous_page",
                KeyConflictContext.GUI,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_LEFT,
                tooltipCategory
        );

        event.register(NEXT_PAGE);
        event.register(PREVIOUS_PAGE);

        ZenithLib.LOGGER.info("Registered ZenithLib tooltip keybinds");
    }

    /**
     * Client tick event handler for checking key presses.
     * This is registered separately in ZenithLibClient.
     */
    public static class ClientInputHandler {

        private static boolean wasNextPagePressed = false;
        private static boolean wasPrevPagePressed = false;

        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();

            // Only process when a screen is open (tooltip is visible)
            if (minecraft.screen == null) {
                return;
            }

            // Check if we have a valid stack with multiple pages
            if (PageState.getCurrentStack().isEmpty()) {
                return;
            }

            if (!PageState.hasMultiplePages(PageState.getCurrentStack())) {
                return;
            }

            // Handle next page key
            boolean isNextPagePressed = NEXT_PAGE.isDown();
            if (isNextPagePressed && !wasNextPagePressed) {
                PageState.nextPage();
            }
            wasNextPagePressed = isNextPagePressed;

            // Handle previous page key
            boolean isPrevPagePressed = PREVIOUS_PAGE.isDown();
            if (isPrevPagePressed && !wasPrevPagePressed) {
                PageState.previousPage();
            }
            wasPrevPagePressed = isPrevPagePressed;
        }
    }

    /**
     * Returns the display name of the next page key.
     */
    public static String getNextPageKeyName() {
        if (NEXT_PAGE == null) {
            return "->";
        }
        return NEXT_PAGE.getTranslatedKeyMessage().getString();
    }

    /**
     * Returns the display name of the previous page key.
     */
    public static String getPreviousPageKeyName() {
        if (PREVIOUS_PAGE == null) {
            return "<-";
        }
        return PREVIOUS_PAGE.getTranslatedKeyMessage().getString();
    }
}