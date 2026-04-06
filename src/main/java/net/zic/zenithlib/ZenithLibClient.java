package net.zic.zenithlib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.zenithlib.tooltip.api.ThemeDefinition;
import net.zic.zenithlib.tooltip.client.render.*;
import net.zic.zenithlib.tooltip.api.TooltipProvider;
import net.zic.zenithlib.tooltip.api.TooltipProviderRegistry;

import java.util.List;
import java.util.Optional;

/**
 * Client-side initialization for ZenithLib.
 * This class will not load on dedicated servers.
 * Updated for NeoForge 26.1
 */
@Mod(value = ZenithLib.MOD_ID, dist = Dist.CLIENT)
public class ZenithLibClient {

    public ZenithLibClient(ModContainer container, IEventBus modEventBus) {
        // Config stuff
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.registerConfig(ModConfig.Type.CLIENT, TooltipNavigationConfig.SPEC);

        // Register event listeners on the MOD event bus
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerReloadListeners);
        modEventBus.addListener(this::registerKeyMappings);

        // Register to NeoForge event bus for game events (not mod lifecycle events)
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(TooltipKeybinds.ClientInputHandler::onClientTick);
    }

    /**
     * Client setup - runs on mod event bus
     */
    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ZenithLib.LOGGER.info("(!) Zenith Lib Client Initialized (!)");

            // Register the generic tooltip provider
            TooltipProviderRegistry.register(GenericTooltipProvider.INSTANCE);
        });
    }

    /**
     * Register reload listeners - runs on mod event bus
     */
    private void registerReloadListeners(AddClientReloadListenersEvent event) {
        // Unique keys for each listener - this prevents the "already registered" error
        Identifier themesKey = Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "tooltip_themes");
        Identifier itemThemesKey = Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "item_themes");

        // Only register if not already present (safe for multiple event firings)
        if (!event.getRegistry().containsKey(themesKey)) {
            event.addListener(themesKey, new ThemeRegistry());
        }

        if (!event.getRegistry().containsKey(itemThemesKey)) {
            event.addListener(itemThemesKey, new ItemThemeRegistry());
        }

        ZenithLib.LOGGER.info("Registered tooltip reload listeners (themes + item themes)");
    }

    /**
     * Register key mappings - runs on mod event bus
     * NOTE: This is NOT annotated with @SubscribeEvent because we're using addListener
     */
    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // Register tooltip navigation keybinds
        TooltipKeybinds.registerKeyMappings(event);
    }

    /**
     * Event-based tooltip rendering - runs on NeoForge event bus
     * This catches tooltips that might not be intercepted by the mixin.
     */
    @SubscribeEvent
    public void onRenderTooltip(RenderTooltipEvent.Pre event) {
        // Check if modern tooltip rendering is enabled
        if (!TooltipNavigationConfig.tooltipRenderingEnabled()) {
            return;
        }

        // Get the item stack from the event
        var stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) {
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

        // Cancel the vanilla tooltip and render our custom one
        event.setCanceled(true);

        // Update page state
        PageState.setCurrentStack(stack);

        // Get the theme
        String themeKey = provider.get().getThemeKey(stack).orElse(null);
        ThemeDefinition theme;
        if (themeKey != null && ThemeRegistry.has(themeKey)) {
            theme = ThemeRegistry.get(themeKey);
        } else if (ItemThemeRegistry.hasThemeForStack(stack)) {
            theme = ThemeRegistry.get(ItemThemeRegistry.getThemeKey(stack));
        } else {
            theme = ThemeRegistry.getDefault();
        }

        if (!theme.enabled()) {
            return;
        }

        // Get tooltip lines directly from the item stack (like in the mixin)
        Minecraft minecraft = Minecraft.getInstance();
        List<Component> components = Screen.getTooltipFromItem(minecraft, stack);

        if (components == null || components.isEmpty()) {
            return;
        }

        // Build pages
        var sections = provider.get().getSections(stack);
        var pages = buildPages(components, sections, theme.maxLinesPerPage());

        int totalPages = Math.max(1, pages.size());
        PageState.setPageCount(stack, totalPages);

        int currentPageIndex = PageState.getCurrentPage(stack);
        currentPageIndex = Math.min(currentPageIndex, totalPages - 1);

        var pageLines = pages.isEmpty() ? components : pages.get(currentPageIndex).lines();

        // Render using our custom painter
        TooltipPainter.paint(
                event.getGraphics(),
                stack,
                pageLines,
                theme,
                event.getX(),
                event.getY(),
                event.getScreenWidth(),
                event.getScreenHeight(),
                currentPageIndex,
                totalPages
        );
    }

    /**
     * Builds pages from tooltip lines and provider sections.
     */
    private java.util.List<Page> buildPages(java.util.List<net.minecraft.network.chat.Component> originalLines,
                                            java.util.List<TooltipProvider.Section> sections,
                                            int maxLinesPerPage) {
        java.util.List<Page> pages = new java.util.ArrayList<>();
        java.util.List<net.minecraft.network.chat.Component> currentPageLines = new java.util.ArrayList<>();

        // Add title to first page
        if (!originalLines.isEmpty()) {
            currentPageLines.add(originalLines.get(0));
        }

        int lineCount = currentPageLines.size();

        // Add original tooltip lines (excluding title)
        for (int i = 1; i < originalLines.size(); i++) {
            var line = originalLines.get(i);

            if (lineCount >= maxLinesPerPage) {
                pages.add(new Page(new java.util.ArrayList<>(currentPageLines)));
                currentPageLines.clear();
                lineCount = 0;
            }

            currentPageLines.add(line);
            lineCount++;
        }

        // Add provider sections
        for (TooltipProvider.Section section : sections) {
            if (section.newPage() && !currentPageLines.isEmpty()) {
                pages.add(new Page(new java.util.ArrayList<>(currentPageLines)));
                currentPageLines.clear();
                lineCount = 0;
            }

            if (section.header() != null && !section.header().isEmpty()) {
                if (lineCount >= maxLinesPerPage) {
                    pages.add(new Page(new java.util.ArrayList<>(currentPageLines)));
                    currentPageLines.clear();
                    lineCount = 0;
                }
                currentPageLines.add(net.minecraft.network.chat.Component.literal("§6" + section.header()));
                lineCount++;
            }

            for (String lineText : section.lines()) {
                if (lineCount >= maxLinesPerPage) {
                    pages.add(new Page(new java.util.ArrayList<>(currentPageLines)));
                    currentPageLines.clear();
                    lineCount = 0;
                }
                currentPageLines.add(net.minecraft.network.chat.Component.literal("  " + lineText));
                lineCount++;
            }
        }

        if (!currentPageLines.isEmpty()) {
            pages.add(new Page(currentPageLines));
        }

        return pages;
    }

    private record Page(java.util.List<net.minecraft.network.chat.Component> lines) {}
}