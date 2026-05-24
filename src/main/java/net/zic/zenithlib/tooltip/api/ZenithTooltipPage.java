package net.zic.zenithlib.tooltip.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;

import java.util.List;

/**
 * Defines one page of a Zenith tooltip document.
 *
 * <p>Each page contains a display title and an ordered list of polymorphic tooltip
 * elements such as text, dividers, rows, or item-icon headers. The codec is used by
 * reusable JSON document definitions, while the immutable element list makes loaded
 * page content safe to share across multiple themed rules and repeated renders.</p>
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
