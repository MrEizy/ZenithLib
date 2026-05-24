package net.zic.zenithlib.tooltip.api;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

/**
 * Represents display text used by tooltip pages and elements.
 *
 * <p>The type explicitly distinguishes translatable language keys from literal text,
 * avoiding heuristics based on string contents. Its codec maps resource JSON objects
 * using either {@code translate} or {@code literal}, and {@link #component()} creates
 * the corresponding Minecraft chat component when layout or rendering requires it.</p>
 *
 * <p>This separation makes tooltip documents localisable by default while still
 * supporting generated or deliberately literal content.</p>
 */

public record ZenithTooltipText(
        String value,
        boolean translatable
) {
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

    public static final Codec<ZenithTooltipText> CODEC = Codec.either(TRANSLATABLE_CODEC, LITERAL_CODEC).xmap(
            either -> either.map(text -> text, text -> text),
            text -> text.translatable() ? Either.left(text) : Either.right(text)
    );

    public static ZenithTooltipText translatable(String key) {
        return new ZenithTooltipText(key, true);
    }

    public static ZenithTooltipText literal(String text) {
        return new ZenithTooltipText(text, false);
    }

    public Component component() {
        return translatable ? Component.translatable(value) : Component.literal(value);
    }

    public boolean isBlank() {
        return value.isBlank();
    }
}
