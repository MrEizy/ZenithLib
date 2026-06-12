package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;

/**
 * Applies a small vertical travelling wave to the glyphs of a tooltip text element.
 */
public record WaveTextEffect(
        int period,
        float wavelength,
        int amplitude,
        Mode mode,
        boolean reverse
) implements ZenithTooltipTextEffect {
    public static final MapCodec<WaveTextEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("period", 900).forGetter(WaveTextEffect::period),
                    Codec.FLOAT.optionalFieldOf("wavelength", 7.0F).forGetter(WaveTextEffect::wavelength),
                    Codec.INT.optionalFieldOf("amplitude", 2).forGetter(WaveTextEffect::amplitude),
                    Mode.CODEC.optionalFieldOf("mode", Mode.BOUNCE).forGetter(WaveTextEffect::mode),
                    Codec.BOOL.optionalFieldOf("reverse", false).forGetter(WaveTextEffect::reverse)
            ).apply(instance, WaveTextEffect::new)
    );

    public WaveTextEffect {
        period = Math.max(100, period);
        wavelength = Math.max(1.0F, wavelength);
        amplitude = Math.max(0, Math.min(4, amplitude));
        mode = mode == null ? Mode.BOUNCE : mode;
    }

    @Override
    public MapCodec<? extends ZenithTooltipTextEffect> codec() {
        return CODEC;
    }

    public enum Mode {
        BOUNCE("bounce"),
        SINE("sine");

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
            return DataResult.error(() -> "Unsupported wave text mode: " + raw);
        }
    }
}
