package net.zic.zenithlib.configuration.attribute_scaling;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;

import java.util.Map;

public record AttributeScaling(Map<Stat, ModifierHolder<Double>> scaling) {
    public static final Codec<AttributeScaling> CODEC = Codec.unboundedMap(
                            ZenithRegistries.STAT_REGISTRY.byNameCodec(),
                            ValueContainerCodecHelper.modifierHolderCodec(Codec.DOUBLE)
                    ).xmap(AttributeScaling::new, AttributeScaling::scaling);
}
