package net.zic.zenithlib.tooltip.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipSubject;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** Runtime provider registry for contextual Zenith tooltips. */
public final class ZenithTooltipProviders {
    private static final List<Entry> PROVIDERS = new CopyOnWriteArrayList<>();

    private ZenithTooltipProviders() {}

    public static void register(Identifier id, Provider provider) {
        Objects.requireNonNull(provider, "provider");
        registerInternal(id, context -> {
            Optional<ZenithTooltipDocument> document = provider.create(
                    context.stack(),
                    context.itemId(),
                    context.registryAccess()
            );
            return document == null
                    ? Optional.empty()
                    : document.map(value -> Result.of(value, context));
        });
    }

    public static void registerContextual(Identifier id, ContextualProvider provider) {
        registerInternal(id, provider);
    }

    private static void registerInternal(Identifier id, ContextualProvider provider) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(provider, "provider");

        for (Entry entry : PROVIDERS) {
            if (entry.id().equals(id)) {
                ZenithLib.LOGGER.debug("Skipped duplicate Zenith tooltip provider registration for {}", id);
                return;
            }
        }

        PROVIDERS.add(new Entry(id, provider));
        ZenithLib.LOGGER.info("Registered Zenith tooltip provider {}", id);
    }

    public static Optional<Result> create(ZenithTooltipContext context) {
        Objects.requireNonNull(context, "context");

        if (context.stack().isEmpty()) {
            return Optional.empty();
        }

        for (Entry entry : PROVIDERS) {
            try {
                Optional<Result> result = entry.provider().create(context);

                if (result != null && result.isPresent()) {
                    return result;
                }
            } catch (RuntimeException exception) {
                ZenithLib.LOGGER.warn(
                        "Zenith tooltip provider {} failed while handling {}",
                        entry.id(),
                        context.itemId(),
                        exception
                );
            }
        }

        return Optional.empty();
    }

    public static Optional<ZenithTooltipDocument> create(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess
    ) {
        return create(ZenithTooltipContext.of(stack, itemId, registryAccess))
                .map(Result::document);
    }

    public static List<Identifier> ids() {
        return PROVIDERS.stream()
                .map(Entry::id)
                .toList();
    }

    @FunctionalInterface
    public interface Provider {
        Optional<ZenithTooltipDocument> create(
                ItemStack stack,
                Identifier itemId,
                Optional<RegistryAccess> registryAccess
        );
    }

    @FunctionalInterface
    public interface ContextualProvider {
        Optional<Result> create(ZenithTooltipContext context);
    }

    public record Result(
            ZenithTooltipDocument document,
            ZenithTooltipContext context
    ) {
        public Result {
            Objects.requireNonNull(document, "document");
            Objects.requireNonNull(context, "context");
        }

        public static Result of(ZenithTooltipDocument document, ZenithTooltipContext context) {
            return new Result(document, context);
        }

        public static Result withSubject(
                ZenithTooltipDocument document,
                ZenithTooltipContext context,
                Identifier subjectId,
                ZenithTooltipSubject subject
        ) {
            return new Result(document, context.withSubject(subjectId, subject));
        }

        public static Result withSubject(
                ZenithTooltipDocument document,
                ZenithTooltipContext context,
                Identifier subjectId,
                Object subjectValue,
                ZenithTooltipSubject presentation
        ) {
            return new Result(document, context.withSubject(subjectId, subjectValue, presentation));
        }

        public static Result withData(
                ZenithTooltipDocument document,
                ZenithTooltipContext context,
                Identifier key,
                Object value
        ) {
            return new Result(document, context.withData(key, value));
        }
    }

    private record Entry(Identifier id, ContextualProvider provider) {}
}
