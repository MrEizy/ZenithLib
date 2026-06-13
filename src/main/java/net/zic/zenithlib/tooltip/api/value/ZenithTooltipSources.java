package net.zic.zenithlib.tooltip.api.value;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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
import java.util.function.BiFunction;
import java.util.function.Function;

/** Registry for tooltip runtime data sources and dynamic element producers. */
public final class ZenithTooltipSources {
    public static final Identifier DURABILITY = id("durability");
    public static final Identifier ITEM_NAME = id("item_name");
    public static final Identifier ITEM_ID = id("item_id");
    public static final Identifier SUBJECT_NAME = id("subject_name");
    public static final Identifier SUBJECT_DESCRIPTION = id("subject_description");
    public static final Identifier SUBJECT_ID = id("subject_id");

    private static final Map<Identifier, ValueSource> VALUE_SOURCES = new ConcurrentHashMap<>();
    private static final Map<Identifier, ElementSource> ELEMENT_SOURCES = new ConcurrentHashMap<>();

    static {
        registerBuiltInValues();
    }

    private ZenithTooltipSources() {}

    public static void registerValue(Identifier id, ValueSource source) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(source, "source");

        ValueSource existing = VALUE_SOURCES.putIfAbsent(id, source);
        if (existing != null) {
            ZenithLib.LOGGER.debug("Skipped duplicate Zenith tooltip value source registration for {}", id);
            return;
        }

