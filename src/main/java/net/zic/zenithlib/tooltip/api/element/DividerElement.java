package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;

import java.util.Optional;

public record DividerElement(
        Optional<ZenithTooltipColor> color,
        Optional<ZenithTooltipColor> endColor,
        Optional<Integer> inset,
        Optional<Integer> thickness
) implements ZenithTooltipElement {
    public DividerElement() {
        this(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public DividerElement {
        color = color == null ? Optional.empty() : color;
        endColor = endColor == null ? Optional.empty() : endColor;
        inset = inset == null ? Optional.empty() : inset.map(value -> Math.max(0, value));
        thickness = thickness == null ? Optional.empty() : thickness.map(value -> Math.max(1, value));
    }

    public DividerElement withColor(ZenithTooltipColor color) {
        return new DividerElement(Optional.of(color), Optional.empty(), inset, thickness);
    }

    public DividerElement withGradient(ZenithTooltipColor start, ZenithTooltipColor end) {
        return new DividerElement(Optional.of(start), Optional.of(end), inset, thickness);
    }

    public DividerElement withInset(int inset) {
        return new DividerElement(color, endColor, Optional.of(inset), thickness);
    }

    public DividerElement withThickness(int thickness) {
        return new DividerElement(color, endColor, inset, Optional.of(thickness));
    }

    public static final MapCodec<DividerElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipColor.CODEC.optionalFieldOf("color").forGetter(DividerElement::color),
                    ZenithTooltipColor.CODEC.optionalFieldOf("end_color").forGetter(DividerElement::endColor),
                    Codec.INT.optionalFieldOf("inset").forGetter(DividerElement::inset),
                    Codec.INT.optionalFieldOf("thickness").forGetter(DividerElement::thickness)
            ).apply(instance, DividerElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
