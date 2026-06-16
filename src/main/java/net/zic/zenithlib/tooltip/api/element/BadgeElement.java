package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipInlineIcon;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect;

import java.util.Optional;

/**
 * Compact framed label used for rarity, item class, restrictions, or other status
 * markers inside a tooltip document.
 */
public record BadgeElement(
        ZenithTooltipText text,
        ZenithTooltipColor textColor,
        ZenithTooltipColor backgroundColor,
        ZenithTooltipColor borderColor,
        Optional<ZenithTooltipInlineIcon> icon,
        Optional<ZenithTooltipTextEffect> effect
) implements ZenithTooltipElement {
    private static final ZenithTooltipColor DEFAULT_BACKGROUND = new ZenithTooltipColor("background");

    public BadgeElement(
            ZenithTooltipText text,
            ZenithTooltipColor textColor,
            ZenithTooltipColor backgroundColor,
            ZenithTooltipColor borderColor
    ) {
        this(text, textColor, backgroundColor, borderColor, Optional.empty(), Optional.empty());
    }

    public BadgeElement {
        icon = icon == null ? Optional.empty() : icon;
        effect = effect == null ? Optional.empty() : effect;
    }

    public BadgeElement withIcon(ZenithTooltipInlineIcon icon) {
        return new BadgeElement(text, textColor, backgroundColor, borderColor, Optional.of(icon), effect);
    }

    public BadgeElement withEffect(ZenithTooltipTextEffect effect) {
        return new BadgeElement(text, textColor, backgroundColor, borderColor, icon, Optional.of(effect));
    }

    public static final MapCodec<BadgeElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("text").forGetter(BadgeElement::text),
                    ZenithTooltipColor.CODEC.optionalFieldOf("text_color", ZenithTooltipColor.ACCENT).forGetter(BadgeElement::textColor),
                    ZenithTooltipColor.CODEC.optionalFieldOf("background_color", DEFAULT_BACKGROUND).forGetter(BadgeElement::backgroundColor),
                    ZenithTooltipColor.CODEC.optionalFieldOf("border_color", ZenithTooltipColor.ACCENT).forGetter(BadgeElement::borderColor),
                    ZenithTooltipInlineIcon.CODEC.optionalFieldOf("icon").forGetter(BadgeElement::icon),
                    ZenithTooltipTextEffect.CODEC.optionalFieldOf("effect").forGetter(BadgeElement::effect)
            ).apply(instance, BadgeElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
