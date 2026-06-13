package net.zic.zenithlib.tooltip.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Reusable, theme-independent tooltip document definition.
 */
public record ZenithTooltipTemplate(List<ZenithTooltipPage> pages, List<Identifier> animationPresets) {
    public static final MapCodec<ZenithTooltipTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipPage.CODEC.listOf().fieldOf("pages").forGetter(ZenithTooltipTemplate::pages),
                    Identifier.CODEC.listOf().optionalFieldOf("animation_presets", List.of()).forGetter(ZenithTooltipTemplate::animationPresets)
            ).apply(instance, ZenithTooltipTemplate::new)
    );
    public static final Codec<ZenithTooltipTemplate> CODEC = MAP_CODEC.codec();

    public ZenithTooltipTemplate(List<ZenithTooltipPage> pages) {
        this(pages, List.of());
    }

    public ZenithTooltipTemplate {
        pages = List.copyOf(pages);
        animationPresets = animationPresets == null ? List.of() : List.copyOf(animationPresets);
    }

    public ZenithTooltipDocument themed(ZenithTooltipTheme theme) {
        return new ZenithTooltipDocument(theme, pages, animationPresets);
    }
}
