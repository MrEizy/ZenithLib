package net.zic.zenithlib.tooltip.manager;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class ToolTipManager {
    public static void registerAllTooltips() {
        // Example tooltip registrations
        /*ToolTipHandler.registerTooltip(
                ModItems.EXAMPLE_ITEM.get(),
                "This is a simple tooltip",
                "Second line of tooltip"
        );*/

        // Tooltip with custom formatting

        ToolTipHandler.registerTooltip(Items.DIAMOND, Component.literal("Test ToolTip").withStyle(ChatFormatting.DARK_RED));
        ToolTipHandler.registerAnimatedTooltip(Items.DIAMOND, Component.literal("Test RGB"), 0.001f);




        // Advanced example with conditional logic
        registerAdvancedTooltips();
    }

    private static void registerAdvancedTooltips() {
        // You can add more complex tooltip logic here
        //ToolTipHandler.registerTooltip(
        //ModItems.SPECIAL_ITEM.get(),
        //"Special Item Tooltip",
        //"Hold Shift for more info"
        //);
    }
}

