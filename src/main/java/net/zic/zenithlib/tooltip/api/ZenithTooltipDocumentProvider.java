package net.zic.zenithlib.tooltip.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Runtime hook for mods that want to supply a Zenith tooltip document from
 * context that is not known to ZenithLib's normal resource-pack tooltip rules.
 *
 * <p>Dependent mods can use this for datapack registries, item components,
 * attachments, capabilities, or other runtime data. Providers should return
 * {@link Optional#empty()} when they do not handle the supplied stack.</p>
 */
@FunctionalInterface
public interface ZenithTooltipDocumentProvider {
    Optional<ZenithTooltipDocument> create(
            ItemStack stack,
            Identifier itemId,
            Optional<RegistryAccess> registryAccess
    );
}