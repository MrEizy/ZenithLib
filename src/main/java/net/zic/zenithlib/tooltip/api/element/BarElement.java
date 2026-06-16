package net.zic.zenithlib.tooltip.api.element;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

/**
 * Labelled horizontal progress/value bar for durability, charge, mana, affinity,
 * upgrade progress, or other bounded numerical values.
 */
public record BarElement(
        ZenithTooltipText label,
        int value,
        int max,
        ZenithTooltipText valueText,
        ZenithTooltipColor color,
        String source,
        String valueSource,
        String maxSource
) implements ZenithTooltipElement {
    private static final Scalar DEFAULT_VALUE = Scalar.fixed(0);
    private static final Scalar DEFAULT_MAX = Scalar.fixed(1);

    public static final MapCodec<BarElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ZenithTooltipText.CODEC.fieldOf("label").forGetter(BarElement::label),
                    Scalar.CODEC.optionalFieldOf("value", DEFAULT_VALUE).forGetter(BarElement::valueScalar),
                    Scalar.CODEC.optionalFieldOf("max", DEFAULT_MAX).forGetter(BarElement::maxScalar),
                    ZenithTooltipText.CODEC.optionalFieldOf("value_text", ZenithTooltipText.literal("")).forGetter(BarElement::valueText),
                    ZenithTooltipColor.CODEC.optionalFieldOf("color", ZenithTooltipColor.ACCENT).forGetter(BarElement::color),
                    Codec.STRING.optionalFieldOf("source", "").forGetter(BarElement::source),
                    Codec.STRING.optionalFieldOf("value_source", "").forGetter(bar -> ""),
                    Codec.STRING.optionalFieldOf("max_source", "").forGetter(bar -> "")
            ).apply(instance, BarElement::fromCodec)
    );

    public BarElement {
        source = clean(source);
        valueSource = clean(valueSource);
        maxSource = clean(maxSource);
        max = Math.max(1, max);
        value = Math.max(0, Math.min(value, max));
    }

    public BarElement(
            ZenithTooltipText label,
            int value,
            int max,
            ZenithTooltipText valueText,
            ZenithTooltipColor color
    ) {
        this(label, value, max, valueText, color, "", "", "");
    }

    public BarElement(
            ZenithTooltipText label,
            int value,
            int max,
            ZenithTooltipText valueText,
            ZenithTooltipColor color,
            String source
    ) {
        this(label, value, max, valueText, color, source, "", "");
    }

    public static BarElement dynamic(
            ZenithTooltipText label,
            String source,
            ZenithTooltipColor color
    ) {
        return new BarElement(label, 0, 1, ZenithTooltipText.literal(""), color, source, "", "");
    }

    public static BarElement dynamic(
            ZenithTooltipText label,
            Identifier source,
            ZenithTooltipColor color
    ) {
        return dynamic(label, source.toString(), color);
    }

    public static BarElement sourced(
            ZenithTooltipText label,
            String valueSource,
            String maxSource,
            ZenithTooltipColor color
    ) {
        return new BarElement(label, 0, 1, ZenithTooltipText.literal(""), color, "", valueSource, maxSource);
    }

    public boolean isDynamic() {
        return !source.isBlank();
    }

    public boolean hasScalarSources() {
        return !valueSource.isBlank() || !maxSource.isBlank();
    }

    public float progress() {
        return (float) value / (float) max;
    }

    @Override
    public MapCodec<? extends ZenithTooltipElement> codec() {
        return CODEC;
    }

    private Scalar valueScalar() {
        return valueSource.isBlank() ? Scalar.fixed(value) : Scalar.source(valueSource);
    }

    private Scalar maxScalar() {
        return maxSource.isBlank() ? Scalar.fixed(max) : Scalar.source(maxSource);
    }

    private static BarElement fromCodec(
            ZenithTooltipText label,
            Scalar value,
            Scalar max,
            ZenithTooltipText valueText,
            ZenithTooltipColor color,
            String source,
            String valueSource,
            String maxSource
    ) {
        String resolvedValueSource = clean(valueSource).isBlank() ? value.source() : valueSource;
        String resolvedMaxSource = clean(maxSource).isBlank() ? max.source() : maxSource;
        return new BarElement(label, value.value(), max.value(), valueText, color, source, resolvedValueSource, resolvedMaxSource);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private record Scalar(int value, String source) {
        private static final Codec<Scalar> SOURCE_OBJECT_CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("source").forGetter(scalar -> Identifier.parse(scalar.source()))
                ).apply(instance, id -> Scalar.source(id.toString()))
        );
        private static final Codec<Scalar> CODEC = Codec.either(
                Codec.INT,
                Codec.either(Codec.STRING, SOURCE_OBJECT_CODEC)
        ).xmap(
                either -> either.map(
                        Scalar::fixed,
                        source -> source.map(Scalar::source, scalar -> scalar)
                ),
                scalar -> scalar.source().isBlank()
                        ? Either.left(scalar.value())
                        : Either.right(Either.right(scalar))
        );

        private Scalar {
            source = clean(source);
        }

        private static Scalar fixed(int value) {
            return new Scalar(value, "");
        }

        private static Scalar source(String source) {
            return new Scalar(0, source);
        }
    }
}
