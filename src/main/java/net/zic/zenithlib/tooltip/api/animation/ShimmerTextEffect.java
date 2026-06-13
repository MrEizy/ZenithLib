package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Sends a narrow sweep across stable tooltip text. */
public record ShimmerTextEffect(
        int period,
        float width,
        float brightness,
        boolean reverse
) implements ZenithTooltipTextEffect {
    public static final MapCodec<ShimmerTextEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("period", 2200).forGetter(ShimmerTextEffect::period),
                    Codec.FLOAT.optionalFieldOf("width", 0.18F).forGetter(ShimmerTextEffect::width),
                    Codec.FLOAT.optionalFieldOf("brightness", 0.55F).forGetter(ShimmerTextEffect::brightness),
                    Codec.BOOL.optionalFieldOf("reverse", false).forGetter(ShimmerTextEffect::reverse)
            ).apply(instance, ShimmerTextEffect::new)
    );

    public ShimmerTextEffect {
        period = Math.max(200, period);
        width = Math.max(0.02F, Math.min(0.75F, width));
        brightness = Math.max(0.0F, Math.min(1.0F, brightness));
    }

    @Override
    public MapCodec<? extends ZenithTooltipTextEffect> codec() {
        return CODEC;
    }
}
