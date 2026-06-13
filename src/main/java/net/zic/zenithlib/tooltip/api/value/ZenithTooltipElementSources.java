package net.zic.zenithlib.tooltip.api.value;

import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for dynamic tooltip sections that convert runtime data into normal
 * tooltip elements.
 *
 * <p>Use value sources for data extraction and element sources for presentation.
 * This keeps mod-specific concepts such as cultivation, energy, storage, or ranks
 * outside the base renderer while still allowing JSON to reference them by id.</p>
 */
public final class ZenithTooltipElementSources {
    private static final Map<Identifier, Source> SOURCES = new ConcurrentHashMap<>();

    private ZenithTooltipElementSources() {}

    public static void register(Identifier id, Source source) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(source, "source");

        Source previous = SOURCES.putIfAbsent(id, source);
        if (previous != null) {
            ZenithLib.LOGGER.debug("Skipped duplicate Zenith tooltip element source registration for {}", id);
            return;
        }

        ZenithLib.LOGGER.info("Registered Zenith tooltip element source {}", id);
    }

    public static void registerIfLoaded(String requiredModId, Identifier id, Source source) {
        Objects.requireNonNull(requiredModId, "requiredModId");
        if (ModList.get().isLoaded(requiredModId)) {
            register(id, source);
        }
    }

    public static <T extends ZenithTooltipValue> void registerValueBacked(
            Identifier id,
            Identifier valueSource,
            Class<T> valueType,
            ValueConverter<T> converter
    ) {
        Objects.requireNonNull(valueSource, "valueSource");
        Objects.requireNonNull(valueType, "valueType");
        Objects.requireNonNull(converter, "converter");
        register(id, context -> ZenithTooltipValueSources.resolve(valueSource, context, valueType)
                .map(value -> converter.convert(value, context))
                .orElseGet(List::of));
    }

    public static <T extends ZenithTooltipValue> void registerValueBackedIfLoaded(
            String requiredModId,
            Identifier id,
            Identifier valueSource,
            Class<T> valueType,
            ValueConverter<T> converter
    ) {
        Objects.requireNonNull(requiredModId, "requiredModId");
        if (ModList.get().isLoaded(requiredModId)) {
            registerValueBacked(id, valueSource, valueType, converter);
        }
    }

    public static Optional<List<ZenithTooltipElement>> resolve(Identifier id, ZenithTooltipContext context) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(context, "context");

        Source source = SOURCES.get(id);
        if (source == null) {
            return Optional.empty();
        }

        try {
            List<ZenithTooltipElement> elements = source.resolve(context);
            return Optional.of(elements == null ? List.of() : List.copyOf(elements));
        } catch (RuntimeException exception) {
            ZenithLib.LOGGER.warn(
                    "Zenith tooltip element source {} failed while handling {}",
                    id,
                    context.itemId(),
                    exception
            );
            return Optional.of(List.of());
        }
    }

    public static boolean contains(Identifier id) {
        return SOURCES.containsKey(id);
    }

    public static List<Identifier> ids() {
        return SOURCES.keySet().stream().sorted().toList();
    }

    @FunctionalInterface
    public interface Source {
        List<ZenithTooltipElement> resolve(ZenithTooltipContext context);
    }

    @FunctionalInterface
    public interface ValueConverter<T extends ZenithTooltipValue> {
        List<ZenithTooltipElement> convert(T value, ZenithTooltipContext context);
    }
}
