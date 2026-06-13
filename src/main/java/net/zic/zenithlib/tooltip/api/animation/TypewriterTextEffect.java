package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Reveals glyphs from left to right once during a tooltip hover or page-entry session. */
public record TypewriterTextEffect(
        int duration,
        int delay,
        boolean revealWhitespace,
        boolean cursor
) implements ZenithTooltipTextEffect {
    public static final MapCodec<TypewriterTextEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("duration", 700).forGetter(TypewriterTextEffect::duration),
                    Codec.INT.optionalFieldOf("delay", 0).forGetter(TypewriterTextEffect::delay),
                    Codec.BOOL.optionalFieldOf("reveal_whitespace", true).forGetter(TypewriterTextEffect::revealWhitespace),
                    Codec.BOOL.optionalFieldOf("cursor", false).forGetter(TypewriterTextEffect::cursor)
            ).apply(instance, TypewriterTextEffect::new)
    );

    public TypewriterTextEffect {
        duration = Math.max(1, duration);
        delay = Math.max(0, delay);
    }

    @Override
    public MapCodec<? extends ZenithTooltipTextEffect> codec() {
        return CODEC;
    }
}
