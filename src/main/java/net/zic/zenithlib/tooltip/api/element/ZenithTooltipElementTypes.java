package net.zic.zenithlib.tooltip.api.element;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for tooltip element data codecs and optional resolve hooks.
 *
 * <p>JSON may use either a full namespaced id, such as {@code zenithlib:text}, or one
 * of the built-in short aliases, such as {@code text}. Custom elements should use
 * namespaced ids.</p>
 */
public final class ZenithTooltipElementTypes {
    public static final Identifier TEXT = id("text");
    public static final Identifier DIVIDER = id("divider");
    public static final Identifier SPACER = id("spacer");
    public static final Identifier HEADER = id("header");
    public static final Identifier ROW = id("row");
    public static final Identifier ICON = id("icon");
    public static final Identifier TITLE_ICON = id("title_icon");
    public static final Identifier BADGE = id("badge");
    public static final Identifier BAR = id("bar");
    public static final Identifier ENTITY_PREVIEW = id("entity_preview");
    public static final Identifier COLLECTION = id("collection");

    private static final Map<String, Entry<?>> BY_NAME = new ConcurrentHashMap<>();
    private static final Map<Identifier, Entry<?>> BY_ID = new ConcurrentHashMap<>();
    private static final Map<MapCodec<? extends ZenithTooltipElement>, Identifier> ID_BY_CODEC = new ConcurrentHashMap<>();
    private static final Map<Identifier, ResolverEntry<?>> RESOLVERS = new ConcurrentHashMap<>();

    static {
        registerBuiltIn("text", TEXT, TextElement.CODEC);
        registerBuiltIn("divider", DIVIDER, DividerElement.CODEC);
        registerBuiltIn("spacer", SPACER, SpacerElement.CODEC);
        registerBuiltIn("header", HEADER, HeaderElement.CODEC);
        registerBuiltIn("row", ROW, RowElement.CODEC);
        registerBuiltIn("icon", ICON, IconElement.CODEC);
        registerBuiltIn("title_icon", TITLE_ICON, TitleIconElement.CODEC);
        registerBuiltIn("badge", BADGE, BadgeElement.CODEC);
        registerBuiltIn("bar", BAR, BarElement.CODEC);
        registerBuiltIn("entity_preview", ENTITY_PREVIEW, EntityPreviewElement.CODEC);
        registerBuiltIn("collection", COLLECTION, CollectionElement.CODEC);
    }

    private ZenithTooltipElementTypes() {}

    public static <T extends ZenithTooltipElement> void register(Identifier id, MapCodec<T> codec) {
        registerInternal(null, id, codec);
    }

    public static <T extends ZenithTooltipElement> void registerIfLoaded(
            String requiredModId,
            Identifier id,
            MapCodec<T> codec
    ) {
        Objects.requireNonNull(requiredModId, "requiredModId");
        if (ModList.get().isLoaded(requiredModId)) {
            register(id, codec);
        }
    }

    public static <T extends ZenithTooltipElement> void register(
            Identifier id,
            MapCodec<T> codec,
            Class<T> elementClass,
            Resolver<T> resolver
    ) {
        register(id, codec);
        registerResolver(id, elementClass, resolver);
    }

    public static <T extends ZenithTooltipElement> void registerResolver(
            Identifier id,
            Class<T> elementClass,
            Resolver<T> resolver
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(elementClass, "elementClass");
        Objects.requireNonNull(resolver, "resolver");
        RESOLVERS.put(id, new ResolverEntry<>(elementClass, resolver));
    }

    public static Optional<MapCodec<? extends ZenithTooltipElement>> codec(String rawType) {
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

    public static Optional<Identifier> idFor(MapCodec<? extends ZenithTooltipElement> codec) {
        return Optional.ofNullable(ID_BY_CODEC.get(codec));
    }

    public static Optional<String> typeName(ZenithTooltipElement element) {
        return idFor(element.codec()).map(Identifier::toString);
    }

    public static List<ZenithTooltipElement> resolve(ZenithTooltipElement element, ZenithTooltipContext context) {
        ResolverEntry<?> entry = RESOLVERS.get(element.type());
        if (entry == null) {
            return List.of(element);
        }
        return entry.resolve(element, context);
    }

    public static Map<Identifier, MapCodec<? extends ZenithTooltipElement>> view() {
        Map<Identifier, MapCodec<? extends ZenithTooltipElement>> copy = new LinkedHashMap<>();
        BY_ID.forEach((id, entry) -> copy.put(id, entry.codec()));
        return Map.copyOf(copy);
    }

    private static <T extends ZenithTooltipElement> void registerBuiltIn(String legacyName, Identifier id, MapCodec<T> codec) {
        registerInternal(legacyName, id, codec);
    }

    private static <T extends ZenithTooltipElement> void registerInternal(
            String legacyName,
            Identifier id,
            MapCodec<T> codec
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(codec, "codec");

        Entry<T> entry = new Entry<>(id, codec);
        Entry<?> previous = BY_ID.putIfAbsent(id, entry);
        if (previous != null) {
            ZenithLib.LOGGER.debug("Skipped duplicate Zenith tooltip element type registration for {}", id);
            return;
        }

        BY_NAME.put(id.toString(), entry);
        if (legacyName != null && !legacyName.isBlank()) {
            BY_NAME.put(legacyName, entry);
        }
        ID_BY_CODEC.put(codec, id);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, path);
    }

    @FunctionalInterface
    public interface Resolver<T extends ZenithTooltipElement> {
        List<ZenithTooltipElement> resolve(T element, ZenithTooltipContext context);
    }

    private record Entry<T extends ZenithTooltipElement>(Identifier id, MapCodec<T> codec) {}

    private record ResolverEntry<T extends ZenithTooltipElement>(
            Class<T> elementClass,
            Resolver<T> resolver
    ) {
        private List<ZenithTooltipElement> resolve(ZenithTooltipElement element, ZenithTooltipContext context) {
            if (!elementClass.isInstance(element)) {
                return List.of(element);
            }
            List<ZenithTooltipElement> resolved = resolver.resolve(elementClass.cast(element), context);
            return resolved == null ? List.of() : List.copyOf(resolved);
        }
    }
}
