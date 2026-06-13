package net.zic.zenithlib.tooltip.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;

import java.util.List;

/**
 * Defines one page of a Zenith tooltip document.
 */

public record ZenithTooltipPage(
        ZenithTooltipText title,
        List<ZenithTooltipElement> elements
) {
    public static final Codec<ZenithTooltipPage> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("title").forGetter(ZenithTooltipPage::title),
                    ZenithTooltipElement.CODEC.listOf().optionalFieldOf("elements", List.of()).forGetter(ZenithTooltipPage::elements)
            ).apply(instance, ZenithTooltipPage::new)
    );

    public ZenithTooltipPage {
        elements = List.copyOf(elements);
    }
}
