package net.zic.zenithlib.tooltip.api;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** Small texture icon that can be embedded beside compact text elements. */
public record ZenithTooltipInlineIcon(
        Identifier texture,
        int size,
        int textureWidth,
        int textureHeight
) {
    private static final int DEFAULT_SIZE = 9;
    private static final int DEFAULT_TEXTURE_SIZE = 16;

    private static final MapCodec<ZenithTooltipInlineIcon> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(ZenithTooltipInlineIcon::texture),
                    Codec.INT.optionalFieldOf("size", DEFAULT_SIZE).forGetter(ZenithTooltipInlineIcon::size),
                    Codec.INT.optionalFieldOf("texture_width", DEFAULT_TEXTURE_SIZE).forGetter(ZenithTooltipInlineIcon::textureWidth),
                    Codec.INT.optionalFieldOf("texture_height", DEFAULT_TEXTURE_SIZE).forGetter(ZenithTooltipInlineIcon::textureHeight)
            ).apply(instance, ZenithTooltipInlineIcon::new)
    );

    public static final Codec<ZenithTooltipInlineIcon> CODEC = Codec.either(Identifier.CODEC, MAP_CODEC.codec())
            .xmap(
                    either -> either.map(ZenithTooltipInlineIcon::new, value -> value),
                    value -> Either.<Identifier, ZenithTooltipInlineIcon>right(value)
            );

    public ZenithTooltipInlineIcon(Identifier texture) {
        this(texture, DEFAULT_SIZE, DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
    }

    public ZenithTooltipInlineIcon {
        size = Math.max(1, size);
        textureWidth = Math.max(1, textureWidth);
        textureHeight = Math.max(1, textureHeight);
    }
}
