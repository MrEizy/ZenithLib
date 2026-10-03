package net.zic.zenithlib.value_containers.typed;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.network.Decoder;
import net.zic.zenithlib.network.Encoder;

import java.util.function.BiFunction;

public class RangedValueContainer<T extends Number> extends ValueContainer<T> {

    private T minValue;
    private T maxValue;
    private BiFunction<T,T,Integer> comparator;
    public RangedValueContainer(
            Identifier containerId,
            BiFunction<T, T, T> adder,
            BiFunction<T, Double, T> multiplier,
            BiFunction<T,T,Integer> comparator,
            Encoder<T> encoder,
            Decoder<T> decoder,
            T defaultValue,
            T minValue,
            T maxValue
            ) {
        super(containerId, adder, multiplier, encoder, decoder, defaultValue);
        this.comparator = comparator;
        this.minValue = minValue;
        this.maxValue = maxValue;
        if(comparator.apply(minValue,maxValue) > 0){
            this.minValue = maxValue;
            this.maxValue = minValue;
        }
    }
    public void setMaxValue(T maxValue){
        this.maxValue = maxValue;
        if(comparator.apply(minValue,maxValue) > 0){
            this.maxValue = minValue;
            this.minValue = maxValue;
        }
    }
    public void setMinValue(T minValue){
        this.minValue = minValue;
        if(comparator.apply(minValue,maxValue) > 0){
            this.minValue = maxValue;
            this.maxValue = minValue;
        }
    }
    public T getMinValue(){
        return minValue;
    }
    public T getMaxValue(){
        return maxValue;
    }

    public T clamp(T value){
        if(comparator.apply(value,getMinValue()) < 0) return minValue;
        if(comparator.apply(value,getMinValue()) > 0) return maxValue;
        return value;
    }


    @Override
    public T getValue() {
        return clamp(super.getValue());
    }

    @Override
    public T getBaseValue() {
        return clamp(super.getBaseValue());
    }

    //TODO setup encoding and decoding
}
