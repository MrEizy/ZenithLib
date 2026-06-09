package net.zic.zenithlib.cooldown;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.common.ZenithAttachments;

@EventBusSubscriber(modid = ZenithLib.MOD_ID)
public class Listener {

    @SubscribeEvent
    public static void onServerTick(EntityTickEvent.Pre pre){
        if(!(pre.getEntity() instanceof LivingEntity entity)) return;

        if(!entity.hasData(ZenithAttachments.COOLDOWN_HANDLER)) return;

        entity.getData(ZenithAttachments.COOLDOWN_HANDLER).tick();
    }

}
