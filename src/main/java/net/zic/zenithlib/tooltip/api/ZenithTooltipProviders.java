package net.zic.zenithlib.tooltip.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Runtime provider registry for contextual Zenith tooltips.
 *
 * <p>The original three-argument document provider remains supported. New providers
 * can use {@link #registerContextual(Identifier, ZenithContextualTooltipDocumentProvider)}
 * to enrich the context with a registry subject or other generic metadata before
 * source-backed tooltip text and bars are resolved.</p>
 */
public final class ZenithTooltipProviders {
    private static final List<Entry> PROVIDERS = new CopyOnWriteArrayList<>();

    private ZenithTooltipProviders() {}

    /** Registers the original provider shape without breaking dependent mods. */
    public static void register(Identifier id, ZenithTooltipDocumentProvider provider) {
        Objects.requireNonNull(provider, "provider");
        registerInternal(id, context -> {
            Optional<ZenithTooltipDocument> document = provider.create(
                    context.stack(),
                    context.itemId(),
                    context.registryAccess()
            );
            return document == null
                    ? Optional.empty()
                    : document.map(value -> ZenithTooltipProviderResult.of(value, context));
        });
    }

    /** Registers a provider that may return an enriched runtime context. */
    public static void registerContextual(
            Identifier id,
            ZenithContextualTooltipDocumentProvider provider
    ) {
        registerInternal(id, provider);
    }

    private static void registerInternal(
            Identifier id,
            ZenithContextualTooltipDocumentProvider provider
    ) {
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

    /**
     * Resolves the first matching provider and preserves any context enrichment it
     * supplied for the later value-source resolution pass.
     */
    public static Optional<ZenithTooltipProviderResult> create(ZenithTooltipContext context) {
        Objects.requireNonNull(context, "context");

        if (context.stack().isEmpty()) {
            return Optional.empty();
        }

        for (Entry entry : PROVIDERS) {
            try {
                Optional<ZenithTooltipProviderResult> result = entry.provider().create(context);

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

    /** Preserves the original lookup API for existing callers. */
    public static Optional<ZenithTooltipDocument> create(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess
    ) {
        return create(ZenithTooltipContext.of(stack, itemId, registryAccess))
                .map(ZenithTooltipProviderResult::document);
    }

    public static List<Identifier> ids() {
        return PROVIDERS.stream()
                .map(Entry::id)
                .toList();
    }

    private record Entry(
            Identifier id,
            ZenithContextualTooltipDocumentProvider provider
    ) {}
}
