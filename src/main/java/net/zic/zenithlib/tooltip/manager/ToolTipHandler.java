package net.zic.zenithlib.tooltip.manager;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = "zenithlib")
public class ToolTipHandler {

    private static final Map<Item, AnimatedTooltip> ANIMATED_TOOLTIPS = new HashMap<>();

    // Existing method for string literals
    public static void registerAnimatedTooltip(Item item, String text, float speed) {
        ANIMATED_TOOLTIPS.put(item, new AnimatedTooltip(Component.literal(text), speed));
    }

    // New method for translatable components
    public static void registerAnimatedTooltip(Item item, Component text, float speed) {
        ANIMATED_TOOLTIPS.put(item, new AnimatedTooltip(text, speed));
    }

    private static class AnimatedTooltip {
        private final Component text;
        private final float speed;
        private final long startTime;

        public AnimatedTooltip(Component text, float speed) {
            this.text = text;
            this.speed = speed;
            this.startTime = System.currentTimeMillis();
        }

        public MutableComponent getComponent() {
            float time = ((System.currentTimeMillis() - startTime) * speed * 0.1f) % 1.0f;
            return ToolTipsGradient.RGBEachLetter(time, text.getString(), 0.01f);
        }
    }
    private static final Map<Item, List<Component>> ITEM_TOOLTIPS = new HashMap<>();

    /**
     * Register a tooltip for an item
     * @param item The item to add tooltip to
     * @param tooltipLines The tooltip lines to display
     */
    public static void registerTooltip(Item item, Component... tooltipLines) {
        ITEM_TOOLTIPS.put(item, List.of(tooltipLines));
    }

    /**
     * Register a tooltip for an item with formatting
     * @param item The item to add tooltip to
     * @param tooltipLines The tooltip lines as strings (will be formatted with GRAY)
     */
    public static void registerTooltip(Item item, String... tooltipLines) {
        List<Component> components = List.of(tooltipLines).stream()
                .map(line -> Component.translationArg(Component.literal(line).withStyle(ChatFormatting.GRAY)))
                .toList();
        ITEM_TOOLTIPS.put(item, components);
    }

    /**
     * Register advanced tooltip with custom components
     * @param item The item to add tooltip to
     * @param tooltipLines List of components to display
     */
    public static void registerAdvancedTooltip(Item item, List<Component> tooltipLines) {
        ITEM_TOOLTIPS.put(item, tooltipLines);
    }

    /**
     * Remove tooltip from an item
     * @param item The item to remove tooltip from
     */
    public static void removeTooltip(Item item) {
        ITEM_TOOLTIPS.remove(item);
    }

    /**
     * Check if an item has a registered tooltip
     * @param item The item to check
     * @return true if tooltip exists
     */
    public static boolean hasTooltip(Item item) {
        return ITEM_TOOLTIPS.containsKey(item);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();

        // Handle static tooltips
        if (ITEM_TOOLTIPS.containsKey(item)) {
            List<Component> tooltipLines = ITEM_TOOLTIPS.get(item);
            event.getToolTip().addAll(tooltipLines);
        }

        // Handle animated tooltips
        if (ANIMATED_TOOLTIPS.containsKey(item)) {
            AnimatedTooltip animated = ANIMATED_TOOLTIPS.get(item);
            event.getToolTip().add(animated.getComponent());
        }
    }
}
