package net.zic.zenithlib.tooltip.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.zic.zenithlib.Config;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.input.InputHandler;
import net.zic.zenithlib.tooltip.api.ZenithTooltipData;
import net.zic.zenithlib.tooltip.api.ZenithTooltipDocument;
import net.zic.zenithlib.tooltip.api.ZenithTooltipProviders;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.manager.ZenithTooltipRepository;
import net.zic.zenithlib.tooltip.manager.ZenithTooltipResolver;
import net.zic.zenithlib.tooltip.manager.ZenithVanillaTooltipConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Client event integration layer for custom Zenith tooltips.
 *
 * <p>The subscriber registers the client-side component factory, intercepts tooltip
 * component gathering, and replaces normal tooltip output with one
 * {@link ZenithTooltipData} component. Contextual provider documents are checked
 * first, configured items obtain documents from {@link ZenithTooltipRepository},
 * and unmatched items are converted from their vanilla tooltip content by
 * {@link ZenithVanillaTooltipConverter}. It also selects a transparent vanilla
 * tooltip texture so only the Zenith renderer's themed frame is visible.</p>
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

        if (!Config.ENABLE_ZENITH_TOOLTIPS.get() || stack.isEmpty()) {
            return;
        }

        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<Either<FormattedText, TooltipComponent>> tooltipElements = event.getTooltipElements();

        Optional<RegistryAccess> registryAccess = Optional.empty();

        if (Minecraft.getInstance().level != null) {
            registryAccess = Optional.of(Minecraft.getInstance().level.registryAccess());
        }

        Optional<Player> player = Optional.ofNullable((Player) Minecraft.getInstance().player);
        ZenithTooltipContext baseContext = ZenithTooltipContext.of(stack, id, registryAccess, player);
        Optional<ZenithTooltipProviders.Result> provided = ZenithTooltipProviders.create(baseContext);

        ZenithTooltipContext resolutionContext = provided
                .map(ZenithTooltipProviders.Result::context)
                .orElse(baseContext);
        ZenithTooltipDocument document = provided
                .map(ZenithTooltipProviders.Result::document)
                .orElseGet(() -> ZenithTooltipRepository.get(stack, id));

        if (document == null) {
            List<FormattedText> vanillaLines = new ArrayList<>();

            for (Either<FormattedText, TooltipComponent> element : tooltipElements) {
                element.ifLeft(vanillaLines::add);
            }

            document = ZenithVanillaTooltipConverter.convert(stack, vanillaLines);
        }

        document = ZenithTooltipResolver.resolve(document, resolutionContext);
        tooltipElements.clear();
        tooltipElements.add(Either.right(new ZenithTooltipData(id, stack.copy(), document)));
    }

    @SubscribeEvent
    public static void changeTooltipTexture(RenderTooltipEvent.Texture event) {
        if (Config.ENABLE_ZENITH_TOOLTIPS.get() && !event.getItemStack().isEmpty()) {
            event.setTexture(TRANSPARENT_TOOLTIP_TEXTURE);
        }
    }

    /**
     * Handles keyboard page-navigation bindings while an inventory-like screen is open.
     * NeoForge recommends screen events for mappings used inside GUIs rather than
     * polling ordinary gameplay input ticks.
     */
    @SubscribeEvent
    public static void navigateTooltipPage(ScreenEvent.KeyPressed.Pre event) {
        if (!Config.ENABLE_ZENITH_TOOLTIPS.get()) {
            return;
        }

        InputConstants.Key key = InputConstants.getKey(event.getKeyEvent());

        if (InputHandler.TOOLTIP_PREVIOUS_PAGE.isActiveAndMatches(key)
                && ZenithTooltipLayout.previousPage()) {
            event.setCanceled(true);
        } else if (InputHandler.TOOLTIP_NEXT_PAGE.isActiveAndMatches(key)
                && ZenithTooltipLayout.nextPage()) {
            event.setCanceled(true);
        }
    }

    /**
     * Scrolls long tooltip bodies while preserving their fixed title and footer.
     * The event is consumed only while the recently rendered tooltip has overflowing
     * content, preventing accidental interaction with the underlying screen.
     */
    @SubscribeEvent
    public static void scrollTooltipPage(ScreenEvent.MouseScrolled.Pre event) {
        if (!Config.ENABLE_ZENITH_TOOLTIPS.get()) {
            return;
        }

        if (ZenithTooltipLayout.scrollBody(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    /**
     * Allows the Controls menu to remap tooltip page navigation to mouse buttons too.
     */
    @SubscribeEvent
    public static void navigateTooltipPage(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!Config.ENABLE_ZENITH_TOOLTIPS.get()) {
            return;
        }

        InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(event.getButton());

        if (InputHandler.TOOLTIP_PREVIOUS_PAGE.isActiveAndMatches(key)
                && ZenithTooltipLayout.previousPage()) {
            event.setCanceled(true);
        } else if (InputHandler.TOOLTIP_NEXT_PAGE.isActiveAndMatches(key)
                && ZenithTooltipLayout.nextPage()) {
            event.setCanceled(true);
        }
    }
}