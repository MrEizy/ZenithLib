package net.zic.zenithlib.tooltip.api;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents authored or runtime-resolved display text used by tooltip pages and
 * elements.
 */
public final class ZenithTooltipText {
    private static final Codec<ZenithTooltipText> SOURCE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("source").forGetter(text -> text.source.orElseThrow())
            ).apply(instance, ZenithTooltipText::source)
    );

    private static final Codec<ZenithTooltipText> TRANSLATABLE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("translate").forGetter(ZenithTooltipText::value)
            ).apply(instance, ZenithTooltipText::translatable)
    );

    private static final Codec<ZenithTooltipText> LITERAL_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("literal").forGetter(ZenithTooltipText::value)
            ).apply(instance, ZenithTooltipText::literal)
    );

    public static final Codec<ZenithTooltipText> CODEC = Codec.either(
            SOURCE_CODEC,
            Codec.either(TRANSLATABLE_CODEC, LITERAL_CODEC)
    ).xmap(
            either -> either.map(
                    text -> text,
                    staticText -> staticText.map(text -> text, text -> text)
            ),
            text -> {
                if (text.source.isPresent()) {
                    return Either.left(text);
                }
                if (text.translatable) {
                    return Either.right(Either.left(text));
                }
                return Either.right(Either.right(text));
            }
    );

    private final String value;
    private final boolean translatable;
    private final Optional<Identifier> source;
    private final Optional<Component> resolvedComponent;

    public ZenithTooltipText(String value, boolean translatable) {
        this(value, translatable, Optional.empty(), Optional.empty());
    }

    private ZenithTooltipText(
            String value,
            boolean translatable,
            Optional<Identifier> source,
            Optional<Component> resolvedComponent
    ) {
        this.value = value == null ? "" : value;
        this.translatable = translatable;
        this.source = source == null ? Optional.empty() : source;
        this.resolvedComponent = resolvedComponent == null
                ? Optional.empty()
                : resolvedComponent.map(Component::copy);
    }

    public static ZenithTooltipText translatable(String key) {
        return new ZenithTooltipText(key, true);
    }

    public static ZenithTooltipText literal(String text) {
        return new ZenithTooltipText(text, false);
    }

    public static ZenithTooltipText source(Identifier source) {
        return new ZenithTooltipText(
                "",
                false,
                Optional.of(Objects.requireNonNull(source, "source")),
                Optional.empty()
        );
    }

    public static ZenithTooltipText source(String source) {
        String trimmed = Objects.requireNonNull(source, "source").trim();
        Identifier id = trimmed.indexOf(':') < 0
                ? Identifier.fromNamespaceAndPath("zenithlib", trimmed)
                : Identifier.parse(trimmed);
        return source(id);
    }

    public static ZenithTooltipText resolved(Component component) {
        Component copy = Objects.requireNonNull(component, "component").copy();
        return new ZenithTooltipText(copy.getString(), false, Optional.empty(), Optional.of(copy));
    }

    public String value() {
        return value;
    }

    public boolean translatable() {
        return translatable;
    }

    public Optional<Identifier> source() {
        return source;
    }

    public boolean isDynamic() {
        return source.isPresent();
    }

    public Component component() {
        if (resolvedComponent.isPresent()) {
            return resolvedComponent.orElseThrow().copy();
        }
        if (source.isPresent()) {
            return Component.empty();
        }
        return translatable ? Component.translatable(value) : Component.literal(value);
    }

    public boolean isBlank() {
        if (resolvedComponent.isPresent()) {
            return resolvedComponent.orElseThrow().getString().isBlank();
        }
        return source.isEmpty() && value.isBlank();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ZenithTooltipText other)) {
            return false;
        }
        return translatable == other.translatable
                && value.equals(other.value)
                && source.equals(other.source)
                && resolvedComponent.equals(other.resolvedComponent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, translatable, source, resolvedComponent);
    }

    @Override
    public String toString() {
        if (source.isPresent()) {
            return "ZenithTooltipText[source=" + source.orElseThrow() + "]";
        }
        return "ZenithTooltipText[value=" + value + ", translatable=" + translatable + "]";
    }
}
