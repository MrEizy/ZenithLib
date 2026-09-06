package net.zic.zenithlib.value_containers.typed;


import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.function.BiFunction;

/*
    TODO
        i will prob make it so we store a map of Identifier->Modifier
        and regardless of operationGroup, group or Modifier type you can only have one
        with such an id?
        so i think we will throw an error?

 */
public class ValueContainer<T extends Number>{
    private T baseValue;
    private T calculatedValue;

    private final Identifier containerId;
    private final BiFunction<T,T,T> adder;
    private final BiFunction<T,Double,T> multiplier;
    private final Map<Integer,OperationGroup<T>> operationGroups = new HashMap<>();
    private final Map<Identifier,Modifier> modifiers = new HashMap<>();

    public ValueContainer(Identifier containerId,T baseValue,BiFunction<T, T, T> adder, BiFunction<T, Double, T> multiplier) {
        this.containerId = containerId;
        this.baseValue = baseValue;
        this.adder = adder;
        this.multiplier = multiplier;
    }

    //TODO consider throwing error if they try to replace an existing modifier?
    private static class OperationGroup<T extends Number>{
        private final Map<Identifier,BonusModifier<T>> bonusModifiers = new HashMap<>();

        private final Map<Identifier,MultiplierModifier> multiplierModifiers = new HashMap<>();
        private final Map<Identifier,Set<MultiplierModifier>> groupedMultiplierModifiers =  new HashMap<>();

        public void addBonusModifier(BonusModifier<T> modifier){
            bonusModifiers.put(modifier.getId(),modifier);
        }
        public void addMultiplierModifier(MultiplierModifier modifier){
            multiplierModifiers.put(modifier.getId(),modifier);
            groupedMultiplierModifiers.computeIfAbsent(modifier.getGroup(),key->new HashSet<>()).add(modifier);
        }
        public void removeModifier(Identifier id){
            if(bonusModifiers.containsKey(id)) bonusModifiers.remove(id);
            else if(multiplierModifiers.containsKey(id)){
                Modifier modifier = multiplierModifiers.remove(id);
                groupedMultiplierModifiers.computeIfPresent(modifier.getGroup(),(key,val)->{
                    val.remove(modifier);
                    return val.isEmpty() ? null : val;
                });
            }
        }
        public T getBonus(BiFunction<T,T,T> adder){
            if(bonusModifiers.isEmpty()) return null;
            List<BonusModifier<T>> raw = bonusModifiers.values().stream().toList();
            T sum = raw.getFirst().val();
            for(int i =1;i<bonusModifiers.size();i++){
                sum = adder.apply(sum,raw.get(i).val());
            }
            return sum;
        }
        public Collection<Identifier> getMultiplierGroups(){
            return groupedMultiplierModifiers.keySet();
        }
        public double getGroupMultiplier(Identifier group){
            Set<MultiplierModifier> modifiers = groupedMultiplierModifiers.getOrDefault(group,Set.of());
            double multiplier = 1;
            for(MultiplierModifier modifier : modifiers){
                multiplier += modifier.val();
            }
            return Math.max(multiplier,0);
        }
        public double getMultiplier(){
            double multiplier = 1;
            for(Identifier group : getMultiplierGroups()) multiplier *= getGroupMultiplier(group);
            return multiplier;
        }
    }

    public Modifier removeModifier(Identifier id){
        if(!modifiers.containsKey(id)) return null;
        Modifier modifier = modifiers.remove(id);
        operationGroups.get(modifier.getOperationGroup()).removeModifier(id);
        return modifier;
    }

    public void addBonusModifier(BonusModifier<T> modifier) {
        addBonusModifier(modifier,true);
    }
    public void addBonusModifier(BonusModifier<T> modifier,boolean recalculate){
        if(modifiers.containsKey(modifier.getId())) return;

        operationGroups.computeIfAbsent(modifier.operationGroup(),key->new OperationGroup<>()).addBonusModifier(modifier);
        if(recalculate) calculateValue();
    }
    public void addMultiplierModifier(MultiplierModifier modifier){
        addMultiplierModifier(modifier,true);
    }
    public void addMultiplierModifier(MultiplierModifier modifier,boolean recalculate){
        if(modifiers.containsKey(modifier.getId())) return;
        operationGroups.computeIfAbsent(modifier.operationGroup(),key->new OperationGroup<>()).addMultiplierModifier(modifier);
        if(recalculate) calculateValue();
    }

    public void calculateValue(){
        //TODO consider just using a TreeMap
        List<Integer> groups = operationGroups.keySet().stream().sorted().toList();

        calculatedValue = baseValue;

        for(Integer operationGroup : groups){
            OperationGroup<T> group = operationGroups.get(operationGroup);

            double modifier = group.getMultiplier();

            calculatedValue = multiplier.apply(calculatedValue,modifier);

            T bonus = group.getBonus(adder);
            if(bonus != null){
                calculatedValue = adder.apply(calculatedValue,bonus);
            }

        }

    }

    public T getValue(){
        return calculatedValue;
    }
    public T getBaseValue(){
        return baseValue;
    }

    public Identifier getContainerId() {
        return containerId;
    }
}
