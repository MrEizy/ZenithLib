package net.zic.zenithlib.custom_attributes;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.StatInstance;
import net.zic.zenithlib.stats.StatSheet;
import net.zic.zenithlib.value_containers.ModifierOperation;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.*;

/**
 * a zenith attribute scales of 2 things, the attribute it wraps and the stats it scales off
 *
 * combined with the zenithAttribute Holder and mixins we can replace base attributes
 *
 * how it works.
 *
 * you pick an attribute. this makes up part of the base value
 *
 * then we add stat scaling. here we either set the base value or a multiplier of the base value
 *
 * those 2 combined make the base value
 *
 * we then apply any modifiers added on to the Zenith attribute
 *
 */
public class ZenithAttribute extends ValueContainer {
    private LivingEntity attachedEntity;


    private final HashMap<Stat,ValueContainer> scaling = new HashMap<>();

    double cachedBaseStatBonus;
    double cachedAttributeValue;

    public ZenithAttribute(Holder<Attribute> attribute, LivingEntity attachedEntity) {
        super(BuiltInRegistries.ATTRIBUTE.getKey(attribute.value()),0);
        this.attachedEntity = attachedEntity;
        validateAttributeValue();

    }
    protected ZenithAttribute(Identifier identifier) {
        super(identifier,0);

    }

    public void setAttachedEntity(LivingEntity entity){
        this.attachedEntity = entity;
        validateAttributeValue();
    }
    public Holder<Attribute> getAttribute(){
        return Holder.direct(Objects.requireNonNull(BuiltInRegistries.ATTRIBUTE.getValue(getIdentifier())));
    }


    public void addStatScaling(Stat stat,Identifier identifier,double value){
        if(!scaling.containsKey(stat)){
            scaling.put(stat,new  ValueContainer(ZenithRegistries.STAT_REGISTRY.getKey(stat),0));
        }

        scaling.get(stat).addModifier(new ValueContainerModifier(value, ModifierOperation.ADD_BASE,identifier));
    }
    //if we scale of 200% Vit and give it a 2x bonus it now scales of 400%
    public void addStatScalingMultiplier(Stat stat,Identifier identifier,double value){
        if(!scaling.containsKey(stat)){
            scaling.put(stat,new  ValueContainer(ZenithRegistries.STAT_REGISTRY.getKey(stat),0));
        }

        scaling.get(stat).addModifier(new ValueContainerModifier(value, ModifierOperation.MULTIPLY_FINAL,identifier));
    }
    //if we scale of 200% Vit and give it a x1.2 in the same group as x1.4 it is now a x1.8 bonus not  (all multipliers have +1 added to em)
    public void addStatScalingMultiplier(Stat stat,Identifier identifier,Identifier group,double value){
        if(!scaling.containsKey(stat)){
            scaling.put(stat,new  ValueContainer(ZenithRegistries.STAT_REGISTRY.getKey(stat),0));
        }

        scaling.get(stat).addModifier(new ValueContainerModifier(value, ModifierOperation.MULTIPLY_FINAL,identifier,group));
    }
    //removes both multiplier and base
    public void removeScaling(Stat stat,Identifier identifier){
        if(!scaling.containsKey(stat)) return;
        scaling.get(stat).removeModifier(identifier);

        //trim empty scaling
        if(scaling.get(stat).getAllModifiers().isEmpty()) scaling.remove(stat);
    }


    //make sure to call this whenever you update a stat
    public void update(Map<Stat,StatInstance> stats){
        //used to retrieve the stat sheet for calculations

        double baseVal = 0;
        for (Stat stat : scaling.keySet()){
            StatInstance instance = stats.get(stat);
            if(instance == null) continue;
            baseVal += instance.getValue()*scaling.get(stat).getValue();
        }
        cachedBaseStatBonus = baseVal;

        calculateCachedVal();
    }

    @Override
    public double getBaseValue() {
        validateAttributeValue();
        return super.getBaseValue();
    }

    @Override
    public double getValue() {
        validateAttributeValue();
        return super.getValue();
    }

    public void validateAttributeValue(){
        if(attachedEntity == null) return;
        var inst = attachedEntity.getAttribute(getAttribute());
        double target = (inst != null) ? inst.getValue() : 0; //treat it as if they do not have it
        if(target != cachedAttributeValue) {
            cachedAttributeValue = target;
            calculateCachedVal();
        }
    }
    @Override
    public void calculateCachedVal() {
        if(cachedAttributeValue+ cachedBaseStatBonus != getBaseValue()) setBaseValue(cachedAttributeValue+cachedBaseStatBonus);
        super.calculateCachedVal();
    }


    public void encode(ByteBuf buf){
        ByteBufHelpers.encodeIdentifier(getIdentifier(),buf);
        ByteBufHelpers.encodeCollection(getAllModifiers(),buf,ValueContainerModifier::encode);
        ByteBufHelpers.encodeCollection(scaling.values(),buf,(val,buffer)->ValueContainer.encode(buffer,val));

    }
    //you need to make sure to attach the correct entity and then call update()
    public static ZenithAttribute decode(ByteBuf buf){
        Identifier identifier = ByteBufHelpers.decodeIdentifier(buf);
        List<ValueContainerModifier> modifiers = ByteBufHelpers.decodeArray(buf, ValueContainerModifier::decode);
        List<ValueContainer> scaling = ByteBufHelpers.decodeArray(buf, ValueContainer::decode);

        ZenithAttribute attribute = new ZenithAttribute(identifier);

        modifiers.forEach(attribute::addModifier);
        scaling.forEach(container -> attribute.scaling.put(ZenithRegistries.STAT_REGISTRY.getValue(container.getIdentifier()),container));

        attribute.calculateCachedVal();
        return attribute;
    }
}
