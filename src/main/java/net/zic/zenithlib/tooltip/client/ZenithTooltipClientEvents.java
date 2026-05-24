package net.zic.zenithlib.tooltip.client;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.ZenithTooltipData;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.manager.ZenithTooltipRepository;
import net.zic.zenithlib.tooltip.manager.ZenithVanillaTooltipConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Client event integration layer for custom Zenith tooltips.
 *
 * <p>The subscriber registers the client-side component factory, intercepts tooltip
 * component gathering, and replaces normal tooltip output with one
 * {@link ZenithTooltipData} component. Configured items obtain documents from
 * {@link ZenithTooltipRepository}; unmatched items are converted from their vanilla
 * tooltip content by {@link ZenithVanillaTooltipConverter}. It also selects a
 * transparent vanilla tooltip texture so only the Zenith renderer's themed frame is
 * visible.</p>
 *
 * <p>This class owns NeoForge event wiring only; resource loading and visual rendering
 * are delegated to dedicated collaborators.</p>
 */

@EventBusSubscriber(
        modid = ZenithLib.MOD_ID,
        value = Dist.CLIENT
)
public final class ZenithTooltipClientEvents {
    private static final Identifier TRANSPARENT_TOOLTIP_TEXTURE =
            Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "transparent");

    private ZenithTooltipClientEvents() {}

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ZenithTooltipData.class, ClientZenithTooltip::new);
    }

    @SubscribeEvent
    public static void gatherTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();

        if (stack.isEmpty()) {
            return;
        }

        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<Either<FormattedText, TooltipComponent>> tooltipElements = event.getTooltipElements();
        ZenithTooltipDocument document = ZenithTooltipRepository.get(stack, id);

        if (document == null) {
            List<FormattedText> vanillaLines = new ArrayList<>();

            for (Either<FormattedText, TooltipComponent> element : tooltipElements) {
                element.ifLeft(vanillaLines::add);
            }

            document = ZenithVanillaTooltipConverter.convert(stack, vanillaLines);
        }

        tooltipElements.clear();

        tooltipElements.add(Either.right(new ZenithTooltipData(id, stack.copy(), document)));
    }

    @SubscribeEvent
    public static void changeTooltipTexture(RenderTooltipEvent.Texture event) {
        if (!event.getItemStack().isEmpty()) {
            event.setTexture(TRANSPARENT_TOOLTIP_TEXTURE);
        }
    }
}
