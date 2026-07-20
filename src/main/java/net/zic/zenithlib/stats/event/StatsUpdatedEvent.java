package net.zic.zenithlib.stats.event;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatInstance;
import net.zic.zenithlib.stats.StatSheet;


import java.util.Collection;
import java.util.Map;

public class StatsUpdatedEvent extends Event {
    private final LivingEntity entity;
    private final Collection<Stat> dirtyStats;

    public StatsUpdatedEvent(LivingEntity entity, Collection<Stat> dirtyStats) {
        this.entity = entity;
        this.dirtyStats = dirtyStats;
    }

    public LivingEntity getEntity(){
        return entity;
    }
    public Collection<Stat> getModifiedStats(){
        return dirtyStats;
    }
}
