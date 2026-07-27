package net.zic.zenithlib.custom_attributes;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.cooldown.Cooldown;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatInstance;
import net.zic.zenithlib.stats.StatProvider;
import net.zic.zenithlib.stats.StatSheet;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

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
    private final HashMap<Holder<Attribute>, ZenithAttribute> attributes = new HashMap<>();

    private final HashMap<Holder<Attribute>,Double> cachedSuppressionValues = new HashMap<>();

    public ZenithAttributeHolder(LivingEntity attachedEntity) {
        this.attachedEntity = attachedEntity;
    }


    public void sync(){
        if(attachedEntity == null) return;
        attachedEntity.syncData(ZenithAttachments.ATTRIBUTE_HOLDER);
    }

    public void addAttribute(Holder<Attribute> attributeHolder) {
        if (hasAttribute(attributeHolder)) return;
        attributes.put(attributeHolder, new ZenithAttribute(attributeHolder, attachedEntity));
        sync();
    }
    public void addSuppressedAttribute(Holder<Attribute> attributeHolder) {
        if (!hasAttribute(attributeHolder)) {
            SuppressedZenithAttribute attribute = new SuppressedZenithAttribute(attributeHolder, attachedEntity);
            if (cachedSuppressionValues.containsKey(attributeHolder)) {
                attribute.setSuppression(cachedSuppressionValues.remove(attributeHolder));
            }
            attributes.put(attributeHolder, attribute);
        } else makeAttributeSuppressable(attributeHolder);
        sync();
    }

    public void removeAttribute(Holder<Attribute> attributeHolder) {
        attributes.remove(attributeHolder);
        sync();
    }

    public ZenithAttribute getAttribute(Holder<Attribute> attributeHolder) {
        return attributes.get(attributeHolder);
    }

    public boolean hasAttribute(Holder<Attribute> attributeHolder) {
        return attributes.containsKey(attributeHolder);
    }

    public boolean isSuppressable(Holder<Attribute> attributeHolder){
        if(!attributes.containsKey(attributeHolder)) return false;
        return getAttribute(attributeHolder) instanceof SuppressedZenithAttribute;
    }
    public void makeAttributeSuppressable(Holder<Attribute> attributeHolder){
        if(!hasAttribute(attributeHolder) || isSuppressable(attributeHolder)) return;
        ZenithAttribute attribute = getAttribute(attributeHolder);

        SuppressedZenithAttribute suppressedAttribute = new SuppressedZenithAttribute(attribute.getIdentifier());
        if(cachedSuppressionValues.containsKey(attributeHolder)){
            suppressedAttribute.setSuppression(cachedSuppressionValues.remove(attributeHolder));
        }
        for (ValueContainerModifier modifier : attribute.getAllModifiers()) suppressedAttribute.addModifierNoCacheUpdate(modifier);
        suppressedAttribute.scaling.putAll(attribute.getScaling());

        suppressedAttribute.calculateCachedVal();
        attributes.put(attributeHolder,suppressedAttribute);
        sync();
    }
    public void setSuppression(Holder<Attribute> attributeHolder,double suppression){
        if(!hasAttribute(attributeHolder) || !isSuppressable(attributeHolder)) return;

        ((SuppressedZenithAttribute) getAttribute(attributeHolder)).setSuppression(suppression);
        sync();
    }

    public Collection<Holder<Attribute>> getSuppressedAttributes(){
        ArrayList<Holder<Attribute>> suppressed = new ArrayList<>();
        for(Holder<Attribute> attribute : attributes.keySet()){
            if(isSuppressable(attribute)) suppressed.add(attribute);
        }
        return suppressed;
    }

    public void update(StatProvider provider) {
        attributes.forEach((holder, attribute) -> attribute.update(provider));
        sync();
    }



    public void attachEntity() {
        attributes.forEach(((attributeHolder, zenithAttribute) -> zenithAttribute.setAttachedEntity(attachedEntity)));
    }


    public void encode(ByteBuf buf) {
        ByteBufHelpers.encodeCollection(attributes.values(), buf, SuppressedZenithAttribute::encode);
    }


    public void decode(ByteBuf buf) {
        attributes.clear();
        ByteBufHelpers.decodeArray(buf, SuppressedZenithAttribute::decode).forEach(container -> attributes.put(container.getAttribute(), container));
        attachEntity();
    }

    public static class SyncHandler implements AttachmentSyncHandler<ZenithAttributeHolder> {

        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, ZenithAttributeHolder attachment, boolean initialSync) {
            attachment.encode(buf);
        }

        @Override
        public @Nullable ZenithAttributeHolder read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable ZenithAttributeHolder previousValue) {
            if (!(holder instanceof LivingEntity entity)) return null;
            if (previousValue == null) previousValue = new ZenithAttributeHolder(entity);
            previousValue.decode(buf);
            return previousValue;
        }

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return true;
        }
    }
    public static class Provider implements IAttachmentSerializer<ZenithAttributeHolder> {
        @Override
        public ZenithAttributeHolder read(
                @NonNull IAttachmentHolder holder,
                ValueInput input
        ) {
            if (!(holder instanceof LivingEntity entity)) {
                return null;
            }
            ZenithAttributeHolder attributeHolder = new ZenithAttributeHolder(entity);
            ValueInput.ValueInputList suppressableAttributes = input.childrenListOrEmpty("suppressed_attributes");
            for(ValueInput suppressedInput : suppressableAttributes){
                SuppressedZenithAttribute  attribute = SuppressedZenithAttribute.load(suppressedInput);
                try {
                    attributeHolder.cachedSuppressionValues.put(attribute.getAttribute(),attribute.getSuppression());
                } catch (Exception e){
                    ZenithLib.LOGGER.error("unable load suppression value for attribute {}",attribute.getIdentifier());
                }
            }

            return attributeHolder;
        }

        @Override
        public boolean write(ZenithAttributeHolder attachment, ValueOutput output) {
            ValueOutput.ValueOutputList outputList = output.childrenList("suppressed_attributes");
            for(ZenithAttribute attribute:attachment.attributes.values()){
                if(attribute instanceof SuppressedZenithAttribute suppressedAttribute) SuppressedZenithAttribute.write(suppressedAttribute,outputList.addChild());
            }
            return true;
        }
    }

}
