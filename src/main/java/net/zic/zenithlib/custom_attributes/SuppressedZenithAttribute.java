package net.zic.zenithlib.custom_attributes;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SuppressedZenithAttribute  extends ZenithAttribute{
    private double suppression = 1;

    public SuppressedZenithAttribute(Holder<Attribute> attribute, LivingEntity attachedEntity){
        super(attribute,attachedEntity);
    }
    public SuppressedZenithAttribute(Identifier identifier) {
        super(identifier);
    }
    public SuppressedZenithAttribute(Identifier identifier,double suppression) {
        super(identifier);
        this.suppression = suppression;
    }
    public void setSuppression(double amount){
        suppression = Math.clamp(amount,0,1);
    }
    public double getSuppression(){
        return suppression;
    }
    @Override
    public double getValue() {
        return super.getValue()*suppression;
    }

    public double getUnsuppressedValue(){
        return super.getValue();
    }

    public static void encode(ZenithAttribute attribute, ByteBuf buf){
        ByteBufHelpers.encodeIdentifier(attribute.getIdentifier(),buf);
        buf.writeBoolean(attribute instanceof SuppressedZenithAttribute);
        if(attribute instanceof SuppressedZenithAttribute suppressedZenithAttribute) buf.writeDouble(suppressedZenithAttribute.getSuppression());

        ByteBufHelpers.encodeCollection(attribute.getAllModifiers(),buf, ValueContainerModifier::encode);
        ByteBufHelpers.encodeCollection(attribute.getScaling().values(),buf,(val,buffer)-> ValueContainer.encode(buffer,val));

    }
    public static ZenithAttribute decode(ByteBuf buf){
        Identifier identifier = ByteBufHelpers.decodeIdentifier(buf);

        ZenithAttribute attribute;
        if(buf.readBoolean()){
            double suppression = buf.readDouble();
            attribute = new SuppressedZenithAttribute(identifier,suppression);
        } attribute = new ZenithAttribute(identifier);

        List<ValueContainerModifier> modifiers = ByteBufHelpers.decodeArray(buf, ValueContainerModifier::decode);
        List<ValueContainer> scaling = ByteBufHelpers.decodeArray(buf, ValueContainer::decode);

        modifiers.forEach(attribute::addModifierNoCacheUpdate);
        for(ValueContainer container : scaling){
            attribute.scaling.put(ZenithRegistries.STAT_REGISTRY.getValue(container.getIdentifier()),container);
        }

        return attribute;
    }
    public static void write(SuppressedZenithAttribute attribute, ValueOutput output){

        output.putString("attribute",attribute.getIdentifier().toString());
        output.putDouble("suppression",attribute.suppression);
    }
    public static SuppressedZenithAttribute load(ValueInput input){

        Identifier attribute = Identifier.parse(input.getStringOr("attribute","none"));
        double suppression = input.getDoubleOr("suppression",1);

        return new SuppressedZenithAttribute(attribute,suppression);
    }

}
