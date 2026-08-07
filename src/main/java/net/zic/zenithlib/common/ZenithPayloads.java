package net.zic.zenithlib.common;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.input.action.ActionChangedPacket;

@EventBusSubscriber(modid = ZenithLib.MOD_ID)
public class ZenithPayloads {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ActionChangedPacket.TYPE,
                ActionChangedPacket.STREAM_CODEC,
                ActionChangedPacket::handlePayload
        );
    }
}
