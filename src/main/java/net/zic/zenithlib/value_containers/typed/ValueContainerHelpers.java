package net.zic.zenithlib.value_containers.typed;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.naming.Name;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Provides methods to create typed value containers with adder and multiplier provided
 */
public class ValueContainerHelpers {
    private static final BiFunction<Double,Double,Double> DOUBLE_MUL = (a,b)->a*b;

    private static final BiFunction<Integer,Double,Integer> INTEGER_MUL = (a,b)->  (int) (a*b);

    private static final BiFunction<Long,Double,Long> LONG_MUL = (a,b)->(long) (a*b);

    private static final BiFunction<Float,Double,Float> FLOAT_MUL = (a,b)->(float)(a*b);

    public static ValueContainer<Double> doubleValueContainer(Identifier containerId,double baseValue){

        return new ValueContainer<>(containerId,baseValue,Double::sum,DOUBLE_MUL,0d);
    }
    public static NamedValueContainer<Double> namedDoubleValueContainer(Component name, Identifier containerId, double baseValue){
        return new NamedValueContainer<>(name,containerId,baseValue,Double::sum,DOUBLE_MUL,0d);
    }


    public static ValueContainer<Integer> integerValueContainer(Identifier containerId,int baseValue){
        return new ValueContainer<>(containerId,baseValue,Integer::sum,INTEGER_MUL,0);
    }
    public static NamedValueContainer<Integer> namedIntegerValueContainer(Component name, Identifier containerId, int baseValue){
        return new NamedValueContainer<>(name,containerId,baseValue,Integer::sum,INTEGER_MUL,0);
    }


    public static ValueContainer<Long> longValueContainer(Identifier containerId,long baseValue){
        return new ValueContainer<>(containerId,baseValue,Long::sum,LONG_MUL,0L);
    }
    public static NamedValueContainer<Long> namedLongValueContainer(Component name, Identifier containerId, long baseValue){
        return new NamedValueContainer<>(name,containerId,baseValue,Long::sum,LONG_MUL,0L);
    }

    public static ValueContainer<Float> floatValueContainer(Identifier containerId,float baseValue){
        return new ValueContainer<>(containerId,baseValue,Float::sum,FLOAT_MUL,0f);
    }
    public static NamedValueContainer<Float> namedFloatValueContainer(Component name, Identifier containerId, float baseValue){
        return new NamedValueContainer<>(name,containerId,baseValue,Float::sum,FLOAT_MUL,0f);
    }
}
