package net.zic.zenithlib.tooltip.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Reusable, theme-independent tooltip document definition.
 *
 * <p>A template stores immutable page content decoded from JSON and can be referenced
 * by any number of item rules. Applying a {@link ZenithTooltipTheme} creates a resolved
 * {@link ZenithTooltipDocument}, allowing content authors to define an item description
 * once while library users or resource packs select entirely different palettes and
 * layout metrics.</p>
 */

public record ZenithTooltipTemplate(List<ZenithTooltipPage> pages) {
    public static final MapCodec<ZenithTooltipTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipPage.CODEC.listOf().fieldOf("pages").forGetter(ZenithTooltipTemplate::pages)
            ).apply(instance, ZenithTooltipTemplate::new)
    );
    public static final Codec<ZenithTooltipTemplate> CODEC = MAP_CODEC.codec();

    public ZenithTooltipTemplate {
        pages = List.copyOf(pages);
    }

    public ZenithTooltipDocument themed(ZenithTooltipTheme theme) {
        return new ZenithTooltipDocument(theme, pages);
    }
}
