package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;

/**
 * Replaces unrevealed letters and digits with rapidly changing glyphs while leaving
 * the configured portion of the final text stable and readable.
 *
 * <p>The reveal value is clamped to {@code 0..1}. A scattered reveal is deterministic,
 * so increasing the reveal later never hides characters that were already readable.</p>
 */
public record ScrambleRevealTextEffect(
        float reveal,
        int speed,
        Mode mode,
        String glyphs
) implements ZenithTooltipTextEffect {
    public static final String DEFAULT_GLYPHS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#%&?@";

    public static final MapCodec<ScrambleRevealTextEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.FLOAT.optionalFieldOf("reveal", 0.35F).forGetter(ScrambleRevealTextEffect::reveal),
                    Codec.INT.optionalFieldOf("speed", 50).forGetter(ScrambleRevealTextEffect::speed),
                    Mode.CODEC.optionalFieldOf("mode", Mode.SCATTERED).forGetter(ScrambleRevealTextEffect::mode),
                    Codec.STRING.optionalFieldOf("glyphs", DEFAULT_GLYPHS).forGetter(ScrambleRevealTextEffect::glyphs)
            ).apply(instance, ScrambleRevealTextEffect::new)
    );

    public ScrambleRevealTextEffect {
        reveal = Math.max(0.0F, Math.min(1.0F, reveal));
        speed = Math.max(16, speed);
        mode = mode == null ? Mode.SCATTERED : mode;
        glyphs = glyphs == null || glyphs.isBlank() ? DEFAULT_GLYPHS : glyphs;
    }

    @Override
    public MapCodec<? extends ZenithTooltipTextEffect> codec() {
        return CODEC;
    }

    public enum Mode {
        PREFIX("prefix"),
        SCATTERED("scattered");

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
            return DataResult.error(() -> "Unsupported scramble reveal mode: " + raw);
        }
    }
}
