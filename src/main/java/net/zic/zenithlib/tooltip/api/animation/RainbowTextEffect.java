package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;

/**
 * Animates a colour gradient across the glyphs of a tooltip text element.
 */
public record RainbowTextEffect(
        int period,
        float spread,
        float saturation,
        float brightness,
        Mode mode,
        float minHue,
        float maxHue,
        boolean reverse
) implements ZenithTooltipTextEffect {
    public static final MapCodec<RainbowTextEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("period", 2400).forGetter(RainbowTextEffect::period),
                    Codec.FLOAT.optionalFieldOf("spread", 0.045F).forGetter(RainbowTextEffect::spread),
                    Codec.FLOAT.optionalFieldOf("saturation", 0.9F).forGetter(RainbowTextEffect::saturation),
                    Codec.FLOAT.optionalFieldOf("brightness", 1.0F).forGetter(RainbowTextEffect::brightness),
                    Mode.CODEC.optionalFieldOf("mode", Mode.SPECTRUM).forGetter(RainbowTextEffect::mode),
                    Codec.FLOAT.optionalFieldOf("min_hue", 0.0F).forGetter(RainbowTextEffect::minHue),
                    Codec.FLOAT.optionalFieldOf("max_hue", 1.0F).forGetter(RainbowTextEffect::maxHue),
                    Codec.BOOL.optionalFieldOf("reverse", false).forGetter(RainbowTextEffect::reverse)
            ).apply(instance, RainbowTextEffect::new)
    );

    public RainbowTextEffect {
        period = Math.max(100, period);
        spread = Math.max(-1.0F, Math.min(1.0F, spread));
        saturation = clamp01(saturation);
        brightness = clamp01(brightness);
        mode = mode == null ? Mode.SPECTRUM : mode;
        minHue = clamp01(minHue);
        maxHue = clamp01(maxHue);

        if (maxHue < minHue) {
            float swap = minHue;
            minHue = maxHue;
            maxHue = swap;
        }
    }

    @Override
    public MapCodec<? extends ZenithTooltipTextEffect> codec() {
        return CODEC;
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    public enum Mode {
        SPECTRUM("spectrum"),
        PING_PONG("ping_pong");

        public static final Codec<Mode> CODEC = Codec.STRING.comapFlatMap(Mode::decode, Mode::serializedName);

        private final String serializedName;

        Mode(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        private static DataResult<Mode> decode(String raw) {
            String normalized = raw.toLowerCase(Locale.ROOT);
            for (Mode mode : values()) {
                if (mode.serializedName.equals(normalized)) {
                    return DataResult.success(mode);
                }
            }
            return DataResult.error(() -> "Unsupported rainbow text mode: " + raw);
        }
    }
}
