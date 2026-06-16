package net.zic.zenithlib.tooltip.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;

import java.util.List;
import java.util.Optional;

/**
 * Defines one page of a Zenith tooltip document.
 */

public record ZenithTooltipPage(
        ZenithTooltipText title,
        Optional<ZenithTooltipTextEffect> titleEffect,
        List<ZenithTooltipElement> elements
) {
    public static final Codec<ZenithTooltipPage> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("title").forGetter(ZenithTooltipPage::title),
                    ZenithTooltipTextEffect.CODEC.optionalFieldOf("title_effect").forGetter(ZenithTooltipPage::titleEffect),
                    ZenithTooltipElement.CODEC.listOf().optionalFieldOf("elements", List.of()).forGetter(ZenithTooltipPage::elements)
            ).apply(instance, ZenithTooltipPage::new)
    );

    public ZenithTooltipPage(ZenithTooltipText title, List<ZenithTooltipElement> elements) {
        this(title, Optional.empty(), elements);
    }

    public ZenithTooltipPage {
        titleEffect = titleEffect == null ? Optional.empty() : titleEffect;
        elements = List.copyOf(elements);
    }
}
