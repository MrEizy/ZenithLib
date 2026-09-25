package net.zic.zenithlib.value_containers.typed;


import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.network.Decoder;
import net.zic.zenithlib.network.Encoder;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/*
    TODO
        i will prob make it so we store a map of Identifier->Modifier
        and regardless of operationGroup, group or Modifier type you can only have one
        with such an id?
        so i think we will throw an error?

 */
public class ValueContainer<T extends Number>{
    private T calculatedBaseValue;
    private T calculatedValue;

    private final Identifier containerId;
    private final BiFunction<T,T,T> adder;
    private final BiFunction<T,Double,T> multiplier;
    private final Encoder<T> encoder;
    private final Decoder<T> decoder;
    private final T defaultValue;
    private final Map<Integer,OperationGroup<T>> operationGroups = new HashMap<>();
    private final Map<Identifier, Modifier<?>> modifiers = new HashMap<>();

    public ValueContainer(Identifier containerId, T baseValue, BiFunction<T, T, T> adder, BiFunction<T, Double, T> multiplier, Encoder<T> encoder, Decoder<T> decoder, T defaultValue) {
        this.containerId = containerId;
        this.adder = adder;
        this.multiplier = multiplier;
        this.encoder = encoder;
        this.decoder = decoder;
        this.defaultValue = defaultValue;
        addFlatModifier(Modifier.flat(
                Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID,"bases"),
                0,
                baseValue
        ));
    }
    public ValueContainer(Identifier containerId, BiFunction<T, T, T> adder, BiFunction<T, Double, T> multiplier, Encoder<T> encoder, Decoder<T> decoder, T defaultValue) {
        this.containerId = containerId;
        this.adder = adder;
        this.multiplier = multiplier;
        this.encoder = encoder;
        this.decoder = decoder;
        this.defaultValue = defaultValue;
        this.calculatedValue = defaultValue;
        this.calculatedBaseValue = defaultValue;
    }
    //TODO consider throwing error if they try to replace an existing modifier?
    private static class OperationGroup<T extends Number>{
        private final Map<Identifier, Modifier<T>> flatModifiers = new HashMap<>();

        private final Map<Identifier, Modifier<Double>> multiplierModifiers = new HashMap<>();
        private final Map<Identifier,Set<Modifier<Double>>> groupedMultiplierModifiers =  new HashMap<>();

        public void addFlatModifier(Modifier<T> modifier){
            flatModifiers.put(modifier.id(),modifier);
        }
        public void addMultiplierModifier(Modifier<Double> modifier){
            multiplierModifiers.put(modifier.id(),modifier);
            groupedMultiplierModifiers.computeIfAbsent(modifier.group(),key->new HashSet<>()).add(modifier);
        }
        public void removeModifier(Identifier id){
            if(flatModifiers.containsKey(id)) flatModifiers.remove(id);
            else if(multiplierModifiers.containsKey(id)){
                Modifier<Double> modifier = multiplierModifiers.remove(id);
                groupedMultiplierModifiers.computeIfPresent(modifier.group(),(key,val)->{
                    val.remove(modifier);
                    return val.isEmpty() ? null : val;
                });
            }
        }
        public T getBonus(BiFunction<T,T,T> adder){
            if(flatModifiers.isEmpty()) return null;
            List<Modifier<T>> raw = flatModifiers.values().stream().toList();
            T sum = raw.getFirst().value();
            for(int i =1;i<flatModifiers.size();i++){
                sum = adder.apply(sum,raw.get(i).value());
            }
            return sum;
        }
        public Collection<Identifier> getMultiplierGroups(){
            return groupedMultiplierModifiers.keySet();
        }
        public double getGroupMultiplier(Identifier group){
            Set<Modifier<Double>> modifiers = groupedMultiplierModifiers.getOrDefault(group,Set.of());
            double multiplier = 1;
            for(Modifier<Double> modifier : modifiers){
                multiplier += modifier.value();
            }
            return Math.max(multiplier,0);
        }
        public double getMultiplier(){
            double multiplier = 1;
            for(Identifier group : getMultiplierGroups()) multiplier *= getGroupMultiplier(group);
            return multiplier;
        }
    }

    public Modifier<?> removeModifier(Identifier id){
        if(!modifiers.containsKey(id)) return null;
        Modifier<?> modifier = modifiers.remove(id);
        operationGroups.get(modifier.operationGroup()).removeModifier(id);
        return modifier;
    }

    public void addFlatModifier(Modifier<T> modifier) {
        addFlatModifier(modifier,true);
    }
    public void addFlatModifier(Modifier<T> modifier, boolean recalculate){
        if(!modifier.type().equals("flat")) return;
        if(modifiers.containsKey(modifier.id())) return;
        modifiers.put(modifier.id(),modifier);
        operationGroups.computeIfAbsent(modifier.operationGroup(),key->new OperationGroup<>()).addFlatModifier(modifier);
        if(recalculate) calculateValue();
    }
    public void addMultiplierModifier(Modifier<Double> modifier){
        addMultiplierModifier(modifier,true);
    }
    public void addMultiplierModifier(Modifier<Double> modifier, boolean recalculate){
        if(!modifier.type().equals("multiplier")) return;
        if(modifiers.containsKey(modifier.id())) return;
        modifiers.put(modifier.id(),modifier);
        operationGroups.computeIfAbsent(modifier.operationGroup(),key->new OperationGroup<>()).addMultiplierModifier(modifier);
        if(recalculate) calculateValue();
    }

    public void calculateValue(){
        List<Integer> groups = operationGroups.keySet().stream().sorted().toList();

        calculatedValue = defaultValue;

        for(Integer operationGroup : groups){
            OperationGroup<T> group = operationGroups.get(operationGroup);

            T bonus = group.getBonus(adder);
            if(bonus != null){
                if(operationGroup == 0) calculatedBaseValue = bonus;
                calculatedValue = adder.apply(calculatedValue,bonus);
            }else if (operationGroup == 0) calculatedBaseValue = defaultValue;

            double modifier = group.getMultiplier();

            calculatedValue = multiplier.apply(calculatedValue,modifier);



        }

    }



    public T getValue(){
        return calculatedValue;
    }
    public T getBaseValue(){
        return calculatedBaseValue;
    }

    public Identifier getContainerId() {
        return containerId;
    }

    public List<Modifier<T>> getFlatModifiers(){
        List<Modifier<T>> flatModifiers = new ArrayList<>();
        for(OperationGroup<T> operationGroup : operationGroups.values()) flatModifiers.addAll(operationGroup.flatModifiers.values());
        return flatModifiers;
    }
    public List<Modifier<Double>> getMultiplierModifiers(){
        List<Modifier<Double>> multiplierModifiers = new ArrayList<>();
        for(OperationGroup<T> operationGroup : operationGroups.values()) multiplierModifiers.addAll(operationGroup.multiplierModifiers.values());
        return multiplierModifiers;
    }


    public Encoder<T> getEncoder(){return encoder;}
    public Decoder<T> getDecoder(){return decoder;}
    public static <T extends Number> void encode(ValueContainer<T> container, ByteBuf buf,Codec<T> valueCodec){
        StreamCodec<ByteBuf,ModifierHolder<T>> STREAM_CODEC = ByteBufCodecs.fromCodec(
                ValueContainerCodecHelper.modifierHolderCodec(valueCodec)
        );

        ByteBufHelpers.encodeIdentifier(container.containerId,buf);
        container.getEncoder().encode(container.calculatedBaseValue,buf);
        container.getEncoder().encode(container.calculatedValue,buf);

        buf.writeInt(container.operationGroups.size());
        for(Integer group : container.operationGroups.keySet()){
            buf.writeInt(group);
            OperationGroup<T> operationGroup = container.operationGroups.get(group);
            ModifierHolder<T> holder = new ModifierHolder<>(List.copyOf(operationGroup.flatModifiers.values()),List.copyOf(operationGroup.multiplierModifiers.values()));
            STREAM_CODEC.encode(buf,holder);
        }
    }
    public static <T extends Number> ValueContainer<T> decode(Function<Identifier,ValueContainer<T>> containerProvider, ByteBuf buf, Codec<T> valueCodec){
        StreamCodec<ByteBuf,ModifierHolder<T>> STREAM_CODEC = ByteBufCodecs.fromCodec(
                ValueContainerCodecHelper.modifierHolderCodec(valueCodec)
        );
        Identifier identifier = ByteBufHelpers.decodeIdentifier(buf);
        ValueContainer<T> container = containerProvider.apply(identifier);
        container.calculatedBaseValue = container.getDecoder().decode(buf);
        container.calculatedValue = container.getDecoder().decode(buf);
        int size = buf.readInt();
        for(int i =0;i<size;i++){
            int operationGroup = buf.readInt();
            ModifierHolder<T> holder = STREAM_CODEC.decode(buf);

            for(Modifier<T> flat : holder.flat()) container.addFlatModifier(flat,false);
            for(Modifier<Double> multiplier : holder.multiplier()) container.addMultiplierModifier(multiplier,false);
        }

        return container;

    }

    public static <T extends Number> ValueContainer<T> from(Identifier containerId,List<ValueContainer<T>> containers){
        if(containers.isEmpty()) return null;
        T baseValue = containers.getFirst().defaultValue;
        BiFunction<T,T,T> adder = containers.getFirst().adder;
        BiFunction<T,Double,T> multiplier = containers.getFirst().multiplier;
        Encoder<T> encoder = containers.getFirst().encoder;
        Decoder<T> decoder = containers.getFirst().decoder;

        List<Modifier<T>> flatModifiers = new ArrayList<>();
        List<Modifier<Double>> multiplierModifiers = new ArrayList<>();
        for(ValueContainer<T> container : containers){
            baseValue = adder.apply(baseValue,container.getBaseValue());
            flatModifiers.addAll(container.getFlatModifiers());
            multiplierModifiers.addAll(container.getMultiplierModifiers());
        }

        ValueContainer<T> container = new ValueContainer<>(containerId,baseValue,adder,multiplier,encoder,decoder,containers.getFirst().defaultValue);
        for (Modifier<T> flatModifier : flatModifiers) container.addFlatModifier(flatModifier,false);
        for(Modifier<Double> multiplierModifier:multiplierModifiers) container.addMultiplierModifier(multiplierModifier,false);

        container.calculateValue();
        return container;
    }
}
