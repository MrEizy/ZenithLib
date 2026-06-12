package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Combines multiple text effects while keeping the existing single {@code effect}
 * field used by tooltip JSON and Java builders.
 */
public record TextEffectStack(List<ZenithTooltipTextEffect> effects) implements ZenithTooltipTextEffect {
    public static final MapCodec<TextEffectStack> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipTextEffect.CODEC.listOf().fieldOf("effects").forGetter(TextEffectStack::effects)
            ).apply(instance, TextEffectStack::new)
    );

    public TextEffectStack {
        effects = effects == null ? List.of() : List.copyOf(effects);
        if (effects.isEmpty()) {
            throw new IllegalArgumentException("A text effect stack must contain at least one effect");
        }
    }

    @Override
    public MapCodec<? extends ZenithTooltipTextEffect> codec() {
        return CODEC;
    }
}
