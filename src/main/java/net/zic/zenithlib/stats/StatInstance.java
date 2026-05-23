package net.zic.zenithlib.stats;

import net.zic.zenithlib.registry.RegistryHelper;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.value_containers.ValueContainer;

public class StatInstance extends ValueContainer {

    public StatInstance(Stat stat, double base) {
        super(ZenithRegistries.STAT_REGISTRY.getKey(stat), base);
    }

    public Stat getStat(){
        return RegistryHelper.getRegistryObject(ZenithRegistries.STAT_REGISTRY,getIdentifier());
    }
}
