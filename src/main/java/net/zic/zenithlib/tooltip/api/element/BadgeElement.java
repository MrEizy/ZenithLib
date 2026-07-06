package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipInlineIcon;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Compact framed label used for rarity, item class, restrictions, or other status
 * markers inside a tooltip page.
 */
public record BadgeElement(
        ZenithTooltipText text,
        ZenithTooltipColor textColor,
        ZenithTooltipColor backgroundColor,
        ZenithTooltipColor borderColor,
        Optional<ZenithTooltipInlineIcon> icon,
        Optional<ZenithTooltipTextEffect> effect,
        List<ZenithTooltipColor> backgroundGradient,
        GradientDirection gradientDirection
) implements ZenithTooltipElement {
    private static final ZenithTooltipColor DEFAULT_BACKGROUND = new ZenithTooltipColor("background");

    public BadgeElement(
            ZenithTooltipText text,
            ZenithTooltipColor textColor,
            ZenithTooltipColor backgroundColor,
            ZenithTooltipColor borderColor
    ) {
        this(text, textColor, backgroundColor, borderColor, Optional.empty(), Optional.empty());
    }

    public BadgeElement(
            ZenithTooltipText text,
            ZenithTooltipColor textColor,
            ZenithTooltipColor backgroundColor,
            ZenithTooltipColor borderColor,
            Optional<ZenithTooltipInlineIcon> icon,
            Optional<ZenithTooltipTextEffect> effect
    ) {
        this(text, textColor, backgroundColor, borderColor, icon, effect, List.of(), GradientDirection.HORIZONTAL);
    }

    public BadgeElement {
        icon = icon == null ? Optional.empty() : icon;
        effect = effect == null ? Optional.empty() : effect;
        backgroundGradient = backgroundGradient == null ? List.of() : List.copyOf(backgroundGradient);
        gradientDirection = gradientDirection == null ? GradientDirection.HORIZONTAL : gradientDirection;
    }

    public BadgeElement withIcon(ZenithTooltipInlineIcon icon) {
        return new BadgeElement(text, textColor, backgroundColor, borderColor, Optional.of(icon), effect, backgroundGradient, gradientDirection);
    }

    public BadgeElement withEffect(ZenithTooltipTextEffect effect) {
        return new BadgeElement(text, textColor, backgroundColor, borderColor, icon, Optional.of(effect), backgroundGradient, gradientDirection);
    }

    public BadgeElement withBackgroundColor(ZenithTooltipColor color) {
        return new BadgeElement(text, textColor, color, borderColor, icon, effect, List.of(), gradientDirection);
    }

    public BadgeElement withBackgroundGradient(List<ZenithTooltipColor> colors) {
        return withBackgroundGradient(GradientDirection.HORIZONTAL, colors);
    }

    public BadgeElement withBackgroundGradient(GradientDirection direction, List<ZenithTooltipColor> colors) {
        return new BadgeElement(text, textColor, backgroundColor, borderColor, icon, effect, colors, direction);
    }

    public BadgeElement withBackgroundGradient(GradientDirection direction, ZenithTooltipColor... colors) {
        return withBackgroundGradient(direction, List.of(colors));
    }

    public boolean hasBackgroundGradient() {
        return backgroundGradient.size() > 1;
    }

    public static final MapCodec<BadgeElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("text").forGetter(BadgeElement::text),
                    ZenithTooltipColor.CODEC.optionalFieldOf("text_color", ZenithTooltipColor.ACCENT).forGetter(BadgeElement::textColor),
                    ZenithTooltipColor.CODEC.optionalFieldOf("background_color", DEFAULT_BACKGROUND).forGetter(BadgeElement::backgroundColor),
                    ZenithTooltipColor.CODEC.optionalFieldOf("border_color", ZenithTooltipColor.ACCENT).forGetter(BadgeElement::borderColor),
                    ZenithTooltipInlineIcon.CODEC.optionalFieldOf("icon").forGetter(BadgeElement::icon),
                    ZenithTooltipTextEffect.CODEC.optionalFieldOf("effect").forGetter(BadgeElement::effect),
                    ZenithTooltipColor.CODEC.listOf().optionalFieldOf("background_gradient", List.<ZenithTooltipColor>of()).forGetter(BadgeElement::backgroundGradient),
                    GradientDirection.CODEC.optionalFieldOf("gradient_direction", GradientDirection.HORIZONTAL).forGetter(BadgeElement::gradientDirection)
            ).apply(instance, BadgeElement::new)
    );

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }

    public enum GradientDirection {
        HORIZONTAL("horizontal"),
        VERTICAL("vertical");

        public static final Codec<GradientDirection> CODEC = Codec.STRING.comapFlatMap(GradientDirection::decode, GradientDirection::serializedName);

        private final String serializedName;

        GradientDirection(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        private static DataResult<GradientDirection> decode(String raw) {
            String normalized = raw.toLowerCase(Locale.ROOT);
            for (GradientDirection direction : values()) {
                if (direction.serializedName.equals(normalized)) {
                    return DataResult.success(direction);
                }
            }
            return DataResult.error(() -> "Unsupported badge gradient direction: " + raw);
        }
    }
}
