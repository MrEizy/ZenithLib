package net.zic.zenithlib.tooltip.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.zic.zenithlib.tooltip.api.TooltipProvider;
import net.zic.zenithlib.tooltip.api.TooltipProviderRegistry;
import net.zic.zenithlib.tooltip.client.render.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Mixin to intercept tooltip rendering and replace with modern tooltips.
 * Updated for NeoForge 26.1 with GuiGraphicsExtractor.
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class DrawContextMixin {

    @Unique
    private static ItemStack zenithlib$lastRealStack = ItemStack.EMPTY;

    @Unique
    private static Field zenithlib$cachedHoveredSlotField = null;

    @Unique
    private static boolean zenithlib$hoveredSlotFieldResolved = false;

    @Unique
    private static final Map<String, ItemStack> zenithlib$nameToStackCache = new HashMap<>();

    @Unique
    private static Map<String, ItemStack> zenithlib$itemNameLookup = null;

    /**
     * Intercepts the main tooltip rendering method.
     * In 26.1, this is setTooltipForNextFrame with ItemStack parameter.
     */
    @Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
            at = @At("HEAD"), cancellable = true)
    private void zenithlib$onRenderItemTooltip(Font font, ItemStack stack, int x, int y, CallbackInfo ci) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        zenithlib$lastRealStack = stack;

        // Check if modern tooltip rendering is enabled
        if (!TooltipNavigationConfig.tooltipRenderingEnabled()) {
            return;
        }

        // Check if we should use modern tooltip for this stack
        if (!TooltipRenderer.shouldUseModernTooltip(stack)) {
            return;
        }

        Optional<TooltipProvider> provider = TooltipProviderRegistry.find(stack);
        if (provider.isEmpty()) {
            return;
        }

        List<Component> lines = Screen.getTooltipFromItem(minecraft, stack);
        if (lines == null || lines.isEmpty()) {
            return;
        }

        // Cancel vanilla rendering and render our custom tooltip
        ci.cancel();

        // Render our modern tooltip
        TooltipRenderer.render(
                (GuiGraphicsExtractor) (Object) this,
                font,
                stack,
                lines,
                provider.get(),
                x,
                y,
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight()
        );
    }

    /**
     * Intercepts tooltip rendering from component list.
     * This handles cases where tooltips are rendered without an ItemStack.
     */
    @Inject(method = "setComponentTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V",
            at = @At("HEAD"), cancellable = true)
    private void zenithlib$onRenderComponentTooltip(Font font, List<Component> lines, int x, int y, CallbackInfo ci) {
        if (lines == null || lines.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        // Check if modern tooltip rendering is enabled
        if (!TooltipNavigationConfig.tooltipRenderingEnabled()) {
            return;
        }

        // Try to find the real item stack from various sources
        ItemStack resolved = zenithlib$resolveStackFromLines(minecraft, lines);

        if (resolved.isEmpty()) {
            return;
        }

        // Check if we should use modern tooltip for this stack
        if (!TooltipRenderer.shouldUseModernTooltip(resolved)) {
            return;
        }

        Optional<TooltipProvider> provider = TooltipProviderRegistry.find(resolved);
        if (provider.isEmpty()) {
            return;
        }

        // Cancel vanilla rendering
        ci.cancel();

        // Render our modern tooltip
        TooltipRenderer.render(
                (GuiGraphicsExtractor) (Object) this,
                font,
                resolved,
                lines,
                provider.get(),
                x,
                y,
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight()
        );
    }

    /**
     * Resolves an ItemStack from tooltip lines by checking various sources.
     */
    @Unique
    private static ItemStack zenithlib$resolveStackFromLines(Minecraft minecraft, List<Component> lines) {
        if (lines.isEmpty()) {
            return ItemStack.EMPTY;
        }

        String title = lines.get(0).getString();

        // First check the last real stack we saw
        if (!zenithlib$lastRealStack.isEmpty()) {
            String lastStackName = zenithlib$lastRealStack.getHoverName().getString();
            if (lastStackName.equals(title)) {
                return zenithlib$lastRealStack;
            }
        }

        // Check hovered slot in container screens
        if (minecraft.screen instanceof AbstractContainerScreen<?> containerScreen) {
            try {
                Slot hoveredSlot = zenithlib$getHoveredSlot(containerScreen);
                if (hoveredSlot != null && hoveredSlot.hasItem()) {
                    ItemStack slotStack = hoveredSlot.getItem();
                    if (slotStack.getHoverName().getString().equals(title)) {
                        zenithlib$lastRealStack = slotStack;
                        return slotStack;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Check cursor/held item
        if (minecraft.player != null) {
            ItemStack cursorStack = minecraft.player.containerMenu.getCarried();
            if (!cursorStack.isEmpty() && cursorStack.getHoverName().getString().equals(title)) {
                zenithlib$lastRealStack = cursorStack;
                return cursorStack;
            }
        }

        // Check merchant offers
        if (minecraft.screen instanceof MerchantScreen merchantScreen) {
            for (MerchantOffer offer : merchantScreen.getMenu().getOffers()) {
                ItemStack result = offer.getResult();
                if (!result.isEmpty() && result.getHoverName().getString().equals(title)) {
                    zenithlib$lastRealStack = result;
                    return result;
                }
            }
        }

        // Try to find from cache or build lookup
        return zenithlib$findFromCacheOrLookup(title);
    }

    /**
     * Gets the hovered slot from a container screen using reflection.
     */
    @Unique
    private static Slot zenithlib$getHoveredSlot(AbstractContainerScreen<?> screen) throws Exception {
        if (!zenithlib$hoveredSlotFieldResolved) {
            for (Field field : AbstractContainerScreen.class.getDeclaredFields()) {
                if (field.getType().equals(Slot.class)) {
                    field.setAccessible(true);
                    zenithlib$cachedHoveredSlotField = field;
                    break;
                }
            }
            zenithlib$hoveredSlotFieldResolved = true;
        }

        if (zenithlib$cachedHoveredSlotField != null) {
            return (Slot) zenithlib$cachedHoveredSlotField.get(screen);
        }

        return null;
    }

    /**
     * Finds an item stack from cache or by building a name lookup map.
     */
    @Unique
    private static ItemStack zenithlib$findFromCacheOrLookup(String name) {
        // Check cache first
        if (zenithlib$nameToStackCache.containsKey(name)) {
            ItemStack cached = zenithlib$nameToStackCache.get(name);
            return cached != null ? cached : ItemStack.EMPTY;
        }

        // Build lookup map lazily
        if (zenithlib$itemNameLookup == null) {
            Map<String, ItemStack> lookup = new HashMap<>();
            for (Item item : BuiltInRegistries.ITEM) {
                ItemStack candidate = new ItemStack(item);
                lookup.put(candidate.getHoverName().getString(), candidate);
            }
            zenithlib$itemNameLookup = lookup;
        }

        ItemStack found = zenithlib$itemNameLookup.getOrDefault(name, ItemStack.EMPTY);
        zenithlib$nameToStackCache.put(name, found.isEmpty() ? null : found);

        if (!found.isEmpty()) {
            zenithlib$lastRealStack = found;
        }

        return found;
    }
}