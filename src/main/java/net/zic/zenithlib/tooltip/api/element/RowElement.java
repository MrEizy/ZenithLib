package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipInlineIcon;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

import java.util.Optional;

/* Tooltip element for a labelled left-and-right value row */

public record RowElement(
        ZenithTooltipText left,
        ZenithTooltipText right,
        ZenithTooltipColor leftColor,
        ZenithTooltipColor rightColor,
        Optional<ZenithTooltipInlineIcon> icon
) implements ZenithTooltipElement {

    public RowElement(
            ZenithTooltipText left,
            ZenithTooltipText right,
            ZenithTooltipColor leftColor,
            ZenithTooltipColor rightColor
    ) {
        this(left, right, leftColor, rightColor, Optional.empty());
    }

    public RowElement(String left, String right, ZenithTooltipColor leftColor, ZenithTooltipColor rightColor) {
        this(ZenithTooltipText.translatable(left), ZenithTooltipText.translatable(right), leftColor, rightColor);
    }

    public RowElement {
        icon = icon == null ? Optional.empty() : icon;
    }

    public RowElement withIcon(ZenithTooltipInlineIcon icon) {
        return new RowElement(left, right, leftColor, rightColor, Optional.of(icon));
    }

    public static RowElement literal(
            String left,
            String right,
            ZenithTooltipColor leftColor,
            ZenithTooltipColor rightColor
    ) {
        return new RowElement(ZenithTooltipText.literal(left), ZenithTooltipText.literal(right), leftColor, rightColor);
    }

    public static final MapCodec<RowElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.optionalFieldOf("left").forGetter(row -> Optional.of(row.left())),
                    ZenithTooltipText.CODEC.optionalFieldOf("label").forGetter(row -> Optional.empty()),
                    ZenithTooltipText.CODEC.optionalFieldOf("right").forGetter(row -> Optional.of(row.right())),
                    ZenithTooltipText.CODEC.optionalFieldOf("value").forGetter(row -> Optional.empty()),
                    ZenithTooltipColor.CODEC.optionalFieldOf("left_color", ZenithTooltipColor.TEXT).forGetter(RowElement::leftColor),
                    ZenithTooltipColor.CODEC.optionalFieldOf("right_color", ZenithTooltipColor.ACCENT).forGetter(RowElement::rightColor),
                    ZenithTooltipInlineIcon.CODEC.optionalFieldOf("icon").forGetter(RowElement::icon)
            ).apply(instance, RowElement::fromCodec)
    );

    private static RowElement fromCodec(
            Optional<ZenithTooltipText> left,
            Optional<ZenithTooltipText> label,
            Optional<ZenithTooltipText> right,
            Optional<ZenithTooltipText> value,
            ZenithTooltipColor leftColor,
            ZenithTooltipColor rightColor,
            Optional<ZenithTooltipInlineIcon> icon
    ) {
        return new RowElement(
                left.or(() -> label).orElseThrow(() -> new IllegalArgumentException("Row element requires left or label")),
                right.or(() -> value).orElseThrow(() -> new IllegalArgumentException("Row element requires right or value")),
                leftColor,
                rightColor,
                icon
        );
    }

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }
}
