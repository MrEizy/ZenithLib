package net.zic.zenithlib.custom_attributes;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.zic.zenithlib.cooldown.Cooldown;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.StatSheet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

/**
 * This is our custom attribute holder that all mods using this mod will include
 *
 * this way no scenario will pop up where a single entity has multiple sources of attributes
 * so mods should only be modifying this through modifiers or scaling with custom stats
 *
 * when trying to access an attribute if they do not have it, it will default to 0.
 * if they try to add a scaling and it does not exist it will add it
 * TO CONSIDER (create some sort of on First created event so mods can hook in and provide default attribtues?)
 */
public class ZenithAttributeHolder {

    private final LivingEntity attachedEntity;
    private final HashMap<Holder<Attribute>,ZenithAttribute> attributes = new HashMap();


    public ZenithAttributeHolder(LivingEntity attachedEntity){
        this.attachedEntity = attachedEntity;
    }


    public void addAttribute(Holder<Attribute> attributeHolder){
        attributes.put(attributeHolder,new ZenithAttribute(attributeHolder,attachedEntity));
    }

    public void removeAttribute(Holder<Attribute> attributeHolder){
        attributes.remove(attributeHolder);
    }

    public ZenithAttribute getAttribute(Holder<Attribute> attributeHolder){
        return attributes.get(attributeHolder);
    }

    public boolean hasAttribute(Holder<Attribute> attributeHolder){
        return attributes.containsKey(attributeHolder);
    }

    public void update(StatSheet sheet){
        attributes.forEach((attributeHolder, zenithAttribute) -> zenithAttribute.update(sheet));
    }

    public void attachEntity(){
        attributes.forEach(((attributeHolder, zenithAttribute) -> zenithAttribute.setAttachedEntity(attachedEntity)));
    }

    public void encode(ByteBuf buf){
        ByteBufHelpers.encodeMap(
                attributes,
                (attribute,byteBuf)->ByteBufHelpers.encodeIdentifier(attribute.getKey().identifier(),byteBuf),
                ZenithAttribute::encode,
                buf
        );
    }


    public void decode(ByteBuf buf){
        attributes.clear();
        ByteBufHelpers.decodeMap(attributes,
                (byteBuf)->Holder.direct(Objects.requireNonNull(BuiltInRegistries.ATTRIBUTE.getValue(ByteBufHelpers.decodeIdentifier(byteBuf)))),
                ZenithAttribute::decode,
                buf
                );
        attachEntity();
    }

    public static class SyncHandler implements AttachmentSyncHandler<ZenithAttributeHolder> {

        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, ZenithAttributeHolder attachment, boolean initialSync) {
            attachment.encode(buf);
        }

        @Override
        public @Nullable ZenithAttributeHolder read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable ZenithAttributeHolder previousValue) {
            if(!(holder instanceof LivingEntity entity)) return null;
            if(previousValue == null) previousValue = new ZenithAttributeHolder(entity);
            previousValue.decode(buf);
            return previousValue;
        }

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return true;
        }
    }

}
