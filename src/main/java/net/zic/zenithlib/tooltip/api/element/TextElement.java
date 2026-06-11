package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect;

import java.util.Optional;

/* General-purpose tooltip element for one wrapped block of body text */

public record TextElement(
        ZenithTooltipText text,
        ZenithTooltipColor color,
        Optional<ZenithTooltipTextEffect> effect
) implements ZenithTooltipElement {

    public TextElement(ZenithTooltipText text, ZenithTooltipColor color) {
        this(text, color, Optional.empty());
    }

    public TextElement(String key) {
        this(ZenithTooltipText.translatable(key), ZenithTooltipColor.TEXT);
    }

    public TextElement(String key, ZenithTooltipColor color) {
        this(ZenithTooltipText.translatable(key), color);
    }

    public TextElement {
        effect = effect == null ? Optional.empty() : effect;
    }

    public static TextElement literal(String text, ZenithTooltipColor color) {
        return new TextElement(ZenithTooltipText.literal(text), color);
    }

    public static TextElement animated(
            ZenithTooltipText text,
            ZenithTooltipColor color,
            ZenithTooltipTextEffect effect
    ) {
        return new TextElement(text, color, Optional.of(effect));
    }

    public static final MapCodec<TextElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("text").forGetter(TextElement::text),
                    ZenithTooltipColor.CODEC.optionalFieldOf("color", ZenithTooltipColor.TEXT).forGetter(TextElement::color),
                    ZenithTooltipTextEffect.CODEC.optionalFieldOf("effect").forGetter(TextElement::effect)
            ).apply(instance, TextElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
