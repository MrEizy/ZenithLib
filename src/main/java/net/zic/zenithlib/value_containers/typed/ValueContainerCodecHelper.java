package net.zic.zenithlib.value_containers.typed;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.fish.Cod;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValueContainerCodecHelper {


    public static <T extends Number> Codec<Map<Identifier,T>> containerBaseValuesCodec(Codec<T> valCodec){
        return Codec.unboundedMap(Identifier.CODEC,valCodec);
    }

    public static <T extends Number> Codec<BonusModifier<T>> bonusModifierCodec(Codec<T> valCodec){
        return RecordCodecBuilder.create(
                instance->instance.group(
                        Codec.STRING.validate(
                                val-> val.equals("bonus") ? DataResult.success(val) : DataResult.error(()->"expected multiplier")
                        ).fieldOf("type").forGetter((val)->"bonus"),
                        Identifier.CODEC.fieldOf("id").forGetter(BonusModifier::id),
                        Codec.INT.optionalFieldOf("operation_group",0).forGetter(BonusModifier::operationGroup),
                        valCodec.fieldOf("value").forGetter(BonusModifier<T>::val)
                ).apply(instance,BonusModifier::new)
        );
    }
    public static Codec<MultiplierModifier> multiplierModifierCodec(){
        return RecordCodecBuilder.create(
                instance->instance.group(
                        Codec.STRING.validate(
                                val-> val.equals("multiplier") ? DataResult.success(val) : DataResult.error(()->"expected multiplier")
                        ).fieldOf("type").forGetter((val)->"multiplier"),
                        Identifier.CODEC.fieldOf("id").forGetter(MultiplierModifier::id),
                        Identifier.CODEC.optionalFieldOf("group",Identifier.parse("none")).forGetter(MultiplierModifier::group),
                        Codec.INT.optionalFieldOf("operation_group",0).forGetter(MultiplierModifier::operationGroup),
                        Codec.DOUBLE.fieldOf("value").forGetter(MultiplierModifier::val)
                ).apply(instance,MultiplierModifier::new)
        );
    }


    public static <T extends Number> Codec<Either<BonusModifier<T>,MultiplierModifier>> modifierCodec(Codec<T> valCodec){
        return Codec.either(bonusModifierCodec(valCodec),multiplierModifierCodec());
    }

    public static <T extends Number> Codec<Map<Identifier,List<Either<BonusModifier<T>,MultiplierModifier>>>> containerModifiersCodec(Codec<T> valCodec){

        return Codec.unboundedMap(Identifier.CODEC,modifierCodec(valCodec).listOf());
    }
}
