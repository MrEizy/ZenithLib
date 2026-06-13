package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Starts as generated glyphs and settles into the final readable text once per session. */
public record RuneDecipherTextEffect(
        int duration,
        int delay,
        int speed,
        ScrambleRevealTextEffect.Mode mode,
        String glyphs
) implements ZenithTooltipTextEffect {
    public static final String DEFAULT_RUNE_GLYPHS = "ᚠᚢᚦᚨᚱᚲᚷᚹᛉᛏᛒᛖᛗᛟᛞᛝ✧✦◇◆";

    public static final MapCodec<RuneDecipherTextEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("duration", 950).forGetter(RuneDecipherTextEffect::duration),
                    Codec.INT.optionalFieldOf("delay", 0).forGetter(RuneDecipherTextEffect::delay),
                    Codec.INT.optionalFieldOf("speed", 45).forGetter(RuneDecipherTextEffect::speed),
                    ScrambleRevealTextEffect.Mode.CODEC.optionalFieldOf("mode", ScrambleRevealTextEffect.Mode.PREFIX).forGetter(RuneDecipherTextEffect::mode),
                    Codec.STRING.optionalFieldOf("glyphs", DEFAULT_RUNE_GLYPHS).forGetter(RuneDecipherTextEffect::glyphs)
            ).apply(instance, RuneDecipherTextEffect::new)
    );

    public RuneDecipherTextEffect {
        duration = Math.max(1, duration);
        delay = Math.max(0, delay);
        speed = Math.max(16, speed);
        mode = mode == null ? ScrambleRevealTextEffect.Mode.PREFIX : mode;
        glyphs = glyphs == null || glyphs.isBlank() ? DEFAULT_RUNE_GLYPHS : glyphs;
    }

    @Override
    public MapCodec<? extends ZenithTooltipTextEffect> codec() {
        return CODEC;
    }
}
