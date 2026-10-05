package net.zic.zenithlib.stats;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
//TODO update to use dirty syncing
public class EntityStatHolder extends MultiSourceStatHolder {
    private final LivingEntity attachedEntity;

    public EntityStatHolder(LivingEntity attachedEntity) {
        super(); //ensures the on resolved is set
        this.attachedEntity = attachedEntity;

    }

    @Override
    public void cachedSheetUpdated() {
        sync();
        updateAttributes();
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
    public static class SyncHandler implements AttachmentSyncHandler<EntityStatHolder> {

        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, EntityStatHolder attachment, boolean initialSync) {
            attachment.encode(buf);
        }

        @Override
        public @Nullable EntityStatHolder read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable EntityStatHolder previousValue) {
            if(!(holder instanceof LivingEntity entity)) return null;
            if(previousValue == null) previousValue = new EntityStatHolder(entity);
            previousValue.decode(buf);
            return previousValue;
        }

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return true;
        }
    }

}
