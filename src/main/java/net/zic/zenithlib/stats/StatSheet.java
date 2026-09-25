package net.zic.zenithlib.stats;

import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StatSheet {

    private final HashMap<Stat, ValueContainer<Double>> statInstances = new HashMap<>();


    public void setStat(ValueContainer<Double>  instance){
        statInstances.put(ZenithStatHelper.stat(instance),instance);
    }
    public void removeStat(Stat stat){
        statInstances.remove(stat);
    }

    public Map<Stat,ValueContainer<Double>> asMap(){
        return statInstances;
    }

    public ValueContainer<Double> getStatInstance(Stat stat){
        return statInstances.get(stat);
    }
    public Collection<Stat> getAllStats() {return statInstances.keySet();}
    public Collection<ValueContainer<Double>> getAllInstances(){
        return statInstances.values();
    }
}
