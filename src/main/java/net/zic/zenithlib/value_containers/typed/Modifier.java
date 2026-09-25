package net.zic.zenithlib.value_containers.typed;

import net.minecraft.resources.Identifier;

public record Modifier<T extends Number>(String type, Identifier id, Identifier group, int operationGroup, T value){


    public static Modifier<Double> multiplier(Identifier id, double value){
        return multiplier(id,Identifier.parse("none"),0,value);
    }
    public static Modifier<Double> multiplier(Identifier id, Identifier group, double value){
        return multiplier(id,group,0,value);
    }
    public static Modifier<Double> multiplier(Identifier id, int operationGroup, double value){
        return multiplier(id,Identifier.parse("none"),operationGroup,value);
    }
    public static Modifier<Double> multiplier(Identifier id, Identifier group, int operationGroup, double value){
        return new Modifier<>("multiplier",id,group,operationGroup,value);
    }

    public static <T extends Number> Modifier<T> flat(Identifier id, int operationGroup, T value){
        return new Modifier<>("flat",id,Identifier.parse("none"),operationGroup,value);
    }
    public static <T extends Number> Modifier<T> base(Identifier id, T value){
        return flat(id,0,value);
    }



}
