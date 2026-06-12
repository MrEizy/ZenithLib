package net.zic.zenithlib.tooltip.api;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipContext;
import net.zic.zenithlib.tooltip.api.context.ZenithTooltipSubject;

import java.util.Objects;

/** Document selected by a contextual provider together with its enriched context. */
public record ZenithTooltipProviderResult(
        ZenithTooltipDocument document,
        ZenithTooltipContext context
) {
    public ZenithTooltipProviderResult {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(context, "context");
    }

    public static ZenithTooltipProviderResult of(
            ZenithTooltipDocument document,
            ZenithTooltipContext context
    ) {
        return new ZenithTooltipProviderResult(document, context);
    }

    public static ZenithTooltipProviderResult withSubject(
            ZenithTooltipDocument document,
            ZenithTooltipContext context,
            Identifier subjectId,
            ZenithTooltipSubject subject
    ) {
        return new ZenithTooltipProviderResult(
                document,
                context.withSubject(subjectId, subject)
        );
    }

    public static ZenithTooltipProviderResult withSubject(
            ZenithTooltipDocument document,
            ZenithTooltipContext context,
            Identifier subjectId,
            Object subjectValue,
            ZenithTooltipSubject presentation
    ) {
        return new ZenithTooltipProviderResult(
                document,
                context.withSubject(subjectId, subjectValue, presentation)
        );
    }
}
