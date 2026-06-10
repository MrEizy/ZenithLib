package net.zic.zenithlib.tooltip.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.zic.zenithlib.ZenithLib;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Small runtime provider registry for contextual Zenith tooltips.
 *
 * <p>This is intentionally separate from the JSON tooltip repository. The JSON
 * repository is for static item rules; this provider registry is for dependent
 * mods that need to inspect item components, datapack registries, capabilities,
 * or other runtime state before building a tooltip document.</p>
 */
public final class ZenithTooltipProviders {
    private static final List<Entry> PROVIDERS = new CopyOnWriteArrayList<>();

    private ZenithTooltipProviders() {}

    public static void register(Identifier id, ZenithTooltipDocumentProvider provider) {
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

    public static Optional<ZenithTooltipDocument> create(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess
    ) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        for (Entry entry : PROVIDERS) {
            try {
                Optional<ZenithTooltipDocument> document = entry.provider().create(stack, itemId, registryAccess);

                if (document != null && document.isPresent()) {
                    return document;
                }
            } catch (RuntimeException exception) {
                ZenithLib.LOGGER.warn(
                        "Zenith tooltip provider {} failed while handling {}",
                        entry.id(),
                        itemId,
                        exception
                );
            }
        }

        return Optional.empty();
    }

    public static List<Identifier> ids() {
        return PROVIDERS.stream()
                .map(Entry::id)
                .toList();
    }

    private record Entry(
            Identifier id,
            ZenithTooltipDocumentProvider provider
    ) {}
}