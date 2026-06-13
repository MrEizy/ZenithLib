package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/**
 * Base type for serialisable effects applied to ordinary Zenith tooltip text.
 */
public sealed interface ZenithTooltipTextEffect
        permits ScrambleRevealTextEffect, RuneDecipherTextEffect, TypewriterTextEffect,
        RainbowTextEffect, ShimmerTextEffect, WaveTextEffect, TextEffectStack {
    MapCodec<? extends ZenithTooltipTextEffect> codec();

    Codec<ZenithTooltipTextEffect> CODEC = Codec.STRING.dispatch(
            "type",
            effect -> switch (effect) {
                case ScrambleRevealTextEffect ignored -> "scramble_reveal";
                case RuneDecipherTextEffect ignored -> "rune_decipher";
                case TypewriterTextEffect ignored -> "typewriter";
                case RainbowTextEffect ignored -> "rainbow";
                case ShimmerTextEffect ignored -> "shimmer";
                case WaveTextEffect ignored -> "wave";
                case TextEffectStack ignored -> "stack";
            },
            type -> switch (type) {
                case "scramble_reveal" -> ScrambleRevealTextEffect.CODEC;
                case "rune_decipher" -> RuneDecipherTextEffect.CODEC;
                case "typewriter" -> TypewriterTextEffect.CODEC;
                case "rainbow" -> RainbowTextEffect.CODEC;
                case "shimmer" -> ShimmerTextEffect.CODEC;
                case "wave" -> WaveTextEffect.CODEC;
                case "stack" -> TextEffectStack.CODEC;
                default -> throw new IllegalArgumentException("Unknown Zenith tooltip text effect type: " + type);
            }
    );
}
