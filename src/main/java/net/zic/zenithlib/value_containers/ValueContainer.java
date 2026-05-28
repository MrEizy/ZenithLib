package net.zic.zenithlib.value_containers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.nbt.Reader;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.*;

public class ValueContainer {
    private final Identifier valueIdentifier;
    private double base;
    private final HashMap<Identifier,ValueContainerModifier> addBase = new HashMap<>();
    private final HashMap<Identifier,ValueContainerModifier> addFinal = new HashMap<>();

    private final HashMap<Identifier,ValueContainerModifier> multiBase = new HashMap<>();
    private final HashMap<Identifier, HashSet<ValueContainerModifier>> multiBaseByGroup = new HashMap<>();

    private final HashMap<Identifier,ValueContainerModifier> multiFinal = new HashMap<>();
    private final HashMap<Identifier, HashSet<ValueContainerModifier>> multiFinalByGroup = new HashMap<>();

    private double cachedVal;

    private static final StreamCodec<ByteBuf,ValueContainer> STREAM_CODEC = StreamCodec.of(ValueContainer::encode,ValueContainer::decode);


    public static final Codec<BaseModifier> BASE_MODIFIER_CODEC = RecordCodecBuilder.create(instance->
            instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(BaseModifier::container),
                    Codec.DOUBLE.fieldOf("value").forGetter(BaseModifier::val)
            ).apply(instance,BaseModifier::new)
            );

    public ValueContainer(Identifier valueIdentifier,double base){
        this.valueIdentifier = valueIdentifier;
        this.base = base;

        calculateCachedVal();
    }

    public record BaseModifier(Identifier container,double val){}

    public Collection<ValueContainerModifier> getAllModifiers(){
        List<ValueContainerModifier> modifiers = new ArrayList<>();
        modifiers.addAll(addBase.values());
        modifiers.addAll(addFinal.values());
        modifiers.addAll(multiBase.values());
        modifiers.addAll(multiFinal.values());
        return modifiers;
    }
    public Identifier getIdentifier(){return valueIdentifier;}

    public void calculateCachedVal(){

        double finalBaseMultiplier = 1;
        for(HashSet<ValueContainerModifier> group : multiBaseByGroup.values()){
            double multiplier = 1;
            for(ValueContainerModifier modifier : group) multiplier += modifier.getVal();
            multiplier = Math.max(multiplier,0); //TODO might change to clamp while adding multipliers
            finalBaseMultiplier *= multiplier;
        }
        double finalVal = base*finalBaseMultiplier;


        for(ValueContainerModifier modifier : addBase.values()){
            finalVal += modifier.getVal();
        }


        double finalMultiplier = 1;
        for(HashSet<ValueContainerModifier> group : multiFinalByGroup.values()){
            double multiplier = 1;
            for(ValueContainerModifier modifier : group) multiplier += modifier.getVal();
            multiplier = Math.max(multiplier,0); //TODO might change to clamp while adding multipliers
            finalMultiplier *= multiplier;
        }
        finalVal *= finalMultiplier;

        for(ValueContainerModifier modifier : addFinal.values()){
            finalVal += modifier.getVal();
        }

        cachedVal = Math.max(0,finalVal);
    }
    public void setBaseValue(double base){
        this.base = Math.max(0,base);
        calculateCachedVal();
    }

    protected void addModifierNoCacheUpdate(ValueContainerModifier modifier){
        if(modifier == null) return;
        if(modifier.getOperation() == ModifierOperation.ADD_BASE)addBase.put(modifier.getIdentifier(),modifier);
        else if(modifier.getOperation() == ModifierOperation.ADD_FINAL) addFinal.put(modifier.getIdentifier(),modifier);
        else if (modifier.getOperation() == ModifierOperation.MULTIPLY_BASE) {
            if(multiBase.containsKey(modifier.getIdentifier())){
                ValueContainerModifier old = multiBase.remove(modifier.getIdentifier());
                multiBaseByGroup.get(old.getGroupIdentifier()).remove(old);
                if(multiBaseByGroup.get(old.getGroupIdentifier()).isEmpty()) multiBaseByGroup.remove(old.getGroupIdentifier());
            }
            multiBase.put(modifier.getIdentifier(),modifier);
            if(!multiBaseByGroup.containsKey(modifier.getGroupIdentifier())) multiBaseByGroup.put(modifier.getGroupIdentifier(),new HashSet<>());
            multiBaseByGroup.get(modifier.getGroupIdentifier()).add(modifier);
        }
        else if (modifier.getOperation() == ModifierOperation.MULTIPLY_FINAL) {
            if(multiFinal.containsKey(modifier.getIdentifier())){
                ValueContainerModifier old = multiFinal.remove(modifier.getIdentifier());
                multiFinalByGroup.get(old.getGroupIdentifier()).remove(old);
                if(multiFinalByGroup.get(old.getGroupIdentifier()).isEmpty()) multiFinalByGroup.remove(old.getGroupIdentifier());
            }
            multiFinal.put(modifier.getIdentifier(),modifier);
            if(!multiFinalByGroup.containsKey(modifier.getGroupIdentifier())) multiFinalByGroup.put(modifier.getGroupIdentifier(),new HashSet<>());
            multiFinalByGroup.get(modifier.getGroupIdentifier()).add(modifier);
        }
    }

    public void addModifier(ValueContainerModifier modifier){
        addModifierNoCacheUpdate(modifier);
        calculateCachedVal();
    }
    public void removeModifier(Identifier id){
        addFinal.remove(id);
        addBase.remove(id);
        if(multiFinal.containsKey(id)){
            ValueContainerModifier modifier = multiFinal.remove(id);
            Identifier group = modifier.getGroupIdentifier();
            multiFinalByGroup.get(group).remove(modifier);
        }
        if(multiBase.containsKey(id)){
            ValueContainerModifier modifier = multiBase.remove(id);
            Identifier group = modifier.getGroupIdentifier();
            multiBaseByGroup.get(group).remove(modifier);
        }
        calculateCachedVal();
    }

    public double getValue(){return cachedVal;}
    public double getBaseValue(){return base;}


    public static void encode(ByteBuf buf,ValueContainer container){
        ByteBufHelpers.encodeIdentifier(container.getIdentifier(),buf);
        buf.writeDouble(container.base);
        ByteBufHelpers.encodeCollection(container.getAllModifiers(),buf,ValueContainerModifier::encode);
    }
    public static void decode(ByteBuf buf,ValueContainer container){

        Collection<ValueContainerModifier> modifiers = ByteBufHelpers.decodeArray(buf,ValueContainerModifier::decode);
        for(ValueContainerModifier modifier : modifiers){
            container.addModifierNoCacheUpdate(modifier);
        }

        container.calculateCachedVal();
    }
    public static ValueContainer decode(ByteBuf buf){
        Identifier identifier = ByteBufHelpers.decodeIdentifier(buf);
        double base = buf.readDouble();
        ValueContainer container = new ValueContainer(identifier,base);
        decode(buf,container);
        return container;
    }
}
