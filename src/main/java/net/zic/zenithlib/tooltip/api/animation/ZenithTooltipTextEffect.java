package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/**
 * Base type for serialisable effects applied to ordinary Zenith tooltip text.
 *
 * <p>Effects alter only the rendered glyphs. Tooltip wrapping and measurement still
 * use the final, fully readable text so animations cannot resize or reflow the card.</p>
 */
public sealed interface ZenithTooltipTextEffect permits ScrambleRevealTextEffect {
    MapCodec<? extends ZenithTooltipTextEffect> codec();

    Codec<ZenithTooltipTextEffect> CODEC = Codec.STRING.dispatch(
            "type",
            effect -> switch (effect) {
                case ScrambleRevealTextEffect ignored -> "scramble_reveal";
            },
            type -> switch (type) {
                case "scramble_reveal" -> ScrambleRevealTextEffect.CODEC;
                default -> throw new IllegalArgumentException("Unknown Zenith tooltip text effect type: " + type);
            }
    );
}
