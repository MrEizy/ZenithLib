package net.zic.zenithlib.stats;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.Collection;

/**
 * describes a class that can provide stats,
 * will be registered to one or more AttributeHolders
 * this interface only describes immutable actions
 */
public interface StatProvider {


    Collection<Stat> getStats();

    ValueContainer<Double> getStatInstance(Stat stat);

    double getStat(Stat stat);
    double getBaseStat(Stat stat);
}
