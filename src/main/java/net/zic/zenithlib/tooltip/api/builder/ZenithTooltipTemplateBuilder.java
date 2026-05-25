package net.zic.zenithlib.tooltip.api.builder;

import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Builder for a reusable, theme-independent tooltip template resource. */
public final class ZenithTooltipTemplateBuilder {
    private final List<ZenithTooltipPage> pages = new ArrayList<>();

    public ZenithTooltipTemplateBuilder page(ZenithTooltipPageBuilder page) {
        return page(Objects.requireNonNull(page, "page").build());
    }

    public ZenithTooltipTemplateBuilder page(ZenithTooltipPage page) {
        this.pages.add(Objects.requireNonNull(page, "page"));
        return this;
    }

    public ZenithTooltipTemplate build() {
        if (this.pages.isEmpty()) {
            throw new IllegalStateException("A generated Zenith tooltip template must contain at least one page");
        }

        return new ZenithTooltipTemplate(List.copyOf(this.pages));
    }
}
