package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

/* Tooltip element that combines the hovered item's themed icon box with a title and
 * optional subtitle positioned beside it */

public record TitleIconElement(
        ZenithTooltipText title,
        ZenithTooltipText subtitle
) implements ZenithTooltipElement {

    public TitleIconElement(String title, String subtitle) {
        this(ZenithTooltipText.translatable(title), ZenithTooltipText.translatable(subtitle));
    }

    public static TitleIconElement literal(String title, String subtitle) {
        return new TitleIconElement(
                ZenithTooltipText.literal(title),
                subtitle.isBlank() ? ZenithTooltipText.literal("") : ZenithTooltipText.literal(subtitle)
        );
    }

    public static final MapCodec<TitleIconElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("title").forGetter(TitleIconElement::title),
                    ZenithTooltipText.CODEC.optionalFieldOf("subtitle", ZenithTooltipText.literal(""))
                            .forGetter(TitleIconElement::subtitle)
            ).apply(instance, TitleIconElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
