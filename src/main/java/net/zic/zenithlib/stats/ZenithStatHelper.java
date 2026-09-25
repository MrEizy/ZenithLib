package net.zic.zenithlib.stats;

import net.minecraft.world.entity.LivingEntity;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;

import java.util.Collection;

public class ZenithStatHelper {
    public static ValueContainer<Double> statInstance(Stat stat,double baseValue){
        return ValueContainerHelpers.doubleValueContainer(ZenithRegistries.STAT_REGISTRY.getKey(stat),baseValue);
    }
    public static Stat stat(ValueContainer<Double> container){
        return ZenithRegistries.STAT_REGISTRY.getValue(container.getContainerId());
    }
    public static void updateStats(Collection<Stat> dirtyStats, LivingEntity targetEntity){
        if(targetEntity == null) return;
        targetEntity.getData(ZenithAttachments.STAT_HOLDER).updateStats(dirtyStats);

    }
}
