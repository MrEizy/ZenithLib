package net.zic.zenithlib.tooltip.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Original runtime hook for mods that supply a Zenith tooltip document from context
 * not known to ordinary resource-pack rules.
 *
 * <p>This interface remains source-compatible for existing integrations. Providers
 * that need to attach a registry subject for dynamic value sources should implement
 * {@link ZenithContextualTooltipDocumentProvider} and register it through
 * {@link ZenithTooltipProviders#registerContextual(Identifier, ZenithContextualTooltipDocumentProvider)}.</p>
 */
@FunctionalInterface
public interface ZenithTooltipDocumentProvider {
    Optional<ZenithTooltipDocument> create(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess
    );
}
