package net.zic.zenithlib.value_containers.typed;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.custom_attributes.ZenithAttribute;
import net.zic.zenithlib.network.Decoder;
import net.zic.zenithlib.network.Encoder;

import java.util.function.BiFunction;
import java.util.function.Function;

public class RangedValueContainer<T extends Number> extends ValueContainer<T> {

    private T minValue;
    private T maxValue;
    private final BiFunction<T,T,Integer> comparator;
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
    public void setRange(T min,T max){
        this.minValue = min;
        this.maxValue = max;
        if(comparator.apply(minValue,maxValue) > 0){
            this.minValue = max;
            this.maxValue = min;
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
    public static <T extends Number,S extends RangedValueContainer<T>> S decodeRanged(Function<Identifier,S> provider, ByteBuf buf, Codec<T> valueCodec) {
        StreamCodec<ByteBuf,T> STREAM_CODEC = ByteBufCodecs.fromCodec(valueCodec);
        T min = STREAM_CODEC.decode(buf);
        T max = STREAM_CODEC.decode(buf);
        S container = ValueContainer.decode(provider,buf,valueCodec);
        container.setRange(min,max);
        return container;
    }
    public static <T extends Number> void encodeRanged(RangedValueContainer<T> container, ByteBuf buf,Codec<T> valueCodec){
        StreamCodec<ByteBuf,T> STREAM_CODEC = ByteBufCodecs.fromCodec(valueCodec);

        STREAM_CODEC.encode(buf, container.getMinValue());
        STREAM_CODEC.encode(buf, container.getMaxValue());

        ValueContainer.encode(container,buf,valueCodec);

    }

}
