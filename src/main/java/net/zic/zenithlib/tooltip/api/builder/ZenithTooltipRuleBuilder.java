package net.zic.zenithlib.tooltip.api.builder;

import net.minecraft.resources.Identifier;
import net.zic.zenithlib.tooltip.api.ZenithTooltipRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Builder for a selector rule stored in the tooltip definitions resource folder. */
public final class ZenithTooltipRuleBuilder {
    private int priority;
    private boolean all;
    private final List<Identifier> items = new ArrayList<>();
    private final List<Identifier> tags = new ArrayList<>();
    private final List<String> namespaces = new ArrayList<>();
    private Identifier template;
    private Identifier theme = ZenithTooltipRule.DEFAULT_THEME;

    public ZenithTooltipRuleBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public ZenithTooltipRuleBuilder all() {
        this.all = true;
        return this;
    }

    public ZenithTooltipRuleBuilder items(Identifier... items) {
        this.items.addAll(Arrays.stream(items).map(item -> Objects.requireNonNull(item, "item")).toList());
        return this;
    }

    public ZenithTooltipRuleBuilder tags(Identifier... tags) {
        this.tags.addAll(Arrays.stream(tags).map(tag -> Objects.requireNonNull(tag, "tag")).toList());
        return this;
    }

    public ZenithTooltipRuleBuilder namespaces(String... namespaces) {
        this.namespaces.addAll(Arrays.stream(namespaces).map(namespace -> Objects.requireNonNull(namespace, "namespace")).toList());
        return this;
    }

    public ZenithTooltipRuleBuilder template(Identifier template) {
        this.template = Objects.requireNonNull(template, "template");
        return this;
    }

    public ZenithTooltipRuleBuilder document(Identifier document) {
        return template(document);
    }

    public ZenithTooltipRuleBuilder theme(Identifier theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
        return this;
    }

    public ZenithTooltipRule build() {
        if (this.template == null) {
            throw new IllegalStateException("A generated Zenith tooltip rule must reference a template");
        }

        return new ZenithTooltipRule(
                this.priority,
                new ZenithTooltipRule.Selector(this.all, this.items, this.tags, this.namespaces),
                this.template,
                this.theme
        );
    }
}
