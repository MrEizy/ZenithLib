package net.zic.zenithlib.tooltip.api.builder;

import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;
import net.zic.zenithlib.tooltip.api.element.ZenithTooltipElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Builder for one ordered tooltip page. */
public final class ZenithTooltipPageBuilder {
    private final ZenithTooltipText title;
    private final List<ZenithTooltipElement> elements = new ArrayList<>();

    public ZenithTooltipPageBuilder(ZenithTooltipText title) {
        this.title = Objects.requireNonNull(title, "title");
    }

    public ZenithTooltipPageBuilder add(ZenithTooltipElement element) {
        this.elements.add(Objects.requireNonNull(element, "element"));
        return this;
    }

    public ZenithTooltipPageBuilder addAll(ZenithTooltipElement... elements) {
        for (ZenithTooltipElement element : elements) {
            add(element);
        }
        return this;
    }

    public ZenithTooltipPage build() {
        return new ZenithTooltipPage(this.title, List.copyOf(this.elements));
    }
}
