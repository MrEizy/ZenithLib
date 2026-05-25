package net.zic.zenithlib;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.datagen.ZenithLibDataGenerators;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ZenithLib.MOD_ID)
public class ZenithLib {
    public static float hue;
    public static final String MOD_ID = "zenithlib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ZenithLib(IEventBus modEventBus, ModContainer modContainer) {


        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ZenithLibDataGenerators::gatherData);


        NeoForge.EVENT_BUS.register(this);

        ZenithAttachments.register(modEventBus);

        modEventBus.addListener(this::registerKeyBindings);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void registerKeyBindings(RegisterKeyMappingsEvent event) {

    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {

    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

        LOGGER.info("(!) Zenith Lib Connected (!)");
    }

}