package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

/* Tooltip element for a labelled left-and-right value row */

public record RowElement(
        ZenithTooltipText left,
        ZenithTooltipText right,
        ZenithTooltipColor leftColor,
        ZenithTooltipColor rightColor
) implements ZenithTooltipElement {

    public RowElement(String left, String right, ZenithTooltipColor leftColor, ZenithTooltipColor rightColor) {
        this(ZenithTooltipText.translatable(left), ZenithTooltipText.translatable(right), leftColor, rightColor);
    }

    public static RowElement literal(
            String left,
            String right,
            ZenithTooltipColor leftColor,
            ZenithTooltipColor rightColor
    ) {
        return new RowElement(ZenithTooltipText.literal(left), ZenithTooltipText.literal(right), leftColor, rightColor);
    }

    public static final MapCodec<RowElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("left").forGetter(RowElement::left),
                    ZenithTooltipText.CODEC.fieldOf("right").forGetter(RowElement::right),
                    ZenithTooltipColor.CODEC.optionalFieldOf("left_color", ZenithTooltipColor.TEXT).forGetter(RowElement::leftColor),
                    ZenithTooltipColor.CODEC.optionalFieldOf("right_color", ZenithTooltipColor.ACCENT).forGetter(RowElement::rightColor)
            ).apply(instance, RowElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
