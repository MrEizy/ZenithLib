package net.zic.zenithlib;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.zic.zenithlib.classification.ZenithClassifications;
import net.zic.zenithlib.tooltip.manager.ZenithTooltipRepository;


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
        event.addListener(
                Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "zenith_tooltip_themes"),
                new ZenithTooltipRepository.ThemesReloadListener()
        );

        event.addListener(
                Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "zenith_tooltips"),
                new ZenithTooltipRepository.RulesReloadListener()
        );

        event.addListener(
                Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "zenith_classification_categories"),
                new ZenithClassifications.CategoriesReloadListener()
        );

        event.addListener(
                Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "zenith_classification_ranks"),
                new ZenithClassifications.RanksReloadListener()
        );

        event.addListener(
                Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "zenith_classifications"),
                new ZenithClassifications.RulesReloadListener()
        );

        ZenithLib.LOGGER.info("Registered tooltip reload listeners (themes + rules + classifications)");
    }


}