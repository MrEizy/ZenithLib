package net.zic.zenithlib;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;



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

        // Register event listeners on the MOD event bus
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerReloadListeners);

        // Register to NeoForge event bus for game events (not mod lifecycle events)
        NeoForge.EVENT_BUS.register(this);
    }

    /**
     * Client setup - runs on mod event bus
     */
    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ZenithLib.LOGGER.info("(!) Zenith Lib Client Initialized (!)");


        });
    }


    /**
     * Register reload listeners - runs on mod event bus
     */
    private void registerReloadListeners(AddClientReloadListenersEvent event) {


        ZenithLib.LOGGER.info("Registered tooltip reload listeners (themes + item themes)");
    }

    /**
     * Event-based tooltip rendering - runs on NeoForge event bus
     * This catches tooltips that might not be intercepted by the mixin.
     */
    @SubscribeEvent
    public void onRenderTooltip(RenderTooltipEvent.Pre event) {

    }


}