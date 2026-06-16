package net.zic.zenithlib.tooltip.api.condition;

import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Registry for stack-aware tooltip section conditions. */
public final class ZenithTooltipConditions {
    public static final Identifier ALWAYS = id("always");
    public static final Identifier NEVER = id("never");
    public static final Identifier SHIFT_DOWN = id("shift_down");
    public static final Identifier CTRL_DOWN = id("ctrl_down");
    public static final Identifier ALT_DOWN = id("alt_down");
    public static final Identifier NOT_SHIFT_DOWN = id("not_shift_down");
    public static final Identifier NOT_CTRL_DOWN = id("not_ctrl_down");
    public static final Identifier NOT_ALT_DOWN = id("not_alt_down");

    private static final Map<Identifier, Condition> CONDITIONS = new ConcurrentHashMap<>();

    static {
        register(ALWAYS, context -> true);
        register(NEVER, context -> false);
        register(SHIFT_DOWN, context -> modifier(context, SHIFT_DOWN));
        register(CTRL_DOWN, context -> modifier(context, CTRL_DOWN));
        register(id("control_down"), context -> modifier(context, CTRL_DOWN));
        register(ALT_DOWN, context -> modifier(context, ALT_DOWN));
        register(NOT_SHIFT_DOWN, context -> !modifier(context, SHIFT_DOWN));
        register(NOT_CTRL_DOWN, context -> !modifier(context, CTRL_DOWN));
        register(id("not_control_down"), context -> !modifier(context, CTRL_DOWN));
        register(NOT_ALT_DOWN, context -> !modifier(context, ALT_DOWN));
    }

    private ZenithTooltipConditions() {}

    public static void register(Identifier id, Condition condition) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(condition, "condition");
        if (CONDITIONS.putIfAbsent(id, condition) != null) {
            ZenithLib.LOGGER.debug("Skipped duplicate Zenith tooltip condition registration for {}", id);
        }
    }

    public static void registerIfLoaded(String requiredModId, Identifier id, Condition condition) {
        Objects.requireNonNull(requiredModId, "requiredModId");
        if (ModList.get().isLoaded(requiredModId)) {
            register(id, condition);
        }
    }

    public static boolean matches(Identifier id, ZenithTooltipContext context) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(context, "context");

        Optional<Boolean> contextFlag = context.data(id, Boolean.class);
        if (contextFlag.isPresent()) {
            return contextFlag.orElseThrow();
        }

        Condition condition = CONDITIONS.get(id);
        if (condition == null) {
            return false;
        }

        try {
            return condition.matches(context);
        } catch (RuntimeException exception) {
            ZenithLib.LOGGER.warn(
                    "Zenith tooltip condition {} failed while handling {}",
                    id,
                    context.itemId(),
                    exception
            );
            return false;
        }
    }

    public static boolean contains(Identifier id) {
        return CONDITIONS.containsKey(id);
    }

    public static List<Identifier> ids() {
        return CONDITIONS.keySet().stream().sorted().toList();
    }

    public static Identifier identifier(String condition) {
        String trimmed = Objects.requireNonNull(condition, "condition").trim();
        return trimmed.indexOf(':') < 0 ? id(trimmed) : Identifier.parse(trimmed);
    }

    private static boolean modifier(ZenithTooltipContext context, Identifier id) {
        return context.data(id, Boolean.class).orElse(false);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, path);
    }

    @FunctionalInterface
    public interface Condition {
        boolean matches(ZenithTooltipContext context);
    }
}
