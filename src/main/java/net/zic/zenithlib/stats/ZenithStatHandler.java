package net.zic.zenithlib.stats;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.stats.event.StatsUpdatedEvent;

@EventBusSubscriber(modid = ZenithLib.MOD_ID)
public class ZenithStatHandler {

    @SubscribeEvent
    public static void onStatChange(StatsUpdatedEvent event){

        event.getEntity().getData(ZenithAttachments.STAT_HOLDER).updateStats(event.getModifiedStats());
        event.getEntity().getData(ZenithAttachments.ATTRIBUTE_HOLDER).update(event.getEntity().getData(ZenithAttachments.STAT_HOLDER));

        event.getEntity().syncData(ZenithAttachments.STAT_HOLDER);
        event.getEntity().syncData(ZenithAttachments.ATTRIBUTE_HOLDER);
    }

}
