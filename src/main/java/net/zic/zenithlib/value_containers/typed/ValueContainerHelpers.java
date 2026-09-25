package net.zic.zenithlib.value_containers.typed;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.network.Decoder;
import net.zic.zenithlib.network.Encoder;

import javax.naming.Name;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Provides methods to create typed value containers with adder and multiplier provided
 */
public class ValueContainerHelpers {
    public static final BiFunction<Double,Double,Double> DOUBLE_MUL = (a,b)->a*b;

    public static final BiFunction<Integer,Double,Integer> INTEGER_MUL = (a,b)->  (int) (a*b);

    public static final BiFunction<Long,Double,Long> LONG_MUL = (a,b)->(long) (a*b);

    public static final BiFunction<Float,Double,Float> FLOAT_MUL = (a,b)->(float)(a*b);

    public static final Encoder<Integer> INTEGER_ENCODER = (val, buf) -> buf.writeInt(val);
    public static final Decoder<Integer> INTEGER_DECODER = ByteBuf::readInt;
    public static final Encoder<Double>  DOUBLE_ENCODER = (val,buf)->buf.writeDouble(val);
    public static final Decoder<Double>  DOUBLE_DECODER = ByteBuf::readDouble;
    public static final Encoder<Long> LONG_ENCODER = (val,buf)->buf.writeLong(val);
    public static final Decoder<Long> LONG_DECODER = ByteBuf::readLong;
    public static final Encoder<Float> FLOAT_ENCODER = (val, buf) -> buf.writeFloat(val);
    public static final Decoder<Float> FLOAT_DECODER = ByteBuf::readFloat;

    public static ValueContainer<Double> doubleValueContainer(Identifier containerId){

        return new ValueContainer<>(containerId,Double::sum,DOUBLE_MUL,DOUBLE_ENCODER,DOUBLE_DECODER,0d);
    }
    public static ValueContainer<Double> doubleValueContainer(Identifier containerId,double baseValue){

        return new ValueContainer<>(containerId,baseValue,Double::sum,DOUBLE_MUL,DOUBLE_ENCODER,DOUBLE_DECODER,0d);
    }

    public static NamedValueContainer<Double> namedDoubleValueContainer(Component name, Identifier containerId, double baseValue){
        return new NamedValueContainer<>(name,containerId,baseValue,Double::sum,DOUBLE_MUL,DOUBLE_ENCODER,DOUBLE_DECODER,0d);
    }

    public static ValueContainer<Integer> integerValueContainer(Identifier containerId){
        return new ValueContainer<>(containerId,Integer::sum,INTEGER_MUL,INTEGER_ENCODER,INTEGER_DECODER,0);
    }
    public static ValueContainer<Integer> integerValueContainer(Identifier containerId,int baseValue){
        return new ValueContainer<>(containerId,baseValue,Integer::sum,INTEGER_MUL,INTEGER_ENCODER,INTEGER_DECODER,0);
    }
    public static NamedValueContainer<Integer> namedIntegerValueContainer(Component name, Identifier containerId, int baseValue){
        return new NamedValueContainer<>(name,containerId,baseValue,Integer::sum,INTEGER_MUL,INTEGER_ENCODER,INTEGER_DECODER,0);
    }

    public static ValueContainer<Long> longValueContainer(Identifier containerId){
        return new ValueContainer<>(containerId,Long::sum,LONG_MUL,LONG_ENCODER,LONG_DECODER,0L);
    }
    public static ValueContainer<Long> longValueContainer(Identifier containerId,long baseValue){
        return new ValueContainer<>(containerId,baseValue,Long::sum,LONG_MUL,LONG_ENCODER,LONG_DECODER,0L);
    }
    public static NamedValueContainer<Long> namedLongValueContainer(Component name, Identifier containerId, long baseValue){
        return new NamedValueContainer<>(name,containerId,baseValue,Long::sum,LONG_MUL,LONG_ENCODER,LONG_DECODER,0L);
    }

    public static ValueContainer<Float> floatValueContainer(Identifier containerId){
        return new ValueContainer<>(containerId,Float::sum,FLOAT_MUL,FLOAT_ENCODER,FLOAT_DECODER,0f);
    }
    public static ValueContainer<Float> floatValueContainer(Identifier containerId,float baseValue){
        return new ValueContainer<>(containerId,baseValue,Float::sum,FLOAT_MUL,FLOAT_ENCODER,FLOAT_DECODER,0f);
    }
    public static NamedValueContainer<Float> namedFloatValueContainer(Component name, Identifier containerId, float baseValue){
        return new NamedValueContainer<>(name,containerId,baseValue,Float::sum,FLOAT_MUL,FLOAT_ENCODER,FLOAT_DECODER,0f);
    }
}
