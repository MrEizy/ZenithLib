package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

/**
 * Base type for serialisable effects applied to ordinary Zenith tooltip text.
 */
public interface ZenithTooltipTextEffect {
    MapCodec<? extends ZenithTooltipTextEffect> codec();

    default Identifier type() {
        return ZenithTooltipTextEffects.idFor(codec()).orElseThrow(() ->
                new IllegalStateException("Unregistered Zenith tooltip text effect codec: " + codec())
        );
    }

    Codec<ZenithTooltipTextEffect> CODEC = Codec.STRING.dispatch(
            "type",
            effect -> ZenithTooltipTextEffects.typeName(effect).orElseThrow(() ->
                    new IllegalStateException("Unregistered Zenith tooltip text effect type for " + effect.getClass().getName())
            ),
            type -> ZenithTooltipTextEffects.codec(type).orElseThrow(() ->
                    new IllegalArgumentException("Unknown Zenith tooltip text effect type: " + type)
            )
    );
}
