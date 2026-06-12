package net.zic.zenithlib.tooltip.api;

import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;

import java.util.Optional;

/**
 * Context-aware tooltip provider capable of returning both a document and an enriched
 * runtime context, such as one carrying a resolved registry subject.
 */
@FunctionalInterface
public interface ZenithContextualTooltipDocumentProvider {
    Optional<ZenithTooltipProviderResult> create(ZenithTooltipContext context);
}
