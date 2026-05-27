package net.zic.zenithlib.custom_attributes;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.stats.event.StatsUpdatedEvent;

@EventBusSubscriber(modid = ZenithLib.MOD_ID)
public class ZenithAttributeHandler {


    @SubscribeEvent
    public static void onStatChange(StatsUpdatedEvent event){
        event.getEntity().getData(ZenithAttachments.ATTRIBUTE_HOLDER).update(event.getStatSheet());

    }
}
