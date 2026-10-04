package net.zic.zenithlib.configuration.attribute_scaling;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.common.ZenithDataMaps;
import net.zic.zenithlib.custom_attributes.ZenithAttribute;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.Map;

//TODO consider moving this over to zenith lib
public record LivingEntityScaling(Map<Holder<Attribute>, AttributeScaling> attributes){
    public static final Codec<LivingEntityScaling> CODEC = Codec.unboundedMap(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec(),
            AttributeScaling.CODEC
    ).xmap(LivingEntityScaling::new,LivingEntityScaling::attributes);

    public static void apply(LivingEntity entity,ZenithAttribute attribute){
        LivingEntityScaling scaling = entity.getData(ZenithDataMaps.ATTRIBUTE_SCALING_CONFIG);
        if(scaling != null) scaling.applyScaling(entity,attribute);
    }
    //this is called by the attribute itself
    private void applyScaling(LivingEntity entity, ZenithAttribute attribute){
        if(!attributes.containsKey(attribute.getAttribute())) return;
        AttributeScaling scaling = attributes.get(attribute.getAttribute());
        attribute.startProcess("applying_scaling");
        for(Stat stat : scaling.scaling().keySet()){

            ModifierHolder<Double> holder = scaling.scaling().get(stat);
            for(Modifier<Double> flatModifier : holder.flat()) addFlatModifier(attribute,stat,flatModifier);
            for(Modifier<Double> mulitplierModifier : holder.multiplier()) addMultiplierModifier(attribute,stat,mulitplierModifier);
            attribute.calculateStatValue(stat);
        }
        attribute.updateStatBonus();
        attribute.resolveProcess("applying_scaling");
    }

    public void addFlatModifier(ZenithAttribute container, Stat stat,Modifier<Double> modifier){
        container.removeStatScaling(stat,modifier.id());
        container.addFlatScaling(stat,modifier);
    }
    public void addMultiplierModifier(ZenithAttribute container,Stat stat,Modifier<Double> modifier){
        container.removeStatScaling(stat,modifier.id());
        container.addMultiplierScaling(stat,modifier);
    }

}
