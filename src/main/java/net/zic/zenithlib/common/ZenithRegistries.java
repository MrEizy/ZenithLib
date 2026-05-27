package net.zic.zenithlib.common;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.registry.RegistryHelper;
import net.zic.zenithlib.stats.Stat;

@EventBusSubscriber(modid = ZenithLib.MOD_ID)
public class ZenithRegistries {

    public static final Registry<Stat> STAT_REGISTRY = RegistryHelper.registry(ZenithLib.MOD_ID,"stats");



    @SubscribeEvent // on the mod event bus
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(STAT_REGISTRY);

    }
}
