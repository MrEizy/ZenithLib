package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

/**
 * Labelled horizontal progress/value bar for durability, charge, mana, affinity,
 * upgrade progress, or other bounded numerical values.
 */
public record BarElement(
        ZenithTooltipText label,
        int value,
        int max,
        ZenithTooltipText valueText,
        ZenithTooltipColor color,
        String source
) implements ZenithTooltipElement {
    public static final MapCodec<BarElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("label").forGetter(BarElement::label),
                    Codec.INT.optionalFieldOf("value", 0).forGetter(BarElement::value),
                    Codec.INT.optionalFieldOf("max", 1).forGetter(BarElement::max),
                    ZenithTooltipText.CODEC.optionalFieldOf("value_text", ZenithTooltipText.literal("")).forGetter(BarElement::valueText),
                    ZenithTooltipColor.CODEC.optionalFieldOf("color", ZenithTooltipColor.ACCENT).forGetter(BarElement::color),
                    Codec.STRING.optionalFieldOf("source", "").forGetter(BarElement::source)
            ).apply(instance, BarElement::new)
    );

    public BarElement {
        source = source == null ? "" : source.trim();
        max = Math.max(1, max);
        value = Math.max(0, Math.min(value, max));
    }

    public BarElement(
            ZenithTooltipText label,
            int value,
            int max,
            ZenithTooltipText valueText,
            ZenithTooltipColor color
    ) {
        this(label, value, max, valueText, color, "");
    }

    public static BarElement dynamic(
            ZenithTooltipText label,
            String source,
            ZenithTooltipColor color
    ) {
        return new BarElement(label, 0, 1, ZenithTooltipText.literal(""), color, source);
    }

    public static BarElement dynamic(
            ZenithTooltipText label,
            Identifier source,
            ZenithTooltipColor color
    ) {
        return dynamic(label, source.toString(), color);
    }

    public boolean isDynamic() {
        return !source.isBlank();
    }

    public float progress() {
        return (float) value / (float) max;
    }

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
