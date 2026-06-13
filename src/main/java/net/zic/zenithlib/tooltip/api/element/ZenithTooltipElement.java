package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;

/**
 * Base type for serialisable tooltip elements.
 */
public interface ZenithTooltipElement {
    MapCodec<? extends ZenithTooltipElement> codec();

    default Identifier type() {
        return ZenithTooltipElementTypes.idFor(codec()).orElseThrow(() ->
                new IllegalStateException("Unregistered Zenith tooltip element codec: " + codec())
        );
    }

    Codec<ZenithTooltipElement> CODEC = Codec.STRING.dispatch(
            "type",
            element -> ZenithTooltipElementTypes.typeName(element).orElseThrow(() ->
                    new IllegalStateException("Unregistered Zenith tooltip element type for " + element.getClass().getName())
            ),
            type -> ZenithTooltipElementTypes.codec(type).orElseThrow(() ->
                    new IllegalArgumentException("Unknown Zenith tooltip element type: " + type)
            )
    );
}
