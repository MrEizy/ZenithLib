package net.zic.zenithlib.stats;

import net.minecraft.world.entity.LivingEntity;
import net.zic.zenithlib.common.ZenithAttachments;

import java.util.Collection;

public class ZenithStatHelper {

    public static void updateStats(Collection<Stat> dirtyStats, LivingEntity targetEntity){
        if(targetEntity == null) return;
        targetEntity.getData(ZenithAttachments.STAT_HOLDER).updateStats(dirtyStats);

    }
}
