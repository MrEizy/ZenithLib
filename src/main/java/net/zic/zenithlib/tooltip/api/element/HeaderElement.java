package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

/* Tooltip element that renders a prominent text heading within a page body */

public record HeaderElement(
        ZenithTooltipText text,
        ZenithTooltipColor color
) implements ZenithTooltipElement {

    public HeaderElement(String key, ZenithTooltipColor color) {
        this(ZenithTooltipText.translatable(key), color);
    }

    public static HeaderElement literal(String text, ZenithTooltipColor color) {
        return new HeaderElement(ZenithTooltipText.literal(text), color);
    }

    public static final MapCodec<HeaderElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("text").forGetter(HeaderElement::text),
                    ZenithTooltipColor.CODEC.optionalFieldOf("color", ZenithTooltipColor.ACCENT).forGetter(HeaderElement::color)
            ).apply(instance, HeaderElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
