package net.zic.zenithlib.tooltip.api.element;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

/* Tooltip element that combines the hovered item's themed icon box with a title and
 * optional subtitle positioned beside it */

public record TitleIconElement(
        ZenithTooltipText title,
        ZenithTooltipText subtitle,
        boolean onAllPages
) implements ZenithTooltipElement {
    private static final Codec<Boolean> LENIENT_BOOLEAN_CODEC = Codec.either(Codec.BOOL, Codec.STRING)
            .xmap(
                    either -> either.map(value -> value, Boolean::parseBoolean),
                    value -> Either.<Boolean, String>left(value)
            );

    public TitleIconElement(ZenithTooltipText title, ZenithTooltipText subtitle) {
        this(title, subtitle, false);
    }

    public TitleIconElement(String title, String subtitle) {
        this(ZenithTooltipText.translatable(title), ZenithTooltipText.translatable(subtitle), false);
    }

    public static TitleIconElement literal(String title, String subtitle) {
        return new TitleIconElement(
                ZenithTooltipText.literal(title),
                subtitle.isBlank() ? ZenithTooltipText.literal("") : ZenithTooltipText.literal(subtitle),
                false
        );
    }

    public TitleIconElement withOnAllPages(boolean onAllPages) {
        return new TitleIconElement(title, subtitle, onAllPages);
    }

    public static final MapCodec<TitleIconElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("title").forGetter(TitleIconElement::title),
                    ZenithTooltipText.CODEC.optionalFieldOf("subtitle", ZenithTooltipText.literal(""))
                            .forGetter(TitleIconElement::subtitle),
                    LENIENT_BOOLEAN_CODEC.optionalFieldOf("on_all_pages", false)
                            .forGetter(TitleIconElement::onAllPages)
            ).apply(instance, TitleIconElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
