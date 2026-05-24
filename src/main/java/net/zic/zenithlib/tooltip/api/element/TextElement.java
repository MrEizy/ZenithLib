package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

/* General-purpose tooltip element for one wrapped block of body text */

public record TextElement(
        ZenithTooltipText text,
        ZenithTooltipColor color
) implements ZenithTooltipElement {

    public TextElement(String key) {
        this(ZenithTooltipText.translatable(key), ZenithTooltipColor.TEXT);
    }

    public TextElement(String key, ZenithTooltipColor color) {
        this(ZenithTooltipText.translatable(key), color);
    }

    public static TextElement literal(String text, ZenithTooltipColor color) {
        return new TextElement(ZenithTooltipText.literal(text), color);
    }

    public static final MapCodec<TextElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("text").forGetter(TextElement::text),
                    ZenithTooltipColor.CODEC.optionalFieldOf("color", ZenithTooltipColor.TEXT).forGetter(TextElement::color)
            ).apply(instance, TextElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
