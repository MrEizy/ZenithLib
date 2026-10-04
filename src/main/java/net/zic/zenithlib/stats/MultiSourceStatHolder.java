package net.zic.zenithlib.stats;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.util.Processable;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;

import java.util.*;

/**
 * A Stat Holder that holds its own internal stat sheet that can be modified
 * And a combined stat sheet using its internal sheet and the sheet of multiple Stat Providers
 */
public class MultiSourceStatHolder extends Processable implements StatProvider{
    private final HashSet<StatProvider> providers = new HashSet<>();

    private final Set<Stat> dirtyInternalStats = new HashSet<>();

    private final StatSheet internalStatSheet = new StatSheet();
    private StatSheet cachedStatSheet = new StatSheet();

    //used to differentiate internal sheet being modified by a process vs cached sheet being modified
    boolean cachedSheetDirty = false;

    public MultiSourceStatHolder(){
        setOnResolved(this::resolve);
    }
    public void resolve(){
        if(cachedSheetDirty){
            cachedSheetUpdated();
            cachedSheetDirty = false;
        }else{
            updateStats(dirtyInternalStats);
            dirtyInternalStats.clear();
        }
    }

    //====================== INTERNAL SHEET ======================
    //not safe to use on client, will cause a desync
    public void addFlatModifier(Stat stat, Modifier<Double> modifier){
        internalStatSheet.getOrCreateStatInstance(stat).addFlatModifier(modifier);
        dirtyInternalStats.add(stat);
        startAndResolveProcess();
    }
    //not safe to use on client, will cause a desync
    public void addMultiplierModifier(Stat stat,Modifier<Double> modifier){
        internalStatSheet.getOrCreateStatInstance(stat).addMultiplierModifier(modifier);
        dirtyInternalStats.add(stat);
        startAndResolveProcess();
    }
    //not safe to use on client, will cause a desync
    public void removeMultiplierModifier(Stat stat,Identifier modifier){
        if(internalStatSheet.getStatInstance(stat) == null) return;
        internalStatSheet.getStatInstance(stat).removeModifier(modifier);
        if(internalStatSheet.getStatInstance(stat).isEmpty()) internalStatSheet.removeStat(stat);
        dirtyInternalStats.add(stat);
        startAndResolveProcess();
    }








    //====================== CACHED SHEET ======================
    public void registerStatProvider(StatProvider provider){
        providers.add(provider);
        updateStats(provider.getStats());

    }
    public void removeStatProvider(StatProvider provider){
        providers.remove(provider);
        updateStats(provider.getStats());
    }


    public void cachedSheetUpdated(){}
    /**
     * attempts to merge all the modifiers of existing value containers for this stat
     * will only merge operation group 0, type flat modifiers(base value modifiers)
     * @param stat the stat we want to generate a cached version of
     */
    public void updateStat(Stat stat){
        String processId = "small_stat_update:"+ UUID.randomUUID();
        startProcess(processId);

        cachedSheetDirty = true;

        List<ValueContainer<Double>> containers = new ArrayList<>();
        if(internalStatSheet.getStatInstance(stat) != null) containers.add(internalStatSheet.getStatInstance(stat));

        for(StatProvider provider : providers){
            ValueContainer<Double> instance = provider.getStatInstance(stat);
            if(instance == null) continue;
            containers.add(instance);
        }
        ValueContainer<Double> container = ValueContainer.from(
                ()-> ValueContainerHelpers.doubleValueContainer(ZenithRegistries.STAT_REGISTRY.getKey(stat)),
                containers
        );
        if(container == null) cachedStatSheet.removeStat(stat);
        else cachedStatSheet.setStat(container);

        resolveProcess(processId);
    }
    public void updateStats(Collection<Stat> stats){
        String processId = "bulk_stat_update"+UUID.randomUUID();
        startProcess(processId);

        for(Stat stat:stats) updateStat(stat);
        cachedSheetDirty = true;
        resolveProcess(processId);
    }
    //====================== NETWORK METHODS ======================
    //I do not need to send the internal sheet over network. since it does not do anything on the client.
    //and the cached sheet is the only one exposed.
    public void encode(ByteBuf buf){
        ByteBufHelpers.encodeCollection(cachedStatSheet.getAllInstances(), buf, (val, byteBuf)-> ValueContainer.encode(val,byteBuf, Codec.DOUBLE));
    }
    public void decode(ByteBuf buf){
        cachedStatSheet = new StatSheet(); //prob not the cheapest way to clear it lol
        List<ValueContainer<Double>> instances = ByteBufHelpers.<ValueContainer<Double>>decodeArray(buf,
                (byteBuf)->ValueContainer.decode(ValueContainerHelpers::doubleValueContainer,byteBuf, Codec.DOUBLE));
        for(ValueContainer<Double> instance : instances) cachedStatSheet.setStat(instance);

    }
    //====================== STAT PROVIDER ======================


    @Override
    public Collection<Stat> getStats() {
        return cachedStatSheet.getAllStats();
    }

    @Override
    public ValueContainer<Double> getStatInstance(Stat stat) {
        return cachedStatSheet.getStatInstance(stat);
    }

    @Override
    public double getStat(Stat stat) {
        return cachedStatSheet.getStatInstance(stat) == null ? 0 : cachedStatSheet.getStatInstance(stat).getValue();
    }

    @Override
    public double getBaseStat(Stat stat) {
        return cachedStatSheet.getStatInstance(stat) == null ? 0 :  cachedStatSheet.getStatInstance(stat).getBaseValue();
    }
}
