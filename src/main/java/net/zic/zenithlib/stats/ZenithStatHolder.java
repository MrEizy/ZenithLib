package net.zic.zenithlib.stats;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ZenithStatHolder implements StatProvider{
    private final LivingEntity attachedEntity;
    private final HashSet<StatProvider> providers = new HashSet<>();

    private final StatSheet cachedStatSheet = new StatSheet();
    private String process = null;
    private final Random random  = new Random();
    public ZenithStatHolder(LivingEntity attachedEntity) {
        this.attachedEntity = attachedEntity;
    }

    public void startProcess(String process){
        if(this.process == null) this.process = process;
    }
    public boolean resolveProcess(String process){

        if(this.process == null) return false;
        if(!this.process.equals(process)) return false;
        this.process = null;
        updateAttributes();
        sync();
        return true;
    }
    public void sync(){
        if(attachedEntity == null) return;
        attachedEntity.syncData(ZenithAttachments.STAT_HOLDER);
    }
    public void updateAttributes(){
        if(attachedEntity == null) return;

        ZenithAttributeHolder holder = attachedEntity.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
        holder.update(this);

    }

    public void registerStatProvider(StatProvider provider){
        providers.add(provider);
        updateStats(provider.getStats());

    }
    public void removeStatProvider(StatProvider provider){
        providers.remove(provider);
        updateStats(provider.getStats());
    }

    public void updateStat(Stat stat){
        String processId = "small_stat_update"+random.nextLong();
        startProcess(processId);
        List<ValueContainer<Double>> containers = new ArrayList<>();
        for(StatProvider provider : providers){
            ValueContainer<Double> instance = provider.getStatInstance(stat);
            if(instance == null) continue;
            containers.add(instance);
        }
        ValueContainer<Double> container = ValueContainer.from(
                ZenithRegistries.STAT_REGISTRY.getKey(stat),
                containers
        );
        if(container == null) cachedStatSheet.removeStat(stat);
        else cachedStatSheet.setStat(container);

        resolveProcess(process);
    }

    public void updateStats(Collection<Stat> stats){
        String processId = "bulk_stat_update"+random.nextLong();
        startProcess(processId);
        for(Stat stat:stats) updateStat(stat);
        resolveProcess(processId);
    }

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
        return getStatInstance(stat) == null? 0 : getStatInstance(stat).getValue();
    }

    @Override
    public double getBaseStat(Stat stat) {
        return getStatInstance(stat) == null? 0 : getStatInstance(stat).getBaseValue();
    }


    public static class SyncHandler implements AttachmentSyncHandler<ZenithStatHolder> {

        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, ZenithStatHolder attachment, boolean initialSync) {

            ByteBufHelpers.encodeCollection(attachment.cachedStatSheet.getAllInstances(), buf, (val,byteBuf)->ValueContainer.encode(val,byteBuf,Codec.DOUBLE));
        }

        @Override
        public @Nullable ZenithStatHolder read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable ZenithStatHolder previousValue) {
            if(!(holder instanceof LivingEntity entity)) return null;
            if(previousValue == null) previousValue = new ZenithStatHolder(entity);

            List<ValueContainer<Double>> instances = ByteBufHelpers.<ValueContainer<Double>>decodeArray(buf,
                    (byteBuf)->ValueContainer.decode(ValueContainerHelpers::doubleValueContainer,byteBuf, Codec.DOUBLE));
            for(ValueContainer<Double> instance : instances) previousValue.cachedStatSheet.setStat(instance);

            return previousValue;
        }

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return true;
        }
    }

}
