package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect;

import java.util.Optional;

/* Tooltip element that renders a prominent text heading within a page body */

public record HeaderElement(
        ZenithTooltipText text,
        ZenithTooltipColor color,
        Optional<ZenithTooltipTextEffect> effect,
        boolean underline
) implements ZenithTooltipElement {

    public HeaderElement(ZenithTooltipText text, ZenithTooltipColor color) {
        this(text, color, Optional.empty(), true);
    }

    public HeaderElement(ZenithTooltipText text, ZenithTooltipColor color, Optional<ZenithTooltipTextEffect> effect) {
        this(text, color, effect, true);
    }

    public HeaderElement(String key, ZenithTooltipColor color) {
        this(ZenithTooltipText.translatable(key), color);
    }

    public HeaderElement {
        effect = effect == null ? Optional.empty() : effect;
    }

    public HeaderElement withEffect(ZenithTooltipTextEffect effect) {
        return new HeaderElement(text, color, Optional.of(effect), underline);
    }

    public HeaderElement withUnderline(boolean underline) {
        return new HeaderElement(text, color, effect, underline);
    }

    public HeaderElement withoutUnderline() {
        return withUnderline(false);
    }

    public static HeaderElement literal(String text, ZenithTooltipColor color) {
        return new HeaderElement(ZenithTooltipText.literal(text), color);
    }

    public static final MapCodec<HeaderElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("text").forGetter(HeaderElement::text),
                    ZenithTooltipColor.CODEC.optionalFieldOf("color", ZenithTooltipColor.ACCENT).forGetter(HeaderElement::color),
                    ZenithTooltipTextEffect.CODEC.optionalFieldOf("effect").forGetter(HeaderElement::effect),
                    Codec.BOOL.optionalFieldOf("underline", true).forGetter(HeaderElement::underline)
            ).apply(instance, HeaderElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
