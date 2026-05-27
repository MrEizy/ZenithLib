package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

/**
 * Compact framed label used for rarity, item class, restrictions, or other status
 * markers inside a tooltip document.
 */
public record BadgeElement(
        ZenithTooltipText text,
        ZenithTooltipColor textColor,
        ZenithTooltipColor backgroundColor,
        ZenithTooltipColor borderColor
) implements ZenithTooltipElement {
    private static final ZenithTooltipColor DEFAULT_BACKGROUND = new ZenithTooltipColor("background");

    public static final MapCodec<BadgeElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("text").forGetter(BadgeElement::text),
                    ZenithTooltipColor.CODEC.optionalFieldOf("text_color", ZenithTooltipColor.ACCENT).forGetter(BadgeElement::textColor),
                    ZenithTooltipColor.CODEC.optionalFieldOf("background_color", DEFAULT_BACKGROUND).forGetter(BadgeElement::backgroundColor),
                    ZenithTooltipColor.CODEC.optionalFieldOf("border_color", ZenithTooltipColor.ACCENT).forGetter(BadgeElement::borderColor)
            ).apply(instance, BadgeElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
