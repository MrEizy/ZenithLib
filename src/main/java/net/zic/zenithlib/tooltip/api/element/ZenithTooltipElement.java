package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/* Sealed base type for all serialisable elements that may appear in a tooltip page. */

public sealed interface ZenithTooltipElement
        permits TextElement, DividerElement, SpacerElement, HeaderElement, RowElement,
        IconElement, TitleIconElement, BadgeElement, BarElement {

    MapCodec<? extends ZenithTooltipElement> codec();

    Codec<ZenithTooltipElement> CODEC = Codec.STRING.dispatch(
            "type",
            element -> switch (element) {
                case TextElement ignored -> "text";
                case DividerElement ignored -> "divider";
                case SpacerElement ignored -> "spacer";
                case HeaderElement ignored -> "header";
                case RowElement ignored -> "row";
                case IconElement ignored -> "icon";
                case TitleIconElement ignored -> "title_icon";
                case BadgeElement ignored -> "badge";
                case BarElement ignored -> "bar";
            },
            type -> switch (type) {
                case "text" -> TextElement.CODEC;
                case "divider" -> DividerElement.CODEC;
                case "spacer" -> SpacerElement.CODEC;
                case "header" -> HeaderElement.CODEC;
                case "row" -> RowElement.CODEC;
                case "icon" -> IconElement.CODEC;
                case "title_icon" -> TitleIconElement.CODEC;
                case "badge" -> BadgeElement.CODEC;
                case "bar" -> BarElement.CODEC;
                default -> throw new IllegalArgumentException("Unknown Zenith tooltip element type: " + type);
            }
    );
}
