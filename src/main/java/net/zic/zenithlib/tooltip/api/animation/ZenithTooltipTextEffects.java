package net.zic.zenithlib.tooltip.api.animation;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.ZenithLib;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Registry for serialisable tooltip text effects. */
public final class ZenithTooltipTextEffects {
    public static final Identifier SCRAMBLE_REVEAL = id("scramble_reveal");
    public static final Identifier RUNE_DECIPHER = id("rune_decipher");
    public static final Identifier TYPEWRITER = id("typewriter");
    public static final Identifier RAINBOW = id("rainbow");
    public static final Identifier SHIMMER = id("shimmer");
    public static final Identifier WAVE = id("wave");
    public static final Identifier STACK = id("stack");

    private static final Map<String, Entry<?>> BY_NAME = new ConcurrentHashMap<>();
    private static final Map<Identifier, Entry<?>> BY_ID = new ConcurrentHashMap<>();
    private static final Map<MapCodec<? extends ZenithTooltipTextEffect>, Identifier> ID_BY_CODEC = new ConcurrentHashMap<>();

    static {
        registerBuiltIn("scramble_reveal", SCRAMBLE_REVEAL, ScrambleRevealTextEffect.CODEC);
        registerBuiltIn("rune_decipher", RUNE_DECIPHER, RuneDecipherTextEffect.CODEC);
        registerBuiltIn("typewriter", TYPEWRITER, TypewriterTextEffect.CODEC);
        registerBuiltIn("rainbow", RAINBOW, RainbowTextEffect.CODEC);
        registerBuiltIn("shimmer", SHIMMER, ShimmerTextEffect.CODEC);
        registerBuiltIn("wave", WAVE, WaveTextEffect.CODEC);
        registerBuiltIn("stack", STACK, TextEffectStack.CODEC);
    }

    private ZenithTooltipTextEffects() {}

    public static <T extends ZenithTooltipTextEffect> void register(Identifier id, MapCodec<T> codec) {
        registerInternal(null, id, codec);
    }

    public static Optional<MapCodec<? extends ZenithTooltipTextEffect>> codec(String rawType) {
        Objects.requireNonNull(rawType, "rawType");
        Entry<?> entry = BY_NAME.get(rawType.trim());
        if (entry != null) {
            return Optional.of(entry.codec());
        }

        try {
            Identifier id = Identifier.parse(rawType.trim());
            return Optional.ofNullable(BY_ID.get(id)).map(Entry::codec);
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Identifier> idFor(MapCodec<? extends ZenithTooltipTextEffect> codec) {
        return Optional.ofNullable(ID_BY_CODEC.get(codec));
    }

    public static Optional<String> typeName(ZenithTooltipTextEffect effect) {
        return idFor(effect.codec()).map(Identifier::toString);
    }

    public static Map<Identifier, MapCodec<? extends ZenithTooltipTextEffect>> view() {
        Map<Identifier, MapCodec<? extends ZenithTooltipTextEffect>> copy = new LinkedHashMap<>();
        BY_ID.forEach((id, entry) -> copy.put(id, entry.codec()));
        return Map.copyOf(copy);
    }

    private static <T extends ZenithTooltipTextEffect> void registerBuiltIn(String aliasName, Identifier id, MapCodec<T> codec) {
        registerInternal(aliasName, id, codec);
    }

    private static <T extends ZenithTooltipTextEffect> void registerInternal(
            String aliasName,
            Identifier id,
            MapCodec<T> codec
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(codec, "codec");

        Entry<T> entry = new Entry<>(id, codec);
        Entry<?> previous = BY_ID.putIfAbsent(id, entry);
        if (previous != null) {
            ZenithLib.LOGGER.debug("Skipped duplicate Zenith tooltip text effect registration for {}", id);
            return;
        }

        BY_NAME.put(id.toString(), entry);
        if (aliasName != null && !aliasName.isBlank()) {
            BY_NAME.put(aliasName, entry);
        }
        ID_BY_CODEC.put(codec, id);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, path);
    }

    private record Entry<T extends ZenithTooltipTextEffect>(Identifier id, MapCodec<T> codec) {}
}
