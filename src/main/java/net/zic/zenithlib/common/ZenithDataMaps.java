package net.zic.zenithlib.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.configuration.attribute_scaling.LivingEntityScaling;
import net.zic.zenithlib.registry.RegistryHelper;

import java.util.List;
@EventBusSubscriber(modid = ZenithLib.MOD_ID)
public class ZenithDataMaps {
    public static final DataMapType<EntityType<?>, LivingEntityScaling> ATTRIBUTE_SCALING_CONFIG = DataMapType.builder(
            Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "attribute_scaling"),
            Registries.ENTITY_TYPE,
            LivingEntityScaling.CODEC
    ).build();

    @SubscribeEvent
    public static void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(ATTRIBUTE_SCALING_CONFIG);

    }
}
