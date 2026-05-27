package net.zic.zenithlib.stats.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatInstance;
import net.zic.zenithlib.stats.StatSheet;

import java.util.Map;

public class StatsUpdatedEvent extends Event {
    private final LivingEntity entity;
    private final Map<Stat, StatInstance>  statSheet;

    public StatsUpdatedEvent(LivingEntity entity, StatSheet statSheet) {
        this.entity = entity;
        this.statSheet = Map.copyOf(statSheet.asMap()); //ensures it is not modified during the event
    }

    public LivingEntity getEntity(){
        return entity;
    }
    public Map<Stat, StatInstance> getStatSheet(){
        return statSheet;
    }
}
