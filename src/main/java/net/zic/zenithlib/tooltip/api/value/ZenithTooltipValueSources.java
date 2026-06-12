package net.zic.zenithlib.tooltip.api.value;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry for generic tooltip value sources.
 *
 * <p>Dependent mods should register namespaced sources during client setup. Duplicate
 * identifiers are ignored so one mod cannot silently replace another mod's binding.</p>
 */
public final class ZenithTooltipValueSources {
    public static final Identifier DURABILITY = id("durability");
    public static final Identifier ITEM_NAME = id("item_name");
    public static final Identifier ITEM_ID = id("item_id");
    public static final Identifier SUBJECT_NAME = id("subject_name");
    public static final Identifier SUBJECT_DESCRIPTION = id("subject_description");
    public static final Identifier SUBJECT_ID = id("subject_id");

    private static final Map<Identifier, ZenithTooltipValueSource> SOURCES = new ConcurrentHashMap<>();

    static {
        registerBuiltIns();
    }

    private ZenithTooltipValueSources() {}

    public static void register(Identifier id, ZenithTooltipValueSource source) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(source, "source");

        ZenithTooltipValueSource existing = SOURCES.putIfAbsent(id, source);
        if (existing != null) {
            ZenithLib.LOGGER.debug("Skipped duplicate Zenith tooltip value source registration for {}", id);
            return;
        }

        ZenithLib.LOGGER.info("Registered Zenith tooltip value source {}", id);
    }

    public static Optional<ZenithTooltipValue> resolve(
            Identifier id,
            ZenithTooltipContext context
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(context, "context");

        ZenithTooltipValueSource source = SOURCES.get(id);
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

    public static <T extends ZenithTooltipValue> Optional<T> resolve(
            Identifier id,
            ZenithTooltipContext context,
            Class<T> valueType
    ) {
        Objects.requireNonNull(valueType, "valueType");
        return resolve(id, context)
                .filter(valueType::isInstance)
                .map(valueType::cast);
    }

    public static Optional<ZenithTooltipValue> resolve(
            String source,
            ZenithTooltipContext context
    ) {
        if (source == null || source.isBlank()) {
            return Optional.empty();
        }

        try {
            return resolve(identifier(source), context);
        } catch (RuntimeException exception) {
            ZenithLib.LOGGER.warn("Invalid Zenith tooltip value source identifier '{}': {}", source, exception.getMessage());
            return Optional.empty();
        }
    }

    public static <T extends ZenithTooltipValue> Optional<T> resolve(
            String source,
            ZenithTooltipContext context,
            Class<T> valueType
    ) {
        Objects.requireNonNull(valueType, "valueType");
        return resolve(source, context)
                .filter(valueType::isInstance)
                .map(valueType::cast);
    }

    public static boolean contains(Identifier id) {
        return SOURCES.containsKey(id);
    }

    public static List<Identifier> ids() {
        return SOURCES.keySet().stream().sorted().toList();
    }

    /**
     * Resolves legacy unnamespaced source names inside ZenithLib's namespace while
     * preserving ordinary namespaced identifiers for dependent mods.
     */
    public static Identifier identifier(String source) {
        String trimmed = Objects.requireNonNull(source, "source").trim();
        if (trimmed.indexOf(':') < 0) {
            return id(trimmed);
        }
        return Identifier.parse(trimmed);
    }

    private static void registerBuiltIns() {
        register(DURABILITY, context -> {
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

        register(ITEM_NAME, context -> Optional.of(
                ZenithTooltipValue.text(context.stack().getHoverName())
        ));

        register(ITEM_ID, context -> Optional.of(
                ZenithTooltipValue.text(Component.literal(context.itemId().toString()))
        ));

        register(SUBJECT_NAME, context -> context.subjectPresentation().map(subject ->
                ZenithTooltipValue.text(subject.tooltipName(context))
        ));

        register(SUBJECT_DESCRIPTION, context -> context.subjectPresentation()
                .flatMap(subject -> subject.tooltipDescription(context))
                .map(ZenithTooltipValue::text));

        register(SUBJECT_ID, context -> context.subjectId().map(subjectId ->
                ZenithTooltipValue.text(Component.literal(subjectId.toString()))
        ));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, path);
    }
}