        ZenithLib.LOGGER.info("Registered Zenith tooltip value source {}", id);
    }

    public static void registerValueIfLoaded(String requiredModId, Identifier id, ValueSource source) {
        Objects.requireNonNull(requiredModId, "requiredModId");
        if (ModList.get().isLoaded(requiredModId)) {
            registerValue(id, source);
        }
    }

    public static void registerText(Identifier id, Function<ZenithTooltipContext, Optional<Component>> source) {
        Objects.requireNonNull(source, "source");
        registerValue(id, context -> source.apply(context).map(ZenithTooltipValue::text));
    }

    public static void registerProgress(
            Identifier id,
            Function<ZenithTooltipContext, Optional<ZenithTooltipValue.Progress>> source
    ) {
        Objects.requireNonNull(source, "source");
        registerValue(id, context -> source.apply(context).map(value -> (ZenithTooltipValue) value));
    }

    public static void registerRows(
            Identifier id,
            Function<ZenithTooltipContext, Optional<ZenithTooltipValue.Rows>> source
    ) {
        Objects.requireNonNull(source, "source");
        registerValue(id, context -> source.apply(context).map(value -> (ZenithTooltipValue) value));
    }

    public static <T> void registerItemComponent(
            Identifier id,
            DataComponentType<T> componentType,
            BiFunction<T, ZenithTooltipContext, Optional<ZenithTooltipValue>> source
    ) {
        Objects.requireNonNull(componentType, "componentType");
        Objects.requireNonNull(source, "source");
        registerValue(id, context -> {
            T component = context.stack().get(componentType);
            return component == null ? Optional.empty() : source.apply(component, context);
        });
    }

    public static void registerItem(
            Identifier id,
            Function<ZenithTooltipContext, Optional<ZenithTooltipValue>> source
    ) {
        Objects.requireNonNull(source, "source");
        registerValue(id, source::apply);
    }

    public static <T> void registerSubject(
            Identifier id,
            Class<T> subjectType,
            BiFunction<T, ZenithTooltipContext, Optional<ZenithTooltipValue>> source
    ) {
        Objects.requireNonNull(subjectType, "subjectType");
        Objects.requireNonNull(source, "source");
        registerValue(id, context -> context.subject(subjectType)
                .flatMap(subject -> source.apply(subject, context)));
    }

    public static Optional<ZenithTooltipValue> resolveValue(Identifier id, ZenithTooltipContext context) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(context, "context");

        ValueSource source = VALUE_SOURCES.get(id);
        if (source == null) {
            return Optional.empty();
        }

        try {
            Optional<ZenithTooltipValue> value = source.resolve(context);
            return value == null ? Optional.empty() : value;
        } catch (RuntimeException exception) {
            ZenithLib.LOGGER.warn(
                    "Zenith tooltip value source {} failed while handling {}",
                    id,
                    context.itemId(),
                    exception
            );
            return Optional.empty();
        }
    }

    public static <T extends ZenithTooltipValue> Optional<T> resolveValue(
            Identifier id,
            ZenithTooltipContext context,
            Class<T> valueType
    ) {
        Objects.requireNonNull(valueType, "valueType");
        return resolveValue(id, context)
                .filter(valueType::isInstance)
                .map(valueType::cast);
    }

    public static Optional<ZenithTooltipValue> resolveValue(String source, ZenithTooltipContext context) {
        if (source == null || source.isBlank()) {
            return Optional.empty();
        }

        try {
            return resolveValue(identifier(source), context);
        } catch (RuntimeException exception) {
            ZenithLib.LOGGER.warn("Invalid Zenith tooltip value source identifier '{}': {}", source, exception.getMessage());
            return Optional.empty();
        }
    }

    public static <T extends ZenithTooltipValue> Optional<T> resolveValue(
            String source,
            ZenithTooltipContext context,
            Class<T> valueType
    ) {
        Objects.requireNonNull(valueType, "valueType");
        return resolveValue(source, context)
                .filter(valueType::isInstance)
                .map(valueType::cast);
    }

    public static void registerElement(Identifier id, ElementSource source) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(source, "source");

        ElementSource previous = ELEMENT_SOURCES.putIfAbsent(id, source);
        if (previous != null) {
            ZenithLib.LOGGER.debug("Skipped duplicate Zenith tooltip element source registration for {}", id);
            return;
        }

        ZenithLib.LOGGER.info("Registered Zenith tooltip element source {}", id);
    }

    public static void registerElementIfLoaded(String requiredModId, Identifier id, ElementSource source) {
        Objects.requireNonNull(requiredModId, "requiredModId");
        if (ModList.get().isLoaded(requiredModId)) {
            registerElement(id, source);
        }
    }

    public static <T extends ZenithTooltipValue> void registerElementFromValue(
            Identifier id,
            Identifier valueSource,
            Class<T> valueType,
            ValueElementConverter<T> converter
    ) {
        Objects.requireNonNull(valueSource, "valueSource");
        Objects.requireNonNull(valueType, "valueType");
        Objects.requireNonNull(converter, "converter");
        registerElement(id, context -> resolveValue(valueSource, context, valueType)
                .map(value -> converter.convert(value, context))
                .orElseGet(List::of));
    }

    public static <T extends ZenithTooltipValue> void registerElementFromValueIfLoaded(
            String requiredModId,
            Identifier id,
            Identifier valueSource,
            Class<T> valueType,
            ValueElementConverter<T> converter
    ) {
        Objects.requireNonNull(requiredModId, "requiredModId");
        if (ModList.get().isLoaded(requiredModId)) {
            registerElementFromValue(id, valueSource, valueType, converter);
        }
    }

    public static Optional<List<ZenithTooltipElement>> resolveElements(Identifier id, ZenithTooltipContext context) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(context, "context");

        ElementSource source = ELEMENT_SOURCES.get(id);
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

    public static boolean containsValue(Identifier id) {
        return VALUE_SOURCES.containsKey(id);
    }

    public static boolean containsElement(Identifier id) {
        return ELEMENT_SOURCES.containsKey(id);
    }

    public static List<Identifier> valueIds() {
        return VALUE_SOURCES.keySet().stream().sorted().toList();
    }

    public static List<Identifier> elementIds() {
        return ELEMENT_SOURCES.keySet().stream().sorted().toList();
    }

    public static Identifier identifier(String source) {
        String trimmed = Objects.requireNonNull(source, "source").trim();
        if (trimmed.indexOf(':') < 0) {
            return id(trimmed);
        }
        return Identifier.parse(trimmed);
    }

    private static void registerBuiltInValues() {
        registerValue(DURABILITY, context -> {
            Integer maxDamage = context.stack().get(DataComponents.MAX_DAMAGE);
            Integer damage = context.stack().get(DataComponents.DAMAGE);

            if (maxDamage == null || damage == null || maxDamage <= 0) {
                return Optional.empty();
            }

            return Optional.of(ZenithTooltipValue.progress(
                    Math.max(0, maxDamage - damage),
                    maxDamage
            ));
        });

        registerValue(ITEM_NAME, context -> Optional.of(
                ZenithTooltipValue.text(context.stack().getHoverName())
        ));

        registerValue(ITEM_ID, context -> Optional.of(
                ZenithTooltipValue.text(Component.literal(context.itemId().toString()))
        ));

        registerValue(SUBJECT_NAME, context -> context.subjectPresentation().map(subject ->
                ZenithTooltipValue.text(subject.tooltipName(context))
        ));

        registerValue(SUBJECT_DESCRIPTION, context -> context.subjectPresentation()
                .flatMap(subject -> subject.tooltipDescription(context))
                .map(ZenithTooltipValue::text));

        registerValue(SUBJECT_ID, context -> context.subjectId().map(subjectId ->
                ZenithTooltipValue.text(Component.literal(subjectId.toString()))
        ));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, path);
    }

    @FunctionalInterface
    public interface ValueSource {
        Optional<ZenithTooltipValue> resolve(ZenithTooltipContext context);
    }

    @FunctionalInterface
    public interface ElementSource {
        List<ZenithTooltipElement> resolve(ZenithTooltipContext context);
    }

    @FunctionalInterface
    public interface ValueElementConverter<T extends ZenithTooltipValue> {
        List<ZenithTooltipElement> convert(T value, ZenithTooltipContext context);
    }
}
