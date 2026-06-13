package net.zic.zenithlib.tooltip.api.builder;

import net.zic.zenithlib.tooltip.api.ZenithTooltipPage;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTemplate;
import net.zic.zenithlib.tooltip.api.ZenithTooltipText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** Builder for a reusable, theme-independent tooltip template resource. */
public final class ZenithTooltipTemplateBuilder {
    private final List<ZenithTooltipPage> pages = new ArrayList<>();
    private final List<Identifier> animationPresets = new ArrayList<>();

    public ZenithTooltipTemplateBuilder page(ZenithTooltipPageBuilder page) {
        return page(Objects.requireNonNull(page, "page").build());
    }

    public ZenithTooltipTemplateBuilder page(ZenithTooltipPage page) {
        this.pages.add(Objects.requireNonNull(page, "page"));
        return this;
    }

    public ZenithTooltipTemplateBuilder page(ZenithTooltipText title, Consumer<ZenithTooltipPageBuilder> action) {
        ZenithTooltipPageBuilder page = ZenithTooltipBuilders.page(title);
        Objects.requireNonNull(action, "action").accept(page);
        return page(page);
    }

    public ZenithTooltipTemplateBuilder firstPage(Consumer<ZenithTooltipPageBuilder> action) {
        if (this.pages.isEmpty()) {
            throw new IllegalStateException("Cannot edit the first page of an empty Zenith tooltip template");
        }
        ZenithTooltipPage first = this.pages.remove(0);
        ZenithTooltipPageBuilder page = ZenithTooltipBuilders.page(first.title());
        first.elements().forEach(page::add);
        Objects.requireNonNull(action, "action").accept(page);
        this.pages.add(0, page.build());
        return this;
    }

    public ZenithTooltipTemplateBuilder pages(ZenithTooltipTemplate template) {
        Objects.requireNonNull(template, "template").pages().forEach(this::page);
        template.animationPresets().forEach(this::animationPreset);
        return this;
    }

    public ZenithTooltipTemplateBuilder pages(ZenithTooltipTemplateBuilder template) {
        return pages(Objects.requireNonNull(template, "template").build());
    }

    public ZenithTooltipTemplateBuilder pages(ZenithTooltipPage... pages) {
        for (ZenithTooltipPage page : pages) {
            page(page);
        }
        return this;
    }

    public ZenithTooltipTemplateBuilder animationPreset(Identifier preset) {
        this.animationPresets.add(Objects.requireNonNull(preset, "preset"));
        return this;
    }

    public ZenithTooltipTemplateBuilder animation(Identifier preset) {
        return animationPreset(preset);
    }

    public ZenithTooltipTemplateBuilder animations(Identifier... presets) {
        return animationPresets(presets);
    }

    public ZenithTooltipTemplateBuilder animationPresets(Identifier... presets) {
        for (Identifier preset : presets) {
            animationPreset(preset);
        }
        return this;
    }

    public ZenithTooltipTemplate build() {
        if (this.pages.isEmpty()) {
            throw new IllegalStateException("A generated Zenith tooltip template must contain at least one page");
        }

        return new ZenithTooltipTemplate(List.copyOf(this.pages), List.copyOf(this.animationPresets));
    }
}
