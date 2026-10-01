package net.zic.zenithlib.custom_attributes;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.StatProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Random;

public class ZenithAttributeHolder {
    private final LivingEntity attachedEntity;
    private final HashMap<Holder<Attribute>, ZenithAttribute> attributes = new HashMap<>();
    private final HashMap<Holder<Attribute>,Double> cachedSuppressionValues = new HashMap<>();


    private String process = null;
    private final Random random = new Random();

    public ZenithAttributeHolder(LivingEntity attachedEntity) {
        this.attachedEntity = attachedEntity;
    }

    public void startProcess(String process){
        if(this.process == null) this.process = process;
    }
    public boolean resolveProcess(String process){
        if(this.process == null) return false;
        if(!this.process.equals(process)) return false;
        this.process = null;
        sync();
        return true;
    }
    public void sync(){
        if(attachedEntity == null || attachedEntity.level().isClientSide() ||( attachedEntity instanceof ServerPlayer serverPlayer && serverPlayer.connection == null)) return;

        attachedEntity.syncData(ZenithAttachments.ATTRIBUTE_HOLDER);
    }
    protected void startAndResolve(String process){
        startProcess(process);
        resolveProcess(process);
    }

    public void update(StatProvider provider) {
        String processId = "provider_update"+random.nextLong();
        startProcess(processId);
        attributes.forEach((holder, attribute) -> attribute.update(provider));
        resolveProcess(processId);
    }


    public void addAttribute(Holder<Attribute> attributeHolder) {
        if (attributes.containsKey(attributeHolder)) return;

        ZenithAttribute zenithAttribute =  new ZenithAttribute(attributeHolder, attachedEntity);
        attributes.put(attributeHolder, zenithAttribute);

        if(cachedSuppressionValues.containsKey(zenithAttribute.getAttribute())) suppress(zenithAttribute.getAttribute(),cachedSuppressionValues.remove(zenithAttribute.getAttribute()));

        startAndResolve("small_attribute_modification"+random.nextLong());

    }
    public void removeAttribute(Holder<Attribute> attributeHolder) {
        attributes.remove(attributeHolder);

    }
    public ZenithAttribute getAttribute(Holder<Attribute> attribute) {
        if(attachedEntity != null && attachedEntity.getAttribute(attribute) != null && !attributes.containsKey(attribute)) addAttribute(attribute);
        return attributes.get(attribute);
    }

    public boolean hasAttribute(Holder<Attribute> attribute) {
        return getAttribute(attribute) != null;
    }


    public boolean isSuppressable(Holder<Attribute> attribute){
        return SuppressedAttributeHelper.isSuppressible(attribute);
    }

    public boolean suppress(Identifier attribute,double suppression){
        return suppress(SuppressedAttributeHelper.getAttribute(attribute),suppression);
    }
    public boolean suppress(Holder<Attribute> attribute,double suppression){
        if(!hasAttribute(attribute)) return false;
        if(!isSuppressable(attribute)) return false;
        SuppressedAttributeHelper.applySuppression(getAttribute(attribute), suppression);
        return true;
    }
    public double getSuppression(Holder<Attribute> attribute){
        if(!hasAttribute(attribute)) return 1;
        if(!isSuppressable(attribute)) return 1;
        return SuppressedAttributeHelper.getSuppression(getAttribute(attribute));
    }

    public void attachEntity() {
        attributes.forEach(((attributeHolder, zenithAttribute) -> zenithAttribute.setAttachedEntity(attachedEntity)));
    }
    public void encode(ByteBuf buf) {
        ByteBufHelpers.encodeCollection(attributes.values(), buf, ZenithAttribute::encode);
    }
    public void decode(ByteBuf buf) {
        attributes.clear();
        ByteBufHelpers.decodeArray(buf, ZenithAttribute::decode).forEach(
                container -> attributes.put(container.getAttribute(), container));
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

                Identifier attribute = Identifier.parse(suppressedInput.getStringOr("attribute","none"));
                double suppression = suppressedInput.getDoubleOr("suppression",1);

                ZenithAttribute attributeWrapper = new ZenithAttribute(attribute);
                if(attributeWrapper.getAttribute() == null || !SuppressedAttributeHelper.isSuppressible(attribute)) continue;

                attributeHolder.cachedSuppressionValues.put(attributeWrapper.getAttribute(),suppression);

            }

            return attributeHolder;
        }

        @Override
        public boolean write(ZenithAttributeHolder attachment, ValueOutput output) {
            ValueOutput.ValueOutputList outputList = output.childrenList("suppressed_attributes");
            for(ZenithAttribute attribute:attachment.attributes.values()){
                if(attachment.isSuppressable(attribute.getAttribute())) {
                    ValueOutput suppressionOutput = outputList.addChild();
                    suppressionOutput.putString("attribute",attribute.getContainerId().toString());
                    suppressionOutput.putDouble("suppression",SuppressedAttributeHelper.getSuppression(attribute));
                }
            }
            return true;
        }
    }

}
