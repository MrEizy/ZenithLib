package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

import java.util.Locale;
import java.util.Optional;

/**
 * Source-backed collection that expands into ordinary badges or rows before layout.
 */
public record CollectionElement(
        String source,
        Presentation presentation,
        Optional<ZenithTooltipText> header,
        boolean hideWhenEmpty,
        boolean dividerBefore,
        boolean dividerAfter,
        ZenithTooltipColor rowLeftColor,
        ZenithTooltipColor badgeTextColor,
        ZenithTooltipColor badgeBackgroundColor,
        ZenithTooltipColor badgeBorderColor
) implements ZenithTooltipElement {
    public enum Presentation {
        BADGES,
        ROWS;

        public static final Codec<Presentation> CODEC = Codec.STRING.xmap(
                value -> Presentation.valueOf(value.trim().toUpperCase(Locale.ROOT)),
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }

    public static final MapCodec<CollectionElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("source").forGetter(CollectionElement::source),
                    Presentation.CODEC.optionalFieldOf("presentation", Presentation.ROWS).forGetter(CollectionElement::presentation),
                    ZenithTooltipText.CODEC.optionalFieldOf("header").forGetter(CollectionElement::header),
                    Codec.BOOL.optionalFieldOf("hide_when_empty", true).forGetter(CollectionElement::hideWhenEmpty),
                    Codec.BOOL.optionalFieldOf("divider_before", false).forGetter(CollectionElement::dividerBefore),
                    Codec.BOOL.optionalFieldOf("divider_after", false).forGetter(CollectionElement::dividerAfter),
                    ZenithTooltipColor.CODEC.optionalFieldOf("row_left_color", ZenithTooltipColor.TEXT).forGetter(CollectionElement::rowLeftColor),
                    ZenithTooltipColor.CODEC.optionalFieldOf("badge_text_color", ZenithTooltipColor.BACKGROUND).forGetter(CollectionElement::badgeTextColor),
                    ZenithTooltipColor.CODEC.optionalFieldOf("badge_background_color", ZenithTooltipColor.ACCENT).forGetter(CollectionElement::badgeBackgroundColor),
                    ZenithTooltipColor.CODEC.optionalFieldOf("badge_border_color", ZenithTooltipColor.ACCENT).forGetter(CollectionElement::badgeBorderColor)
            ).apply(instance, CollectionElement::new)
    );

    public CollectionElement {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Tooltip collection source may not be blank");
        }
        source = source.trim();
        presentation = presentation == null ? Presentation.ROWS : presentation;
        header = header == null ? Optional.empty() : header;
        rowLeftColor = rowLeftColor == null ? ZenithTooltipColor.TEXT : rowLeftColor;
        badgeTextColor = badgeTextColor == null ? ZenithTooltipColor.BACKGROUND : badgeTextColor;
        badgeBackgroundColor = badgeBackgroundColor == null ? ZenithTooltipColor.ACCENT : badgeBackgroundColor;
        badgeBorderColor = badgeBorderColor == null ? ZenithTooltipColor.ACCENT : badgeBorderColor;
    }

    public static CollectionElement badges(String source, ZenithTooltipText header) {
        return new CollectionElement(
                source,
                Presentation.BADGES,
                Optional.ofNullable(header),
                true,
                false,
                false,
                ZenithTooltipColor.TEXT,
                ZenithTooltipColor.BACKGROUND,
                ZenithTooltipColor.ACCENT,
                ZenithTooltipColor.ACCENT
        );
    }

    public static CollectionElement rows(String source, ZenithTooltipText header) {
        return new CollectionElement(
                source,
                Presentation.ROWS,
                Optional.ofNullable(header),
                true,
                false,
                false,
                ZenithTooltipColor.TEXT,
                ZenithTooltipColor.BACKGROUND,
                ZenithTooltipColor.ACCENT,
                ZenithTooltipColor.ACCENT
        );
    }

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
