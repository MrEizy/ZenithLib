package net.zic.zenithlib.stats;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ZenithStatHolder implements StatProvider{
    private final LivingEntity attachedEntity;
    private final HashSet<StatProvider> providers = new HashSet<>();

    private final StatSheet cachedStatSheet = new StatSheet();

    public ZenithStatHolder(LivingEntity attachedEntity) {
        this.attachedEntity = attachedEntity;
    }

    public void registerStatProvider(StatProvider provider){
        providers.add(provider);
        //TODO if new trigger recalc
    }
    public void removeStatProvider(StatProvider provider){
        providers.remove(provider);
        //TODO trigger recalc
    }

    public void updateStat(Stat stat){

        StatInstance newStatInstance = new StatInstance(stat,0);
        for(StatProvider provider : providers){
            StatInstance instance = provider.getStatInstance(stat);
            if(instance == null) continue;
            newStatInstance.setBaseValue(newStatInstance.getBaseValue()+instance.getBaseValue());
            for(ValueContainerModifier modifier :instance.getAllModifiers()){
                newStatInstance.addModifierNoCacheUpdate(modifier);
            }
        }
        newStatInstance.calculateCachedVal();
        cachedStatSheet.setStat(newStatInstance);
    }

    public void updateStats(Collection<Stat> stats){
        for(Stat stat:stats) updateStat(stat);
    }

    @Override
    public Collection<Stat> getStats() {
        return cachedStatSheet.getAllStats();
    }

    @Override
    public StatInstance getStatInstance(Stat stat) {
        return cachedStatSheet.getStatInstance(stat);
    }

    @Override
    public double getStat(Stat stat) {
        return getStatInstance(stat) == null? 0 : getStatInstance(stat).getValue();
    }

    @Override
    public double getBaseStat(Stat stat) {
        return getStatInstance(stat) == null? 0 : getStatInstance(stat).getBaseValue();
    }


    public static class SyncHandler implements AttachmentSyncHandler<ZenithStatHolder> {

        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, ZenithStatHolder attachment, boolean initialSync) {

            ByteBufHelpers.encodeCollection(attachment.cachedStatSheet.getAllInstances(), buf, StatInstance::encode);
        }

        @Override
        public @Nullable ZenithStatHolder read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable ZenithStatHolder previousValue) {
            if(!(holder instanceof LivingEntity entity)) return null;
            if(previousValue == null) previousValue = new ZenithStatHolder(entity);

            List<StatInstance> instances = ByteBufHelpers.decodeArray(buf, StatInstance::decode);
            for(StatInstance instance : instances) previousValue.cachedStatSheet.setStat(instance);

            return previousValue;
        }

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return true;
        }
    }

}
