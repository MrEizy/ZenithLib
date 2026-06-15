package net.zic.zenithlib.tooltip.api.element;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

import java.util.Locale;

/**
 * Element resolved into category/rank rows or a badge for the current stack.
 */
public record ClassificationElement(
        boolean showCategory,
        boolean showRank,
        Style style,
        ZenithTooltipText categoryLabel,
        ZenithTooltipText rankLabel,
        ZenithTooltipColor labelColor
) implements ZenithTooltipElement {
    private static final Codec<Boolean> LENIENT_BOOLEAN_CODEC = Codec.either(Codec.BOOL, Codec.STRING)
            .xmap(
                    either -> either.map(value -> value, Boolean::parseBoolean),
                    value -> Either.<Boolean, String>left(value)
            );

    private static final ZenithTooltipText DEFAULT_CATEGORY_LABEL = ZenithTooltipText.translatable("tooltip.zenithlib.classification.category");
    private static final ZenithTooltipText DEFAULT_RANK_LABEL = ZenithTooltipText.translatable("tooltip.zenithlib.classification.rank");

    public static final MapCodec<ClassificationElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    LENIENT_BOOLEAN_CODEC.optionalFieldOf("show_category", true).forGetter(ClassificationElement::showCategory),
                    LENIENT_BOOLEAN_CODEC.optionalFieldOf("show_rank", true).forGetter(ClassificationElement::showRank),
                    Style.CODEC.optionalFieldOf("style", Style.ROWS).forGetter(ClassificationElement::style),
                    ZenithTooltipText.CODEC.optionalFieldOf("category_label", DEFAULT_CATEGORY_LABEL).forGetter(ClassificationElement::categoryLabel),
                    ZenithTooltipText.CODEC.optionalFieldOf("rank_label", DEFAULT_RANK_LABEL).forGetter(ClassificationElement::rankLabel),
                    ZenithTooltipColor.CODEC.optionalFieldOf("label_color", ZenithTooltipColor.MUTED).forGetter(ClassificationElement::labelColor)
            ).apply(instance, ClassificationElement::new)
    );

    public ClassificationElement() {
        this(true, true, Style.ROWS, DEFAULT_CATEGORY_LABEL, DEFAULT_RANK_LABEL, ZenithTooltipColor.MUTED);
    }

    public static ClassificationElement rows() {
        return new ClassificationElement(true, true, Style.ROWS, DEFAULT_CATEGORY_LABEL, DEFAULT_RANK_LABEL, ZenithTooltipColor.MUTED);
    }

    public static ClassificationElement badge() {
        return new ClassificationElement(true, true, Style.BADGE, DEFAULT_CATEGORY_LABEL, DEFAULT_RANK_LABEL, ZenithTooltipColor.MUTED);
    }

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }

    public enum Style {
        ROWS("rows"),
        BADGE("badge");

        public static final Codec<Style> CODEC = Codec.STRING.xmap(Style::fromName, Style::serializedName);

        private final String serializedName;

        Style(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        public static Style fromName(String raw) {
            String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
            if ("row".equals(value)) {
                return ROWS;
            }
            if ("badges".equals(value)) {
                return BADGE;
            }
            for (Style style : values()) {
                if (style.serializedName.equals(value)) {
                    return style;
                }
            }
            throw new IllegalArgumentException("Unknown classification element style: " + raw);
        }
    }
}
