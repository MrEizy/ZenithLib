package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public record BadgeRowElement(
        List<BadgeElement> badges,
        int spacing,
        int rowSpacing,
        boolean wrap
) implements ZenithTooltipElement {
    public static final int DEFAULT_SPACING = 4;
    public static final int DEFAULT_ROW_SPACING = 3;

    public BadgeRowElement(List<BadgeElement> badges) {
        this(badges, DEFAULT_SPACING, DEFAULT_ROW_SPACING, true);
    }

    public BadgeRowElement(BadgeElement... badges) {
        this(List.of(badges));
    }

    public BadgeRowElement {
        badges = badges == null ? List.of() : List.copyOf(badges);
        spacing = Math.max(0, spacing);
        rowSpacing = Math.max(0, rowSpacing);
    }

    public BadgeRowElement withSpacing(int spacing) {
        return new BadgeRowElement(badges, spacing, rowSpacing, wrap);
    }

    public BadgeRowElement withRowSpacing(int rowSpacing) {
        return new BadgeRowElement(badges, spacing, rowSpacing, wrap);
    }

    public BadgeRowElement withWrap(boolean wrap) {
        return new BadgeRowElement(badges, spacing, rowSpacing, wrap);
    }

    public BadgeRowElement add(BadgeElement badge) {
        List<BadgeElement> copy = new ArrayList<>(badges);
        copy.add(badge);
        return new BadgeRowElement(copy, spacing, rowSpacing, wrap);
    }

    public static final MapCodec<BadgeRowElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BadgeElement.CODEC.codec().listOf().fieldOf("badges").forGetter(BadgeRowElement::badges),
                    com.mojang.serialization.Codec.INT.optionalFieldOf("spacing", DEFAULT_SPACING).forGetter(BadgeRowElement::spacing),
                    com.mojang.serialization.Codec.INT.optionalFieldOf("row_spacing", DEFAULT_ROW_SPACING).forGetter(BadgeRowElement::rowSpacing),
                    com.mojang.serialization.Codec.BOOL.optionalFieldOf("wrap", true).forGetter(BadgeRowElement::wrap)
            ).apply(instance, BadgeRowElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
