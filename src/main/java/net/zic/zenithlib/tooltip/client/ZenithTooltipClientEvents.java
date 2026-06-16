package net.zic.zenithlib.tooltip.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

import org.jspecify.annotations.Nullable;

/**
 * Client event integration layer for custom Zenith tooltips.
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
        event.register(ZenithTooltipData.class, TooltipComponentView::new);
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
        ZenithTooltipContext baseContext = ZenithTooltipContext.of(stack, id, registryAccess, player)
                .withData(Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "shift_down"), InputHandler.isShiftDown())
                .withData(Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "ctrl_down"), InputHandler.isControlDown())
                .withData(Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "alt_down"), InputHandler.isAltDown());
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

    @SubscribeEvent
    public static void scrollTooltipPage(ScreenEvent.MouseScrolled.Pre event) {
        if (!Config.ENABLE_ZENITH_TOOLTIPS.get()) {
            return;
        }

        if (ZenithTooltipLayout.scrollBody(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }


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

    private static final class TooltipComponentView implements net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent {
        private final Identifier itemId;
        private final ItemStack stack;
        private final ZenithTooltipDocument document;

        private @Nullable Font preparedFont;
        private ZenithTooltipLayout.@Nullable Layout preparedLayout;

        private TooltipComponentView(ZenithTooltipData data) {
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
            ZenithTooltipRenderer.render(font, graphics, x, y, stack, layout(font));
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

}
