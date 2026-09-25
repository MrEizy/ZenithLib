package net.zic.zenithlib.value_containers.typed;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class ValueContainerCodecHelper {


    public static <T extends Number> Codec<Modifier<T>> flatModifierCodec(Codec<T> valueCodec){
        return RecordCodecBuilder.create(
                instance->instance.group(
                        Identifier.CODEC.fieldOf("id").forGetter(Modifier::id),
                        Codec.INT.optionalFieldOf("operation_group",0).forGetter(Modifier::operationGroup),
                        valueCodec.fieldOf("value").forGetter(Modifier<T>::value)
                ).apply(instance, Modifier::flat));
    }
    public static Codec<Modifier<Double>> multiplierModifierCodec(){
        return RecordCodecBuilder.create(
                instance->instance.group(
                        Identifier.CODEC.fieldOf("id").forGetter(Modifier::id),
                        Identifier.CODEC.optionalFieldOf("group",Identifier.parse("none")).forGetter(Modifier::group),
                        Codec.INT.optionalFieldOf("operation_group",0).forGetter(Modifier::operationGroup),
                        Codec.DOUBLE.fieldOf("value").forGetter(Modifier<Double>::value)
                ).apply(instance, Modifier::multiplier));
    }

    public static <T extends Number> Codec<ModifierHolder<T>> modifierHolderCodec(Codec<T> valueCodec){

        return RecordCodecBuilder.create(
                instance->instance.group(
                    flatModifierCodec(valueCodec).listOf().fieldOf("flat").forGetter(ModifierHolder::flat),
                    multiplierModifierCodec().listOf().fieldOf("multiplier").forGetter(ModifierHolder::multiplier)
                ).apply(instance,ModifierHolder<T>::new)
        );

    }
    public static <T extends Number> Codec<Map<Identifier,ModifierHolder<T>>> containersCodec(Codec<T> valueCodec){

        return Codec.unboundedMap(Identifier.CODEC,modifierHolderCodec(valueCodec));

    }
}
