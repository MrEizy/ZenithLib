package net.zic.zenithlib.stats;

import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StatSheet {

    private final HashMap<Stat,StatInstance> statInstances = new HashMap<>();


    //mainly used for syncing
    public void setStat(StatInstance instance){
        statInstances.put(instance.getStat(),instance);
    }

    public void addStat(Stat stat,double val){
        if(!statInstances.containsKey(stat)) statInstances.put(stat,stat.newInstance(val));
        else{
            statInstances.get(stat).setBaseValue(
                    statInstances.get(stat).getBaseValue()+val
            );
        }
    }
    public void addStat(StatInstance instance){
        if(!statInstances.containsKey(instance.getStat())) statInstances.put(instance.getStat(),instance);
        else{
            Collection<ValueContainerModifier> modifiers = instance.getAllModifiers();
            StatInstance current = statInstances.get(instance.getStat());

            current.setBaseValue(current.getBaseValue()+instance.getBaseValue());

            for(ValueContainerModifier modifier : modifiers) current.addModifier(modifier);
        }
    }
    //not recommended unless you know it wont go below 0 (you might want it dunno wont judge)
    public void removeStat(Stat stat, double val){
        addStat(stat,-val);
    }

    public Map<Stat,StatInstance> asMap(){
        return statInstances;
    }

    public StatInstance getStatInstance(Stat stat){
        return statInstances.get(stat);
    }
    public Collection<StatInstance> getAllInstances(){
        return statInstances.values();
    }
}
