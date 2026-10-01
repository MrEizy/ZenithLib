package net.zic.zenithlib.custom_attributes;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatProvider;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ValueContainer;
import net.zic.zenithlib.value_containers.typed.ValueContainerHelpers;


import java.util.HashMap;
import java.util.Map;

public class ZenithAttribute extends ValueContainer<Double> {
    private LivingEntity attachedEntity;

    private static final Identifier statBonusModifier = Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID,"stat_bonus");
    private static final Identifier attributeValueModifier = Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID,"attribute_value");

    final HashMap<Stat, ValueContainer<Double>> scaling = new HashMap<>();


    public ZenithAttribute(Holder<Attribute> attribute){
        this(BuiltInRegistries.ATTRIBUTE.getKey(attribute.value()));
    }

    public ZenithAttribute(Identifier containerId) {
        super(containerId, Double::sum, ValueContainerHelpers.DOUBLE_MUL, ValueContainerHelpers.DOUBLE_ENCODER, ValueContainerHelpers.DOUBLE_DECODER, 0d);
    }

    public ZenithAttribute(Holder<Attribute> attribute, LivingEntity attachedEntity){
        this(attribute);
        setAttachedEntity(attachedEntity);
    }
    public ZenithAttribute(Identifier containerId, LivingEntity attachedEntity){
        this(containerId);
        setAttachedEntity(attachedEntity);
    }
    private double getStatBonus(){
        return getFlatModifier(statBonusModifier) == null ? 0: getFlatModifier(statBonusModifier).value();
    }
    private double getAttributeValue(){
        return getFlatModifier(attributeValueModifier) == null ? 0  : getFlatModifier(attributeValueModifier).value();
    }

    private void setStatBonus(double value){
        removeModifier(statBonusModifier);
        addFlatModifier(Modifier.base(statBonusModifier,value));
        calculateValue();
    }
    private void setAttributeValue(double value){
        removeModifier(attributeValueModifier);
        addFlatModifier(Modifier.base(attributeValueModifier,value));
        calculateValue();
    }


    public void validateAttributeValue(){
        if(attachedEntity == null) {
            setAttributeValue(0);
            return;
        }
        AttributeInstance instance = attachedEntity.getAttribute(getAttribute());
        double attributeValue = instance == null ? 0 : instance.getValue();
        if(getAttributeValue() != attributeValue) setAttributeValue(attributeValue);
    }

    public void update(StatProvider provider){
        double baseVal = 0;
        for(Stat stat : scaling.keySet()){
            baseVal += provider.getStat(stat)*scaling.get(stat).getValue();
        }
        if(getStatBonus() != baseVal) setStatBonus(baseVal);
    }


    public Holder<Attribute> getAttribute(){
        return !BuiltInRegistries.ATTRIBUTE.containsKey(getContainerId()) ? null : BuiltInRegistries.ATTRIBUTE.get(getContainerId()).get();
    }

    public Map<Stat, ValueContainer<Double>> getScaling(){
        return scaling;
    }

    public void addFlatScaling(Stat stat,Modifier<Double> modifier){
        scaling.computeIfAbsent(stat,key->stat.statInstance()).addFlatModifier(modifier);
    }
    public void addMultiplierScaling(Stat stat,Modifier<Double> modifier){
        scaling.computeIfAbsent(stat,key->stat.statInstance()).addMultiplierModifier(modifier);
    }

    public void removeStatScaling(Stat stat,Identifier modifier){
        if(!scaling.containsKey(stat)) return;
        scaling.get(stat).removeModifier(modifier);
    }
    public void setAttachedEntity(LivingEntity entity){
        setAttachedEntity(entity,true);
    }
    public void setAttachedEntity(LivingEntity entity,boolean update){
        this.attachedEntity = entity;
        if(update) validateAttributeValue();
    }
    @Override
    public Double getValue() {
        validateAttributeValue();
        return super.getValue();
    }

    @Override
    public Double getBaseValue() {
        validateAttributeValue();
        return super.getBaseValue();
    }

    @Override
    public void calculateValue() {
        super.calculateValue();
        if(attachedEntity == null || attachedEntity.getAttribute(getAttribute()) == null) return;

        attachedEntity.getAttribute(getAttribute()).setDirty();
    }

    public static void encode(ZenithAttribute attributeContainer, ByteBuf buf){
        ValueContainer.encode(attributeContainer,buf, Codec.DOUBLE);
    }

    public static ZenithAttribute decode(ByteBuf buf){
        return ValueContainer.decode(ZenithAttribute::new,buf,Codec.DOUBLE);
    }

}
