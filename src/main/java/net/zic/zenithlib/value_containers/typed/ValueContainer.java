package net.zic.zenithlib.value_containers.typed;


import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.network.Decoder;
import net.zic.zenithlib.network.Encoder;
import net.zic.zenithlib.util.Processable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/*
    TODO
        i will prob make it so we store a map of Identifier->Modifier
        and regardless of operationGroup, group or Modifier type you can only have one
        with such an id?
        so i think we will throw an error?

 */
public class ValueContainer<T extends Number> extends Processable {
    T calculatedBaseValue;
    T calculatedValue;

    private final Identifier containerId;
    private final BiFunction<T,T,T> adder;
    private final BiFunction<T,Double,T> multiplier;
    private final Encoder<T> encoder;
    private final Decoder<T> decoder;
    private final T defaultValue;
    private final Map<Integer,OperationGroup<T>> operationGroups = new HashMap<>();
    private final Map<Identifier, Modifier<T>> flatModifiers = new HashMap<>();
    private final Map<Identifier, Modifier<Double>> multiplierModifiers = new HashMap<>();

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
        setOnResolved(this::calculateValue);
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
        setOnResolved(this::calculateValue);
    }

    private static class OperationGroup<T extends Number>{
        private final T defaultValue;
        private final Map<Identifier, Modifier<T>> flatModifiers = new HashMap<>();

        private final Map<Identifier, Modifier<Double>> multiplierModifiers = new HashMap<>();
        private final Map<Identifier,Set<Modifier<Double>>> groupedMultiplierModifiers =  new HashMap<>();

        private OperationGroup(T defaultValue) {
            this.defaultValue = defaultValue;
        }

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
        public T getFlat(BiFunction<T,T,T> adder){
            return getFlat(adder,(v)->true);
        }
        public T getFlat(BiFunction<T,T,T> adder,Predicate<Modifier<T>> predicate){
            if(flatModifiers.isEmpty()) return null;
            List<Modifier<T>> raw = flatModifiers.values().stream().toList();
            T sum = defaultValue;
            for(int i =0;i<flatModifiers.size();i++){
                if(!predicate.test(raw.get(i))) continue;

                sum = adder.apply(sum,raw.get(i).value());
            }
            return sum;
        }

        public Collection<Identifier> getMultiplierGroups(){
            return groupedMultiplierModifiers.keySet();
        }

        public double getGroupMultiplier(Identifier group,Predicate<Modifier<Double>> predicate){
            Set<Modifier<Double>> modifiers = groupedMultiplierModifiers.getOrDefault(group,Set.of());
            double multiplier = 1;
            for(Modifier<Double> modifier : modifiers){
                if(!predicate.test(modifier)) continue;
                multiplier += modifier.value();
            }
            return Math.max(multiplier,0);
        }
        public double getMultiplier(){
            return getMultiplier((v)->true);
        }
        public double getMultiplier(Predicate<Modifier<Double>> predicate){
            double multiplier = 1;
            for(Identifier group : getMultiplierGroups()) multiplier *= getGroupMultiplier(group,predicate);
            return Math.max(multiplier,0);
        }
    }


    public Modifier<T> removeFlatModifier(Identifier id){
        if(!flatModifiers.containsKey(id)) return null;
        Modifier<T> modifier = flatModifiers.remove(id);

        operationGroups.get(modifier.operationGroup()).removeModifier(id);
        startAndResolveProcess();
        return modifier;
    }
    public Modifier<Double> removeMultiplierModifier(Identifier id){
        if(!multiplierModifiers.containsKey(id)) return null;
        Modifier<Double> modifier = multiplierModifiers.remove(id);

        operationGroups.get(modifier.operationGroup()).removeModifier(id);
        startAndResolveProcess();
        return modifier;
    }
    public Modifier<?> removeModifier(Identifier id){
        if(!flatModifiers.containsKey(id) && ! multiplierModifiers.containsKey(id)) return null;
        Modifier<?> modifier = flatModifiers.remove(id);
        if(modifier == null) modifier = multiplierModifiers.remove(id);

        operationGroups.get(modifier.operationGroup()).removeModifier(id);
        startAndResolveProcess();
        return modifier;
    }

    public void addFlatModifier(Modifier<T> modifier){
        if(!modifier.type().equals("flat")) return;
        if(flatModifiers.containsKey(modifier.id()) || multiplierModifiers.containsKey(modifier.id())) return;

        flatModifiers.put(modifier.id(),modifier);

        operationGroups.computeIfAbsent(modifier.operationGroup(),key->new OperationGroup<>(defaultValue)).addFlatModifier(modifier);
        startAndResolveProcess();
    }

    public void addMultiplierModifier(Modifier<Double> modifier){
        if(!modifier.type().equals("multiplier")) return;
        if(flatModifiers.containsKey(modifier.id()) || multiplierModifiers.containsKey(modifier.id())) return;

        multiplierModifiers.put(modifier.id(),modifier);

        operationGroups.computeIfAbsent(modifier.operationGroup(),key->new OperationGroup<>(defaultValue)).addMultiplierModifier(modifier);
        startAndResolveProcess();
    }

    //allows you to calculate a value while excluding specific modifiers
    public Pair<T,T> calculateValue(Predicate<Modifier<T>> flatPredicate, Predicate<Modifier<Double>> multiplierPredicate){
        List<Integer> groups = operationGroups.keySet().stream().sorted().toList();

        T value = defaultValue;
        T baseValue = defaultValue;
        for(Integer operationGroup : groups){
            OperationGroup<T> group = operationGroups.get(operationGroup);

            T flat = group.getFlat(adder,flatPredicate);
            if(flat != null){
                if(operationGroup == 0) baseValue = flat;
                value = adder.apply(value,flat);
            }else if (operationGroup == 0) baseValue = defaultValue;

            double modifier = group.getMultiplier(multiplierPredicate);

            value = multiplier.apply(value,modifier);

        }


        return new Pair<>(value,baseValue);
    }

    public void calculateValue(){
        Pair<T,T> result = calculateValue((v)->true,(v)->true);
        calculatedValue = result.getFirst();
        calculatedBaseValue = result.getSecond();

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

    public boolean hasModifier(Identifier modifier){
        return flatModifiers.containsKey(modifier) || multiplierModifiers.containsKey(modifier);
    }

    public Modifier<T> getFlatModifier(Identifier modifier){
        return flatModifiers.get(modifier);
    }
    public Modifier<Double> getMultiplierModifier(Identifier modifier){
        return multiplierModifiers.get(modifier);
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
    public static <T extends Number,S extends ValueContainer<T>> S decode(Function<Identifier,S> provider, ByteBuf buf, Codec<T> valueCodec){
        StreamCodec<ByteBuf,ModifierHolder<T>> STREAM_CODEC = ByteBufCodecs.fromCodec(
                ValueContainerCodecHelper.modifierHolderCodec(valueCodec)
        );
        Identifier identifier = ByteBufHelpers.decodeIdentifier(buf);
        S container = provider.apply(identifier);
        container.calculatedBaseValue = container.getDecoder().decode(buf);
        container.calculatedValue = container.getDecoder().decode(buf);
        int size = buf.readInt();
        //I do this to prevent calculate being called on the client
        //never needs this directly, if you want to get a different view use calculate with predicate
        //to get a value without updating
        container.setOnResolved(Processable.EMPTY_RUNNABLE);
        for(int i =0;i<size;i++){
            int operationGroup = buf.readInt();
            ModifierHolder<T> holder = STREAM_CODEC.decode(buf);

            for(Modifier<T> flat : holder.flat()) container.addFlatModifier(flat);
            for(Modifier<Double> multiplier : holder.multiplier()) container.addMultiplierModifier(multiplier);
        }
        return container;
    }
    public static <T extends Number> ValueContainer<T> normalDecode(Function<Identifier,ValueContainer<T>> containerProvider, ByteBuf buf, Codec<T> valueCodec){
        return decode(containerProvider,buf,valueCodec);

    }


    /**
     *
     * @param provider takes in a double(base value) and expects a value container
     * @param containers the value containers to combine
     * @return a new value container with all the modifiers of other containers combined
     * @param <T> the data type we store in the container
     * @param <S> the container type we want from the merge
     */
    public static <T extends Number,S extends ValueContainer<T>> S from(Function<T,S> provider, List<ValueContainer<T>> containers) {
        if(containers.isEmpty()) return null;
        T baseValue = containers.getFirst().defaultValue;
        BiFunction<T,T,T> adder = containers.getFirst().adder;

        List<Modifier<T>> flatModifiers = new ArrayList<>();
        List<Modifier<Double>> multiplierModifiers = new ArrayList<>();
        for(ValueContainer<T> container : containers){
            baseValue = adder.apply(baseValue,container.getBaseValue());
            flatModifiers.addAll(container.getFlatModifiers());
            multiplierModifiers.addAll(container.getMultiplierModifiers());
        }

        S container = provider.apply(baseValue);
        container.startProcess("merge_process");
        for (Modifier<T> flatModifier : flatModifiers) container.addFlatModifier(flatModifier);
        for(Modifier<Double> multiplierModifier:multiplierModifiers) container.addMultiplierModifier(multiplierModifier);


        container.resolveProcess("merge_process");
        return container;
    }

}
