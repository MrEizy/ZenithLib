package net.zic.zenithlib.tooltip.api.value;

import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;

import java.util.Optional;

/** Resolves one namespaced runtime value from the current tooltip context. */
@FunctionalInterface
public interface ZenithTooltipValueSource {
    Optional<ZenithTooltipValue> resolve(ZenithTooltipContext context);
}
